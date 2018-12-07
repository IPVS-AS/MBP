package org.citopt.connde.repository;

import org.citopt.connde.domain.component.Component;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface ComponentRepository<C extends Component>
        extends MongoRepository<C, String> {
    C findByName(@Param("name") String name);
}
