package syntax_node;

import core.Environment;
import core.LexicalUnit;
import core.Value;

/**
 * <program>  ::=
 *	             <stmt_list>
 *
 */
public class ProgramNode extends Node {
	
	// StmtListNode
	private Node child;
	
	public ProgramNode(NodeType type, Environment env) {
		super(type, env);
	}
	
	static FirstCollection fc = new FirstCollection(
			StmtListNode.fc,
			BlockNode.fc
			);
	
	public static Node isMatch(Environment env, LexicalUnit first) {
		// LexicalUnit first が　First集合に含まれる字句か判断する.
		if (fc.contains(first)) {
			return new ProgramNode(NodeType.PROGRAM, env);
		}
		
		return null;
	}
	
	@Override
	public boolean parse() {
		NextNodeList nextNodeList = new NextNodeList(StmtListNode.class);
		Node chiled = nextNodeList.nextNode(env, peekLexicalUnit());
		if (chiled != null) {
			this.child = chiled;
			return chiled.parse();
		}
		return false;
	}
	
	@Override
	public Value eval() {
		return child.eval();
	}
	
	@Override
	public String toString() {
		return child.toString();
	}
	
}
