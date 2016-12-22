package lexical_parser;

import java.util.regex.Pattern;

/**
 * 一番最初に切り出すTokenのタイプ
 * @author namaz
 *
 */
public enum TokenType {
	NUMBER("[0-9.]+"), // 0-9から始まりdotを含んで連続する(笑)
	WORD("^[a-zA-Z]\\w*"), // A-Za-zから始まって単語構成文字が連続する
	LITERAL("^\"[^\"]*\"?"), // "から始まりε　"があったらそれでおしまい.（エスケープシーケンス）
	SINGLE_OPERATOR("[\\.\n\\+\\-\\*\\/\\)\\(,]"), // １文字で構成される字句
	MULTY_OPERATOR("[><=]|=[><]|[><]=|<>"); // 　複数文字で構成されうる演算子 > < = =>　>= =<
											// <= <>
	
	private final Pattern pattern;
	
	private TokenType(String regx) {
		this.pattern = Pattern.compile(regx);
	}
	
	public boolean isMatch(String test) {
		return pattern.matcher(test).matches();
	}
}

class TokenTypeTest {
	public static void main(String[] args) {
		
		TokenType testTokenType;
		// NUMBER
		testTokenType = TokenType.NUMBER;
		tokenPatternTest("10", testTokenType, true);
		tokenPatternTest("hoge", testTokenType, false);
		tokenPatternTest("1hoge", testTokenType, false);
		
		// WORD
		testTokenType = TokenType.WORD;
		tokenPatternTest("Aa", testTokenType, true);
		tokenPatternTest("aab", testTokenType, true);
		tokenPatternTest("a1", testTokenType, true);
		tokenPatternTest("1", testTokenType, false);
		tokenPatternTest("b", testTokenType, true);
		
		// LITERAL
		testTokenType = TokenType.LITERAL;
		tokenPatternTest("\"", testTokenType, true);
		tokenPatternTest("\"hoge", testTokenType, true);
		tokenPatternTest("\"hoge\"", testTokenType, true);
		tokenPatternTest("hoge", testTokenType, false);
		tokenPatternTest("\"hoge\"hoge", testTokenType, false);
		
		// SIGLE_OPERATOR
		testTokenType = TokenType.SINGLE_OPERATOR;
		tokenPatternTest(".", testTokenType, true);
		tokenPatternTest("\n", testTokenType, true);
		tokenPatternTest("+", testTokenType, true);
		tokenPatternTest("-", testTokenType, true);
		tokenPatternTest("*", testTokenType, true);
		tokenPatternTest("/", testTokenType, true);
		tokenPatternTest(")", testTokenType, true);
		tokenPatternTest("(", testTokenType, true);
		
		// MULTY_OPERATOR
		testTokenType = TokenType.MULTY_OPERATOR;
		tokenPatternTest(">", testTokenType, true);
		tokenPatternTest("<", testTokenType, true);
		tokenPatternTest(">=", testTokenType, true);
		tokenPatternTest("<=", testTokenType, true);
		tokenPatternTest("=", testTokenType, true);
		tokenPatternTest("=>", testTokenType, true);
		tokenPatternTest("=<", testTokenType, true);
		tokenPatternTest("<>", testTokenType, true);
		tokenPatternTest("><", testTokenType, false);
		
	}
	
	private static void tokenPatternTest(String test, TokenType type, boolean expect) {
		try {
			String res = (type.isMatch(test) == expect) ? "OK" : "FAILED";
			System.out.println(type.name() + ":" + test + " " + res);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
