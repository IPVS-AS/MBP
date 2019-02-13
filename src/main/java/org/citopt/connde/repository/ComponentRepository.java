package org.citopt.connde.repository;

import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.componentType.ComponentType;
import org.citopt.connde.repository.projection.ComponentProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;

@NoRepositoryBean
public interface ComponentRepository<C extends Component>
        extends MongoRepository<C, String> {
    C findByName(@Param("name") String name);

    List<ComponentProjection> findAllByAdapterId(@Param("adapter.id") String adapterId);

    List<ComponentProjection> findAllByDeviceId(@Param("device.id") String deviceId);
}
