package syntax_node;

import java.util.ArrayList;
import java.util.List;

import core.Environment;
import core.LexicalType;
import core.LexicalUnit;
import core.Value;
import core.ValueImpl;
import core.ValueType;

/**
 * <cond>	 ::=
　　　　<expr> <EQ> <expr>
	| <expr> <GT> <expr>
	| <expr> <LT> <expr>
	| <expr> <GE> <expr>
	| <expr> <LE> <expr>
	| <expr> <NE> <expr>
 */
public class CondNode extends Node {
	
	ExprNode left;
	LexicalUnit operator;
	ExprNode right;
	
	public CondNode(NodeType type, Environment env) {
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
			Node node = new CondNode(NodeType.COND, env);
			return node;
		}
		
		return null;
		
	}
	
	private static List<LexicalType> allowOperator = new ArrayList() {
		{
			add(LexicalType.EQ);
			add(LexicalType.GT);
			add(LexicalType.LT);
			add(LexicalType.GE);
			add(LexicalType.LE);
		}
	};
	
	@Override
	public boolean parse() {
		// 左辺解析
		this.left = (ExprNode) ExprNode.isMatch(env, peekLexicalUnit());
		if (this.left == null || !this.left.parse()) {
			return false;
		}
		
		// オペレータ解析
		LexicalUnit ope = env.getInput().get();
		if (!allowOperator.contains(ope.getType())) {
			return false;
		}
		this.operator = ope;
		
		// 右辺解析
		this.right = (ExprNode) ExprNode.isMatch(env, peekLexicalUnit());
		if (this.right == null || !this.right.parse()) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		// UNTIL a < 1 は <[ 1 : a ]　になる
		return String.format("%s[%s:%s]", operator, left, right);
	}
	
	@Override
	public Value eval() {
		Value leftValue = left.eval();
		Value rightValue = right.eval();
		
		if (operator.getType() == LexicalType.EQ) {
			
		}
		boolean res = false;
		switch (operator.getType()) {
		case EQ:
			res = getEvalStr(leftValue).equals(getEvalStr(rightValue));
			break;
		case LT:
			res = leftValue.getDValue() < rightValue.getDValue();
			break;
		case GT:
			res = leftValue.getDValue() > rightValue.getDValue();
			break;
		case GE:
			res = leftValue.getDValue() >= rightValue.getDValue();
			break;
		case LE:
			res = leftValue.getDValue() <= rightValue.getDValue();
			break;
		}
		
		return new ValueImpl(res);
		
	}
	
	private String getEvalStr(Value val) {
		if (val.getType() == ValueType.STRING) {
			return val.getSValue();
		}
		return val.getDValue() + "";
	}
	
}
