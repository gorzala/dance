package net.dancier.dancer.dancers;

import net.dancier.dancer.AbstractPostgreSQLEnabledTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import java.util.List;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(value = {"/dancers/data.sql"})
public class DancerControllerTest extends AbstractPostgreSQLEnabledTest {

    @Test
    @WithUserDetails("user-with-a-profile@dancier.net")
    void getDancersShouldReturnFilteredProfiles() throws Exception {

        mockMvc
                .perform(get("/dancers")
                        .param("range", "20")
                        .param("gender", "FEMALE")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", isA(List.class)))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("503ffad4-148b-4af1-8365-62315ff89b9f"))
                .andExpect(jsonPath("$[0].gender").value("FEMALE"))
                .andExpect(jsonPath("$[0].dancerName").value("perfect_dancer"))
                .andExpect(jsonPath("$[0].aboutMe").value("Hi"))
                .andExpect(jsonPath("$[0].age").isNotEmpty())
                .andExpect(jsonPath("$[0].size").value("178"))
                .andExpect(jsonPath("$[0].city").value("Dortmund"))
                .andExpect(jsonPath("$[0].country").value("GER"));

    }

    @Test
    @WithUserDetails("user-with-a-profile@dancier.net")
    void getDancersUsingDefaultRange() throws Exception {

        mockMvc
                .perform(get("/dancers")
                        .param("gender", "FEMALE")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", isA(List.class)))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("503ffad4-148b-4af1-8365-62315ff89b9f"))
                .andExpect(jsonPath("$[0].gender").value("FEMALE"))
                .andExpect(jsonPath("$[0].dancerName").value("perfect_dancer"))
                .andExpect(jsonPath("$[0].aboutMe").value("Hi"))
                .andExpect(jsonPath("$[0].age").isNotEmpty())
                .andExpect(jsonPath("$[0].size").value("178"))
                .andExpect(jsonPath("$[0].city").value("Dortmund"))
                .andExpect(jsonPath("$[0].country").value("GER"));

    }

    @Test
    @WithUserDetails("user-with-a-profile@dancier.net")
    void shouldFailIfGenderIsNotSet() throws Exception {
        mockMvc .perform(get("/dancers")
                .param("range", "20")
        ).andExpect(status().isBadRequest());
    }
}
