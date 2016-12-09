package de.charite.compbio.jannovar.vardbs.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.tribble.FeatureReader;
import htsjdk.tribble.TabixFeatureReader;
import htsjdk.variant.variantcontext.VariantContext;

/**
 * Abstract base class for annotation based on VCF files.
 * 
 * @author <a href="mailto:max.schubach@charite.de">Max Schubach</a>
 */
public abstract class AbstractTabixDBAnnotationDriver<RecordType> implements DBAnnotationDriver {

	/** Path to dbSNP VCF file */
	protected final String tabixPath;
	/** Helper objects for matching alleles */
	protected final AlleleMatcher matcher;
	/** header fields contained in the tabix file */
	protected final List<String> fields;
	/** Helper for converting from VariantContex to DBSNP record */
	protected final VariantContextToRecordConverter<RecordType> vcToRecord;
	/** Configuration */
	protected final DBAnnotationOptions options;
	/** VCFReader to use for loading the VCF records */
	protected final FeatureReader<TabixFeature> tabixReader;

	/**
	 * Create annotation driver for a coordinate-sorted, bgzip-compressed, VCF file
	 * 
	 * @param tabixPath
	 *            Path to VCF file with dbSNP.
	 * @param fastaPath
	 *            FAI-indexed FASTA file with reference
	 * @param options
	 *            configuration
	 * @param vcToRecord
	 *            converter from {@link VariantContext} to record type
	 * @throws JannovarVarDBException
	 *             on problems loading the reference FASTA/FAI file or incompatible dbSNP version
	 */
	public AbstractTabixDBAnnotationDriver(String tabixPath, String fastaPath, DBAnnotationOptions options,
			VariantContextToRecordConverter<RecordType> vcToRecord) throws JannovarVarDBException {
		this.tabixPath = tabixPath;
		this.matcher = new AlleleMatcher(fastaPath);
		this.vcToRecord = vcToRecord;
		this.fields = getFieldsFromHeader();
		try {
			this.tabixReader = new TabixFeatureReader<>(tabixPath, new TabixCodec(this.fields));
		} catch (IOException e) {
			throw new JannovarVarDBException("Cannot read tabix file", e);
		}
		this.options = options;

	}

	private List<String> getFieldsFromHeader() throws JannovarVarDBException {
		try (FileInputStream in = new FileInputStream(new File(tabixPath));
				BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(in)))) {
			List<String> header = new ArrayList<>();
			String line;
			while ((line = r.readLine()) != null) {
				if (line.matches("^#.*")) {
					if (line.matches("^#[^#]+")) {
						String[] tokens = TabixCodec.SPLIT_PATTERN.split(line, -1);
						if (tokens.length < 4)
							continue;
						for (int i = 4; i < tokens.length; i++) {
							header.add(tokens[i]);
						}
						break;
					} else // skip header
						continue;
				}
				String[] tokens = TabixCodec.SPLIT_PATTERN.split(line, -1);
				if (tokens.length < 4)
					throw new JannovarVarDBException("Not more than 4 tokens in tabix file");
				for (int i = 4; i < tokens.length; i++) {
					header.add("token" + (i - 3));
				}
				break;

			}
			return header;
		} catch (IOException e) {
			throw new JannovarVarDBException("Cannot find tabix file", e);
		}
	}

	@Override
	public VariantContext annotateVariantContext(VariantContext obsVC) {
		try (CloseableIterator<TabixFeature> iter = tabixReader.query(obsVC.getContig(), obsVC.getStart(),
				obsVC.getEnd())) {
			// Fetch all overlapping and matching genotypes from database and pair them with the correct allele from vc.
			List<FeatureMatch> genotypeMatches = new ArrayList<>();
			List<FeatureMatch> positionOverlaps = new ArrayList<>();
			while (iter.hasNext()) {
				final TabixFeature dbFeature = iter.next();
				genotypeMatches.addAll(matcher.matchGenotypes(obsVC, dbFeature));
				if (options.isReportOverlapping() || options.isReportOverlappingAsMatching())
					positionOverlaps.addAll(matcher.positionOverlaps(obsVC, dbFeature));
			}

			// Merge records
			HashMap<Integer, AnnotatingRecord<RecordType>> dbRecordsMatch = buildAnnotatingDBRecordsWrapper(
					genotypeMatches);
			HashMap<Integer, AnnotatingRecord<RecordType>> dbRecordsOverlap = buildAnnotatingDBRecordsWrapper(
					positionOverlaps);
			HashMap<Integer, AnnotatingRecord<RecordType>> emptyMap = new HashMap<Integer, AnnotatingRecord<RecordType>>();

			// Use these records to annotate the variant call in obsVC (record-wise but also per alternative allele)
			if (options.isReportOverlappingAsMatching())
				return annotateWithDBRecords(obsVC, dbRecordsOverlap, emptyMap);
			else if (options.isReportOverlapping())
				return annotateWithDBRecords(obsVC, dbRecordsMatch, dbRecordsOverlap);
			else
				return annotateWithDBRecords(obsVC, dbRecordsMatch, emptyMap);
		} catch (IOException e) {
			throw new RuntimeException("Cannot query tabix", e);
		}
	}

	/**
	 * Build mapping from alternative allele number to db VCF record to use
	 * 
	 * For SNVs, there should only be one value in the value set at which all alleles point to for most cases. The
	 * variant with the lowermost allele number will be chosen for annotating the reference allele.
	 * 
	 * @param genotypeMatches
	 *            List of {@link GenotypeMatch} objects to build the annotating database records from
	 * @return Resulting map from alternative allele ID (starting with 1) to the database record to use
	 */
	private HashMap<Integer, AnnotatingRecord<RecordType>> buildAnnotatingDBRecordsWrapper(
			List<GenotypeMatch> genotypeMatches) {
		// Collect annotating variants for each allele
		HashMap<Integer, ArrayList<GenotypeMatch>> annotatingRecords = new HashMap<>();
		HashMap<GenotypeMatch, AnnotatingRecord<RecordType>> matchToRecord = new HashMap<>();
		for (GenotypeMatch match : genotypeMatches) {
			final int alleleNo = match.getObservedAllele();
			annotatingRecords.putIfAbsent(alleleNo, new ArrayList<GenotypeMatch>());
			annotatingRecords.get(alleleNo).add(match);
			if (!matchToRecord.containsKey(match))
				matchToRecord.put(match,
						new AnnotatingRecord<RecordType>(vcToRecord.convert(match.getDBVC()), match.getDbAllele()));
		}
		
		return pickAnnotatingDBRecords(annotatingRecords, matchToRecord);
	}

	/**
	 * Pick annotating DB records
	 * 
	 * @param annotatingRecords
	 *            Map of alternative allele number to genotype match
	 * @param matchToRecord
	 *            Mapping from alternative allel number to record
	 * @return Mapping from alternative allele number to <code>RecordType</code>
	 */
	protected abstract HashMap<Integer, AnnotatingRecord<RecordType>> pickAnnotatingDBRecords(
			HashMap<Integer, ArrayList<GenotypeMatch>> annotatingRecords,
			HashMap<GenotypeMatch, AnnotatingRecord<RecordType>> matchToRecord);

	/**
	 * Annotate the given {@link VariantContext} with the given database records
	 * 
	 * There can be more than one database record, for example in the case that a SNV is squished together with an
	 * indel.
	 * 
	 * @param vc
	 *            The {@link VariantContext} to annotate
	 * @param dbRecordMatches
	 *            Map from alternative allele index to annotating <code>RecordType</code> with matching allele
	 * @param dbRecordOverlaps
	 *            Map from alternative allele index to annotating <code>RecordType</code> with overlapping positions
	 */
	protected abstract VariantContext annotateWithDBRecords(VariantContext vc,
			HashMap<Integer, AnnotatingRecord<RecordType>> dbRecordMatches,
			HashMap<Integer, AnnotatingRecord<RecordType>> dbRecordOverlaps);

}