package net.dancier.dancer.recommendation;

import net.dancier.dancer.AbstractPostgreSQLEnabledTest;
import net.dancier.dancer.dancers.DancerRepository;
import net.dancier.dancer.core.model.Dancer;
import net.dancier.dancer.recommendation.dto.RecommendationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EndToEndRecommendationTest extends AbstractPostgreSQLEnabledTest {

    @MockBean
    RecommendationServiceClient recommendationServiceClient;

    // see data.sql this id belongs to the user: user-with-a-profile@dancier.net
    UUID dancerIdWithRecommendations = UUID.fromString("11065e54-664a-11ed-872e-1b1eb88b44b6");

    @Autowired
    DancerRepository dancerRepository;
    @BeforeEach
    void init() {
        this.dancerIdWithRecommendations =
                dancerRepository
                        .findByUserId(UUID.fromString("55bbf334-6649-11ed-8f65-5b299f0e161f"))
                        .get()
                        .getId();
        when(recommendationServiceClient
                .getRecommendations(dancerIdWithRecommendations))
                .thenReturn(List.of());
    }
    @Test
    @WithUserDetails("user-with-a-profile@dancier.net")
    public void getRecommendation() throws Exception {
        ResultActions resultActions =
                mockMvc.perform(get("/recommendations"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$").isArray());
    }



    public Dancer getDancer() {
        return null;
    }

    public Dancer getRecommendedDancer() {
        return null;
    }

    public RecommendationDto getRecommendationDto() {
        return null;
    }
}
