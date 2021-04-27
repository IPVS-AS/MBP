package de.ipvs.as.mbp.domain.logs;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.ipvs.as.mbp.util.InstantSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.annotation.Id;

import javax.persistence.GeneratedValue;
import java.time.Instant;

/**
 * Domain model for exception logs, wrapping thrown exceptions and their context.
 */
@ApiModel(description = "Model for exception log entities")
public class ExceptionLog {

    @Id
    @GeneratedValue
    @ApiModelProperty(notes = "Log ID", example = "5c8f7ad66f9e3c1bacb0fa99", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true, required = true)
    private String id;

    @ApiModelProperty(notes = "Exception type", example = "DeploymentException", required = true)
    private String exceptionType;

    @ApiModelProperty(notes = "Message of the exception")
    private String message;

    @ApiModelProperty(notes = "Stack trace of the exception")
    private String stackTrace;

    @ApiModelProperty(notes = "Root cause message of the exception")
    private String rootCauseMessage;

    @JsonSerialize(using = InstantSerializer.class)
    @ApiModelProperty(notes = "Timestamp when the exception was thrown.")
    private Instant timestamp;

    @ApiModelProperty(notes = "Name of the user that was active when the exception was thrown")
    private String userName;

    /**
     * Creates a new exception log.
     */
    public ExceptionLog() {
    }

    /**
     * Creates a new exception log from a given exception without username.
     */

    public ExceptionLog(Exception exception) {
        this(exception, null);
    }

    /**
     * Creates a new exception log from a given exception and user name.
     */

    public ExceptionLog(Exception exception, String userName) {
        //Get exception class
        this.exceptionType = exception.getClass().getName();

        //Get exception message
        this.message = exception.getMessage();

        //Get stack trace
        this.stackTrace = ExceptionUtils.getStackTrace(exception);

        //Get root cause stack trace
        this.rootCauseMessage = ExceptionUtils.getRootCauseMessage(exception);

        //Set timestamp
        this.timestamp = Instant.now();

        //Store username
        this.userName = userName;
    }

    public String getId() {
        return id;
    }

    public ExceptionLog setId(String id) {
        this.id = id;
        return this;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public ExceptionLog setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
        return this;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public ExceptionLog setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
        return this;
    }

    public String getRootCauseMessage() {
        return rootCauseMessage;
    }

    public ExceptionLog setRootCauseMessage(String rootCauseMessage) {
        this.rootCauseMessage = rootCauseMessage;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ExceptionLog setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public ExceptionLog setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public ExceptionLog setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}
