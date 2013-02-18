package com.Sogamo;

public class SogamoException extends Exception {

	private static final long serialVersionUID = 1L;
	public Exception innerException;

    public SogamoException(String message) {
    	super(message);
    }

    public SogamoException(Exception innerException) {
        this.innerException = innerException;
    }
}
