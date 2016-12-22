package syntax_node;

import libfunc.Function;
import core.Environment;
import core.LexicalType;
import core.LexicalUnit;
import core.Value;

/**
 * <call_func>	::=
 *		　<NAME> <LP> <expr_list> <RP>
 *
 * <call_sub> ::=
 * 	      <NAME> <expr_list>
 */
public class FunctionCallNode extends Node {
	
	private LexicalUnit name;
	private ExprListNode exprList;
	
	public FunctionCallNode(NodeType type, Environment env) {
		super(type, env);
	}
	
	public static Node isMatch(Environment env, LexicalUnit first) {
		if (first.getType() != LexicalType.NAME) {
			return null;
		}
		
		// FunctionCallはLL(2）
		env.getInput().get();
		LexicalUnit second = env.getInput().get();
		env.getInput().unget(first);
		env.getInput().unget(second);
		
		// 被っているのはAssignStatementだけだからEQだったら違う
		if (second.getType() == LexicalType.EQ) {
			return null;
		}
		return new FunctionCallNode(NodeType.FUNCTION_CALL, env);
	}
	
	@Override
	public boolean parse() {
		name = env.getInput().get();
		
		if (peekLexicalUnit().getType() == LexicalType.RP) {
			return callSubHandl();
		}
		return callFuncHandl();
	}
	
	private boolean callSubHandl() {
		if (!skipExpectNode(LexicalType.RP)) {
			return false;
		}
		if (!callFuncHandl()) {
			return false;
		}
		if (!skipExpectNode(LexicalType.LP)) {
			return false;
		}
		return true;
	}
	
	private boolean callFuncHandl() {
		exprList = (ExprListNode) ExprListNode.isMatch(env, peekLexicalUnit());
		exprList.fcCallEnv = true;
		if (exprList == null || !exprList.parse()) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		// PRINT "HELLO" -> PRINT[HELLO]
		return String.format("%s[%s]", name.getValue().getSValue(), exprList.toString());
	}
	
	@Override
	public Value eval() {
		Function function = env.getFunction(name);
		return function.eval(exprList);
	}
	
}
