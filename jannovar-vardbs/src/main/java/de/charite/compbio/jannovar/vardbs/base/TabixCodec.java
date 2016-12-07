/**
 * 
 */
package de.charite.compbio.jannovar.vardbs.base;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import htsjdk.tribble.AsciiFeatureCodec;
import htsjdk.tribble.readers.LineIterator;

/**
 * Codec for parsing a tabix CADD file, as described by Kircher et.al.
 * 
 * @author Max Schubach
 *
 */
public class TabixCodec extends AsciiFeatureCodec<TabixFeature> {
	public static final Pattern SPLIT_PATTERN = Pattern.compile("\\t|( +)");
	private final List<String> header;

	/**
	 * @param myClass
	 */
	public TabixCodec(List<String> header) {
		super(TabixFeature.class);
		this.header = header;
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
	public TabixFeature decode(String line) {
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
	public TabixFeature decode(String[] tokens) {
		int tokenCount = tokens.length;

		// The first 3 columns are non optional for this Tabix

		if (tokenCount < 4) {
			return null;
		}

		String chr = tokens[0];
		int start = Integer.parseInt(tokens[1]);
		String ref = tokens[2];
		String alt = tokens[3];
		
		Map<String,String> values = new LinkedHashMap<>();
		
		for (int i = 4; i < tokens.length; i++) {
			values.put(header.get(i-4), tokens[i]);
		}

		TabixFeature output = new TabixFeature(chr, start, ref, alt, values);

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
