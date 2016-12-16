package de.charite.compbio.jannovar.vardbs.tabix;

import java.util.List;

import de.charite.compbio.jannovar.vardbs.base.DBAnnotationOptions;
import de.charite.compbio.jannovar.vardbs.base.VCFHeaderExtender;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

/**
 * Helper class for extending {@link VCFHeader}s for CADD annotations.
 * 
 * @author <a href="mailto:max.schubach@charite.de">Max Schubach</a>
 */
public class TabixVCFHeaderExtender extends VCFHeaderExtender {

	private final List<String> headers;

	public TabixVCFHeaderExtender(DBAnnotationOptions options, List<String> headers) {
		super(options);
		this.headers = headers;
	}

	@Override
	public String getDefaultPrefix() {
		return "tabix_";
	}

	@Override
	public void addHeaders(VCFHeader header, String prefix) {
		addHeadersInfixes(header, prefix, "", "");
		if (options.isReportOverlapping() && !options.isReportOverlappingAsMatching())
			addHeadersInfixes(header, prefix, "OVL_", " (requiring no genotype match, only position overlap)");
	}

	public void addHeadersInfixes(VCFHeader header, String prefix, String infix, String note) {
		for (String token : headers) {
			VCFInfoHeaderLine line = new VCFInfoHeaderLine(prefix + infix + token, 1, VCFHeaderLineType.String,
					"Annotation with TABIX token " + token + note);
			header.addMetaDataLine(line);
		}
	}

}
