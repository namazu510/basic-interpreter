package lexical_parser;

import java.util.EnumSet;
import java.util.Optional;

import core.LexicalType;
import core.LexicalUnit;
import core.ValueImpl;

/**
 * Token
 * ソースコードから切り出したトークン
 *
 * SorceCode -> Token -> LexcalUnit
 * @author namaz
 *
 */
public class Token {
	
	private TokenType type;
	private String value;
	
	Token(TokenType type, String value) {
		this.type = type;
		this.value = value;
	}
	
	public TokenType getType() {
		return type;
	}
	
	public String getValue() {
		return value;
	}
	
	/**
	 * このトークンをタイプに応じて解析し,
	 * より詳しく細分化されたLexicalUnitへ変換します.
	 */
	public LexicalUnit parseLexicalUnit() {
		
		// それぞれのトークン種別に応じて解析.
		LexicalUnit res = null;
		switch (type) {
		case NUMBER:
			res = parseNumberToken();
			break;
		
		case WORD:
			res = parseWordToken();
			break;
		
		case LITERAL:
			res = parseLiteralToken();
			break;
		
		case SINGLE_OPERATOR:
		case MULTY_OPERATOR:
			res = parseOperatorToken();
			break;
		
		}
		
		// if (res == null) {
		// System.out.println("すべてのトークンは"
		// + "いずれかのLexcalUnitに出来るはずなんですがなんかおかしいぞ");
		// }
		
		return res;
	}
	
	private LexicalUnit parseWordToken() {
		// 予約語の抽出.
		Optional<ReservedWord> reservedWordOpt = EnumSet.allOf(ReservedWord.class)
				.stream()
				.filter(t -> t.isMatch(value))
				.findFirst();
		
		if (reservedWordOpt.isPresent()) {
			// 予約語とマッチした.
			LexicalType type = reservedWordOpt.get().getType();
			return new LexicalUnit(type);
		}
		
		// 予約語とマッチしない　単語　->　変数.
		return new LexicalUnit(LexicalType.NAME, new ValueImpl(value));
	}
	
	private LexicalUnit parseNumberToken() {
		// Double
		if (value.contains(".")) {
			return new LexicalUnit(LexicalType.DOUBLEVAL, new ValueImpl(Double.parseDouble(value)));
		}
		
		// Intval
		return new LexicalUnit(LexicalType.INTVAL, new ValueImpl(Integer.parseInt(value)));
		
		// TODO: もっと精巧な.とかの処理. 指数部とか　+ - とかの符号もね
		
	}
	
	private LexicalUnit parseLiteralToken() {
		// 　”を消す
		String literalVal = value.replaceAll("\"", "");
		return new LexicalUnit(LexicalType.LITERAL, new ValueImpl(literalVal));
	}
	
	private LexicalUnit parseOperatorToken() {
		// オペレーター種別の識別.
		Optional<Operator> operatorOpt = EnumSet.allOf(Operator.class)
				.stream()
				.filter(t -> t.isMatch(value))
				.findFirst();
		
		if (operatorOpt.isPresent()) {
			// 値に該当するオペレーターが存在した
			LexicalType type = operatorOpt.get().getType();
			return new LexicalUnit(type);
		}
		
		// 該当しないはずがない
		// System.out.println("１字決まりのやつみつかんない、なんかおかしいぞ");
		return null;
	}
}
