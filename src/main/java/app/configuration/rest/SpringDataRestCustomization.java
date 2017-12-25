package app.configuration.rest;

import app.domain.MimeMessageMeta;
import app.mailextractors.MimeAttachment;
import app.web.MimeMessageMetaRepository;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.hateoas.Identifiable;
import org.springframework.stereotype.Component;

@Component
public class SpringDataRestCustomization extends RepositoryRestConfigurerAdapter {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        // https://github.com/spring-projects/spring-data-rest/blob/master/spring-data-rest-tests/spring-data-rest-tests-shop/src/main/java/org/springframework/data/rest/tests/shop/ProductRepository.java
        config.exposeIdsFor(MimeMessageMeta.class, MimeAttachment.class);

//        config.withEntityLookup()
//                .forRepository(MimeMessageMetaRepository.class, MimeMessageMeta::getEmailId, MimeMessageMetaRepository::findByEmailId);
    }
}
