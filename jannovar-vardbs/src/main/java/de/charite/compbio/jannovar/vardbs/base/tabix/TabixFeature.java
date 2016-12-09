package de.charite.compbio.jannovar.vardbs.base.tabix;

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
	private String ref;
	private String alt;
	private Map<String, String> attributes;

	public TabixFeature(String contig, int position, String ref, String alt, Map<String, String> values) {
		this.ref = ref;
		this.alt = alt;
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
	
	/**
	 * @return the ref
	 */
	public String getRef() {
		return ref;
	}
	
	/**
	 * @return the alt
	 */
	public String getAlt() {
		return alt;
	}
	
	public Map<String, String> getAttributes() {
		return attributes;
	}

	@Override
	public String toString() {
		return contig + ":" + position + " [ ref=" + ref + ", alt=" + alt +", attributes=" + attributes + "]";
	}

}
