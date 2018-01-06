package app.web;

import app.domain.EmailMessage;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
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
        handlers.put(ActionType.TOGGLE_READ, EmailMessage::toggleRead);
        handlers.put(ActionType.READ_ALL, EmailMessage::read);
        handlers.put(ActionType.UNREAD_ALL, EmailMessage::unread);
    }

    @PostMapping(value = "/{id}")
    public ResponseEntity<?> handleSingleEmailAction(@PathVariable("id") UUID id, @RequestBody ActionRequest requestBody) {
        if (requestBody.getAction() != ActionType.TOGGLE_READ) {
            return ResponseEntity.badRequest().build();
        }

        Optional<EmailMessage> email = repository.findById(id);
        if (!email.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        email.get().toggleRead();
        repository.save(email.get());
        return new ResponseEntity<>(emailToResponseBody(email.get()), HttpStatus.CREATED);
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
