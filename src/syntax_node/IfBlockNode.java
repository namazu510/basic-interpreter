package syntax_node;

import java.util.ArrayList;
import java.util.List;

import core.Environment;
import core.LexicalType;
import core.LexicalUnit;
import core.Value;

/**
 * <block> ::=
 *		<if_prefix> <stmt> <NL>
 * 		| <if_prefix> <stmt> <ELSE> <stmt> <NL>
 * 		| <if_prefix> <NL> <stmt_list> <else_block> <ENDIF> <NL>
 *
 * <if_prefix>  ::=
 *		<IF> <cond> <THEN>
 *
 * <else_block>  ::=
 *		<else_if_block>
 *		| <else_if_block> <ELSE> <NL> <stmt_list>
 *
 * <else_if_block>   ::=
 *		φ
 * 		｜<else_if_block> <ELSEIF> <cond> <THEN> <NL> <stmt_list>
 */
public class IfBlockNode extends Node {
	
	/**
	 * IF条件ノード
	 */
	private CondNode cond;
	
	/**
	 * 実行ノード
	 */
	private StmtListNode ifStmtList, elseStmtListNode;
	
	/**
	 * 実行ノード
	 */
	private StmtNode ifStmtNode, elseStmtNode;
	
	/**
	 * この文に連続するIFブロック
	 */
	private final List<IfBlockNode> followIfBlockNodes = new ArrayList<>();
	
	public IfBlockNode(NodeType type, Environment env) {
		super(type, env);
		
	}
	
	static FirstCollection fc = new FirstCollection(LexicalType.IF);
	
	public static Node isMatch(Environment env, LexicalUnit first) {
		if (fc.contains(first)) {
			return new IfBlockNode(NodeType.IF_BLOCK, env);
		}
		
		return null;
		
	}
	
	@Override
	public boolean parse() {
		// if - cond -then - stmt - nl
		// ................|..... |____ else _ stmt _ nl
		// ................|
		// ................|_ nl - stmtlist - <else-block> - endif - nl
		
		// if - cond -then まで
		if (!skipExpectNode(LexicalType.IF)) {
			return false;
		}
		
		cond = (CondNode) CondNode.isMatch(env, peekLexicalUnit());
		if (cond == null || !cond.parse()) {
			return false;
		}
		if (!skipExpectNode(LexicalType.THEN)) {
			return false;
		}
		
		// nlかstmt nlが来れば次はstmtlist
		if (skipExpectNode(LexicalType.NL)) {
			// stmtlist
			ifStmtList = stmtListHandl();
			if (ifStmtList == null) {
				return false;
			}
			
			// else block
			if (!elseBlockHandl()) {
				return false;
			}
			
			// end if
			if (!skipExpectNode(LexicalType.ENDIF)) {
				return false;
			}
			
		} else {
			// stmt
			ifStmtNode = stmtHandl();
			if (ifStmtNode == null) {
				return false;
			}
			
			// elseが来たらelse側のstmtも来る
			// 来なければそのままNLで終わり
			if (skipExpectNode(LexicalType.ELSE)) {
				elseStmtNode = stmtHandl();
				if (elseStmtNode == null) {
					return false;
				}
			}
		}
		
		// 最後のNL
		if (!skipExpectNode(LexicalType.NL)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Stmtが期待されるところでパースする子
	 * Stmtの構文にマッチしないならnullが帰る
	 * @return
	 */
	private StmtNode stmtHandl() {
		StmtNode ret = (StmtNode) StmtNode.isMatch(env, peekLexicalUnit());
		if (ret == null || !ret.parse()) {
			return null;
		}
		return ret;
	}
	
	/**
	 * StmtListが期待されるところでパースする子
	 * Stmtの構文にマッチしないならnullが帰る
	 * @return
	 */
	private StmtListNode stmtListHandl() {
		StmtListNode ret = (StmtListNode) StmtListNode.isMatch(env, peekLexicalUnit());
		if (ret == null || !ret.parse()) {
			return null;
		}
		return ret;
	}
	
	/**
	 * ELSE-IFBlockを解析
	 * インスタンス変数のelseStmtListNodeを構成.
	 * @return
	 */
	private boolean elseBlockHandl() {
		// else-if-blockのFirst集合はELSEIF
		// 無くてもいい要素
		
		// else if 連続する限り追加する
		while (skipExpectNode(LexicalType.ELSEIF)) {
			IfBlockNode node = new IfBlockNode(NodeType.IF_BLOCK, env);
			if (!node.elseIfBlockParser()) {
				return false;
			}
			followIfBlockNodes.add(node);
		}
		
		// else or else if
		if (skipExpectNode(LexicalType.ELSE, LexicalType.NL)) {
			// else
			elseStmtListNode = stmtListHandl();
			return elseStmtListNode != null;
		}
		
		return true;
	}
	
	/**
	 * ELSEIFブロックをパースする子
	 */
	public boolean elseIfBlockParser() {
		cond = (CondNode) CondNode.isMatch(env, peekLexicalUnit());
		if (cond == null || !cond.parse()) {
			return false;
		}
		if (!skipExpectNode(LexicalType.THEN, LexicalType.NL)) {
			return false;
		}
		this.ifStmtList = stmtListHandl();
		if (ifStmtList == null) {
			return false;
		}
		return true;
	}
	
	// 条件真のときの実行ブロック
	public Node getThenNode() {
		if (ifStmtNode == null) {
			return ifStmtList;
		}
		return ifStmtList;
	}
	
	// 条件偽のときの実行ブロック
	public Node getElseNode() {
		if (elseStmtNode == null) {
			return elseStmtListNode;
		}
		return elseStmtNode;
	}
	
	@Override
	public Value eval() {
		Value condValue = cond.eval();
		if (condValue.getBValue()) {
			getThenNode().eval();
		} else {
			
			// 連続するIF文を実行する
			// ELSEIFですべてつながってるので順繰りにやっていく
			// どれか実行されたらevalElseIfNodeの戻り値がfalseで抜ける.
			int n = 0;
			while (evalElseIfNode(n)) {
				n++;
			}
			
			// 本体のELSEを実行する.
			if (getElseNode() != null) {
				getElseNode().eval();
			}
		}
		return null;
	}
	
	private boolean evalElseIfNode(int n) {
		if (n >= followIfBlockNodes.size()) {
			return false;
		}
		
		IfBlockNode node = followIfBlockNodes.get(n);
		if (node.cond.eval().getBValue() == false) {
			return true;
		}
		
		node.eval();
		return false;
	}
	
	@Override
	public String toString() {
		String res = String.format("IF[%s THEN{%s} ", cond.toString(), getThenNode().toString());
		
		for (IfBlockNode ifBlockNode : followIfBlockNodes) {
			res += String.format("{ELSEIF {%s} }", ifBlockNode.toString());
		}
		
		if (getElseNode() != null) {
			res += String.format("ELSE{%s}", getElseNode().toString());
		}
		res += "]";
		
		return res;
	}
	
}
