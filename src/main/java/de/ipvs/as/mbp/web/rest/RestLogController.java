package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.logs.ExceptionLog;
import de.ipvs.as.mbp.error.MissingAdminPrivilegesException;
import de.ipvs.as.mbp.repository.ExceptionLogRepository;
import de.ipvs.as.mbp.service.UserService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for retrieving exception logs. Only administrators are permitted to do so.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/exception-logs")
@Api(tags = {"Exception logs"})
public class RestLogController {

    @Autowired
    private ExceptionLogRepository exceptionLogRepository;

    @Autowired
    private UserService userService;

    /**
     * Retrieves a page of available exception logs, matching the provided page configuration. This action can only
     * be executed by administrators.
     *
     * @param pageable The desired page configuration
     * @return The resulting page of exception logs
     * @throws MissingAdminPrivilegesException In case the current user is no administrator
     */
    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves a page of available exception logs, matching the provided page configuration. Only administrators are permitted to retrieve the logs.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 403, message = "Not authorized to perform this action.")})
    public ResponseEntity<Page<ExceptionLog>> getExceptionLogs(@ApiParam(value = "Page parameters", required = true) Pageable pageable) throws MissingAdminPrivilegesException {
        //Check for admin permissions
        userService.requireAdmin();

        //Retrieve all exception logs matching the passed page configuration
        Page<ExceptionLog> exceptionLogsPage = exceptionLogRepository.findAll(pageable);

        //Create response entity from the found exception logs
        return ResponseEntity.ok(exceptionLogsPage);
    }
}
