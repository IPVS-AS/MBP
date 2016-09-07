package org.citopt.websensor.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

@ControllerAdvice
public class ExceptionHandlerController {

    //@ExceptionHandler(Exception.class)
    //@RequestMapping({"/404", "error/404"})
    public String handleException(Exception e) {
        return "error/404";
    }
}
