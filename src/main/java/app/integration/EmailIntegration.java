package app.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.ConsumerEndpointSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.jpa.dsl.Jpa;
import org.springframework.integration.jpa.support.PersistMode;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ReflectionUtils;
import app.domain.MimeMessageMeta;
import app.mailextractors.MimeMetaExtractor;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

@Configuration
@Profile("!test")
public class EmailIntegration {
    private final Session session = Session.getDefaultInstance(new Properties());

    private EntityManagerFactory entityManagerFactory;

    public EmailIntegration(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
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
    public IntegrationFlow readFileSystemFlow(@Value("${input-directory}") File in,
                                              @Value("${poll-rate-seconds}") long pollRateSeconds) {
        return IntegrationFlows.from(Files.inboundAdapter(in)
                .autoCreateDirectory(false)
                .preventDuplicates(true)
                .patternFilter("*.eml"), c -> c.poller(Pollers.fixedRate(pollRateSeconds)))
                .transform(File.class, emailFileTransformer())
                .channel(emailChannel())
                .get();
    }

    /**
     * When the {@code emailChannel} publishes a {@code Message<MimeMessageMeta>}, save it to the
     * database which is exposed as a REST service through a repository.
     */
    @Bean
    public IntegrationFlow databaseFlow() {
        return IntegrationFlows.from(emailChannel())
                .handle(Jpa.outboundAdapter(entityManagerFactory)
                        .entityClass(MimeMessageMeta.class)
                        .persistMode(PersistMode.PERSIST), ConsumerEndpointSpec::transactional)
                .get();
    }

    /**
     * Transforms a raw email file which FakeSMTP produces (.eml / RFC822 format) into a
     * {@code MimeMessageMeta} domain object.
     */
    private GenericTransformer<File, Message<MimeMessageMeta>> emailFileTransformer() {
        return (File source) -> {
            try (InputStream is = new FileInputStream(source)) {
                MimeMessage message = new MimeMessage(session, is);
                MimeMessageMeta meta = MimeMetaExtractor.parse(message);

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
