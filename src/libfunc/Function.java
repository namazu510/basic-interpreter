package libfunc;

import syntax_node.ExprListNode;
import core.Value;

public abstract class Function {
	public abstract Value eval(ExprListNode arg);
}
