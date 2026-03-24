package com.togezzer.chatsauvegarde;

import java.lang.reflect.Field;

public final class TestReflectionUtils {

    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible d'injecter le champ " + fieldName, e);
        }
    }
}
