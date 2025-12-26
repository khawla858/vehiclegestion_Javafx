package com.example.vehiclegestion.logging.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    // Méthodes d'aide pour le logging
    public static void info(Class<?> clazz, String message) {
        LoggerFactory.getLogger(clazz).info(message);
    }

    public static void info(Class<?> clazz, String message, Object... args) {
        LoggerFactory.getLogger(clazz).info(message, args);
    }

    public static void debug(Class<?> clazz, String message) {
        LoggerFactory.getLogger(clazz).debug(message);
    }

    public static void debug(Class<?> clazz, String message, Object... args) {
        LoggerFactory.getLogger(clazz).debug(message, args);
    }

    public static void error(Class<?> clazz, String message) {
        LoggerFactory.getLogger(clazz).error(message);
    }

    public static void error(Class<?> clazz, String message, Object... args) {
        LoggerFactory.getLogger(clazz).error(message, args);
    }

    public static void warn(Class<?> clazz, String message) {
        LoggerFactory.getLogger(clazz).warn(message);
    }

    public static void warn(Class<?> clazz, String message, Object... args) {
        LoggerFactory.getLogger(clazz).warn(message, args);
    }

    public static void trace(Class<?> clazz, String message) {
        LoggerFactory.getLogger(clazz).trace(message);
    }
}