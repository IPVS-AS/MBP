package de.ipvs.as.mbp.util.testexecution;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RequiresMQTT {
}
