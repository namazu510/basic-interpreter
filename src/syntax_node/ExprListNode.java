package syntax_node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import core.Environment;
import core.LexicalType;
import core.LexicalUnit;
import core.Value;

public class ExprListNode extends Node {
	
	List<Node> childNodes = new ArrayList<>();
	
	public ExprListNode(NodeType type, Environment env) {
		super(type, env);
	}
	
	public static Node isMatch(Environment env, LexicalUnit first) {
		FirstCollection fc = new FirstCollection(
				LexicalType.SUB,
				LexicalType.RP,
				LexicalType.NAME,
				LexicalType.INTVAL,
				LexicalType.DOUBLEVAL,
				LexicalType.LITERAL);
		
		if (fc.contains(first)) {
			Node node = new ExprListNode(NodeType.EXPR_LIST, env);
			return node;
		}
		
		return null;
		
	}
	
	boolean fcCallEnv = false;
	
	@Override
	public boolean parse() {
		// first expr
		ExprNode expr = (ExprNode) ExprNode.isMatch(env, peekLexicalUnit());
		expr.fcCallEnv = this.fcCallEnv;
		if (expr == null || !expr.parse()) {
			return false;
		}
		childNodes.add(expr);
		
		// Commaが来ればもう一個式が来る、なければ正常
		while (skipExpectNode(LexicalType.COMMA)) {
			ExprNode nextExpr = (ExprNode) ExprNode.isMatch(env, peekLexicalUnit());
			nextExpr.fcCallEnv = this.fcCallEnv;
			if (nextExpr == null || !nextExpr.parse()) {
				return false;
			}
			childNodes.add(nextExpr);
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		return childNodes.stream()
				.map(Node::toString)
				.collect(Collectors.joining(","));
	}
	
	/**
	 * n番目　(0スタート)
	 * の式を実行した結果を返します.
	 */
	public Value getValue(int n) {
		return childNodes.get(n).eval();
	}
	
	@Override
	public Value eval() {
		throw new UnsupportedOperationException("ExprListはgetValue(int n)で値を取ること");
	}
	
}
