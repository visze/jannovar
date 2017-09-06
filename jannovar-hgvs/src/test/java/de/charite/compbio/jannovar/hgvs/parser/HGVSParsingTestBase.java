package de.charite.compbio.jannovar.hgvs.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

import de.charite.compbio.jannovar.hgvs.parser.Antlr4HGVSParser.Hgvs_variantContext;

/**
 * Base class for parsing tests.
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public class HGVSParsingTestBase {

	public HGVSParsingTestBase() {
		super();
	}

	public static void setLogLevel(Level level) {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration conf = ctx.getConfiguration();
		conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(level);
	}

	protected Hgvs_variantContext parseString(String inputString) {
		return parseString(inputString, false);
	}

	protected Hgvs_variantContext parseString(String inputString, boolean trace) {
		if (trace) {
			CharStream inputStream = CharStreams.fromString(inputString);
			Antlr4HGVSLexer l = new Antlr4HGVSLexer(inputStream);
			System.err.println(l.getAllTokens());
		}
		CharStream inputStream = CharStreams.fromString(inputString);
		HGVSLexer l = new HGVSLexer(inputStream);
		Antlr4HGVSParser p = new Antlr4HGVSParser(new CommonTokenStream(l));
		p.setTrace(trace);
		p.addErrorListener(new BaseErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
			}
		});
		try {
			return p.hgvs_variant();
		} catch (IllegalStateException e) {
			throw new IllegalStateException("Could not parse \"" + inputString + "\"", e);
		}
	}

}