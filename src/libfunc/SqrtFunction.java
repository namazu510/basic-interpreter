package libfunc;

import syntax_node.ExprListNode;
import core.Value;
import core.ValueImpl;

/**
 * 平方根出す関数
 */
public class SqrtFunction extends Function {
	@Override
	public Value eval(ExprListNode arg) {
		Value val = arg.getValue(0);
		return new ValueImpl(Math.sqrt(val.getDValue()));
	}
}
