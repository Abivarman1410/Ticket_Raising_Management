package com.ticketmgmt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InvalidTicketStatusTransitionException extends RuntimeException {
    public InvalidTicketStatusTransitionException(String from, String to) {
        super("Invalid status transition from " + from + " to " + to);
    }
}
