package org.citopt.connde.repository;

import io.swagger.annotations.ApiOperation;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.repository.projection.ComponentExcerpt;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostFilter;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@NoRepositoryBean
@ApiIgnore("Not an actual repository, only used as super interface")
public interface ComponentRepository<C extends Component> extends UserEntityRepository<C> {
    @RestResource(exported = false)
    C findByName(@Param("name") String name);

    @RestResource(exported = false)
    List<ComponentExcerpt> findAllByAdapterId(@Param("adapter.id") String adapterId);

    @RestResource(exported = false)
    List<ComponentExcerpt> findAllByDeviceId(@Param("device.id") String deviceId);
}