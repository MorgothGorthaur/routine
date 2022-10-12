package com.example.routine.exception;
@SuppressWarnings("serial")
public class ParticipantNotFoundException extends RuntimeException {
	public ParticipantNotFoundException(Long id) {
	        super("Participant is not found, id="+id);
	    }
}
