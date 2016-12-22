package syntax_node;

import core.Environment;
import core.LexicalUnit;
import core.Value;

/**
 * <block> ::=
	<if_prefix> <stmt> <NL>
	| <if_prefix> <stmt> <ELSE> <stmt> <NL>
	| <if_prefix> <NL> <stmt_list> <else_block> <ENDIF> <NL>
	| <WHILE> <cond> <NL> <stmt_list> <WEND> <NL>
	| <DO> <WHILE> <cond> <NL> <stmt_list> <LOOP> <NL>
	| <DO> <UNTIL> <cond> <NL> <stmt_list> <LOOP> <NL>
	| <DO> <NL> <stmt_list> <LOOP> <WHILE> <cond> <NL>
	| <DO> <NL> <stmt_list> <LOOP> <UNTIL> <cond> <NL>

 */
public class BlockNode extends Node {
	
	Node childNode;
	
	public BlockNode(NodeType type, Environment env) {
		super(type, env);
		
	}
	
	static FirstCollection fc = new FirstCollection(
			IfBlockNode.fc,
			LoopBlockNode.fc
			);
	
	public static Node isMatch(Environment env, LexicalUnit first) {
		if (fc.contains(first)) {
			return new BlockNode(NodeType.BLOCK, env);
		}
		
		return null;
		
	}
	
	@Override
	public boolean parse() {
		
		NextNodeList nextNodeList = new NextNodeList(LoopBlockNode.class, IfBlockNode.class);
		childNode = nextNodeList.nextNode(env, peekLexicalUnit());
		if (childNode != null) {
			return childNode.parse();
		}
		return false;
	}
	
	@Override
	public String toString() {
		return childNode.toString();
	}
	
	@Override
	public Value eval() {
		return childNode.eval();
	}
}
