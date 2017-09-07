package de.charite.compbio.jannovar.hgvs;

/**
 * Base class for unchecked exceptions in Jannovar in HGVS
 *
 * @author <a href="mailto:max.schubach@bihealth.de">Max Schubach</a>
 */
public class UncheckedHGVSJannovarException extends RuntimeException {

	public static final long serialVersionUID = 2L;

	public UncheckedHGVSJannovarException() {
		super();
	}

	public UncheckedHGVSJannovarException(String msg) {
		super(msg);
	}

	public UncheckedHGVSJannovarException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
