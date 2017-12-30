package app.integration;

import app.domain.EmailMessage;
import app.mailextractors.EmailExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.integration.dsl.ConsumerEndpointSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.jpa.dsl.Jpa;
import org.springframework.integration.jpa.support.PersistMode;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Controller
@Configuration
@Profile("!test")
public class EmailIntegration {
    private final Logger logger = LoggerFactory.getLogger(EmailIntegration.class);

    private final Session session = Session.getDefaultInstance(new Properties());

    private EntityManagerFactory entityManagerFactory;

    private Map<String, SseEmitter> emitters = Collections.synchronizedMap(new HashMap<>());

    // Prevent server sent events from timing out.
    private final long NO_TIMEOUT = -1;

    public EmailIntegration(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * An id is provided to detect browser reloads from the same client. When the browser is refreshed, a new SseEmitter
     * is created leaving the old emitter without a client attached.
     *
     * <p>Given this app should only have 1 user in use its not really an issue. Realistically the id
     * would be a unique token issued by the server on sign in or a JWT token etc.
     *
     * <p>https://stackoverflow.com/questions/34530544/java-spring-sseemitter-responsebodyemitter-detect-browser-reloads
     *
     */
    @GetMapping(value = "/stream/emails/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter emailStream(@PathVariable("id") String id) {
        SseEmitter sseEmitter = new SseEmitter(NO_TIMEOUT);
        emitters.put(id, sseEmitter);
        return sseEmitter;
    }

    /**
     * Emails sent into the email channel are sent out to each server sent event subscriber.
     *
     * <p>The reason this is not done through the new WebFlux api is because its not a reactive application using
     * the WebFlux dependencies. We are using spring data rest and the old mvc way of doing things and the 2 dependencies
     * don't work with each other so you cant mix and match from what I understand.
     */
    @Bean
    public IntegrationFlow sseFlow() {
        return IntegrationFlows
                .from(emailChannel())
                .handle(EmailMessage.class, (emailMessage, headers) -> {
                    emitters.forEach((id, sseEmitter) -> {
                        try {
                            sseEmitter.send(emailMessage);
                        } catch (Throwable e) {
                            logger.info("SseEmitter send error: " + e.getMessage());
                        }
                    });
                    return null;
                })
                .get();
    }

    /**
     * returns a pub/sub channel since there are many consumers such as
     * (Debugging loggers, save to database flow, web socket flow)
     */
    @Bean
    public SubscribableChannel emailChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    /**
     * Polls the {@code input-files} directory every {@code poll-rate-seconds} and
     * sends the transformed file into the {@code emailChannel} for consumers to process.
     */
    @Bean
    public IntegrationFlow incomingEmailsFlow(@Value("${email.input.dir}") File in,
                                              @Value("${email.input.dir.poll.rate.seconds}") long pollRateSeconds) {
        return IntegrationFlows.from(Files.inboundAdapter(in)
                .autoCreateDirectory(false)
                .preventDuplicates(true)
                .patternFilter("*.eml"), c -> c.poller(Pollers.fixedRate(pollRateSeconds)))
                .transform(File.class, emailFileTransformer())
                .channel(emailChannel())
                .log(LoggingHandler.Level.INFO, "test.emailChannel", m -> "emailChannel: " + m.getPayload())
                .get();
    }

    /**
     * When the {@code emailChannel} publishes a {@code Message<EmailMessage>}, save it to the
     * database which is exposed as a REST service through a repository.
     */
    @Bean
    public IntegrationFlow databaseFlow() {
        return IntegrationFlows.from(emailChannel())
                .handle(Jpa.outboundAdapter(entityManagerFactory)
                        .entityClass(EmailMessage.class)
                        .persistMode(PersistMode.PERSIST), ConsumerEndpointSpec::transactional)
                .get();
    }

    /**
     * Transforms a raw email file which FakeSMTP produces (.eml / RFC822 format) into a
     * {@code EmailMessage} domain object.
     */
    private GenericTransformer<File, Message<EmailMessage>> emailFileTransformer() {
        return (File source) -> {
            try (InputStream is = new FileInputStream(source)) {
                MimeMessage message = new MimeMessage(session, is);
                EmailMessage meta = EmailExtractor.parse(message);

                return MessageBuilder.withPayload(meta)
                        .setHeader(FileHeaders.FILENAME, meta.getId().toString())
                        .build();
            } catch (Exception e) {
                ReflectionUtils.rethrowRuntimeException(e);
            }
            return null;
        };
    }
}
