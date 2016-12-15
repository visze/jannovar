package de.charite.compbio.jannovar.vardbs.remm;

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
public class ReMMVCFHeaderExtender extends VCFHeaderExtender {

	public ReMMVCFHeaderExtender(DBAnnotationOptions options) {
		super(options);
	}

	@Override
	public String getDefaultPrefix() {
		return "REMM";
	}

	@Override
	public void addHeaders(VCFHeader header, String prefix) {
		VCFInfoHeaderLine line = new VCFInfoHeaderLine(prefix, 1, VCFHeaderLineType.Float, "ReMM Score of position");
		header.addMetaDataLine(line);
	}

}
