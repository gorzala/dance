package net.dancier.dancer.core;

import lombok.RequiredArgsConstructor;
import net.dancier.dancer.authentication.model.User;
import net.dancier.dancer.authentication.repository.UserRepository;
import net.dancier.dancer.core.dto.DanceProfileDto;
import net.dancier.dancer.core.dto.ProfileOfCurrentUserDto;
import net.dancier.dancer.core.dto.PublicProfileDto;
import net.dancier.dancer.core.events.ProfileUpdatedEvent;
import net.dancier.dancer.core.exception.BusinessException;
import net.dancier.dancer.core.exception.NotFoundException;
import net.dancier.dancer.core.exception.UnresolvableZipCode;
import net.dancier.dancer.core.model.Country;
import net.dancier.dancer.core.model.Dance;
import net.dancier.dancer.core.model.DanceProfile;
import net.dancier.dancer.core.model.Dancer;
import net.dancier.dancer.core.util.ModelMapper;
import net.dancier.dancer.dancers.DancerRepository;
import net.dancier.dancer.location.ZipCode;
import net.dancier.dancer.location.ZipCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final UserRepository userRepository;

    private final DanceService danceService;

    private final DancerRepository dancerRepository;

    private final ZipCodeRepository zipCodeRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    public PublicProfileDto getProfileByDancerId(UUID dancerId) {
        Dancer dancer = dancerRepository.findById(dancerId).orElseThrow(() -> new NotFoundException("No such Dancer"));
        return PublicProfileDto.of(dancer);
    }

    public ProfileOfCurrentUserDto getProfileByUserId(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found for id: " + userId));
        Dancer dancer = dancerRepository.findByUserId(userId).orElseGet(() -> new Dancer());

        return ModelMapper.dancerAndUserToProfile(dancer, user);
    }

    @Transactional
    public void updateProfileForUserId(UUID userId, ProfileOfCurrentUserDto profileOfCurrentUserDto) {
        Dancer dancer = dancerRepository
                .findByUserId(userId)
                .orElseGet(
                        () -> {
                            Dancer d = new Dancer();
                            d.setUserId(userId);
                            d.setVersion(0);
                            return d;
                        });
        Integer oldVersion = dancer.getVersion().intValue();

        dancer.setGender(profileOfCurrentUserDto.getGender());
        dancer.setBirthDate(profileOfCurrentUserDto.getBirthDate());
        dancer.setSize(profileOfCurrentUserDto.getSize());
        dancer.setZipCode(profileOfCurrentUserDto.getZipCode());
        dancer.setProfileImageHash(profileOfCurrentUserDto.getProfileImageHash());
        dancer.setAboutMe(profileOfCurrentUserDto.getAboutMe());
        if (dancer.getDancerName() == null && profileOfCurrentUserDto.getDancerName() != null) {
            checkDancerNameRules(profileOfCurrentUserDto.getDancerName());
            dancer.setDancerName(profileOfCurrentUserDto.getDancerName());
        }
        ZipCode zipCode = zipCodeRepository.findByCountryAndZipCode(profileOfCurrentUserDto.getCountry(), profileOfCurrentUserDto.getZipCode());
        if (zipCode != null) {
            dancer.setCity(zipCode.getCity());
            dancer.setLatitude(zipCode.getLatitude());
            dancer.setLongitude(zipCode.getLongitude());
            dancer.setCountry(Country.valueOf(zipCode.getCountry()));
        } else if (StringUtils.hasText(profileOfCurrentUserDto.getZipCode())) {
            throw new UnresolvableZipCode("Zip Code could not be resolved.");
        } else {
            dancer.setCity(null);
            dancer.setLatitude(null);
            dancer.setLongitude(null);
            dancer.setCountry(null);
        }
        handleDancerProfiles(dancer, profileOfCurrentUserDto);
        dancer.setUpdatedAt(Instant.now());
        dancerRepository.save(dancer);

        log.info("{}/{}", dancer.getVersion(), oldVersion);
        // this hould be unequal but it does not work then
        if (dancer.getVersion().equals(oldVersion)) {
            log.info("Profile-Change detected");
            applicationEventPublisher.publishEvent(
                    ProfileUpdatedEvent
                            .builder()
                            .dancer(dancer)
                            .build());
        } else {
            log.info("Version unchanged");
        }
    }

    private void checkDancerNameRules(String dancerName) {
        if (dancerName == null || dancerName.length() < 3) {
            throw new BusinessException("Dancer name must be at least 3 characters long (name: " + dancerName + ")");
        }
        if (dancerRepository.existsByDancerName(dancerName)) {
            throw new BusinessException("Dancer name already taken (name: " + dancerName + ")");
        }
    }

    private void handleDancerProfiles(Dancer dancer, ProfileOfCurrentUserDto profileOfCurrentUserDto) {
        Set<Dance> allDances = getNeededDances(profileOfCurrentUserDto);
        dancer.setWantsTo(handleDancerProfileInternal(
                dancer.getWantsTo(), profileOfCurrentUserDto.getWantsTo(), allDances
        ));
        dancer.setAbleTo(handleDancerProfileInternal(
                dancer.getAbleTo(), profileOfCurrentUserDto.getAbleTo(), allDances
        ));
    }

    private Set<DanceProfile> handleDancerProfileInternal(
            Set<DanceProfile> currentDanceProfiles,
            Set<DanceProfileDto> wishedProfiles,
            Set<Dance> allDances) {
        Set<DanceProfile> newDanceProfiles = new HashSet<>();
        for (DanceProfileDto danceProfileDto : wishedProfiles) {
            DanceProfile danceProfile = getByName(currentDanceProfiles, danceProfileDto.getDance()).orElseGet(
                    () -> {
                        DanceProfile tmpDp = new DanceProfile();
                        tmpDp.setDance(getDanceByName(allDances, danceProfileDto.getDance()));
                        return tmpDp;
                    }
            );
            danceProfile.setLevel(danceProfileDto.getLevel());
            danceProfile.setLeading(danceProfileDto.getLeading());
            newDanceProfiles.add(danceProfile);
        }
        return newDanceProfiles;
    }

    private Dance getDanceByName(Set<Dance> dances, String name) {
        return dances.stream().filter(dance -> name.equals(dance.getName())).findFirst().get();
    }

    private Optional<DanceProfile> getByName(Set<DanceProfile> danceProfiles, String name) {
        return danceProfiles.stream()
                .filter(dp -> name.equals(dp.getDance().getName()))
                .findFirst();
    }

    public boolean existsByDancerName(String dancerName) {
        return this.dancerRepository.existsByDancerName(dancerName);
    }

    Set<Dance> getNeededDances(ProfileOfCurrentUserDto profileOfCurrentUserDto) {
        Set<DanceProfileDto> allRequestedDanceProfilesDto = new HashSet<>(profileOfCurrentUserDto.getWantsTo());
        allRequestedDanceProfilesDto.addAll(new HashSet<>(profileOfCurrentUserDto.getAbleTo()));
        Set<String> allRequestedDanceNames = allRequestedDanceProfilesDto
                .stream()
                .map(dp -> dp.getDance())
                .collect(Collectors.toSet());
        Set<Dance> alreadyPersistedDances = danceService.getAllDances();
        Set<String> newDanceNames = new HashSet<>(allRequestedDanceNames);
        newDanceNames.removeAll(
                alreadyPersistedDances
                        .stream()
                        .map(d -> d.getName()).collect(Collectors.toSet()));
        Set<Dance> newDances = newDanceNames.stream().map(name -> new Dance(null, name)).collect(Collectors.toSet());
        danceService.saveAll(newDances);
        Set<Dance> allDances = new HashSet<>(alreadyPersistedDances);
        allDances.addAll(newDances);
        return allDances;
    }
}
