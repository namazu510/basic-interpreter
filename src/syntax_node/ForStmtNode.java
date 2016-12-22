package syntax_node;

import core.Environment;
import core.LexicalType;
import core.LexicalUnit;
import core.Value;
import core.ValueImpl;

/**
 * <FOR> <subst> <TO> <INTVAL> <NL> <stmt_list> <NEXT> <NAME>
 */
public class ForStmtNode extends Node {
	
	private AssignStmtNode assignStmtNode;
	private LexicalUnit to;
	private StmtListNode stmt;
	private LexicalUnit name;
	
	public ForStmtNode(NodeType type, Environment env) {
		super(type, env);
	}
	
	public static Node isMatch(Environment env, LexicalUnit first) {
		// LexicalUnit first が　First集合に含まれる字句か判断する.
		FirstCollection fc = new FirstCollection(LexicalType.FOR);
		
		if (fc.contains(first)) {
			return new ForStmtNode(NodeType.FOR_STMT, env);
		}
		
		return null;
	}
	
	@Override
	public boolean parse() {
		
		// for asign-stmt to intval stmtlist next name
		
		if (!skipExpectNode(LexicalType.FOR)) {
			return false;
		}
		
		this.assignStmtNode = (AssignStmtNode) AssignStmtNode.isMatch(env, peekLexicalUnit());
		if (assignStmtNode == null || !assignStmtNode.parse()) {
			return false;
		}
		
		if (!skipExpectNode(LexicalType.TO)) {
			return false;
		}
		
		this.to = peekLexicalUnit();
		if (to.getType() != LexicalType.INTVAL) {
			return false;
		}
		env.getInput().get();
		
		if (!skipExpectNode(LexicalType.NL)) {
			return false;
		}
		
		this.stmt = (StmtListNode) StmtListNode.isMatch(env, peekLexicalUnit());
		if (stmt == null || !stmt.parse()) {
			return false;
		}
		
		if (!skipExpectNode(LexicalType.NEXT)) {
			return false;
		}
		
		this.name = peekLexicalUnit();
		if (name.getType() != LexicalType.NAME) {
			return false;
		}
		env.getInput().get();
		
		return true;
	}
	
	@Override
	public Value eval() {
		// 代入する
		assignStmtNode.eval();
		
		while (isContinue()) {
			stmt.eval();
			// nameを1増やす
			double newval = env.getVarValue(name).getDValue() + 1;
			env.setVarValue(name, new ValueImpl(newval));
			
		}
		
		return null;
	}
	
	private boolean isContinue() {
		// nameがto以下かチェックする
		int toVal = this.to.getValue().getIValue();
		double nameVal = env.getVarValue(name).getDValue();
		
		return nameVal <= toVal;
	}
	
	@Override
	public String toString() {
		return String.format("FOR[%s TO %s {%s}]"
				, assignStmtNode.toString()
				, to.getValue().getSValue()
				, stmt.toString());
	}
}
