package syntax_node;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import core.Environment;
import core.LexicalUnit;

/**
 * 子になりうるNodeのリスト.
 *
 * first集合の字句を投げて、子のnodeのインスタンスが得られるUtilクラス
 *
 * 必須要件.
 * 登録されたNodeにはisMatchが静的メソッドとして実装されている
 */
public class NextNodeList extends ArrayList<Class<? extends Node>> {
	
	public NextNodeList(Class<? extends Node>... nextNodes) {
		addAll(Arrays.asList(nextNodes));
	}
	
	/**
	 * 次の一致するノードを探し出す.
	 */
	public Node nextNode(Environment env, LexicalUnit unit) {
		Iterator<Class<? extends Node>> i = iterator();
		while (i.hasNext()) {
			Method isMatch;
			try {
				isMatch = i.next().getMethod("isMatch", Environment.class, LexicalUnit.class);
				Object res = isMatch.invoke(null, env, unit);
				
				if (res != null) {
					return (Node) res;
				}
				
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException ex) {
				// programming error
				ex.printStackTrace();
			}
		}
		return null;
	}
}
