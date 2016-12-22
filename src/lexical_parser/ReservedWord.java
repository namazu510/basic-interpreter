package lexical_parser;

import core.LexicalType;

/**
 * すべての予約語とその列挙,及びLexcalTypeとの関連付け
 */
public enum ReservedWord {
	
	IF("IF", LexicalType.IF),
	THEN("THEN", LexicalType.THEN),
	ELSE("ELSE", LexicalType.ELSE),
	ELSEIF("ELSEIF", LexicalType.ELSEIF),
	ENDIF("ENDIF", LexicalType.ENDIF),
	FOR("FOR", LexicalType.FOR),
	FORALL("FORALL", LexicalType.FORALL),
	NEXT("NEXT", LexicalType.NEXT),
	FUNC("FUNC", LexicalType.FUNC),
	DIM("DIM", LexicalType.DIM),
	AS("AS", LexicalType.AS),
	END("END", LexicalType.END),
	WHILE("WHILE", LexicalType.WHILE),
	DO("DO", LexicalType.DO),
	UNTIL("UNTIL", LexicalType.UNTIL),
	LOOP("LOOP", LexicalType.LOOP),
	TO("TO", LexicalType.TO),
	WEND("WEND", LexicalType.WEND),
	
	;
	
	private final String val;
	private final LexicalType type;
	
	private ReservedWord(String val, LexicalType type) {
		this.val = val;
		this.type = type;
	}
	
	/**
	 * 与えられた文字列が紐付けられた文字に一致するか調べます.
	 * 一致するならtrue
	 */
	public boolean isMatch(String test) {
		return test.equals(this.val);
	}
	
	public LexicalType getType() {
		return type;
	}
	
}
