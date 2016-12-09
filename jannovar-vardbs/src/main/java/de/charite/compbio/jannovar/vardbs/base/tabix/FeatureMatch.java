package de.charite.compbio.jannovar.vardbs.base.tabix;

import htsjdk.variant.variantcontext.VariantContext;

/**
 * A class for annotating the match between an observed genotype and a database genotype
 * 
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public final class FeatureMatch {

	/** Numeric index of the observed allele */
	final int observedAllele;
	/** The observed VariantContext */
	final VariantContext obsVC;
	/** The database Feature */
	final TabixFeature dbFeature;

	public FeatureMatch(int observedAllele, VariantContext obsVC, TabixFeature dbFeature) {
		this.observedAllele = observedAllele;
		this.obsVC = obsVC;
		this.dbFeature = dbFeature;
	}

	public int getObservedAllele() {
		return observedAllele;
	}

	public VariantContext getObsVC() {
		return obsVC;
	}
	
	public TabixFeature getDbFeature() {
		return dbFeature;
	}

	@Override
	public String toString() {
		return "GenotypeMatch [observedAllele=" + observedAllele + ", obsVC=" + obsVC + ", dbVC=" + dbFeature + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dbFeature == null) ? 0 : dbFeature.hashCode());
		result = prime * result + ((obsVC == null) ? 0 : obsVC.hashCode());
		result = prime * result + observedAllele;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FeatureMatch other = (FeatureMatch) obj;
		if (dbFeature == null) {
			if (other.dbFeature != null)
				return false;
		} else if (!dbFeature.equals(other.dbFeature))
			return false;
		if (obsVC == null) {
			if (other.obsVC != null)
				return false;
		} else if (!obsVC.equals(other.obsVC))
			return false;
		if (observedAllele != other.observedAllele)
			return false;
		return true;
	}

}
