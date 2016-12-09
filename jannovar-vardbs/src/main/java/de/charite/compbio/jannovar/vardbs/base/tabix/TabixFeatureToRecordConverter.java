package de.charite.compbio.jannovar.vardbs.base.tabix;

import java.util.Collection;

import de.charite.compbio.jannovar.vardbs.tabix.TabixRecord;
import htsjdk.variant.variantcontext.VariantContext;

/**
 * Conversion of {@link VariantContext} objects to record objects.
 * 
 * @author <a href="mailto:max.schubach@charite.de">Max Schubach</a>
 */
public interface TabixFeatureToRecordConverter<RecordType> {

	/**
	 * Convert {@link VariantContext} into record type <code>T</code>
	 * 
	 * @param vc
	 *            {@link VariantContext} to convert
	 * @return Resulting record object of type <code>T</code>
	 */
	public RecordType convert(TabixFeature vc);

	public RecordType convert(Collection<TabixFeature> features);

}
