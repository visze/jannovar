package de.charite.compbio.jannovar.cmd.hgvs_to_genomic;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.cli.ParseException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;

import de.charite.compbio.jannovar.JannovarException;
import de.charite.compbio.jannovar.JannovarOptions;
import de.charite.compbio.jannovar.annotation.AnnotationException;
//import de.charite.compbio.jannovar.annotation.VariantAnnotator;
//import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.cmd.CommandLineParsingException;
import de.charite.compbio.jannovar.cmd.HelpRequestedException;
import de.charite.compbio.jannovar.cmd.JannovarAnnotationCommand;

//import de.charite.compbio.jannovar.reference.CDSPosition;

//import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.TranscriptModel;

/**
 * Allows the annotation of a single position.
 *
 * @author <a href="mailto:marten.jaeger@charite.de">Marten Jaeger</a>
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 * @author <a href="mailto:max.schubach@charite.de">Max Schubach</a>
 */
public class HGVSToGenomicPositionCommand extends JannovarAnnotationCommand {

	java.util.List<HGVS> regexlist=new java.util.ArrayList<HGVS>();
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
        String hgvs_file=options.hgvsFile;
        String vcf_file=options.hgvs2vcfFile;
        if(!new java.io.File(hgvs_file).exists()){
			System.err.println("Error: "+hgvs_file+" does not find!");
			return ;
		}
        int column=options.column;
		System.err.println("Deserializing transcripts...");
		if(data==null){
		  deserializeTranscriptDefinitionFile();
		}
//		final VariantAnnotator annotator = new VariantAnnotator(refDict, chromosomeMap, new AnnotationBuilderOptions());
		System.out.println("#change\teffect\thgvs_annotation");
		
		try {
			Utils.writeHeader(vcf_file,false);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		StringBuffer strbuffer=new StringBuffer();
		FileInputStream fstream=null;
		try {
			fstream = new FileInputStream(hgvs_file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		int count=0;
		try {
			while ((strLine = br.readLine()) != null)   {
				String[] strs=strLine.split("\t");
				String code;
				if(strs.length<column){
					System.err.println(hgvs_file+" does not contain "+column+" fields.");
					br.close();
					
				}
				code=strs[column -1];
				HGVS hgvs=new HGVS(code);
				HGVS_Parser parser=new HGVS_Parser(data);
				if(parser.parse(hgvs)){
				if(parser.failedlist.size()>0){
					regexlist.addAll(parser.failedlist);
				}
				strbuffer.append(parser.chr+"\t"+parser.position+"\t"+".\t"+parser.ref+"\t"+parser.alt+"\t100\tPASS\tHGVS="+code+"\n");
				if(count==10000){
					Utils.write(vcf_file, strbuffer.toString(),true);
					strbuffer.setLength(0);
					count=0;
				}
				count++;
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(count>0){
			Utils.write(vcf_file, strbuffer.toString(),true);
			strbuffer.setLength(0);
			count=0;
		}
		if(regexlist.size()>0){
			Utils.write(vcf_file, HGVS_regex(regexlist,data.getTmByAccession()).toString(),true);
		}
		
		
		/**MAX*/
//		for (String hgvsChange : options.chromosomalChanges) {
//			// Parse the chromosomal change string into a GenomeChange object.
//			final CDSPosition cdsPosition = parseHGVSChange(hgvsChange);
//
//			GenomePosition genomePosition = new TranscriptProjectionDecorator(cdsPosition.getTranscript())
//					.cdsToGenomePos(cdsPosition);
//			GenomeVariant genomeChange = new GenomeVariant(genomePosition, "A", "C");
//
//			// Construct VariantAnnotator for building the variant annotations.
//			VariantAnnotations annoList = null;
//			try {
//				annoList = annotator.buildAnnotations(genomeChange);
//			} catch (Exception e) {
//				System.err.println(String.format("[ERROR] Could not annotate variant %s!", hgvsChange));
//				e.printStackTrace(System.err);
//				continue;
//			}
//
//			// Obtain first or all functional annotation(s) and effect(s).
//			final String annotation;
//			final String effect;
//			VariantAnnotationsTextGenerator textGenerator;
//			if (options.showAll)
//				textGenerator = new AllAnnotationListTextGenerator(annoList, 0, 1);
//			else
//				textGenerator = new BestAnnotationListTextGenerator(annoList, 0, 1);
//			annotation = textGenerator.buildHGVSText();
//			effect = textGenerator.buildEffectText();
//
//			System.out.println(String.format("%s\t%s\t%s", genomeChange.toString(), effect, annotation));
//		}
	}

//	private CDSPosition parseHGVSChange(String changeStr) throws JannovarException {
//		
//		TranscriptModel tm = this.data.getTmByAccession().get("NM_004004.5");
//		return new CDSPosition(tm, 211, PositionType.ONE_BASED);
//		
////		HGVSVariant variant = hgvsParser.parseHGVSString(changeStr);
////		
////		if (variant instanceof SingleAlleleNucleotideVariant) {
////			SingleAlleleNucleotideVariant nucleotideVariant = (SingleAlleleNucleotideVariant) variant;
////			
////			return new TranscriptPosition(tm, nucleotideVariant.getSeqType().getgetChange()., PositionType.ONE_BASED);
////			
////		}
////			
////		return null;
//	}

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
	
	
	  private StringBuffer HGVS_regex(List<HGVS> regexlist2, ImmutableMap<String, TranscriptModel> immutableMap) {
		   ImmutableSet<String> keys=immutableMap.keySet();
		   UnmodifiableIterator<String> refseq_it=keys.iterator();
		   StringBuffer strbuff=new StringBuffer();
		   int len=0;
		   while(refseq_it.hasNext()){
			   java.util.Iterator<HGVS> it=regexlist.iterator();
			   String key_ref=null;
			   key_ref=refseq_it.next();
				while(it.hasNext()){
					HGVS fail_hgvs=it.next();
					if(key_ref!=null&&(key_ref.startsWith(fail_hgvs.getTranscriptCode()))){
						fail_hgvs.setTranscript(key_ref);
						len++;
						strbuff.append(new HGVS_Parser(data).parse(fail_hgvs));
						if(len==regexlist.size()){return strbuff;}
					}				
				}
		     }
		   return strbuff;
	       }

}
