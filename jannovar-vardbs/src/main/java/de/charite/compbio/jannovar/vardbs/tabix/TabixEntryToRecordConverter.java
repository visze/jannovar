package de.charite.compbio.jannovar.vardbs.tabix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import de.charite.compbio.jannovar.vardbs.base.tabix.TabixFeature;
import de.charite.compbio.jannovar.vardbs.base.tabix.TabixFeatureToRecordConverter;
import htsjdk.variant.variantcontext.VariantContext;

/**
 * Helper class for the conversion of {@link VariantContext} to {@link TabixRecord} objects
 * 
 * @author <a href="mailto:max.schubach@charite.de">Max Schubach</a>
 */
final class TabixEntryToRecordConverter implements TabixFeatureToRecordConverter<TabixRecord> {

	@Override
	public TabixRecord convert(TabixFeature feature) {
		TabixRecordBuilder builder = new TabixRecordBuilder();

		// Column-level properties from VCF file
		builder.setContig(feature.getContig());
		builder.setPos(feature.getStart() - 1);
		builder.setRef(feature.getRef());
		builder.setAlt(Arrays.asList(feature.getAlt()));

		// Fields fields
		for (String key : feature.getAttributes().keySet()) {

			builder.getFields().put(key, Arrays.asList(feature.getAttributes().get(key)));
		}

		return builder.build();
	}

	@Override
	public TabixRecord convert(Collection<TabixFeature> features) {
		TabixRecordBuilder builder = new TabixRecordBuilder();
		builder.setFields(new HashMap<>());

		for (TabixFeature feature : features) {

			// Column-level properties from VCF file
			builder.setContig(feature.getContig());
			builder.setPos(feature.getStart() - 1);
			builder.setRef(feature.getRef());
			builder.setAlt(Arrays.asList(feature.getAlt()));
			

			// Fields fields
			for (String key : feature.getAttributes().keySet()) {
				
				if (builder.getFields().containsKey(key)) {
					List<String> fields = new ArrayList<>();
					fields.addAll(builder.getFields().get(key));
					fields.add(feature.getAttributes().get(key));
					builder.getFields().put(key,fields);
				} else {
					builder.getFields().put(key, Arrays.asList(feature.getAttributes().get(key)));
				}
			}
		}

		return builder.build();
	}

}
