package app.web;

import app.domain.EmailMessage;
import app.mailextractors.EmailExtractor;
import app.utils.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(EmailActionController.class)
@ActiveProfiles("test")
public class EmailActionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailMessageRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void handleSingleEmailAction_ToggleEmailFromUnreadToRead() throws Exception {
        MimeMessage message1 = new MimeMessage(Session.getDefaultInstance(new Properties()));
        MimeMessageHelper helper1 = new MimeMessageHelper(message1, true, "UTF-8");
        helper1.setFrom("firstEmail@email.com");
        helper1.setTo("you@email.com");
        helper1.setSubject("test 1 email subject");
        helper1.setCc("person3@email.com");
        helper1.setText("some body text", false);

        EmailMessage email = EmailExtractor.parse(message1);
        assertThat(email.isRead()).isFalse();

        when(repository.findById(eq(email.getId()))).thenReturn(Optional.of(email));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", ActionType.TOGGLE_READ.name());

        // unread to read
        mockMvc.perform(post("/emails/actions/" + email.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(email.getId().toString()))
                .andExpect(jsonPath("$.read").value(true));

        verify(repository, times(1)).save(eq(email));
    }

    @Test
    public void handleSingleEmailAction_ToggleEmailFromReadToUnread() throws Exception {
        MimeMessage message1 = new MimeMessage(Session.getDefaultInstance(new Properties()));
        MimeMessageHelper helper1 = new MimeMessageHelper(message1, true, "UTF-8");
        helper1.setFrom("firstEmail@email.com");
        helper1.setTo("you@email.com");
        helper1.setSubject("test 1 email subject");
        helper1.setCc("person3@email.com");
        helper1.setText("some body text", false);

        EmailMessage email = EmailExtractor.parse(message1);
        email.toggleRead();
        assertThat(email.isRead()).isTrue();

        when(repository.findById(eq(email.getId()))).thenReturn(Optional.of(email));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", ActionType.TOGGLE_READ.name());

        // unread to read
        mockMvc.perform(post("/emails/actions/" + email.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(email.getId().toString()))
                .andExpect(jsonPath("$.read").value(false));

        verify(repository, times(1)).save(eq(email));
    }

    @Test
    public void handleSingleEmailAction_InvalidActionKeyBody_IsBadRequest() throws Exception {
        MimeMessage message1 = new MimeMessage(Session.getDefaultInstance(new Properties()));
        MimeMessageHelper helper1 = new MimeMessageHelper(message1, true, "UTF-8");
        helper1.setFrom("firstEmail@email.com");
        helper1.setTo("you@email.com");
        helper1.setSubject("test 1 email subject");
        helper1.setCc("person3@email.com");
        helper1.setText("some body text", false);

        EmailMessage email = EmailExtractor.parse(message1);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("blah", ActionType.TOGGLE_READ.name());

        mockMvc.perform(post("/emails/actions/" + email.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyZeroInteractions(repository);
    }

    @Test
    public void handleSingleEmailAction_InvalidActionTypeBody_IsBadRequest() throws Exception {
        MimeMessage message1 = new MimeMessage(Session.getDefaultInstance(new Properties()));
        MimeMessageHelper helper1 = new MimeMessageHelper(message1, true, "UTF-8");
        helper1.setFrom("firstEmail@email.com");
        helper1.setTo("you@email.com");
        helper1.setSubject("test 1 email subject");
        helper1.setCc("person3@email.com");
        helper1.setText("some body text", false);

        EmailMessage email = EmailExtractor.parse(message1);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "invalid type");

        mockMvc.perform(post("/emails/actions/" + email.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyZeroInteractions(repository);
    }

    @Test
    public void handleSingleEmailAction_EmailNotFound_NotFoundResponse() throws Exception {
        MimeMessage message1 = new MimeMessage(Session.getDefaultInstance(new Properties()));
        MimeMessageHelper helper1 = new MimeMessageHelper(message1, true, "UTF-8");
        helper1.setFrom("firstEmail@email.com");
        helper1.setTo("you@email.com");
        helper1.setSubject("test 1 email subject");
        helper1.setCc("person3@email.com");
        helper1.setText("some body text", false);

        EmailMessage email = EmailExtractor.parse(message1);

        when(repository.findById(eq(email.getId()))).thenReturn(Optional.empty());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", ActionType.TOGGLE_READ.name());

        mockMvc.perform(post("/emails/actions/" + email.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(repository, times(1)).findById(eq(email.getId()));
    }

    @Test
    public void handleSingleEmailAction_InvalidEmailId_IsBadRequest() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", ActionType.TOGGLE_READ.name());

        // email id is not a UUID
        mockMvc.perform(post("/emails/actions/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyZeroInteractions(repository);
    }


    @Test
    public void handleAllEmailsAction_ToggleAllEmailsReadToUnread() throws Exception {
        EmailMessage testEmailOne = TestUtils.createTestEmailOne();
        EmailMessage testEmailTwo = TestUtils.createTestEmailTwo();

        when(repository.findAll()).thenReturn(Arrays.asList(testEmailOne, testEmailTwo));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", ActionType.TOGGLE_READ.name());

        mockMvc.perform(post("/emails/actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.[0].id").value(testEmailOne.getId().toString()))
                .andExpect(jsonPath("$.[0].read").value(true))
                .andExpect(jsonPath("$.[1].id").value(testEmailTwo.getId().toString()))
                .andExpect(jsonPath("$.[1].read").value(true));

        verify(repository, times(1)).findAll();
        verify(repository, times(1)).saveAll(anyIterable());
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void handleAllEmailsAction_ToggleAllEmailsUnreadToRead() throws Exception {
        EmailMessage testEmailOne = TestUtils.createTestEmailOne();
        testEmailOne.toggleRead();
        EmailMessage testEmailTwo = TestUtils.createTestEmailTwo();
        testEmailTwo.toggleRead();

        when(repository.findAll()).thenReturn(Arrays.asList(testEmailOne, testEmailTwo));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", ActionType.TOGGLE_READ.name());

        mockMvc.perform(post("/emails/actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.[0].id").value(testEmailOne.getId().toString()))
                .andExpect(jsonPath("$.[0].read").value(false))
                .andExpect(jsonPath("$.[1].id").value(testEmailTwo.getId().toString()))
                .andExpect(jsonPath("$.[1].read").value(false));

        verify(repository, times(1)).findAll();
        verify(repository, times(1)).saveAll(anyIterable());
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void handleAllEmailsAction_InvalidActionKey() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("blah", ActionType.TOGGLE_READ.name());

        mockMvc.perform(post("/emails/actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyZeroInteractions(repository);
    }

    @Test
    public void handleAllEmailsAction_InvalidActionType() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "invalid action type");

        mockMvc.perform(post("/emails/actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyZeroInteractions(repository);
    }

    @Test
    public void handleAllEmailsAction_ReadAllEmails() throws Exception {
        EmailMessage testEmailOne = TestUtils.createTestEmailOne();
        EmailMessage testEmailTwo = TestUtils.createTestEmailTwo();
        EmailMessage testEmailThree = TestUtils.createTestEmailTwo();
        testEmailThree.toggleRead();

        assertThat(testEmailOne.isRead()).isFalse();
        assertThat(testEmailTwo.isRead()).isFalse();
        assertThat(testEmailThree.isRead()).isTrue();

        when(repository.findAll()).thenReturn(Arrays.asList(testEmailOne, testEmailTwo, testEmailThree));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", ActionType.READ_ALL.name());

        mockMvc.perform(post("/emails/actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id").value(testEmailOne.getId().toString()))
                .andExpect(jsonPath("$.[0].read").value(true))
                .andExpect(jsonPath("$.[1].id").value(testEmailTwo.getId().toString()))
                .andExpect(jsonPath("$.[1].read").value(true))
                .andExpect(jsonPath("$.[2].id").value(testEmailThree.getId().toString()))
                .andExpect(jsonPath("$.[2].read").value(true));

        verify(repository, times(1)).findAll();
        verify(repository, times(1)).saveAll(anyIterable());
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void handleAllEmailsAction_UnreadAllEmails() throws Exception {
        EmailMessage testEmailOne = TestUtils.createTestEmailOne();
        EmailMessage testEmailTwo = TestUtils.createTestEmailTwo();
        EmailMessage testEmailThree = TestUtils.createTestEmailTwo();
        testEmailOne.toggleRead();
        testEmailThree.toggleRead();

        assertThat(testEmailOne.isRead()).isTrue();
        assertThat(testEmailTwo.isRead()).isFalse();
        assertThat(testEmailThree.isRead()).isTrue();

        when(repository.findAll()).thenReturn(Arrays.asList(testEmailOne, testEmailTwo, testEmailThree));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", ActionType.UNREAD_ALL.name());

        mockMvc.perform(post("/emails/actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id").value(testEmailOne.getId().toString()))
                .andExpect(jsonPath("$.[0].read").value(false))
                .andExpect(jsonPath("$.[1].id").value(testEmailTwo.getId().toString()))
                .andExpect(jsonPath("$.[1].read").value(false))
                .andExpect(jsonPath("$.[2].id").value(testEmailThree.getId().toString()))
                .andExpect(jsonPath("$.[2].read").value(false));

        verify(repository, times(1)).findAll();
        verify(repository, times(1)).saveAll(anyIterable());
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void handleAllEmailsAction_ReadAllEmails_NoEmails_ReturnEmptyList() throws Exception {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", ActionType.READ_ALL.name());

        mockMvc.perform(post("/emails/actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());

        verify(repository, times(1)).findAll();
        verify(repository, never()).saveAll(anyIterable());
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void handleAllEmailsAction_UnreadAllEmails_NoEmails_ReturnEmptyList() throws Exception {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", ActionType.UNREAD_ALL.name());

        mockMvc.perform(post("/emails/actions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());

        verify(repository, times(1)).findAll();
        verify(repository, never()).saveAll(anyIterable());
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void handleDeleteAllSuccessfully_NoContentStatusReturned() throws Exception {
        mockMvc.perform(delete("/emails/actions"))
                .andExpect(status().isNoContent());

        verify(repository, times(1)).deleteAll();
        verifyNoMoreInteractions(repository);
    }
}