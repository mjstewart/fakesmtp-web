package web.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ReflectionUtils;
import web.domain.MimeMessageMeta;
import web.mailextractors.MimeMetaExtractor;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManagerFactory;
import java.io.*;
import java.util.Properties;

@Configuration
public class EmailIntegration {

    private final Log logger = LogFactory.getLog(EmailIntegration.class);
    private final Session session = Session.getDefaultInstance(new Properties());

    private EntityManagerFactory entityManagerFactory;

    public EmailIntegration(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public MessageChannel emailChannel() {
        return MessageChannels.publishSubscribe().get();
//        return MessageChannels.direct().get();
    }


    @Bean
    public IntegrationFlow files(@Value("${input-directory}") File in,
                                 @Value("${poll-rate-seconds}") long pollRateSeconds) {

        return IntegrationFlows.from(Files.inboundAdapter(in)
                .autoCreateDirectory(false)
                .preventDuplicates(true)
                .patternFilter("*.eml"), c -> c.poller(Pollers.fixedRate(pollRateSeconds)))
                .transform(File.class, emailFileTransformer())
                .channel(emailChannel())
                .get();
    }


    @Bean
    public IntegrationFlow dbFlow() {
        return IntegrationFlows.from(emailChannel())
                .handle(Jpa.outboundAdapter(entityManagerFactory)
                        .entityClass(MimeMessageMeta.class)
                        .persistMode(PersistMode.PERSIST), ConsumerEndpointSpec::transactional)
                .get();
    }

    private GenericTransformer<File, Message<MimeMessageMeta>> emailFileTransformer() {
        return (File source) -> {
            try (InputStream is = new FileInputStream(source)) {
                MimeMessage message = new MimeMessage(session, is);
                MimeMessageMeta meta = MimeMetaExtractor.parse(message);

                logger.info("emailFileTransformer mapped email for subject '" + meta.getSubject() + "'");

                return MessageBuilder.withPayload(meta)
                        .setHeader(FileHeaders.FILENAME, meta.getEmailId())
                        .build();
            } catch (Exception e) {
                ReflectionUtils.rethrowRuntimeException(e);
            }
            return null;
        };
    }
}
