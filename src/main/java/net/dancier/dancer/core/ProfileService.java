package net.dancier.dancer.core;

import lombok.RequiredArgsConstructor;
import net.dancier.dancer.authentication.model.User;
import net.dancier.dancer.authentication.repository.UserRepository;
import net.dancier.dancer.core.dto.DanceProfileDto;
import net.dancier.dancer.core.dto.ProfileDto;
import net.dancier.dancer.core.exception.NotFoundException;
import net.dancier.dancer.core.model.*;
import net.dancier.dancer.core.util.ModelMapper;
import net.dancier.dancer.location.ZipCode;
import net.dancier.dancer.location.ZipCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    private final DanceService danceService;

    private final DancerRepository dancerRepository;

    private final ZipCodeRepository zipCodeRepository;

    public ProfileDto getProfileByUserId(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found for id: " + userId));
        Dancer dancer = dancerRepository.findByUserId(userId).orElseGet( () -> new Dancer());

        return ModelMapper.dancerAndUserToProfile(dancer, user);
    }

    @Transactional
    public void updateProfileForUserId(UUID userId, ProfileDto profileDto) {
        Dancer dancer = dancerRepository
                .findByUserId(userId)
                .orElseGet(
                () -> {
                    Dancer d = new Dancer();
                    d.setUserId(userId);
                    return d;
                });
        dancer.setGender(profileDto.getGender());
        dancer.setBirthDate(profileDto.getBirthDate());
        dancer.setSize(profileDto.getSize());
        dancer.setZipCode(profileDto.getZipCode());
        dancer.setProfileImageHash(profileDto.getProfileImageHash());
        dancer.setAboutMe(profileDto.getAboutMe());
        if (dancer.getDancerName()==null) {
            dancer.setDancerName(profileDto.getDancerName());
        }
        ZipCode zipCode = zipCodeRepository.findByCountryAndZipCode(profileDto.getCountry(), profileDto.getZipCode());
        if (zipCode!=null) {
            dancer.setCity(zipCode.getCity());
            dancer.setLatitude(zipCode.getLatitude());
            dancer.setLongitude(zipCode.getLongitude());
            dancer.setCountry(Country.valueOf(zipCode.getCountry()));
        }
        handleDancerProfiles(dancer, profileDto);
        dancerRepository.save(dancer);
    };

    private void handleDancerProfiles(Dancer dancer, ProfileDto profileDto) {
        Set<Dance> allDances = getNeededDances(profileDto);
        dancer.setWantsTo(handleDancerProfileInternal(
                dancer.getWantsTo(), profileDto.getWantsTo(), allDances
        ));
        dancer.setAbleTo(handleDancerProfileInternal(
                dancer.getAbleTo(), profileDto.getAbleTo(), allDances
        ));
    }

    private Set<DanceProfile> handleDancerProfileInternal(
            Set<DanceProfile> currentDanceProfiles,
            Set<DanceProfileDto> wishedProfiles,
            Set<Dance> allDances) {
        Set<DanceProfile> newDanceProfiles = new HashSet<>();
        for (DanceProfileDto danceProfileDto: wishedProfiles) {
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

    Set<Dance> getNeededDances(ProfileDto profileDto) {
        Set<DanceProfileDto> allRequestedDanceProfilesDto = new HashSet<>(profileDto.getWantsTo());
        allRequestedDanceProfilesDto.addAll(new HashSet<>(profileDto.getAbleTo()));
        Set<String> allRequestedDanceNames = allRequestedDanceProfilesDto
                .stream()
                .map(dp -> dp.getDance())
                .collect(Collectors.toSet());
        Set<Dance> alreadyPersistedDances = danceService.getAllDances();
        Set<String> newDanceNames = new HashSet<>(allRequestedDanceNames);
            newDanceNames.removeAll(
                alreadyPersistedDances
                        .stream()
                        .map(d-> d.getName()).collect(Collectors.toSet()));
        Set<Dance> newDances = newDanceNames.stream().map(name -> new Dance(null, name)).collect(Collectors.toSet());
        danceService.saveAll(newDances);
        Set<Dance> allDances = new HashSet<>(alreadyPersistedDances);
        allDances.addAll(newDances);
        return allDances;
    }
}