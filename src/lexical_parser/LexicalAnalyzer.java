package lexical_parser;

import core.LexicalType;
import core.LexicalUnit;

public interface LexicalAnalyzer {
    public LexicalUnit get();
    public boolean expect(LexicalType type);
    public void unget(LexicalUnit token);    
}
