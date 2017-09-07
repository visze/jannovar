package de.charite.compbio.jannovar.impl.parse;

import java.io.IOException;

/**
 * {@link InvalidAttributeException} are thrown if a attribute given to a method is invalid and can not be processed.
 *
 * @author mjaeger
 */
public class InvalidAttributeException extends IOException {
	
	/**
	 * Serialization
	 */
	private static final long serialVersionUID = 1L;


	public InvalidAttributeException(String msg) {
		super(msg);
	}


}
