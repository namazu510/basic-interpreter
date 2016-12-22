package syntax_node;

import core.Environment;
import core.LexicalType;
import core.LexicalUnit;
import core.Value;

/**
 * 全構文要素の親クラス.
 * 構文木の非終端要素
 */
public abstract class Node {
	/**
	 * Nodeのタイプ.
	 */
	public final NodeType type;
	
	/**
	 * 実行環境.
	 * 変数テーブル. 関数テーブル. 字句解析器等.
	 */
	public final Environment env;
	
	public Node(NodeType type, Environment env) {
		this.type = type;
		this.env = env;
	}
	
	public NodeType getType() {
		return type;
	}
	
	/**
	 * 文法解析メソッド
	 * @return 構文にマッチしないならfalse
	 */
	public abstract boolean parse();
	
	/**
	 * Nodeを実行する.
	 */
	public abstract Value eval();
	
	// toStringの実装を強制
	public abstract String toString();
	
	/**
	 * 次のLexicalUnitを見る Utilメソッド
	 */
	protected LexicalUnit peekLexicalUnit() {
		LexicalUnit unit = env.getInput().get();
		env.getInput().unget(unit);
		return unit;
	}
	
	/**
	 * 次の字句タイプを見て期待した字句と一致していればその字句を読み飛ばすUtilメソッド
	 * NLとかLOOPとかのチェックしないといけないけど無視する字句に使う
	 *
	 * もし次に現れた字句が期待した物と一致しないのであればfalseを返す
	 *
	 * @param expectType 複数指定した場合は指定した順番でチェックしつつ読み飛ばす
	 * @return 期待した字句と一致しない場合false
	 */
	protected boolean skipExpectNode(LexicalType... expectType) {
		for (LexicalType expect : expectType) {
			if (peekLexicalUnit().getType() == expect) {
				env.getInput().get();
				continue;
			}
			return false;
		}
		
		return true;
	}
	
}
