package app.web;

import app.domain.MimeMessageMeta;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(path = "emails", itemResourceRel = "email", collectionResourceRel = "emails")
public interface MimeMessageMetaRepository extends PagingAndSortingRepository<MimeMessageMeta, UUID> {

    @RestResource(exported = false)
    @Override
    <S extends MimeMessageMeta> S save(S entity);

    @RestResource(exported = false)
    @Override
    <S extends MimeMessageMeta> Iterable<S> saveAll(Iterable<S> entities);

    @RestResource(exported = false)
    @Override
    void deleteById(UUID uuid);

    @RestResource(exported = false)
    @Override
    void delete(MimeMessageMeta entity);

    @RestResource(exported = false)
    @Override
    void deleteAll(Iterable<? extends MimeMessageMeta> entities);

    @RestResource(exported = false)
    @Override
    void deleteAll();
}
