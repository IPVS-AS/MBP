package org.citopt.connde.error;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author Jakob Benz
 */
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException e) {
		ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, LocalDateTime.now(), "An internal error occurred due to an illegal argument. Please contact Pascal Hirmer.");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}
	
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFoundException e) {
		ApiError error = new ApiError(HttpStatus.NOT_FOUND, LocalDateTime.now(), e.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}
	
	@ExceptionHandler(EntityAlreadyExistsException.class)
	public ResponseEntity<ApiError> handleEntityAlreadyExists(EntityAlreadyExistsException e) {
		ApiError error = new ApiError(HttpStatus.CONFLICT, LocalDateTime.now(), e.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}
	
	@ExceptionHandler(EntityStillInUseException.class)
	public ResponseEntity<ApiError> handleEntityStillInUseExists(EntityStillInUseException e) {
		ApiError error = new ApiError(HttpStatus.CONFLICT, LocalDateTime.now(), e.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}
	
	@ExceptionHandler(DeploymentException.class)
	public ResponseEntity<ApiError> handleDeploymentError(DeploymentException e) {
		ApiError error;
		HttpStatus status = e.getInvalidParameters().isEmpty() ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
		if (e.getInvalidParameters().isEmpty()) {
			error = new ApiError(status, LocalDateTime.now(), e.getMessage());
		} else {
			error = new ApiError(status, LocalDateTime.now(), "One or more parameters are invalid or missing.", e.getInvalidParameters());
		}
		return ResponseEntity.status(status).body(error);
	}
	
	@ExceptionHandler(MissingPermissionException.class)
	public ResponseEntity<ApiError> handleMissingPermission(MissingPermissionException e) {
		ApiError error = new ApiError(HttpStatus.UNAUTHORIZED, LocalDateTime.now(), e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}
	
	@ExceptionHandler(MissingAdminPrivilegesException.class)
	public ResponseEntity<ApiError> handleMissingAdminPrivileges(MissingAdminPrivilegesException e) {
		ApiError error = new ApiError(HttpStatus.UNAUTHORIZED, LocalDateTime.now(), e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}
	
	@ExceptionHandler(MissingOwnerPrivilegesException.class)
	public ResponseEntity<ApiError> handleMissingOwnerPrivileges(MissingOwnerPrivilegesException e) {
		ApiError error = new ApiError(HttpStatus.UNAUTHORIZED, LocalDateTime.now(), e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}
	
	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<ApiError> handleInvalidPassword(InvalidPasswordException e) {
		ApiError error = new ApiError(HttpStatus.FORBIDDEN, LocalDateTime.now(), e.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
	}
	
	@ExceptionHandler(MBPException.class)
	public ResponseEntity<ApiError> handleMBPException(MBPException e) {
		ApiError error = new ApiError(e.getStatus() == null ? HttpStatus.INTERNAL_SERVER_ERROR : e.getStatus(), LocalDateTime.now(), e.getMessage());
		return ResponseEntity.status(e.getStatus() == null ? HttpStatus.INTERNAL_SERVER_ERROR : e.getStatus()).body(error);
	}
	
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiError> handleResponseStatusException(ResponseStatusException e) {
		ApiError error = new ApiError(e.getStatus() == null ? HttpStatus.INTERNAL_SERVER_ERROR : e.getStatus(), LocalDateTime.now(), e.getMessage());
		return ResponseEntity.status(e.getStatus() == null ? HttpStatus.INTERNAL_SERVER_ERROR : e.getStatus()).body(error);
	}

}
