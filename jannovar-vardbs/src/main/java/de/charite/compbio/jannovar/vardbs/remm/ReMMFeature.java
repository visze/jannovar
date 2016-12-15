package de.charite.compbio.jannovar.vardbs.remm;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import htsjdk.tribble.Feature;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;

public class ReMMFeature implements Feature {

	private String contig;
	private int position;
	private double score;

	public ReMMFeature(String contig, int position, double score) {
		this.position = position;
		this.score = score;
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
	
	public double getScore() {
		return score;
	}
	

	@Override
	public String toString() {
		return contig + ":" + position + " [ score=" + score + "]";
	}

}
