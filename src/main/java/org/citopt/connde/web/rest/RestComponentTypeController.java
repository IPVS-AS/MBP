package org.citopt.connde.web.rest;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.componentType.ComponentType;
import org.citopt.connde.repository.ComponentTypeRepository;
import org.citopt.connde.web.rest.util.HeaderUtil;
import org.citopt.connde.web.rest.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for component types.
 * @author Imeri Amil
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestComponentTypeController {
	
    @Autowired
    private ComponentTypeRepository componentTypeRepository;
    
    @PostMapping("/component-types")
    public ResponseEntity<?> createComponentType(@Valid @RequestBody ComponentType componentType) throws URISyntaxException {
        ComponentType type = componentTypeRepository.findByName(componentType.getName());
        if (type != null && type.getComponent().equals(componentType.getComponent())) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("Component type already exists", componentType.getName()))
                    .body(null);
        } else {
            componentTypeRepository.save(componentType);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .headers(HeaderUtil.createAlert("Component type created successfully", componentType.getName()))
                    .body(componentType);
        }
    }

    @GetMapping("/component-types")
    public ResponseEntity<List<ComponentType>> getAllComponentTypes(Pageable pageable)
            throws URISyntaxException {
        Page<ComponentType> page = componentTypeRepository.findAll(pageable);
        List<ComponentType> componentTypes = page.getContent().stream()
                .collect(Collectors.toList());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/component-types");
        return new ResponseEntity<>(componentTypes, headers, HttpStatus.OK);
    }

    @GetMapping("/component-types/{component}")
    public ResponseEntity<List<ComponentType>> getSpecificComponentTypes(@PathVariable String component, Pageable pageable) {
        List<ComponentType> componentTypes = componentTypeRepository.findAllByComponent(component, pageable);
        return new ResponseEntity<>(componentTypes, HttpStatus.OK);
    }

}
