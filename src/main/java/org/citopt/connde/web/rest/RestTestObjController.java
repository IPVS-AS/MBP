package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.TestObj;
import org.citopt.connde.domain.device.Device;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller for managing {@link Device devices}.
 * 
 * @author Jakob Benz
 */
@BasePathAwareController
@RequestMapping(RestConfiguration.BASE_PATH + "/testObj")
@Api(tags = {"TestObjs"})
//@EnableHypermediaSupport(type = HypermediaType.HAL)
public class RestTestObjController {
	
//    @GetMapping(path = "/1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
//    @ApiOperation(value = "Retrieves all existing device entities available for the requesting entity.", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
//    public CollectionModel<EntityModel<TestObj>> all1() {
//    	List<TestObj> list = new ArrayList<>();
//    	IntStream.range(0, 3).forEach(i -> list.add(getTestObj(i)));
//    	
//    	for (TestObj t : list) {
//    		Link selfLink = linkTo(RestTestObjController.class).slash("/1").withSelfRel();
//    		t.add(selfLink);
//    	}
//    	
//    	
//    	Link selfLink = linkTo(RestTestObjController.class).withSelfRel();
//    	CollectionModel<EntityModel<TestObj>> cm = CollectionModel.of(list, selfLink);
//    	return cm;
//    }

    @GetMapping(path = "/2", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all existing device entities available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public CollectionModel<EntityModel<TestObj>> all2(ACAccessRequest accessRequest) {
    	List<TestObj> list = new ArrayList<>();
    	IntStream.range(0, 3).forEach(i -> list.add(getTestObj(i)));
    	
    	for (TestObj t : list) {
    		Link selfLink = linkTo(RestTestObjController.class).slash("/2").withSelfRel();
    		t.add(selfLink);
    	}
    	
    	Link selfLink = linkTo(RestTestObjController.class).withSelfRel();
    	CollectionModel<EntityModel<TestObj>> cm = CollectionModel.of(list.stream().map(EntityModel::of).collect(Collectors.toList()), selfLink);
    	return cm;
    }
    
    private TestObj getTestObj(int i ) {
    	TestObj t = new TestObj();
    	t.i = i;
    	t.s = "s" + i;
    	return t;
    }
    
}
