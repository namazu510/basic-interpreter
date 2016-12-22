package syntax_node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import core.Environment;
import core.LexicalType;
import core.LexicalUnit;
import core.Value;

/**
 * <stmt_list>　::=
 *			<stmt>
 *			| <stmt_list> <NL> <stmt>
 *			| <block>
 *			| <block> <stmt_list>
 *
 */
public class StmtListNode extends Node {
	private List<Node> childNodes = new ArrayList<>();
	
	public StmtListNode(NodeType type, Environment env) {
		super(type, env);
	}
	
	static FirstCollection fc = new FirstCollection(
			StmtNode.fc,
			BlockNode.fc
			);
	
	public static Node isMatch(Environment env, LexicalUnit first) {
		if (fc.contains(first)) {
			return new StmtListNode(NodeType.STMT_LIST, env);
		}
		return null;
	}
	
	@Override
	public boolean parse() {
		while (true) {
			// NLを読み飛ばす
			if (peekLexicalUnit().getType() == LexicalType.NL) {
				env.getInput().get();
				continue;
			}
			
			NextNodeList nextNodeList = new NextNodeList(StmtNode.class, BlockNode.class);
			Node chiled = nextNodeList.nextNode(env, peekLexicalUnit());
			if (chiled != null) {
				if (!chiled.parse()) {
					return false;
				}
				// Stmtの後にはNLが存在する
				if (chiled instanceof StmtNode) {
					env.getInput().get();
				}
				childNodes.add(chiled);
				continue;
			}
			return true;
		}
	}
	
	@Override
	public Value eval() {
		for (Node node : childNodes) {
			node.eval();
		}
		return null;
	}
	
	@Override
	public String toString() {
		return childNodes
				.stream()
				.map(Node::toString)
				.collect(Collectors.joining(";"));
	}
}
