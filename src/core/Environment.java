package core;

import java.util.Hashtable;

import lexical_parser.LexicalAnalyzer;
import libfunc.Function;
import libfunc.PrintFunction;
import libfunc.SqrtFunction;

/**
 * 実行環境を持たせる.
 *
 * 変数名表とか関数テーブルとか
 */
public class Environment {
	
	private LexicalAnalyzer input;
	
	private Hashtable<String, Value> varTable;
	private Hashtable<String, Function> funcTable;
	
	public Environment(LexicalAnalyzer my_input) {
		input = my_input;
		varTable = new Hashtable<>();
		funcTable = new Hashtable<>();
		
		funcInit();
	}
	
	private void funcInit() {
		funcTable.put("PRINT", new PrintFunction());
		funcTable.put("SQRT", new SqrtFunction());
	}
	
	public LexicalAnalyzer getInput() {
		return input;
	}
	
	public void setVarValue(LexicalUnit varName, Value value) {
		varTable.put(varName.getValue().getSValue(), value);
	}
	
	public Value getVarValue(LexicalUnit varName) {
		Value res = varTable.get(varName.getValue().getSValue());
		if (res == null) {
			throw new IllegalStateException("存在しない変数を参照しようとしました！");
		}
		return res;
	}
	
	public void setFunc(LexicalUnit funcName, Function function) {
		funcTable.put(funcName.getValue().getSValue(), function);
	}
	
	public Function getFunction(LexicalUnit name) {
		Function res = funcTable.get(name.getValue().getSValue());
		if (res == null) {
			throw new IllegalStateException("存在しない関数を参照しようとしました！");
		}
		return res;
	}
}
