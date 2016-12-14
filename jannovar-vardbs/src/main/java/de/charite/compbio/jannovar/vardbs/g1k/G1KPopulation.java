package de.charite.compbio.jannovar.vardbs.g1k;

/**
 * Enum type for populations in the 1KG data set
 * 
 * @author <a href="mailto:max.schubach@charite.de">Max Schubach</a>
 */
public enum G1KPopulation {
	/** African/African American */
	AFR,
	/** American */
	AMR,
	/** East Asian */
	EAS,
	/** South asian population */
	SAS,
	/** Pseudo-population meaning "all pooled together" */
	ALL;

	public String getLabel() {
		switch (this) {
		case AFR:
			return "African/African American";
		case AMR:
			return "East Asian";
		case SAS:
			return "South Asian";
		case ALL:
			return "All";
		default:
			return "Undefined";
		}
	}
}
