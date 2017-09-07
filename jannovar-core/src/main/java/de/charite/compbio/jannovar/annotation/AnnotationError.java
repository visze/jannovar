package de.charite.compbio.jannovar.annotation;

public class AnnotationError extends Error {

	/**
	 * Serial id
	 */
	private static final long serialVersionUID = 1L;

	public AnnotationError(String msg) {
		super(msg);
	}

	public AnnotationError(String msg, Throwable cause) {
		super(msg, cause);
	}
}
