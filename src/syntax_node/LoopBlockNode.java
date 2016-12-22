package syntax_node;

import core.Environment;
import core.LexicalType;
import core.LexicalUnit;
import core.Value;

/**
 * <block> ::=
 * 		| <WHILE> <cond> <NL> <stmt_list> <WEND> <NL>
 * 		| <DO> <WHILE> <cond> <NL> <stmt_list> <LOOP> <NL>
 * 		| <DO> <UNTIL> <cond> <NL> <stmt_list> <LOOP> <NL>
 * 		| <DO> <NL> <stmt_list> <LOOP> <WHILE> <cond> <NL>
 * 		| <DO> <NL> <stmt_list> <LOOP> <UNTIL> <cond> <NL>
 */
public class LoopBlockNode extends Node {
	
	/**
	 * Do While 文　つまり１度は走る文かどうか
	 */
	private boolean isDo;
	
	/**
	 * While文ならtrue Until文ならfalse
	 */
	private boolean isWhile;
	
	/**
	 * ループ実行条件ノード
	 */
	private CondNode condNode;
	
	/**
	 * ループブロック本体
	 */
	private StmtListNode stmtListNode;
	
	public LoopBlockNode(NodeType type, Environment env) {
		super(type, env);
	}
	
	static FirstCollection fc = new FirstCollection(
			LexicalType.WHILE,
			LexicalType.DO
			);
	
	public static Node isMatch(Environment env, LexicalUnit first) {
		if (fc.contains(first)) {
			return new LoopBlockNode(NodeType.LOOP_BLOCK, env);
		}
		return null;
	}
	
	@Override
	public boolean parse() {
		LexicalUnit first = env.getInput().get();
		// DO
		if (first.getType() == LexicalType.DO) {
			return doParse();
		}
		// WHILE
		if (first.getType() == LexicalType.WHILE) {
			return whileParse();
		}
		return false;
	}
	
	/**
	 * Do文を解析
	 * @return
	 */
	private boolean doParse() {
		isDo = true;
		
		LexicalUnit next = env.getInput().get();
		switch (next.getType()) {
		case WHILE:
		case UNTIL:
			// do - (while or until) - cond -nl -stmtlist - loop - nl
			untilOrWhileHandl(next);
			if (!condHandl()) {
				return false;
			}
			if (!skipExpectNode(LexicalType.NL)) {
				return false;
			}
			if (!stmtListHandl()) {
				return false;
			}
			if (!skipExpectNode(LexicalType.LOOP, LexicalType.NL)) {
				return false;
			}
			return true;
		case NL:
			// do - nl - stmtlist - loop - (while or until) - cond -nl
			if (!stmtListHandl()) {
				return false;
			}
			if (!skipExpectNode(LexicalType.LOOP)) {
				return false;
			}
			if (!untilOrWhileHandl(env.getInput().get())) {
				return false;
			}
			if (!condHandl()) {
				return false;
			}
			if (!skipExpectNode(LexicalType.NL)) {
				return false;
			}
			return true;
		default:
			return false;
		}
		
	}
	
	/**
	 * While文を解析
	 * @return
	 */
	private boolean whileParse() {
		isDo = false;
		isWhile = true;
		
		// while -> cond -> nl -> stmtlist -> loop -> nl
		if (!condHandl()) {
			return false;
		}
		if (!skipExpectNode(LexicalType.NL)) {
			return false;
		}
		if (!stmtListHandl()) {
			return false;
		}
		if (!skipExpectNode(LexicalType.LOOP, LexicalType.NL)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Loopブロックの条件式部分を解析
	 * @return SyntaxErrorならfalse
	 */
	private boolean condHandl() {
		condNode = (CondNode) CondNode.isMatch(env, peekLexicalUnit());
		if (condNode == null || !condNode.parse()) {
			return false;
		}
		return true;
	}
	
	/**
	 * LoopブロックのStmtList部分を解析
	 * @return SyntaxErrorならfalse
	 */
	private boolean stmtListHandl() {
		stmtListNode = (StmtListNode) StmtListNode.isMatch(env, peekLexicalUnit());
		if (stmtListNode == null || !stmtListNode.parse()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Until文なのかWhile文なのか調べてインスタンス変数に情報格納
	 * @param unit Until or Whileの　LexicalUnit
	 */
	private boolean untilOrWhileHandl(LexicalUnit unit) {
		switch (unit.getType()) {
		case WHILE:
			isWhile = true;
			return true;
			
		case UNTIL:
			isWhile = false;
			return true;
		}
		
		return false;
	}
	
	@Override
	public Value eval() {
		if (isDo) {
			stmtListNode.eval();
		}
		
		while (isContinue()) {
			stmtListNode.eval();
		}
		return null;
	}
	
	private boolean isContinue() {
		boolean cond = condNode.eval().getBValue();
		if (!isWhile) {
			cond = !cond;
		}
		return cond;
	}
	
	@Override
	public String toString() {
		return "LOOP[" + condNode + "[" + stmtListNode + "]]";
	}
}
