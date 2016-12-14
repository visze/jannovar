package de.charite.compbio.jannovar.vardbs.g1k;

import de.charite.compbio.jannovar.vardbs.base.DBAnnotationOptions;
import de.charite.compbio.jannovar.vardbs.base.VCFHeaderExtender;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

/**
 * Helper class for extending {@link VCFHeader}s for 1KG annotations.
 * 
 * @author <a href="mailto:max.schubach@charite.de">Max Schubach</a>
 */
public class G1KVCFHeaderExtender extends VCFHeaderExtender {

	public G1KVCFHeaderExtender(DBAnnotationOptions options) {
		super(options);
	}

	@Override
	public String getDefaultPrefix() {
		return "1KG_";
	}

	@Override
	public void addHeaders(VCFHeader header, String prefix) {
		// Add headers for exactly matching variants
		for (G1KPopulation pop : G1KPopulation.values()) {
			addAFHeader(header, prefix, "", "", pop);
		}
		addACHeader(header, prefix, "", "");
		addANHeader(header, prefix, "", "");
		addBestAFHeader(header, prefix, "", "");
		addBestACHeader(header, prefix, "", "");

		if (options.isReportOverlapping() && !options.isReportOverlappingAsMatching()) {
			// Add headers for overlapping variants
			final String note = " (requiring no genotype match, only position overlap)";
			for (G1KPopulation pop : G1KPopulation.values()) {
				addAFHeader(header, prefix, "OVL_", note, pop);
			}
			addACHeader(header, prefix, "OVL_", note);
			addANHeader(header, prefix, "OVL_", note);
			addBestAFHeader(header, prefix, "OVL_", note);
			addBestACHeader(header, prefix, "OVL_", note);
		}
	}

	/** Add header for highest allele frequency */
	private void addBestAFHeader(VCFHeader header, String prefix, String idInfix, String noteInfix) {
		VCFInfoHeaderLine line = new VCFInfoHeaderLine(prefix + idInfix + "BEST_AF", VCFHeaderLineCount.A,
				VCFHeaderLineType.Float, "Highest allele frequency seen in any population" + noteInfix);
		header.addMetaDataLine(line);
	}

	/** Add header for allele count with highest frequency */
	private void addBestACHeader(VCFHeader header, String prefix, String idInfix, String noteInfix) {
		VCFInfoHeaderLine line = new VCFInfoHeaderLine(prefix + idInfix + "BEST_AC", VCFHeaderLineCount.A,
				VCFHeaderLineType.Integer, "Allele count for population with highest frequency" + noteInfix);
		header.addMetaDataLine(line);
	}

	/** Add header with allele frequency */
	private void addAFHeader(VCFHeader header, String prefix, String idInfix, String noteInfix, G1KPopulation pop) {
		String popName;
		if (pop != G1KPopulation.ALL)
			popName = "all populations";
		else
			popName = pop + " / " + pop.getLabel() + " population";
		VCFInfoHeaderLine line = new VCFInfoHeaderLine(prefix + idInfix + "AF_" + pop, VCFHeaderLineCount.A,
				VCFHeaderLineType.Float, "Frequency observed in ExAC data set in " + popName + noteInfix);
		header.addMetaDataLine(line);
	}

	/** Add header with chromosome count */
	private void addANHeader(VCFHeader header, String prefix, String idInfix, String noteInfix) {
		// TODO: change counts to 1 for AN?
		String popName = "Total number of alleles in called genotypes";
		VCFInfoHeaderLine line = new VCFInfoHeaderLine(prefix + idInfix + "AN", 1, VCFHeaderLineType.Integer,
				"Overall number of positions/chromosomes with coverage in " + popName + noteInfix);
		header.addMetaDataLine(line);
	}

	/** Add header with allele counts */
	private void addACHeader(VCFHeader header, String prefix, String idInfix, String noteInfix) {
		String popName = "Total number of alternate alleles in called genotypes";
		VCFInfoHeaderLine line = new VCFInfoHeaderLine(prefix + idInfix + "AC", VCFHeaderLineCount.A,
				VCFHeaderLineType.Integer, "Overall number of observed alleles in " + popName + noteInfix);
		header.addMetaDataLine(line);
	}

}
