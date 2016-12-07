package de.charite.compbio.jannovar.vardbs.tabix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Joiner;

import de.charite.compbio.jannovar.vardbs.base.AbstractTabixDBAnnotationDriver;
import de.charite.compbio.jannovar.vardbs.base.AnnotatingRecord;
import de.charite.compbio.jannovar.vardbs.base.DBAnnotationOptions;
import de.charite.compbio.jannovar.vardbs.base.GenotypeMatch;
import de.charite.compbio.jannovar.vardbs.base.JannovarVarDBException;
import de.charite.compbio.jannovar.vardbs.base.VCFHeaderExtender;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;

/**
 * Annotation driver class for annotations using CADD data
 *
 * @author <a href="mailto:max.schubach@charite.de">Max Schubach</a>
 */
public class TabixAnnotationDriver extends AbstractTabixDBAnnotationDriver<TabixRecord> {

	public TabixAnnotationDriver(String tabixPath, String fastaPath, DBAnnotationOptions options)
			throws JannovarVarDBException {
		super(tabixPath, fastaPath, options, new TabixEntryToRecordConverter());
	}

	@Override
	protected HashMap<Integer, AnnotatingRecord<TabixRecord>> pickAnnotatingDBRecords(
			HashMap<Integer, ArrayList<GenotypeMatch>> annotatingRecords,
			HashMap<GenotypeMatch, AnnotatingRecord<TabixRecord>> matchToRecord) {
		// Pick best annotation for each alternative allele
		HashMap<Integer, AnnotatingRecord<TabixRecord>> annotatingRecord = new HashMap<>();
		for (Entry<Integer, ArrayList<GenotypeMatch>> entry : annotatingRecords.entrySet()) {
			final int alleleNo = entry.getKey();
			for (GenotypeMatch m : entry.getValue()) {
				if (!annotatingRecord.containsKey(alleleNo)) {
					annotatingRecord.put(alleleNo, matchToRecord.get(m));
				} else {
					final TabixRecord current = annotatingRecord.get(alleleNo).getRecord();
					final TabixRecord update = matchToRecord.get(m).getRecord();
					Map<String, List<String>> fields = new HashMap<>();
					Set<String> tokens = new HashSet<>();
					tokens.addAll(current.getFields().keySet());
					tokens.addAll(update.getFields().keySet());
					for (String token : tokens) {
						List<String> list = new ArrayList<>();
						if (current.getFields().containsKey(token))
							list.addAll(current.getFields().get(token));
						if (current.getFields().containsKey(token))
							list.addAll(current.getFields().get(token));
						fields.put(token, list);

					}
					final TabixRecord record = new TabixRecord(current.getChrom(), current.getPos(), current.getRef(),
							current.getAlt(), current.getFilter(), fields);
					annotatingRecord.put(alleleNo, new AnnotatingRecord<>(record, matchToRecord.get(m).getAlleleNo()));
				}
			}
		}
		return annotatingRecord;
	}

	@Override
	public VCFHeaderExtender constructVCFHeaderExtender() {
		return new TabixVCFHeaderExtender(options, fields);
	}

	@Override
	protected VariantContext annotateWithDBRecords(VariantContext vc,
			HashMap<Integer, AnnotatingRecord<TabixRecord>> matchRecords,
			HashMap<Integer, AnnotatingRecord<TabixRecord>> overlapRecords) {
		VariantContextBuilder builder = new VariantContextBuilder(vc);

		// Annotate with records with matching allele
		annotateFields(vc, "", matchRecords, builder);

		// Annotate with records with overlapping positions
		if (options.isReportOverlapping() && !options.isReportOverlappingAsMatching()) {
			annotateFields(vc, "OVL_", overlapRecords, builder);
		}

		return builder.make();
	}

	private void annotateFields(VariantContext vc, String infix,
			HashMap<Integer, AnnotatingRecord<TabixRecord>> records, VariantContextBuilder builder) {
		for (String field : getFields(records)) {
			ArrayList<String> fieldList = new ArrayList<>();
			for (int i = 1; i < vc.getNAlleles(); ++i) {
				if (records.get(i) == null) {
					fieldList.add(".");
					continue;
				}
				final TabixRecord record = records.get(i).getRecord();
				if (record.getFields().get(field).isEmpty()) {
					fieldList.add(".");
				} else {
					fieldList.add(Joiner.on("|").join(record.getFields().get(field)));
				}
			}

			if (fieldList.stream().allMatch(i -> (i.equals("."))))
				return; // do not set list of dots

			final String attrID = options.getVCFIdentifierPrefix() + infix + field;
			if (!fieldList.isEmpty())
				builder.attribute(attrID, fieldList);
		}
	}

	private Set<String> getFields(HashMap<Integer, AnnotatingRecord<TabixRecord>> records) {
		Set<String> output = new LinkedHashSet<>();
		for (AnnotatingRecord<TabixRecord> record : records.values()) {
			if (record == null)
				continue;
			for (String field : record.getRecord().getFields().keySet()) {
				if (!output.contains(field))
					output.add(field);
			}
		}
		return output;
	}
}
