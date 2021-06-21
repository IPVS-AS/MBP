package de.ipvs.as.mbp.service.logs;

import de.ipvs.as.mbp.domain.logs.ExceptionLog;
import de.ipvs.as.mbp.repository.ExceptionLogRepository;
import de.ipvs.as.mbp.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for transforming exceptions to exception logs and writing them into the exception log repository.
 */
@Service
public class ExceptionLogService {

    @Autowired
    private ExceptionLogRepository exceptionLogRepository;

    @Autowired
    private UserService userService;

    /**
     * Creates an exception log with all details and information of interest from a given exception object and writes it
     * into the exception log repository.
     *
     * @param exception The exception from which the exception log is supposed to be created
     */
    public void writeExceptionLog(Exception exception) {
        //Sanity check
        if (exception == null) {
            throw new IllegalArgumentException("Exception must not be null.");
        }

        //Get name of currently active user
        String userName = "None";
        try {
            userName = userService.getLoggedInUser().getUsername();
        } catch (Exception ignored) {
        }

        //Create exception log
        ExceptionLog exceptionLog = new ExceptionLog(exception, userName);

        //Insert log into repository
        exceptionLogRepository.insert(exceptionLog);
    }
}
