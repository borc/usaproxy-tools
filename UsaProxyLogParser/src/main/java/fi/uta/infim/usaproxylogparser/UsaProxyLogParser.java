package fi.uta.infim.usaproxylogparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import fi.uta.infim.usaproxylogparser.TLexer;
import fi.uta.infim.usaproxylogparser.TParser;
import fi.uta.infim.usaproxylogparser.TParser.log_return;

public class UsaProxyLogParser implements IUsaProxyLogParser {

	@Override
	public UsaProxyLog parseLog(File file) throws IOException, ParseException {
		FileInputStream fis = new FileInputStream(file);
		return parseLog(fis);
	}

	@Override
	public UsaProxyLog parseLog(String filename) throws IOException, ParseException {
		FileInputStream fis = new FileInputStream(filename);
		return parseLog(fis);
	}

	@Override
	public UsaProxyLog parseLog(InputStream logStream) throws IOException, ParseException {
		TLexer lexer = new TLexer();
		lexer.setCharStream( new ANTLRInputStream( logStream ) );
		TokenStream tokens = new CommonTokenStream( lexer );
		TParser parser = new TParser( tokens );
		try {
			log_return logParseResult = parser.log();
			UsaProxyLog parsedLog = logParseResult.log;
			return parsedLog;
		} catch (RecognitionException e) {
			throw new java.text.ParseException(
					"Unable to parse log at line " + e.line + ", character " + e.charPositionInLine, 
					e.index );
		}
	}

}
