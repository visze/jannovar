package de.charite.compbio.jannovar.vardbs.base;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import htsjdk.tribble.Feature;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;

public class TabixFeature implements Feature {

	private String contig;
	private int position;
	private List<Allele> alleles;
	private Map<String, String> attributes;

	public TabixFeature(String contig, int position, String ref, String alt, Map<String, String> values) {
		this.alleles = Arrays.asList(Allele.create(ref, true), Allele.create(alt, false));
		this.position = position;
		this.attributes = values;
		this.contig = contig;
	}

	@Override
	public String getContig() {
		return contig;
	}

	@Override
	public int getStart() {
		return position;
	}

	@Override
	public int getEnd() {
		return position;
	}

	@Override
	public String getChr() {
		return contig;
	}

	public VariantContext getVC() {
		return new VariantContextBuilder().chr(contig).start(position).computeEndFromAlleles(alleles, position)
				.alleles(alleles).attributes(attributes).make();
	}

	@Override
	public String toString() {
		return contig + ":" + position + " [ alleles=" + alleles + ", attributes " + attributes + "]";
	}

}
