package lexical_parser;

import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Deque;

import core.LexicalType;
import core.LexicalUnit;

public class LexicalAnalyzerImpl implements LexicalAnalyzer {
	
	private TokenParser tokenParser;
	
	public LexicalAnalyzerImpl(String filePath) throws Exception {
		tokenParser = new TokenParser(new FileReader(filePath));
		this.tokenBuffer = new ArrayDeque<LexicalUnit>();
	}
	
	Deque<LexicalUnit> tokenBuffer;
	
	@Override
	public LexicalUnit get() {
		
		if (tokenBuffer.size() == 0) {
			Token nextToken = tokenParser.get();
			// TokenをLexicalUnitへ
			LexicalUnit nextUnit;
			if (nextToken == null) {
				// nullが返ってきたらEOF
				nextUnit = new LexicalUnit(LexicalType.EOF);
			} else {
				nextUnit = nextToken.parseLexicalUnit();
			}
			
			tokenBuffer.add(nextUnit);
		}
		
		return tokenBuffer.poll();
	}
	
	@Override
	public boolean expect(LexicalType type) {
		return false;
	}
	
	@Override
	public void unget(LexicalUnit token) {
		this.tokenBuffer.add(token);
	}
	
}
