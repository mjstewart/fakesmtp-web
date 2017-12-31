package app.web;

import app.domain.EmailMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/emails/actions")
public class EmailActionController {
    private EmailMessageRepository repository;
    private Map<ActionType, Consumer<EmailMessage>> handlers;

    public EmailActionController(EmailMessageRepository repository) {
        this.repository = repository;

        handlers = new HashMap<>();
        handlers.put(ActionType.READ_ALL, EmailMessage::read);
        handlers.put(ActionType.UNREAD_ALL, EmailMessage::unread);
    }

    @PostMapping
    public ResponseEntity<?> handleAllEmailsAction(@RequestBody ActionRequest requestBody) {
        if (requestBody.getAction() == null) {
            return ResponseEntity.badRequest().build();
        }

        Iterable<EmailMessage> all = repository.findAll();
        List<EmailMessage> emails = StreamSupport.stream(all.spliterator(), false).collect(Collectors.toList());

        if (emails.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.CREATED);
        }

        List<EmailMessage> updatedEmails = apply(emails, handlers.get(requestBody.getAction()));
        repository.saveAll(updatedEmails);
        return new ResponseEntity<>(emailsToResponseBody(updatedEmails), HttpStatus.CREATED);
    }

    /**
     * Spring data rest (SDR) doesn't allow delete on collection resources. It also has to fall under a different URI
     * otherwise it overrides SDR
     */
    @DeleteMapping
    public ResponseEntity<Void> handleDeleteAll() {
        repository.deleteAll();
        return ResponseEntity.noContent().build();
    }

    private List<Map<String, Object>> emailsToResponseBody(List<EmailMessage> emails) {
        return emails.stream()
                .map(this::emailToResponseBody)
                .collect(Collectors.toList());
    }

    private Map<String, Object> emailToResponseBody(EmailMessage emailMessage) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", emailMessage.getId());
        responseBody.put("read", emailMessage.isRead());
        return responseBody;
    }

    /**
     * Apply a side effecting function to the supplied emails.
     *
     * @return The original list with each object mutated.
     */
    private List<EmailMessage> apply(List<EmailMessage> emails, Consumer<EmailMessage> f) {
        emails.forEach(f);
        return emails;
    }
}
