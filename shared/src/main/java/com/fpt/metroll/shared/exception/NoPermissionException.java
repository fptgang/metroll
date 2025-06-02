package com.fpt.metroll.shared.exception;

public class NoPermissionException extends RuntimeException {
    public NoPermissionException() {
        super("No permission");
    }

    public NoPermissionException(String message) {
        super(message);
    }
}
