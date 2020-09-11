package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.constants.Constants;
import org.citopt.connde.domain.entity_type.ComponentType;
import org.citopt.connde.repository.ComponentTypeRepository;
import org.citopt.connde.web.rest.util.HeaderUtil;
import org.citopt.connde.web.rest.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller for component types.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Component types"}, description = "Management of component types")
public class RestComponentTypeController {

    @Autowired
    private ComponentTypeRepository componentTypeRepository;

    @PostMapping("/component-types")
    @Secured({Constants.ADMIN})
    @ApiOperation(value = "Creates a new component type", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "Success"), @ApiResponse(code = 400, message = "Component type does already exist"), @ApiResponse(code = 403, message = "Not authorized to create a new component type")})
    public ResponseEntity<?> createComponentType(@Valid @RequestBody ComponentType componentType) {
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
    @ApiOperation(value = "Retrieves all available component types", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<List<ComponentType>> getAllComponentTypes(Pageable pageable)
            throws URISyntaxException {
        Page<ComponentType> page = componentTypeRepository.findAll(pageable);
        List<ComponentType> componentTypes = new ArrayList<>(page.getContent());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/component-types");
        return new ResponseEntity<>(componentTypes, headers, HttpStatus.OK);
    }

    @GetMapping("/component-types/{component}")
    @ApiOperation(value = "Retrieves a page of only specific component types", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<List<ComponentType>> getSpecificComponentTypes(@PathVariable @ApiParam(value = "Components to retrieve", example = "sensor", required = true) String component, @ApiParam(value = "The page configuration", required = true) Pageable pageable) {
        List<ComponentType> componentTypes = componentTypeRepository.findAllByComponent(component, pageable);
        return new ResponseEntity<>(componentTypes, HttpStatus.OK);
    }

}
