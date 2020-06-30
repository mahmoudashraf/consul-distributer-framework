package com.consul.leader.elections.services;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.consul.leader.elections.dto.ServiceNodeInfo;
import com.orbitz.consul.model.catalog.CatalogService;

public class Helper {
    private Helper() {

    }

    public static boolean matches(String pattern, String target) {
        if (Objects.equals(pattern, target)) {
            return true;
        }

        if (Objects.isNull(pattern)) {
            return true;
        }

        if (Objects.equals("*", pattern)) {
            return true;
        }

        if (AntPathMatcher.INSTANCE.match(pattern, target)) {
            return true;
        }

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(target);

        return m.matches();
    }

    public static <T> void ifNotEmpty(T value, Consumer<T> consumer) {
        if (isNotEmpty(value)) {
            consumer.accept(value);
        }

    }

    public static boolean isNotEmpty(Object value) {
        if (value == null) {
            return false;
        } else if (value instanceof String) {
            String text = (String) value;
            return text.trim().length() > 0;
        } else if (value instanceof Collection) {
            return !((Collection<?>) value).isEmpty();
        } else if (value instanceof Map) {
            return !((Map<?, ?>) value).isEmpty();
        } else {
            return true;
        }
    }


    public static boolean isLocalService(CatalogService catalogService, ServiceNodeInfo service) {
        if (/*
             * catalogService.getAddress().equals(service.getIPAddress()) &&
             */ catalogService.getServicePort() == service.getPort())
            return true;

        return false;

    }

}

