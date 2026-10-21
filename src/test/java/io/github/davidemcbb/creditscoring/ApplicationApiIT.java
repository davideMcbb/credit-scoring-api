package io.github.davidemcbb.creditscoring;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ApplicationApiIT {

    // the best class of every characteristic: 747 points, nothing below average
    static final String STRONG = """
            {"durationMonths": 12, "amount": 2500, "age": 40,
             "employmentDuration": "FROM_4_TO_7_YEARS", "installmentRate": "OVER_35_PERCENT",
             "checkingAccount": "BALANCE_200_OR_SALARY", "creditHistory": "ALL_PAID_AT_THIS_BANK",
             "purpose": "NEW_CAR", "savings": "OVER_1000", "otherDebtors": "GUARANTOR"}
            """;

    // 422 points, below the cutoff of 541.5
    static final String WEAK = """
            {"durationMonths": 48, "amount": 10000, "age": 21,
             "employmentDuration": "UNDER_1_YEAR", "installmentRate": "UNDER_20_PERCENT",
             "checkingAccount": "NO_CHECKING_ACCOUNT", "creditHistory": "PAST_DELAYS",
             "purpose": "RADIO_TV", "savings": "NONE_OR_UNKNOWN", "otherDebtors": "NONE"}
            """;

    @Autowired
    MockMvc mvc;

    @Test
    void approvesAStrongApplication() throws Exception {
        submit(STRONG)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/applications/")))
                .andExpect(jsonPath("$.scorecardVersion").value("1.0"))
                .andExpect(jsonPath("$.score").value(747))
                .andExpect(jsonPath("$.decision").value("APPROVE"))
                .andExpect(jsonPath("$.reasons", hasSize(0)))
                .andExpect(jsonPath("$.points", hasSize(10)));
    }

    @Test
    void rejectsAWeakApplicationAndSaysWhy() throws Exception {
        submit(WEAK)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(422))
                .andExpect(jsonPath("$.probabilityOfDefault", greaterThan(0.9)))
                .andExpect(jsonPath("$.decision").value("REJECT"))
                .andExpect(jsonPath("$.reasons[*].characteristic").value(contains("purpose", "status", "amount")))
                .andExpect(jsonPath("$.reasons[0].pointsBelowAverage", closeTo(29.494286, 1e-6)));
    }

    @Test
    void storedApplicationCanBeReadBack() throws Exception {
        String body = submit(STRONG).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(body, "$.id");

        mvc.perform(get("/api/applications/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(body))
                .andExpect(jsonPath("$.points[7].characteristic").value("purpose"))
                .andExpect(jsonPath("$.points[7].value").value("NEW_CAR"))
                .andExpect(jsonPath("$.points[7].attribute").value("car (new) / vacation"))
                .andExpect(jsonPath("$.points[7].points").value(82));
    }

    @Test
    void unknownApplicationIsNotFound() throws Exception {
        mvc.perform(get("/api/applications/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void missingAndOutOfRangeFieldsAreReported() throws Exception {
        String invalid = STRONG.replace("\"age\": 40", "\"age\": 15").replace("\"purpose\": \"NEW_CAR\", ", "");
        submit(invalid)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.age").exists())
                .andExpect(jsonPath("$.errors.purpose").exists());
    }

    @Test
    void unknownCategoryListsTheAllowedValues() throws Exception {
        submit(STRONG.replace("NEW_CAR", "SPACESHIP"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("purpose")))
                .andExpect(jsonPath("$.detail", containsString("USED_CAR")));
    }

    @Test
    void listCanBeFilteredByDecision() throws Exception {
        submit(STRONG);
        submit(WEAK);
        mvc.perform(get("/api/applications").param("decision", "REJECT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThan(0)))
                .andExpect(jsonPath("$.content[*].decision", everyItem(is("REJECT"))));
    }

    @Test
    void scorecardIsPublished() throws Exception {
        mvc.perform(get("/api/scorecard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.pointsToDoubleOdds", closeTo(20.0, 1e-6)))
                .andExpect(jsonPath("$.characteristics", hasSize(10)))
                .andExpect(jsonPath("$.characteristics[0].name").value("duration"))
                .andExpect(jsonPath("$.characteristics[9].attributes[1].categories[0]").value("GUARANTOR"))
                .andExpect(jsonPath("$.characteristics[9].attributes[1].points").value(95));
    }

    private ResultActions submit(String json) throws Exception {
        return mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
