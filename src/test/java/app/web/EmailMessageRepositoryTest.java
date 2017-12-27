package app.web;

import app.domain.EmailMessage;
import app.utils.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.TypeReferences;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EmailMessageRepositoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmailMessageRepository repository;

    @Test
    public void postIsDisabled_405MethodNotAllowed() throws Exception {
        mockMvc.perform(post("/emails")
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void putIsDisabled_405MethodNotAllowed() throws Exception {
        mockMvc.perform(put("/emails/ff92e909-aafd-4ee2-affe-ecf631efe100")
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void patchIsDisabled_405MethodNotAllowed() throws Exception {
        EmailMessage email = TestUtils.createTestEmailOne();
        repository.save(email);

        mockMvc.perform(patch("/emails/" + email.getId())
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void deleteIsDisabled_405MethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/emails/ff92e909-aafd-4ee2-affe-ecf631efe100"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void get_ReturnsAllEmails() throws Exception {
        EmailMessage firstEmail = TestUtils.createTestEmailOne();
        EmailMessage secondEmail = TestUtils.createTestEmailTwo();
        repository.save(firstEmail);
        repository.save(secondEmail);

        ResponseEntity<PagedResources<EmailMessage>> result = restTemplate.exchange("/emails",
                HttpMethod.GET, null,
                new TypeReferences.PagedResourcesType<EmailMessage>() {
                });

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).containsExactlyInAnyOrder(firstEmail, secondEmail);
    }
}