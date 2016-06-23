package de.charite.compbio.jannovar.cmd.hgvs_to_genomic;

import org.apache.commons.cli.ParseException;

import de.charite.compbio.jannovar.JannovarException;
import de.charite.compbio.jannovar.JannovarOptions;
import de.charite.compbio.jannovar.annotation.AllAnnotationListTextGenerator;
import de.charite.compbio.jannovar.annotation.AnnotationException;
import de.charite.compbio.jannovar.annotation.BestAnnotationListTextGenerator;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantAnnotationsTextGenerator;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.cmd.CommandLineParsingException;
import de.charite.compbio.jannovar.cmd.HelpRequestedException;
import de.charite.compbio.jannovar.cmd.JannovarAnnotationCommand;
import de.charite.compbio.jannovar.hgvs.parser.HGVSParser;
import de.charite.compbio.jannovar.reference.CDSPosition;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import de.charite.compbio.jannovar.reference.TranscriptProjectionDecorator;

/**
 * Allows the annotation of a single position.
 *
 * @author <a href="mailto:marten.jaeger@charite.de">Marten Jaeger</a>
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @author <a href="mailto:max.schubach@charite.de">Max Schubach</a>
 */
public class HGVSToGenomicPositionCommand extends JannovarAnnotationCommand {

	private static HGVSParser hgvsParser = new HGVSParser();

	public HGVSToGenomicPositionCommand(String argv[]) throws CommandLineParsingException, HelpRequestedException {
		super(argv);
	}

	/**
	 * This function will simply annotate given chromosomal position with HGVS compliant output.
	 *
	 * For example, the change <tt>chr1:909238G&gt;C</tt> could be converted to
	 * <tt>PLEKHN1:NM_032129.2:c.1460G&gt;C,p.(Arg487Pro)</tt>.
	 *
	 * @throws AnnotationException
	 *             on problems in the annotation process
	 */
	@Override
	public void run() throws JannovarException {
		System.err.println("Options");
		options.print(System.err);

		System.err.println("Deserializing transcripts...");
		deserializeTranscriptDefinitionFile();
		final VariantAnnotator annotator = new VariantAnnotator(refDict, chromosomeMap, new AnnotationBuilderOptions());
		System.out.println("#change\teffect\thgvs_annotation");
		for (String hgvsChange : options.chromosomalChanges) {
			// Parse the chromosomal change string into a GenomeChange object.
			final CDSPosition cdsPosition = parseHGVSChange(hgvsChange);

			GenomePosition genomePosition = new TranscriptProjectionDecorator(cdsPosition.getTranscript())
					.cdsToGenomePos(cdsPosition);
			GenomeVariant genomeChange = new GenomeVariant(genomePosition, "A", "C");

			// Construct VariantAnnotator for building the variant annotations.
			VariantAnnotations annoList = null;
			try {
				annoList = annotator.buildAnnotations(genomeChange);
			} catch (Exception e) {
				System.err.println(String.format("[ERROR] Could not annotate variant %s!", hgvsChange));
				e.printStackTrace(System.err);
				continue;
			}

			// Obtain first or all functional annotation(s) and effect(s).
			final String annotation;
			final String effect;
			VariantAnnotationsTextGenerator textGenerator;
			if (options.showAll)
				textGenerator = new AllAnnotationListTextGenerator(annoList, 0, 1);
			else
				textGenerator = new BestAnnotationListTextGenerator(annoList, 0, 1);
			annotation = textGenerator.buildHGVSText();
			effect = textGenerator.buildEffectText();

			System.out.println(String.format("%s\t%s\t%s", genomeChange.toString(), effect, annotation));
		}
	}

	private CDSPosition parseHGVSChange(String changeStr) throws JannovarException {
		
		TranscriptModel tm = this.data.getTmByAccession().get("NM_004004.5");
		return new CDSPosition(tm, 211, PositionType.ONE_BASED);
		
//		HGVSVariant variant = hgvsParser.parseHGVSString(changeStr);
//		
//		if (variant instanceof SingleAlleleNucleotideVariant) {
//			SingleAlleleNucleotideVariant nucleotideVariant = (SingleAlleleNucleotideVariant) variant;
//			
//			return new TranscriptPosition(tm, nucleotideVariant.getSeqType().getgetChange()., PositionType.ONE_BASED);
//			
//		}
//			
//		return null;
	}

	@Override
	protected JannovarOptions parseCommandLine(String[] argv)
			throws CommandLineParsingException, HelpRequestedException {
		HGVSToGenomicPositionCommandLineParser parser = new HGVSToGenomicPositionCommandLineParser();
		try {
			return parser.parse(argv);
		} catch (ParseException e) {
			throw new CommandLineParsingException("Problem with command line parsing.", e);
		}
	}

}
