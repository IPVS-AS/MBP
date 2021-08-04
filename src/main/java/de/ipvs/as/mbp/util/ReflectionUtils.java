package de.ipvs.as.mbp.util;

import org.reflections.Reflections;

import java.util.Set;

public class ReflectionUtils {

    private static final String DOMAIN_PACKAGE = "de.ipvs.as.mbp.domain";
    private static final Reflections REFLECTIONS = new Reflections(DOMAIN_PACKAGE);

    public static <T> Set<Class<? extends T>> getSubTypes(Class<T> type) {
        return REFLECTIONS.getSubTypesOf(type);
    }
}
