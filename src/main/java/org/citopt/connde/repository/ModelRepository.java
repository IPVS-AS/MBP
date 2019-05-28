package org.citopt.connde.repository;

import java.util.Optional;

import org.citopt.connde.domain.model.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

/**
* Spring Data MongoDB repository for the Model entity.
* @author Imeri Amil
*/
public interface ModelRepository extends MongoRepository<Model, String> {
    Page<Model> findAllByUsername(@Param("username") String username, Pageable pageable);
    Optional<Model> findOneByNameAndUsername(@Param("name") String name, @Param("username") String username);
}
