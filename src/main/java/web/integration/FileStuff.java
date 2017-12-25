package web.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardOpenOption;

//@Configuration
//public class FileStuff {
//
//
//    @Bean
//    public MessageChannel emailChannel() {
//        return MessageChannels.direct().get();
//    }
//
//    @Bean
//    public IntegrationFlow outputDirFlow(@Value("${output-directory}") File out) {
//        return IntegrationFlows.from(emailChannel())
//                .handle(Files.outboundAdapter(out).fileNameGenerator(message -> {
//                    String filename = String.class.cast(message.getHeaders().get(FileHeaders.FILENAME));
//                    return filename.split("\\.")[0] + "txt";
//                }))
//                .get();
//    }
//
//    @Bean
//    public IntegrationFlow readFileSystemFlow(@Value("${input-directory}") File in,
//                                 @Value("${poll-rate-seconds}") long pollRateSeconds) {
//
//        GenericTransformer<File, Message<File>> transformer = source -> {
//            try {
//                File transformedFile = java.nio.file.Files.write(source.toPath(),
//                        "Transformed line added...".getBytes(), StandardOpenOption.APPEND).toFile();
//                return MessageBuilder.withPayload(transformedFile).setHeader(FileHeaders.FILENAME, source.getAbsoluteFile().getName())
//                        .build();
//            } catch (IOException e) {
//                ReflectionUtils.rethrowRuntimeException(e);
//            }
//            return null;
//        };
//
//
//        return IntegrationFlows.from(Files.inboundAdapter(in).autoCreateDirectory(false)
//                .preventDuplicates(true)
//                .patternFilter("*.eml"), c -> c.poller(Pollers.fixedRate(pollRateSeconds)))
//                .transform(File.class, transformer)
//                .channel(emailChannel())
//                .get();
//
//    }
//}
