package com.mytech.api.models;

public class InsufficientFundsException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = -213127091777902669L;

	public InsufficientFundsException(String message) {
        super(message);
    }
}
