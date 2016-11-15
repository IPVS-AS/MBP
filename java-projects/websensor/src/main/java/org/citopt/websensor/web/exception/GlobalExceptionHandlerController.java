package org.citopt.websensor.web.exception;

import javax.servlet.http.HttpServletRequest;
import org.citopt.websensor.dao.InsertFailureException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandlerController {

    public static final String DEFAULT_ERROR_VIEW = "error";
    
    /*@ResponseStatus(value = HttpStatus.BAD_REQUEST,
            reason = "Fail on insert operation")  // 400*/
    @ExceptionHandler(InsertFailureException.class)
    public String insertFailure() {
        return "error/400";
    }
    
    /*@ResponseStatus(value = HttpStatus.BAD_REQUEST,
            reason = "Bad request")  // 400*/    
    @ExceptionHandler(BadRequestException.class)
    public String badRequest() {
        return "error/400";
    }

    /*@ResponseStatus(value = HttpStatus.NOT_FOUND,
            reason = "Requested id not found")  // 404*/
    @ExceptionHandler(NotFoundException.class)
    public String idNotFound() {
        return "error/404";
    }
    
    @ExceptionHandler(value = Exception.class)
    public ModelAndView
            defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        // If the exception is annotated with @ResponseStatus rethrow it and let
        // the framework handle it - like the OrderNotFoundException example
        // at the start of this post.
        // AnnotationUtils is a Spring Framework utility class.
        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null) {
            throw e;
        }

        // Otherwise setup and send the user to a default error-view.
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", e);
        mav.addObject("url", req.getRequestURL());
        mav.setViewName(DEFAULT_ERROR_VIEW);
        return mav;
    }
}
