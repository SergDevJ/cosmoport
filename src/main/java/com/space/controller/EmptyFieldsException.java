package com.space.controller;

public class EmptyFieldsException extends RuntimeException {
    public EmptyFieldsException() {}
    public EmptyFieldsException(String message) {
        super(message);
    }
}
