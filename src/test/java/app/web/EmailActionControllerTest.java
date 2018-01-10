package app.web;

import app.domain.EmailMessage;
import app.utils.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Would normally be able to use @WebMvcTest if Spring Data Rest and @BasePathAwareController wasn't used.
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EmailActionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailMessageRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void handleAllEmailsAction_ResourceNotFound_WhenTryingToAccessSingleItem() throws Exception {
        EmailMessage testEmailOne = TestUtils.createTestEmailOne();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", ActionType.READ_ALL);

        mockMvc.perform(post("/api/emails/actions/" + testEmailOne.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verifyZeroInteractions(repository);
    }

    @Test
    public void handleAllEmailsAction_InvalidActionType() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "invalid action type");

        mockMvc.perform(post("/api/emails/actions")
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
        testEmailThree.read();

        assertThat(testEmailOne.isRead()).isFalse();
        assertThat(testEmailTwo.isRead()).isFalse();
        assertThat(testEmailThree.isRead()).isTrue();

        when(repository.findAll()).thenReturn(Arrays.asList(testEmailOne, testEmailTwo, testEmailThree));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", ActionType.READ_ALL.name());

        mockMvc.perform(post("/api/emails/actions")
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
        testEmailOne.read();
        testEmailThree.read();

        assertThat(testEmailOne.isRead()).isTrue();
        assertThat(testEmailTwo.isRead()).isFalse();
        assertThat(testEmailThree.isRead()).isTrue();

        when(repository.findAll()).thenReturn(Arrays.asList(testEmailOne, testEmailTwo, testEmailThree));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", ActionType.UNREAD_ALL.name());

        mockMvc.perform(post("/api/emails/actions")
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
    public void handleAllEmailsAction_ReadAllEmails_NoEmailsExist_ReturnEmptyList() throws Exception {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", ActionType.READ_ALL.name());

        mockMvc.perform(post("/api/emails/actions")
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

        mockMvc.perform(post("/api/emails/actions")
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
        mockMvc.perform(delete("/api/emails/actions"))
                .andExpect(status().isNoContent());

        verify(repository, times(1)).deleteAll();
        verifyNoMoreInteractions(repository);
    }
}