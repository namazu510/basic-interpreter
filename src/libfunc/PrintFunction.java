package libfunc;

import syntax_node.ExprListNode;
import core.Value;

public class PrintFunction extends Function {
	
	@Override
	public Value eval(ExprListNode arg) {
		Value value = arg.getValue(0);
		System.out.println(value.getSValue());
		return null;
	}
	
}
