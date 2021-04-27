package de.ipvs.as.mbp.error;

import de.ipvs.as.mbp.service.logs.ExceptionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private ExceptionLogService exceptionLogService;

    /**
     * Handles a EntityValidationException indicating errors during entity validation on creation and
     * replies with a corresponding server response.
     *
     * @param exception The exception to handle
     * @return The response for the given exception
     */
    @ExceptionHandler(EntityValidationException.class)
    public ResponseEntity<ApiError> handleValidationError(EntityValidationException exception) {
        //Create API error
        ApiError error;

        //Decide about status code
        HttpStatus status = exception.hasInvalidFields() ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;

        //Check if there were invalid fies
        if (exception.hasInvalidFields()) {
            error = new ApiError(status, Instant.now(), "One or more parameters are invalid or missing.", exception.getInvalidFields());
        } else {
            error = new ApiError(status, Instant.now(), exception.getMessage());
        }
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(EntityStillInUseException.class)
    public ResponseEntity<ApiError> handleEntityStillInUseExists(EntityStillInUseException exception) {
        //TODO remove
        //Write exception into exception log repository
        exceptionLogService.writeExceptionLog(exception);


        //Create API error from exception and return corresponding response entity
        ApiError error = new ApiError(HttpStatus.CONFLICT, Instant.now(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MissingPermissionException.class)
    public ResponseEntity<ApiError> handleMissingPermission(MissingPermissionException exception) {
        //Create API error from exception and return corresponding response entity
        ApiError error = new ApiError(HttpStatus.UNAUTHORIZED, Instant.now(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MissingAdminPrivilegesException.class)
    public ResponseEntity<ApiError> handleMissingAdminPrivileges(MissingAdminPrivilegesException exception) {
        //Create API error from exception and return corresponding response entity
        ApiError error = new ApiError(HttpStatus.UNAUTHORIZED, Instant.now(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MissingOwnerPrivilegesException.class)
    public ResponseEntity<ApiError> handleMissingOwnerPrivileges(MissingOwnerPrivilegesException exception) {
        //Create API error from exception and return corresponding response entity
        ApiError error = new ApiError(HttpStatus.UNAUTHORIZED, Instant.now(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiError> handleInvalidPassword(InvalidPasswordException exception) {
        //Create API error from exception and return corresponding response entity
        ApiError error = new ApiError(HttpStatus.FORBIDDEN, Instant.now(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(NoSystemUserException.class)
    public ResponseEntity<ApiError> handleNoSystemUser(NoSystemUserException exception) {
        //Create API error from exception and return corresponding response entity
        ApiError error = new ApiError(HttpStatus.FORBIDDEN, Instant.now(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(UserNotLoginableException.class)
    public ResponseEntity<ApiError> handleUserNotLoginable(UserNotLoginableException exception) {
        //Create API error from exception and return corresponding response entity
        ApiError error = new ApiError(HttpStatus.FORBIDDEN, Instant.now(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(DeploymentException.class)
    public ResponseEntity<ApiError> handleDeploymentError(DeploymentException exception) {
        //Write exception into exception log repository
        exceptionLogService.writeExceptionLog(exception);

        //Create API error from exception and return corresponding response entity
        ApiError error;
        HttpStatus status = exception.getInvalidParameters().isEmpty() ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
        if (exception.getInvalidParameters().isEmpty()) {
            error = new ApiError(status, Instant.now(), exception.getMessage());
        } else {
            error = new ApiError(status, Instant.now(), "One or more parameters are invalid or missing.", exception.getInvalidParameters());
        }
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(ResponseStatusException exception) {
        //Write exception into exception log repository
        exceptionLogService.writeExceptionLog(exception);

        //Create API error from exception and return corresponding response entity
        exception.getStatus();
        ApiError error = new ApiError(exception.getStatus(), Instant.now(), exception.getMessage());
        exception.getStatus();
        return ResponseEntity.status(exception.getStatus()).body(error);
    }

    @ExceptionHandler(MBPException.class)
    public ResponseEntity<ApiError> handleMBPException(MBPException exception) {
        //Write exception into exception log repository
        exceptionLogService.writeExceptionLog(exception);

        //Create API error from exception and return corresponding response entity
        ApiError error = new ApiError(exception.getStatus() == null ? HttpStatus.INTERNAL_SERVER_ERROR : exception.getStatus(), Instant.now(), exception.getMessage());
        return ResponseEntity.status(exception.getStatus() == null ? HttpStatus.INTERNAL_SERVER_ERROR : exception.getStatus()).body(error);
    }

    /**
     * Handler for all remaining exceptions of arbitrary type.
     *
     * @param exception The exception to handle
     * @return The response entity as resulting from the exception handling
     */
    @ExceptionHandler
    public ResponseEntity<ApiError> handleException(Exception exception) {
        //Write exception into exception log repository
        exceptionLogService.writeExceptionLog(exception);

        //Create API error from exception
        ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, Instant.now(), "An unexpected internal error occurred.");

        //Wrap API error into response entity
        return ResponseEntity.status(error.getStatus()).body(error);
    }

}
