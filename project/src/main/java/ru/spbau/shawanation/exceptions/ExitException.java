package ru.spbau.shawanation.exceptions;

import org.springframework.boot.ExitCodeGenerator;


/**
 * Throwing this exception will stop Spring Boot application
 */
public class ExitException extends RuntimeException implements ExitCodeGenerator {

    @Override
    public int getExitCode() {
        return 1;
    }

}