/**
 * 
 */
package de.charite.compbio.jannovar.vardbs.remm;

import java.util.regex.Pattern;

import de.charite.compbio.jannovar.vardbs.base.tabix.TabixFeature;
import htsjdk.tribble.AsciiFeatureCodec;
import htsjdk.tribble.readers.LineIterator;

/**
 * Codec for parsing a tabix CADD file, as described by Kircher et.al.
 * 
 * @author Max Schubach
 *
 */
public class ReMMCodec extends AsciiFeatureCodec<ReMMFeature> {
	public static final Pattern SPLIT_PATTERN = Pattern.compile("\\t|( +)");

	/**
	 * @param myClass
	 */
	public ReMMCodec() {
		super(ReMMFeature.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see htsjdk.tribble.FeatureCodec#canDecode(java.lang.String)
	 */
	@Override
	public boolean canDecode(String path) {
		return path.toLowerCase().endsWith(".tsv") || path.toLowerCase().endsWith(".tsv.gz");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see htsjdk.tribble.AsciiFeatureCodec#decode(java.lang.String)
	 */
	@Override
	public ReMMFeature decode(String line) {
		if (line.trim().isEmpty()) {
			return null;
		}

		if (line.startsWith("#")) { // we do not read the header
			return null;
		}

		String[] tokens = SPLIT_PATTERN.split(line, -1);
		return decode(tokens);
	}

	/**
	 * Decode a line splitted by tabs (not a header line) of a Tabix file into a tabix feature
	 * 
	 * @param tokens
	 *            of a tabix line
	 * @return the transformated tokens into a {@link TabixFeature} containing
	 */
	public ReMMFeature decode(String[] tokens) {
		int tokenCount = tokens.length;

		// The first 3 columns are non optional for this Tabix

		if (tokenCount < 4) {
			return null;
		}

		String chr = tokens[0];
		int start = Integer.parseInt(tokens[1]);
		Double score = Double.parseDouble(tokens[2]);

		ReMMFeature output = new ReMMFeature(chr, start, score);

		return output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see htsjdk.tribble.AsciiFeatureCodec#readActualHeader(htsjdk.tribble.readers.LineIterator)
	 */
	@Override
	public Object readActualHeader(LineIterator reader) {
		return null;
	}

}
