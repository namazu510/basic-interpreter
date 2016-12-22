package syntax_node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import core.LexicalType;
import core.LexicalUnit;

/**
 * First集合のコレクション.
 *
 */
public class FirstCollection {
	List<LexicalType> firstListUnit;
	
	public FirstCollection(LexicalType... firstTypes) {
		this.firstListUnit = Arrays.asList(firstTypes);
	}
	
	public FirstCollection(FirstCollection... firstCollections) {
		this.firstListUnit = new ArrayList<>();
		for (FirstCollection firstCollection : firstCollections) {
			firstListUnit.addAll(firstCollection.firstListUnit);
		}
	}
	
	public boolean contains(LexicalUnit unit) {
		LexicalType type = unit.getType();
		return firstListUnit.contains(type);
	}
}
