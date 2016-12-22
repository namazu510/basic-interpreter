package lexical_parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.EnumSet;
import java.util.Optional;

/**
 * Streamを読んでTokenに切り出すクラス.
 * テキストデータを１文字ずつよんでTokenに分割
 */
public class TokenParser {
	
	/**
	 * テキストデータ読み込みReader
	 */
	private Reader reader;
	private boolean readerIsEOF = false;
	
	public TokenParser(Reader reader) {
		super();
		this.reader = reader;
	}
	
	/**
	 * 字句確定のためにreadしちゃった次の字句を持っておく
	 */
	private String nextTokenBuff = "";
	
	/**
	 * 次のTokenを得ます.
	 */
	public Token get() {
		
		if (readerIsEOF == true) {
			return null;
		}
		
		// EOFやスペースではなく次のTokenを構成する１字を得る
		String tokenFirstStr = normalizeStr(nextTokenBuff);
		if (!readerIsEOF && tokenFirstStr.isEmpty()) {
			tokenFirstStr += readNextStr();
		}
		
		// トークン始まりの字にマッチするtokenTypeを得る
		TokenType tokenType = getMatchTokenType(tokenFirstStr);
		
		// マッチが外れ、違う字句になるまで読みTokenの値を得る
		String tokenVal = tokenFirstStr;
		while (!readerIsEOF && tokenType.isMatch(tokenVal)) {
			tokenVal += readNext();
		}
		
		// マッチ外れたときに入ったアルファベットは次の字句の可能性ありありなので保持　
		// cf. a= はa=のときにマッチが外れるが=はそれ自体次の字句
		this.nextTokenBuff = String.valueOf(tokenVal.charAt(tokenVal.length() - 1));
		
		// 出力するトークンからは次の字句の文字は消す
		tokenVal = tokenVal.substring(0, tokenVal.length() - 1);
		
		return new Token(tokenType, tokenVal);
	}
	
	/**
	 * TokenTypeからTokenTypeのパラメータに一致するやつ探し出す.
	 * <始めの１字がどのトークンタイプか判別する.>
	 * @param tokenFirstStr Tokenの初めの１字
	 */
	private TokenType getMatchTokenType(String tokenFirstStr) {
		
		Optional<TokenType> tokenTypeOpt = EnumSet.allOf(TokenType.class)
				.stream()
				.filter(t -> t.isMatch(tokenFirstStr))
				.findFirst();
		
		if (!tokenTypeOpt.isPresent()) {
			// 存在しないとか困るんだよね...
			// Syntaxエラーだから...　
			System.out.println("おいしんたっくすえらーだぞ");
			return null;
		}
		
		return tokenTypeOpt.get();
	}
	
	/**
	 * 文字を標準化
	 * CRをスペースに　（LFだけにする）
	 * タブはスペースに
	 * 最後にスペースは消し去ります
	 */
	private String normalizeStr(String str) {
		return str
				.replaceAll("\t", "  ")
				.replace('\r', ' ')
				.replaceAll(" ", "");
	}
	
	/**
	 * 次の"字"を１つ読み返すよ
	 * (スペースとかは無視されるよ)
	 */
	private String readNextStr() {
		String tmp = "";
		while (tmp.isEmpty() && !readerIsEOF) {
			char c = readNext();
			tmp += c;
			tmp = normalizeStr(tmp);
		}
		
		return tmp;
	}
	
	/**
	 * ストリームから１"文字"読みます. (スペースとかが返ってくるよ）
	 * もしEOFだったらisEOFをtrueにして0を返します...
	 */
	private char readNext() {
		
		if (this.readerIsEOF == true) {
			return 0;
		}
		
		int ci = 0;
		try {
			ci = reader.read();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		// EOF
		if (ci == -1) {
			this.readerIsEOF = true;
			
			try {
				reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			return 0;
		}
		
		return (char) ci;
	}
	
}

class TokenParserTest {
	public static void main(String[] args) throws FileNotFoundException {
		TokenParser parser = new TokenParser(new FileReader("test1.bas"));
		
		Token token = parser.get();
		while (token != null) {
			System.out.println(token.getType() + " : " + token.getValue());
			token = parser.get();
		}
	}
}
