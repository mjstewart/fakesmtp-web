package app.web;

import app.domain.EmailMessage;
import app.mailextractors.EmailExtractor;
import app.utils.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hamcrest.Matchers;
import org.junit.Before;
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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
@ActiveProfiles("test")
public class EmailMessageRepositoryIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmailMessageRepository repository;

    private HttpClient httpClient = HttpClientBuilder.create().build();

    @Before
    public void setUp() throws Exception {
        // PATCH wont work using RestTemplate without this.
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

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

    @Test
    public void rest_DeleteOne_Returns204NoContentWhenSuccessful() throws Exception {
        EmailMessage firstEmail = TestUtils.createTestEmailOne();
        EmailMessage secondEmail = TestUtils.createTestEmailTwo();
        repository.save(firstEmail);
        repository.save(secondEmail);

        // Uses Spring data rest endpoint on single resource
        ResponseEntity<Resource<Void>> result = restTemplate.exchange("/emails/" + firstEmail.getId().toString(),
                HttpMethod.DELETE, null,
                new TypeReferences.ResourceType<Void>() {
                });

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(repository.findAll()).containsExactly(secondEmail);
    }

    @Test
    public void rest_Patch_ChangeEmailReadStatus_Returns200WithUpdatedEmail() throws Exception {
        EmailMessage firstEmail = TestUtils.createTestEmailOne();
        EmailMessage secondEmail = TestUtils.createTestEmailTwo();

        firstEmail.read();
        assertThat(firstEmail.isRead()).isTrue();

        secondEmail.unread();
        assertThat(secondEmail.isRead()).isFalse();

        repository.save(firstEmail);
        repository.save(secondEmail);

        HashMap<String, Boolean> body = new HashMap<>();
        body.put("read", false);

        // Uses Spring data rest endpoint on single resource
        ResponseEntity<Resource<EmailMessage>> result = restTemplate.exchange("/emails/" + firstEmail.getId().toString(),
                HttpMethod.PATCH, new HttpEntity<>(body),
                new TypeReferences.ResourceType<EmailMessage>() {
                });

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent().isRead()).isFalse();

        assertThat(repository.findById(firstEmail.getId()).get().isRead()).isFalse();
        assertThat(repository.findById(secondEmail.getId()).get().isRead()).isFalse();
    }

    @Test
    public void rest_Patch_ResourceNotFound() throws Exception {
        EmailMessage firstEmail = TestUtils.createTestEmailOne();
        firstEmail.unread();
        assertThat(firstEmail.isRead()).isFalse();

        HashMap<String, Boolean> body = new HashMap<>();
        body.put("read", false);

        // Uses Spring data rest endpoint on single resource
        ResponseEntity<Resource<EmailMessage>> result = restTemplate.exchange("/emails/" + firstEmail.getId().toString(),
                HttpMethod.PATCH, new HttpEntity<>(body),
                new TypeReferences.ResourceType<EmailMessage>() {
                });

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(repository.findById(firstEmail.getId()).isPresent()).isFalse();
    }
}