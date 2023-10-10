package com.trankhaihung.cnpm.lamda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

public class SafeValue {

    private static final String EMPTY_STRING = "";
    private static final boolean DEFAULT_BOOLEAN = false;
    private static final int DEFAULT_INTEGER = 0;

    private SafeValue() {
    }

    public static String ofString(Callable<String> callable) {
        return of(callable, EMPTY_STRING);
    }

    public static boolean ofBoolean(Callable<Boolean> callable) {
        return of(callable, DEFAULT_BOOLEAN);
    }

    public static int ofInt(Callable<Integer> callable) {
        return of(callable, DEFAULT_INTEGER);
    }

    public static boolean asBoolean(Callable<Object> callable) {
        return ofBoolean(() -> Boolean.parseBoolean(callable.call().toString()));
    }

    public static <O> List<O> ofList(Callable<List<O>> supplier){
        return of(supplier, new ArrayList<>());
    }

    private static <T> T of(Callable<T> callable, T defaultValue) {
        Objects.requireNonNull(defaultValue);

        T result = null;

        try {
            result = callable.call();
        } catch (Exception ignore) {

        }

        return Objects.nonNull(result) ? result : defaultValue;
    }
}
