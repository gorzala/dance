package net.dancier.dancer.recommendation;

import net.dancier.dancer.AbstractPostgreSQLEnabledTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EndToEndRecommendationTest extends AbstractPostgreSQLEnabledTest {

    @MockBean
    RecommendationServiceClient recommendationServiceClient;

    @Test
    @WithUserDetails("user-with-a-profile@dancier.net")
    public void getRecommendation() throws Exception {
        ResultActions resultActions =
                mockMvc.perform(get("/recommendations"));
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$").isArray());
    }

}
