package com.telemed.exception;

public class BillingRejectedException extends RuntimeException {
    public BillingRejectedException(String message) { super(message); }
}