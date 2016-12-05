package de.charite.compbio.jannovar.vardbs.tabix;

import java.util.List;
import java.util.stream.Collectors;

import de.charite.compbio.jannovar.vardbs.base.VariantContextToRecordConverter;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;

/**
 * Helper class for the conversion of {@link VariantContext} to {@link TabixRecord} objects
 * 
 * @author <a href="mailto:max.schubach@charite.de">Max Schubach</a>
 */
final class TabixEntryToRecordConverter implements VariantContextToRecordConverter<TabixRecord> {

	@Override
	public TabixRecord convert(VariantContext vc) {
		TabixRecordBuilder builder = new TabixRecordBuilder();

		// Column-level properties from VCF file
		builder.setContig(vc.getContig());
		builder.setPos(vc.getStart() - 1);
		builder.setRef(vc.getReference().getBaseString());
		for (Allele all : vc.getAlternateAlleles())
			builder.getAlt().add(all.getBaseString());
		builder.getFilter().addAll(vc.getFilters());

		// Fields from INFO VCF field
		for (String key : vc.getAttributes().keySet()) {

			List<String> lst = vc.getAttributeAsList(key).stream().map(x -> (String) x).collect(Collectors.toList());

			builder.getFields().put(key, lst);
		}

		return builder.build();
	}

}
