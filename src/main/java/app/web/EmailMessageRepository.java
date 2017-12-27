package app.web;

import app.domain.EmailMessage;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.UUID;

@RepositoryRestResource(path = "emails", itemResourceRel = "email", collectionResourceRel = "emails")
public interface EmailMessageRepository extends PagingAndSortingRepository<EmailMessage, UUID> {

    @RestResource(exported = false)
    @Override
    <S extends EmailMessage> S save(S entity);

    @RestResource(exported = false)
    @Override
    <S extends EmailMessage> Iterable<S> saveAll(Iterable<S> entities);

    @RestResource(exported = false)
    @Override
    void deleteById(UUID uuid);

    @RestResource(exported = false)
    @Override
    void delete(EmailMessage entity);

    @RestResource(exported = false)
    @Override
    void deleteAll(Iterable<? extends EmailMessage> entities);

    @RestResource(exported = false)
    @Override
    void deleteAll();
}

