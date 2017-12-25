package web.integration;

import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import web.domain.MimeMessageMeta;

@Configuration
public class EmailChannelLogger {

    @ServiceActivator(inputChannel = "emailChannel")
    public void emailChannelConsoleLogger(MimeMessageMeta meta) {
        System.out.println("--------------------------------------");
        System.out.println("MimeMessageMeta received from emailChannel");
        System.out.println(meta);
    }
}
