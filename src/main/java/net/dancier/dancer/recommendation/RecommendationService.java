package net.dancier.dancer.recommendation;

import lombok.RequiredArgsConstructor;
import net.dancier.dancer.dancers.DancerRepository;
import net.dancier.dancer.core.model.Dancer;
import net.dancier.dancer.recommendation.model.BaseRecommendation;
import net.dancier.dancer.recommendation.model.RecommendationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final DancerRepository dancerRepository;
    private final RecommendationServiceClient recommendationServiceClient;

    // https://www.baeldung.com/java-collectors-tomap
    public List<RecommendationWrapper> getRecommendationsForDancerId(UUID dancerId) {
        log.info("Getting Recommendations for dancer with ID: " + dancerId);
        Dancer dancer = dancerRepository.getReferenceById(dancerId);
        log.info("This has version: " + dancer.getVersion());
        List<BaseRecommendation> baseRecommendations = recommendationServiceClient.getRecommendations(dancerId);
        log.info("Got so many recommendations: " + baseRecommendations.size());
        Map<UUID, Integer> dancerId2Version = baseRecommendations
                .stream()
                .filter(p -> BaseRecommendation.Type.DANCER.equals(p.getType()))
                .collect(Collectors.toMap(BaseRecommendation::getTargetId, BaseRecommendation::getTargetVersion));
        Map<UUID, Integer> dancerId2Score = baseRecommendations
                .stream()
                .collect(Collectors.toMap(BaseRecommendation::getTargetId, BaseRecommendation::getScore));
        // Test what happens when we have
        log.info("With: " + dancerId2Version.keySet());
        List<Dancer> dancers = dancerRepository.findAllById(dancerId2Version.keySet());
        log.info("So many are current: " + dancers.size());

        List<RecommendationWrapper> recommendationWrappers = dancers.stream().filter(d -> {
            Integer currentVersion = d.getVersion();
            Integer recommendedVersion = dancerId2Version.get(d.getId());
            if (currentVersion.equals(recommendedVersion)) {
                log.info("Version Match");
                return Boolean.TRUE;
            } else {
                log.info(String.format("Outdated Recommendation: recommended: %s, current: %s", recommendedVersion, currentVersion));
                return Boolean.FALSE;
            }
        }).map( d-> {
            RecommendationWrapper<Dancer> recommendationWrapper = new RecommendationWrapper();
            recommendationWrapper.setPayload(d);
            recommendationWrapper.setScore(dancerId2Score.get(d.getId()));
            return recommendationWrapper;
        }).collect(Collectors.toList());
        return recommendationWrappers;
    }

}
