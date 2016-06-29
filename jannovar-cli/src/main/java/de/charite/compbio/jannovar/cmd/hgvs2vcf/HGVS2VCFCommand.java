package de.charite.compbio.jannovar.cmd.hgvs2vcf;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;

import de.charite.compbio.jannovar.JannovarException;
import de.charite.compbio.jannovar.JannovarOptions;
import de.charite.compbio.jannovar.annotation.AllAnnotationListTextGenerator;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.AnnotationException;
import de.charite.compbio.jannovar.annotation.BestAnnotationListTextGenerator;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantAnnotationsTextGenerator;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.cmd.CommandLineParsingException;
import de.charite.compbio.jannovar.cmd.HelpRequestedException;
import de.charite.compbio.jannovar.cmd.JannovarAnnotationCommand;
import de.charite.compbio.jannovar.cmd.annotate_vcf.AnnotatedVCFWriter;
import de.charite.compbio.jannovar.htsjdk.InfoFields;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import de.charite.compbio.jannovar.htsjdk.VariantContextWriterConstructionHelper;
import de.charite.compbio.jannovar.reference.TranscriptModel;

/**
 * Allows the annotation of a single position.
 * 
 * @author <a href="mailto:na.zhu@charite.de">Na Zhu</a>
 */
public class HGVS2VCFCommand extends JannovarAnnotationCommand {

	java.util.List<HGVS> regexlist = new java.util.ArrayList<HGVS>();
//	VariantAnnotator annotator = null;
	public HGVS2VCFCommand(String argv[]) throws CommandLineParsingException,
			HelpRequestedException {
		super(argv);
	}

	/**
	 * This function will parse the hgvs code to vcf format For example, the
	 * change NM_032129.2:c.1460G could be converted to <tt>chr1</tt>
	 * <tt>909238</tt>.<tt>T</tt><tt>C...</tt>.
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
//        annotator = new VariantAnnotator(refDict, chromosomeMap, new AnnotationBuilderOptions());
    	java.util.List<HGVS_Parser> variants = new java.util.ArrayList<HGVS_Parser>(0);
        /** the column of HGVS IDs in the HGVS file**/
        int column=options.column;
		
        System.err.println("Deserializing transcripts...");
		if(data==null){
		  deserializeTranscriptDefinitionFile();
		}	
	
		/** write the vcf header **/
		try {
			FileInputStream fstream=null;
		
			fstream = new FileInputStream(hgvs_file);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;

			while ((strLine = br.readLine()) != null)  {
				String[] strs=strLine.split("\t");
				String code;
				
				/**check the validation of the file, it should include at least ${column} fields  **/
				if(strs.length<column){
					System.err.println(hgvs_file+" does not contain "+column+" fields.");
					br.close();
					
				}
				/** read the HGVS ID**/
				code=strs[column -1];
				/** convert string to HGVS code **/
				HGVS hgvs=new HGVS(code);
				if(!hgvs.isCorrect()){ continue; }
				/** parse a HGVS ID to gensortedVCWriteromic postion**/
				HGVS_Parser parser=new HGVS_Parser(data);
				
				/** only output the positions which are parsed successfully **/
				if(parser.parse(hgvs)){
					/*method for writing*/
					variants.add(parser);
				}else{
					/**The HGVS ID will be searched by regex string */
						regexlist.add(hgvs);
				}
				System.out.println(code);
			}
			
			br.close();
			System.out.println(variants.size());
			/** regex search on the failed HGVS by traversing the entire database */
			if(regexlist.size()>0){
				HGVS_regex(regexlist,data.getTmByAccession(),variants);
			}
			write2VCF(variants,vcf_file);
		} catch (IOException e1) {
			System.out.println("Error "+variants.size());
			e1.printStackTrace();
		}	
		
	}

	private void write2VCF(java.util.List<HGVS_Parser> variants,String vcf_file) throws AnnotationException {
		VariantContextWriter vcWriter = new VariantContextWriterBuilder().setOutputFile(vcf_file).unsetOption(Options.INDEX_ON_THE_FLY).build();;
		VCFHeader header = new VCFHeader();
		header.addMetaDataLine(
				 new VCFInfoHeaderLine("HGVS", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "HGVS code"));
		InfoFields fields = InfoFields.build(options.writeVCFAnnotationStandardInfoFields,
				options.writeJannovarInfoFields);
		VariantContextWriterConstructionHelper.extendHeaderFields(header, fields);
		
		header.addMetaDataLine(new VCFHeaderLine("jannovarVersion",
				JannovarOptions.JANNOVAR_VERSION));
		header.addMetaDataLine(new VCFHeaderLine("jannovarCommand", Joiner.on(' ').join(args)));
		vcWriter.writeHeader(header);
		VariantAnnotator annotator = new VariantAnnotator(refDict, chromosomeMap, new AnnotationBuilderOptions());
		
		for(HGVS_Parser hgvs_parser:variants ){
			List<Allele> alleles = new ArrayList<>();	
			alleles.add(Allele.create(hgvs_parser.ref.equals("")?"N":hgvs_parser.ref, true));
			alleles.add(Allele.create(hgvs_parser.alt.equals("")?"N":hgvs_parser.alt, false));
			System.out.println(hgvs_parser.hgvs.getName());
				VariantAnnotations anno=annotator.buildAnnotations(hgvs_parser.variant);
			if (anno == null) {
				throw new AnnotationException();
			}
		
			String annostr="";
			VariantAnnotationsTextGenerator textGenerator;
			
			for (Annotation a : anno.getAnnotations()) {
			
				String effect = a.getEffects().first().getLegacyTerm();
				String annt = Joiner
						.on(":")
						.skipNulls()
						.join(a.getCDSNTChange().toHGVSString(),
								(a.getProteinChange() == null) ? null : ("p." + a.getProteinChange().toHGVSString()));
				String sym = a.getTranscript().getGeneSymbol();
				annostr = String.format("%s|%s|%s", effect, sym, annt);
				
			}
			System.out.println(hgvs_parser.hgvs.getName());
			VariantContext vc = new VariantContextBuilder().chr(hgvs_parser.variant.getChrName()).
					start(hgvs_parser.variant.getPos()+1).
					alleles(alleles).
					computeEndFromAlleles(alleles,hgvs_parser.variant.getPos()+1).
					attribute("ANN", annostr).
					attribute("HGVS",hgvs_parser.hgvs.getName()).make();
		
//			
			vcWriter.add(vc);
		}
		vcWriter.close();
	
		
//		SortingVariantContextWriter sortedVCWriter = null;		
//		sortedVCWriter = new SortingVariantContextWriter(vcWriter, 10000000, true);
//		sortedVCWriter.writeHeader(header);		
	}

	@Override
	protected JannovarOptions parseCommandLine(String[] argv)
			throws CommandLineParsingException, HelpRequestedException {
		HGVS2VCFCommandLineParser parser = new HGVS2VCFCommandLineParser();
		try {
			return parser.parse(argv);
		} catch (ParseException e) {
			parser.printHelp();
			throw new CommandLineParsingException(
					"Problem with command line parsing.", e);
		}
		
	}

	/**
	 * Check the failed HGVS IDs by regex search in the entire database in case
	 * the failure due to difference version
	 * **/
	private void HGVS_regex(List<HGVS> regexlist2,
			ImmutableMap<String, TranscriptModel> immutableMap, java.util.List<HGVS_Parser> variants) {
		ImmutableSet<String> keys = immutableMap.keySet();
		UnmodifiableIterator<String> refseq_it = keys.iterator();
		int len = 0;
		while (refseq_it.hasNext()) {
			java.util.Iterator<HGVS> it = regexlist.iterator();
			String key_ref = null;
			key_ref = refseq_it.next();
			while (it.hasNext()) {
				HGVS fail_hgvs = it.next();
				if (key_ref != null
						&& (key_ref.startsWith(fail_hgvs.getTranscriptCode()))) {
					fail_hgvs.setTranscript(key_ref);
					len++;
					HGVS_Parser parser = new HGVS_Parser(data);
					parser.parse(fail_hgvs);
					variants.add(parser);
					if (len == regexlist.size()) {
						return;
					}
				}
			}
		}

	}

}

// }

