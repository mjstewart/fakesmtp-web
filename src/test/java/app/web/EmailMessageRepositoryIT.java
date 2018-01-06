package app.web;

import app.domain.EmailMessage;
import app.mailextractors.EmailExtractor;
import app.utils.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.TypeReferences;
import org.springframework.http.*;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EmailMessageRepositoryIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmailMessageRepository repository;

    @Test
    public void findAll_SortsBySentDateCorrectly() throws MessagingException {
        MimeMessage message1 = new MimeMessage(Session.getDefaultInstance(new Properties()));
        MimeMessageHelper helper1 = new MimeMessageHelper(message1, true, "UTF-8");
        helper1.setFrom("firstEmail@email.com");
        helper1.setTo("you@email.com");
        helper1.setSubject("test 1 email subject");
        helper1.setCc("person3@email.com");
        helper1.setText("some body text", false);
        message1.setSentDate(new GregorianCalendar(2017, Calendar.JANUARY, 1, 10, 10, 10).getTime());
        EmailMessage firstEmail = EmailExtractor.parse(message1);

        MimeMessage message2 = new MimeMessage(Session.getDefaultInstance(new Properties()));
        MimeMessageHelper helper2 = new MimeMessageHelper(message2, true, "UTF-8");
        helper2.setFrom("secondEmail@email.com");
        helper2.setTo("you@email.com");
        helper2.setSubject("test 2 email subject");
        helper2.setCc("person3@email.com");
        helper2.setText("some body text", false);
        message2.setSentDate(new GregorianCalendar(2017, Calendar.JANUARY, 1, 10, 10, 11).getTime());
        EmailMessage secondEmail = EmailExtractor.parse(message2);

        EmailMessage firstSaved = repository.save(firstEmail);
        EmailMessage secondSaved = repository.save(secondEmail);

        assertThat(repository.findAll(Sort.by(Sort.Direction.ASC, "sentDate")))
                .containsExactly(firstSaved, secondSaved);

        assertThat(repository.findAll(Sort.by(Sort.Direction.DESC, "sentDate")))
                .containsExactly(secondSaved, firstSaved);
    }

    @Test
    public void illegalArgumentResultsIn404() throws Exception {
        mockMvc.perform(get("/emails/blah"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void rest_findAll_WithSortQuery_ReturnsAll() throws Exception {
        EmailMessage firstEmail = TestUtils.createTestEmailOne();
        EmailMessage secondEmail = TestUtils.createTestEmailTwo();
        repository.save(firstEmail);
        repository.save(secondEmail);

        ResponseEntity<Resources<EmailMessage>> result = restTemplate.exchange("/emails?sort=sentDate,desc",
                HttpMethod.GET, null,
                new TypeReferences.ResourcesType<EmailMessage>() {
                });

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).containsExactlyInAnyOrder(firstEmail, secondEmail);
    }

    @Test
    public void rest_DeleteAll_Returns204NoContentWhenSuccessful() throws Exception {
        EmailMessage firstEmail = TestUtils.createTestEmailOne();
        EmailMessage secondEmail = TestUtils.createTestEmailTwo();
        repository.save(firstEmail);
        repository.save(secondEmail);

        ResponseEntity<Resource<Void>> result = restTemplate.exchange("/emails/actions",
                HttpMethod.DELETE, null,
                new TypeReferences.ResourceType<Void>() {
                });

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(repository.findAll()).isEmpty();
    }
}