package de.charite.compbio.jannovar.cmd.hgvs2vcf;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import de.charite.compbio.jannovar.JannovarOptions;
import de.charite.compbio.jannovar.cmd.HelpRequestedException;
import de.charite.compbio.jannovar.cmd.JannovarAnnotationCommandLineParser;

/**
 * Parse the command line for the "hgvs2vcf" command.
 *
 * @author <a href="mailto:na.zhu@charite.de">Na Zhu</a>
 */
public class HGVS2VCFCommandLineParser extends JannovarAnnotationCommandLineParser {

	@Override
	public JannovarOptions parse(String[] argv) throws ParseException, HelpRequestedException {
		// Parse the command line.
		CommandLine cmd = parser.parse(helpOptions, argv, true);
		printHelpIfOptionIsSet(cmd);
		cmd = parser.parse(options, argv);
        if(cmd.getOptions().length<1){	
        	printHelp();
        	throw new HelpRequestedException();
        }
		printHelpIfOptionIsSet(cmd);
		// Fill the resulting JannovarOptions.
		JannovarOptions result = new JannovarOptions();
		result.printProgressBars = true;
		result.command = JannovarOptions.Command.HGVS_TO_GENOMIC_POSITION;


		if (cmd.hasOption("verbose"))
			result.verbosity = 2;
		if (cmd.hasOption("very-verbose"))
			result.verbosity = 3;
          
		
		result.dataFile = cmd.getOptionValue("database");
		
//		for (String change : cmd.getOptionValues("change")) {
//			result.chromosomalChanges.add(change);
//		}
		
		result.hgvsFile=cmd.getOptionValue("input");
		
		result.hgvs2vcfFile=cmd.getOptionValue("output");
		if(cmd.hasOption("column")){
		   result.column=Integer.valueOf(cmd.getOptionValue("column"));
		}

		return result;
	}

	@Override
	protected void initializeParser() {
		super.initializeParser();
	    options.addOption(Option.builder("i").longOpt("input").required().hasArgs()
				.desc(" the hgvs file").build());
	    options.addOption(Option.builder("o").longOpt("output").required().hasArgs()
				.desc(" the output vcf file").build());
	    options.addOption(Option.builder("d").longOpt("database").required().hasArgs()
				.desc(" the database").build());
	    
		options.addOption(Option.builder("c").longOpt("column").optionalArg(true).hasArgs()
				.desc("the column of hgvs code in the hgvs file").build());
	}

	public void printHelp() {
		final String HEADER = new StringBuilder().append("Jannovar Command: hgvs-to-genomic-pos\n\n")
				.append("Use this command to parse HGVS to genomic position.\n\n")
				.append("Usage: java -jar de.charite.compbio.jannovar.jar hgvs-to-genomic-pos [options] -d <database.ser> -i <HGVS_input> -o <vcf_output> -c <column>\n\n").toString();
		final String FOOTER = new StringBuilder().append(
				"\n\nExample: java -jar de.charite.compbio.jannovar.jar hgvs-to-genomic-pos -d data/hg19_refseq.ser -c 1 -i hgvs.txt  -o hgvs2vcf\n\n").toString();

		System.err.print(HEADER);

		HelpFormatter hf = new HelpFormatter();
		PrintWriter pw = new PrintWriter(System.err, true);
		hf.printOptions(pw, 78, options, 2, 2);

		System.err.print(FOOTER);
	}
	protected void printHelpIfOptionIsSet(CommandLine cmd) throws HelpRequestedException {
		if (cmd.hasOption("help")) {
			printHelp();
			throw new HelpRequestedException();
		}
	}

}
