package app.web;

import app.domain.EmailMessage;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.UUID;

@RepositoryRestResource(path = "emails", itemResourceRel = "email", collectionResourceRel = "emails")
public interface EmailMessageRepository extends CrudRepository<EmailMessage, UUID> {

    @RestResource(path = "emails", rel = "emails")
    List<EmailMessage> findAll(Sort sort);

    @RestResource(exported = false)
    @Override
    <S extends EmailMessage> S save(S entity);

    @RestResource(exported = false)
    @Override
    <S extends EmailMessage> Iterable<S> saveAll(Iterable<S> entities);
}

