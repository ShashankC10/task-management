package org.example.task_management.controller;

import org.example.task_management.model.exception.InvalidTaskTransitionException;
import org.example.task_management.model.Status;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404WithMessage() {
        NoSuchElementException ex = new NoSuchElementException("missing");

        ResponseEntity<String> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", response.getBody());
    }

    @Test
    void handleInvalidTransition_returns400() {
        InvalidTaskTransitionException ex =
                new InvalidTaskTransitionException(1L, Status.PENDING, Status.DONE, "invalid");

        ResponseEntity<String> response = handler.handleInvalidTransition(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("invalid", response.getBody());
    }

    @Test
    void handleOtherExceptions_returns500() {
        RuntimeException ex = new RuntimeException("boom");

        ResponseEntity<String> response = handler.handleOtherExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred: boom", response.getBody());
    }
}
