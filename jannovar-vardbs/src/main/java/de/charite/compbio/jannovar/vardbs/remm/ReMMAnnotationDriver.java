package de.charite.compbio.jannovar.vardbs.remm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.charite.compbio.jannovar.vardbs.base.DBAnnotationDriver;
import de.charite.compbio.jannovar.vardbs.base.DBAnnotationOptions;
import de.charite.compbio.jannovar.vardbs.base.JannovarVarDBException;
import de.charite.compbio.jannovar.vardbs.base.VCFHeaderExtender;
import htsjdk.tribble.CloseableTribbleIterator;
import htsjdk.tribble.FeatureReader;
import htsjdk.tribble.TabixFeatureReader;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;

/**
 * Annotation driver class for annotations using ReMM data
 *
 * @author <a href="mailto:max.schubach@charite.de">Max Schubach</a>
 */
public class ReMMAnnotationDriver implements DBAnnotationDriver {

	/** Path to ReMM VCF file */
	private final String remmPath;
	/** Configuration */
	private final DBAnnotationOptions options;
	/** VCFReader to use for loading the VCF records */
	private final FeatureReader<ReMMFeature> remmReader;

	/**
	 * @param remmDBPath
	 * @param options
	 * @throws JannovarVarDBException
	 */
	public ReMMAnnotationDriver(String remmDBPath, DBAnnotationOptions options) throws JannovarVarDBException {
		this.options = options;
		this.remmPath = remmDBPath;
		try {
			this.remmReader = new TabixFeatureReader<>(remmPath, new ReMMCodec());
		} catch (IOException e) {
			throw new JannovarVarDBException("Cannot read ReMM tabix file", e);
		}
	}

	@Override
	public VCFHeaderExtender constructVCFHeaderExtender() {
		return new ReMMVCFHeaderExtender(options);
	}

	@Override
	public VariantContext annotateVariantContext(VariantContext vc) {
		int refLength = vc.getReference().length();
		List<Double> lst = new ArrayList<>();
		for (int i = 1; i < vc.getAlleles().size(); i++) {
			int altLength = vc.getAlleles().get(i).length();
			Optional<Double> max;
			if (refLength < altLength) {
				// be careful with AT to ATT
				try (CloseableTribbleIterator<ReMMFeature> iter = remmReader.query(vc.getContig(),
						vc.getStart() + refLength - 1, vc.getEnd() + 1)) {
					max = iter.stream().map(f -> f.getScore()).max(Double::compare);

				} catch (IOException e) {
					throw new RuntimeException("Cannot query ReMM position" + vc.toString(), e);
				}
			} else if (refLength > altLength) {
				try (CloseableTribbleIterator<ReMMFeature> iter = remmReader.query(vc.getContig(),
						vc.getStart() + altLength, vc.getEnd())) {
					max = iter.stream().map(f -> f.getScore()).max(Double::compare);

				} catch (IOException e) {
					throw new RuntimeException("Cannot query ReMM position" + vc.toString(), e);
				}
			} else {
				try (CloseableTribbleIterator<ReMMFeature> iter = remmReader.query(vc.getContig(), vc.getStart(),
						vc.getEnd())) {
					max = iter.stream().map(f -> f.getScore()).max(Double::compare);

				} catch (IOException e) {
					throw new RuntimeException("Cannot query ReMM position" + vc.toString(), e);
				}
			}
			if (max.isPresent())
				lst.add(max.get());
			else
				lst.add(0.0);
		}
		
		VariantContextBuilder builder = new VariantContextBuilder(vc);
		builder.attribute(options.getVCFIdentifierPrefix(), lst);

		return builder.make();

	}

}
