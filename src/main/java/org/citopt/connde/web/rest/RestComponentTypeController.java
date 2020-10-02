package org.citopt.connde.web.rest;

import java.util.List;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.componentType.ComponentType;
import org.citopt.connde.repository.ComponentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller for component types.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Component types"}, description = "Management of component types")
public class RestComponentTypeController {

    @Autowired
    private ComponentTypeRepository componentTypeRepository;
//
//    @PostMapping("/component-types")
//    @Secured({Constants.ADMIN})
//    @ApiOperation(value = "Creates a new component type", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 201, message = "Success"), @ApiResponse(code = 409, message = "Component type does already exist"), @ApiResponse(code = 401, message = "Not authorized to create a new component type")})
//    public ResponseEntity<?> createComponentType(@Valid @RequestBody ComponentType componentType) {
//        ComponentType type = componentTypeRepository.findByName(componentType.getName());
//        if (type != null && type.getComponent().equals(componentType.getComponent())) {
//            return ResponseEntity.badRequest()
//                    .headers(HeaderUtil.createFailureAlert("Component type already exists", componentType.getName()))
//                    .body(null);
//        } else {
//            componentTypeRepository.save(componentType);
//            return ResponseEntity.status(HttpStatus.CREATED)
//                    .headers(HeaderUtil.createAlert("Component type created successfully", componentType.getName()))
//                    .body(componentType);
//        }
//    }

    @GetMapping("/component-types")
    @ApiOperation(value = "Retrieves all available component types", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<Page<ComponentType>> getAllComponentTypes(Pageable pageable) {
        return ResponseEntity.ok(componentTypeRepository.findAll(pageable));
    }

    @GetMapping("/component-types/{component}")
    @ApiOperation(value = "Retrieves a page of only specific component types", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<List<ComponentType>> getSpecificComponentTypes(@PathVariable @ApiParam(value = "Components to retrieve", example = "sensor", required = true) String component, @ApiParam(value = "The page configuration", required = true) Pageable pageable) {
        return ResponseEntity.ok(componentTypeRepository.findAllByComponent(component, pageable));
    }

}
