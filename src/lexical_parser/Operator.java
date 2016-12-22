package lexical_parser;

import core.LexicalType;

/**
 * すべての演算子の列挙　及びLexcalTypeとの関連付け
 * 一時決まりの字句とかもここだよ.
 */
public enum Operator {
	EQ(LexicalType.EQ, "="),
	LT(LexicalType.LT, "<"),
	GT(LexicalType.GT, ">"),
	LE(LexicalType.LE, "<=", "=<"),
	GE(LexicalType.GE, ">=", "=>"),
	NE(LexicalType.NE, "<>"),
	ADD(LexicalType.ADD, "+"),
	SUB(LexicalType.SUB, "-"),
	MUL(LexicalType.MUL, "*"),
	DIV(LexicalType.DIV, "/"),
	LP(LexicalType.LP, ")"),
	RP(LexicalType.RP, "("),
	COMMA(LexicalType.COMMA, ","),
	
	NL(LexicalType.NL, "\n"),
	DOT(LexicalType.DOT, ".");
	
	private final String[] vals;
	private final LexicalType type;
	
	private Operator(LexicalType type, String... vals) {
		// 　>= => とかが同じ意味なので配列にしときます.
		this.vals = vals;
		this.type = type;
	}
	
	/**
	 * 与えられた文字列と字句の決まり字が一致するかどうか
	 */
	public boolean isMatch(String test) {
		for (String string : vals) {
			if (string.equals(test)) {
				return true;
			}
		}
		return false;
	}
	
	public LexicalType getType() {
		return type;
	}
}
