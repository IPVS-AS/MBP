package de.ipvs.as.mbp.repository;

import java.util.List;

import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.repository.projection.ComponentExcerpt;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import springfox.documentation.annotations.ApiIgnore;

@NoRepositoryBean
@ApiIgnore("Not an actual repository, only used as super interface")
public interface ComponentRepository<C extends Component> extends UserEntityRepository<C> {
	
//	@RestResource(exported = false)
//	Optional<C> findByName(@Param("name") String name);

	@RestResource(exported = false)
	List<ComponentExcerpt> findAllByOperatorId(@Param("adapter.id") String adapterId);

	@RestResource(exported = false)
	List<ComponentExcerpt> findAllByDeviceId(@Param("device.id") String deviceId);
	
}