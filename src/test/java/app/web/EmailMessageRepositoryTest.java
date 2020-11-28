package app.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(EmailMessageRepository.class)
@ActiveProfiles("test")
public class EmailMessageRepositoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void illegalArgumentResultsIn404() throws Exception {
        mockMvc.perform(get("/emails/blah"))
                .andExpect(status().isNotFound());
    }
}