package de.charite.compbio.jannovar.vardbs.g1k;

import java.util.List;
import java.util.stream.Collectors;

import de.charite.compbio.jannovar.vardbs.base.vcf.VariantContextToRecordConverter;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;

/**
 * Helper class for the conversion of {@link VariantContext} to {@link G1KRecord} objects
 * 
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
final class G1KVariantContextToRecordConverter implements VariantContextToRecordConverter<G1KRecord> {

	@Override
	public G1KRecord convert(VariantContext vc) {
		G1KRecordBuilder builder = new G1KRecordBuilder();

		// Column-level properties from VCF file
		builder.setContig(vc.getContig());
		builder.setPos(vc.getStart() - 1);
		builder.setID(vc.getID());
		builder.setRef(vc.getReference().getBaseString());
		for (Allele all : vc.getAlternateAlleles())
			builder.getAlt().add(all.getBaseString());
		builder.getFilter().addAll(vc.getFilters());

		// Fields from INFO VCF field

		// AN: Chromosome count
		int an = vc.getAttributeAsInt("AN", 0);
		builder.setChromCounts(an);

		// AC: Alternative allele count
		for (int i = 0; i < vc.getAlternateAlleles().size(); ++i) {

			List<Integer> lst = vc.getAttributeAsList("AC").stream().map(x -> Integer.parseInt((String) x))
					.collect(Collectors.toList());
			builder.setAlleleCounts(lst);
		}

		// AF: Allele frequencies
		for (G1KPopulation pop : G1KPopulation.values()) {
			List<Double> lst;
			if (pop == G1KPopulation.ALL)
				lst = vc.getAttributeAsList("AF").stream().map(x -> Double.parseDouble((String) x))
						.collect(Collectors.toList());
			else {
				lst = vc.getAttributeAsList(pop + "_AF").stream().map(x -> Double.parseDouble((String) x))
						.collect(Collectors.toList());
			}
			builder.getAlleleFrequencies().put(pop, lst);
		}

		return builder.build();
	}

}
