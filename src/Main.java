import java.io.File;

import lexical_parser.LexicalAnalyzer;
import lexical_parser.LexicalAnalyzerImpl;
import syntax_node.Node;
import syntax_node.ProgramNode;
import core.Environment;
import core.LexicalUnit;

public class Main {
	
	/**
	 * 実行ファイルパス
	 */
	private static final String SORCE_PATH = "test1.bas";
	
	public static void main(String[] args) {
		try {
			LexicalAnalyzer lex;
			LexicalUnit first;
			Environment env;
			Node program;
			
			// File Exsist Check
			if (new File(SORCE_PATH).exists() == false) {
				System.out.println("ソースファイルがないよ？");
				return;
			}
			
			lex = new LexicalAnalyzerImpl(SORCE_PATH);
			env = new Environment(lex);
			first = lex.get();
			lex.unget(first);
			
			program = ProgramNode.isMatch(env, first);
			boolean res = program.parse();
			if (res == false) {
				System.out.println("Syntax error");
				return;
			}
			// 構文木表示
			System.out.println("Syntax parsed ---");
			System.out.println(program);
			System.out.println("-----------------");
			
			System.out.println("-------run!------");
			// プログラム実行
			program.eval();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
