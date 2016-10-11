package ru.spbau.shawanation.utils;

import java.util.logging.Logger;

/**
 * GlobalLogger is a class for convenient wrong behaviour logging
 */
public class GlobalLogger {
    private static Logger logger = Logger.getLogger(GlobalLogger.class.getName());

    /**
     * Logs a message to logger object
     * @param message - info that should be logged
     */
    public static void log(String message) {
        logger.info(message);
    }
}
