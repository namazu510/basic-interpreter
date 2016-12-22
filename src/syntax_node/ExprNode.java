package syntax_node;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.Environment;
import core.LexicalType;
import core.LexicalUnit;
import core.Value;
import core.ValueImpl;
import core.ValueType;

/**
 * 式を計算.
 *
 * 1式を読みつつ逆ポーランド記法変換する
 * 2逆ポーランド記法の計算アルゴリズムを利用しツリーを生成する
 * 3生成したツリーを自信のインスタンスに適用する
 *
 *	<expr>	::=
 *	　　<expr> <ADD> <expr>
 *		| <expr> <SUB> <expr>
 *		| <expr> <MUL> <expr>
 *		| <expr> <DIV> <expr>
 *		| <SUB> <expr>
 *		| <LP> <expr> <RP>
 *		| <NAME>
 *		| <INTVAL>
 *		| <DOUBLEVAL>
 *		| <LITERAL>
 *		| <call_func>
 */
public class ExprNode extends Node {
	
	// 式の末端ならこれ
	private ExprEnd expression;
	
	// ２項式ならこっち
	private ExprEnd ope;
	private ExprNode left, right;
	
	public ExprNode(NodeType type, Environment env) {
		super(type, env);
	}
	
	public ExprNode(Environment env) {
		super(NodeType.EXPR, env);
	}
	
	public static Node isMatch(Environment env, LexicalUnit first) {
		FirstCollection fc = new FirstCollection(
				LexicalType.SUB,
				LexicalType.RP,
				LexicalType.NAME,
				LexicalType.INTVAL,
				LexicalType.DOUBLEVAL,
				LexicalType.LITERAL);
		
		if (fc.contains(first)) {
			Node node = new ExprNode(NodeType.EXPR, env);
			return node;
		}
		
		return null;
	}
	
	// Exprに含まれうる字句
	private static final Set<LexicalType> allowSet = new HashSet() {
		{
			add(LexicalType.NAME);
			
			add(LexicalType.LP);
			add(LexicalType.RP);
			
			add(LexicalType.INTVAL);
			add(LexicalType.DOUBLEVAL);
			add(LexicalType.LITERAL);
			
			add(LexicalType.ADD);
			add(LexicalType.SUB);
			add(LexicalType.MUL);
			add(LexicalType.DIV);
		}
	};
	
	// FunctionCallの時の括弧を式中の括弧か関数を閉じる括弧か見分けるための数値.
	boolean fcCallEnv = false;
	private int nest = 0;
	
	@Override
	public boolean parse() {
		
		// 読みつつ逆ポーランド記法へ
		// LexicalUnitのままだと困るからExprEnd 式末端ノードとして変換する
		
		// 演算子の連続は単項演算子. 数値が来るべき所に単項演算子きたらそれは単項演算子
		// ExprEndは記号を拡張出来るので単項演算子情報をつけておく
		
		// 逆ポーランド記法への変換のアルゴリズムはいくつかあるけどStackを使うやつが楽かな
		
		// 結果格納用
		Deque<ExprEnd> output = new ArrayDeque<>();
		
		// 単項演算子判別用. trueのときにSUBがきたらそれは単項演算子のSUB
		boolean expectValue = true;
		// 単項演算子バッファ.
		Deque<ExprEnd> singleOpeEnd = new ArrayDeque<>();
		
		// 演算子Stack
		Deque<LexicalUnit> opeStack = new ArrayDeque<>();
		while (true) {
			final LexicalUnit in = peekLexicalUnit();
			final LexicalType inType = in.getType();
			
			// 式構成要素じゃないものが飛んできた
			if (!allowSet.contains(inType)) {
				break;
			}
			env.getInput().get();
			
			// CALL SUBの処理
			if (inType == LexicalType.NAME) {
				// NAME -> RP はCALL SUB
				LexicalUnit test = env.getInput().get();
				if (test.getType() == LexicalType.RP) {
					
					// 　FCNのparse呼び出すために字句を戻す.
					env.getInput().unget(in);
					env.getInput().unget(test);
					
					// CALL SUBは必ず１つ値が返るから数値と同じと見なせる
					// 優先順位０でOutputする
					FunctionCallNode fcN = new FunctionCallNode(NodeType.FUNCTION_CALL, env);
					if (!fcN.parse()) {
						return false;
					}
					
					// CALL SUB全体を末端ノードと見なして出力する
					output.add(new ExprEnd(fcN, env));
					
					expectValue = false;
					
					continue;
				}
				env.getInput().unget(test);
			}
			
			if (inType == LexicalType.RP) {
				// RP ( はopeStackに入れる
				opeStack.push(in);
				
				nest++;
				
				continue;
			}
			
			if (inType == LexicalType.LP) {
				// LPが来たらopeStackからRPが出てくるまで出力する
				
				// 関数呼び出し内での閉じ括弧対応
				if (fcCallEnv && nest == 0) {
					env.getInput().unget(in);
					break;
				}
				
				LexicalUnit tmp;
				if (opeStack.size() == 0) {
					return false;
				}
				while ((tmp = opeStack.pop()).getType() != LexicalType.RP) {
					output.add(new ExprEnd(tmp, env));
					
					// （）の組み合わせがおかしい　-> 構文エラー
					if (opeStack.size() == 0) {
						return false;
					}
				}
				
				nest--;
				
				continue;
			}
			
			final int priority = getPriority(inType);
			if (priority != 0) {
				
				// 単項演算子が現れたとき.
				if (expectValue == true) {
					if (inType == LexicalType.SUB) {
						// 単項演算子の優先度は最大
						ExprEnd subEnd = new ExprEnd(in, env);
						subEnd.isSingleOpe = true;
						singleOpeEnd.add(subEnd);
					}
					continue;
				}
				
				// 優先度最高の単項演算子を全部出す.
				while (singleOpeEnd.size() != 0) {
					output.add(singleOpeEnd.poll());
				}
				
				// 優先順位の高い演算子をすべて出し
				// スタックにいれる
				while (opeStack.size() != 0 && getPriority(opeStack.peekFirst().getType()) >= priority) {
					output.add(new ExprEnd(opeStack.pop(), env));
				}
				expectValue = true;
				opeStack.push(in);
			} else {
				// 数値はそのまま出力
				output.add(new ExprEnd(in, env));
				
				expectValue = false;
			}
		}
		// 優先度最高の単項演算子を全部出す.
		while (singleOpeEnd.size() != 0) {
			output.add(singleOpeEnd.poll());
		}
		// 演算子バッファ全出力
		while (opeStack.size() != 0) {
			output.add(new ExprEnd(opeStack.pop(), env));
		}
		
		// DEBUG
		// output.forEach(System.out::println);
		// System.out.println("");
		
		// --------------
		// ここまででoutputに逆ポーランド記法で投入される
		// --------------
		
		// outputをツリーにする.
		
		// この方式思いっきり計算できるんだよなぁ・・・
		Deque<ExprNode> nodeStack = new ArrayDeque<>();
		while (output.size() != 0) {
			ExprNode in = output.pop();
			
			if (in instanceof ExprEnd) {
				ExprEnd endElm = (ExprEnd) in;
				if (endElm.isOpe) {
					
					// 単項演算子
					if (endElm.isSingleOpe == true) {
						if (nodeStack.size() < 1) {
							// 計算不能だから ^ ^;
							return false;
						}
						ExprNode expr = new ExprNode(env);
						expr.left = nodeStack.pop();
						expr.ope = endElm;
						
						nodeStack.push(expr);
						continue;
						
					}
					
					// ２項演算子
					if (nodeStack.size() < 2) {
						// 計算不能だから＾＾；
						return false;
					}
					
					ExprNode expr = new ExprNode(env);
					expr.right = nodeStack.pop();
					expr.left = nodeStack.pop();
					expr.ope = endElm;
					
					nodeStack.push(expr);
					continue;
					
				}
				// 演算子以外はスタックに入れる
				nodeStack.push(endElm);
			}
			
		}
		
		// 何で計算しないでツリーにしちゃったのか謎だけど構文木つくらないとだからね・・・
		// とりあえずこれでツリー完成
		
		// 自分のインスタンスに適用する
		ExprNode expr = nodeStack.pop();
		if (expr.isEnd()) {
			this.expression = (ExprEnd) expr;
		} else {
			this.left = expr.left;
			this.right = expr.right;
			this.ope = expr.ope;
		}
		
		// DEBUG:
		// System.out.println(expr);
		
		return true;
	}
	
	private int getPriority(LexicalType type) {
		// 0 は数値
		switch (type) {
		case MUL:
			return 3;
		case DIV:
			return 3;
			
		case SUB:
			return 2;
			
		case ADD:
			return 2;
			
		case RP:
		case LP:
			return -1;
			
		case INTVAL:
		case DOUBLEVAL:
		case LITERAL:
		case NAME:
			return 0;
			
		default:
			throw new IllegalArgumentException("優先度なんてないLexicalType:" + type.name());
		}
	}
	
	/**
	 * 与えたLexicalTypeが式のなかで使われる演算子ならtrue
	 *  + or - or / or *
	 */
	public boolean isOpe(LexicalType lt) {
		Set<LexicalType> opeSet = new HashSet() {
			{
				add(LexicalType.ADD);
				add(LexicalType.SUB);
				add(LexicalType.DIV);
				add(LexicalType.MUL);
			}
		};
		
		return opeSet.contains(lt);
	}
	
	/**
	 * このExprが末端かどうか返す.
	 * （枝分かれしないかどうか）
	 */
	protected boolean isEnd() {
		// TODO: もうちょっとちゃんとチェック
		if (left == null) {
			return true;
		}
		return false;
	}
	
	@Override
	public Value eval() {
		// 式末尾
		if (isEnd()) {
			return expression.eval();
		}
		// 単項
		if (isSingle()) {
			return evalSingleExpr();
		}
		
		// 二項
		return evalBinaryExpr();
	}
	
	/**
	 * 単項式かどうか
	 */
	private boolean isSingle() {
		if (left != null && right == null) {
			return true;
		}
		return false;
	}
	
	/**
	 * 単項式を計算する
	 */
	private Value evalSingleExpr() {
		LexicalType opeType = ope.val.getType();
		Value leftValue = left.eval();
		
		// 単項演算子いま許してるのは - だけだから
		ValueType evalType = leftValue.getType();
		
		switch (evalType) {
		case INTEGER:
			return new ValueImpl(-leftValue.getIValue());
		case DOUBLE:
			return new ValueImpl(-leftValue.getDValue());
		}
		
		throw new UnsupportedOperationException("おいこら？");
	}
	
	/**
	 * ２項式を計算する
	 */
	private Value evalBinaryExpr() {
		LexicalType opeType = ope.val.getType();
		
		Value rightValue = right.eval();
		Value leftValue = left.eval();
		
		ValueType evalType = evalValueType(rightValue, leftValue);
		
		switch (opeType) {
		case ADD:
			if (evalType == ValueType.STRING) {
				return new ValueImpl(leftValue.getSValue() + rightValue.getSValue());
			}
			if (evalType == ValueType.DOUBLE) {
				return new ValueImpl(leftValue.getDValue() + rightValue.getDValue());
			}
			if (evalType == ValueType.INTEGER) {
				return new ValueImpl(leftValue.getIValue() + rightValue.getIValue());
			}
			break;
		
		case SUB:
			if (evalType == ValueType.STRING) {
				throw new UnsupportedOperationException("Stringで引き算は出来ません");
			}
			if (evalType == ValueType.DOUBLE) {
				return new ValueImpl(leftValue.getDValue() - rightValue.getDValue());
			}
			if (evalType == ValueType.INTEGER) {
				return new ValueImpl(leftValue.getIValue() - rightValue.getIValue());
			}
			break;
		
		case MUL:
			if (evalType == ValueType.STRING) {
				throw new UnsupportedOperationException("Stringでかけ算は出来ません");
			}
			if (evalType == ValueType.DOUBLE) {
				return new ValueImpl(leftValue.getDValue() * rightValue.getDValue());
			}
			if (evalType == ValueType.INTEGER) {
				return new ValueImpl(leftValue.getIValue() * rightValue.getIValue());
			}
			break;
		
		case DIV:
			if (evalType == ValueType.STRING) {
				throw new UnsupportedOperationException("Stringで割り算は出来ません");
			}
			if (evalType == ValueType.DOUBLE) {
				return new ValueImpl(leftValue.getDValue() / rightValue.getDValue());
			}
			if (evalType == ValueType.INTEGER) {
				return new ValueImpl(leftValue.getIValue() / rightValue.getIValue());
			}
			break;
		
		default:
			break;
		}
		throw new UnsupportedOperationException("おいこら");
	}
	
	/**
	 * 2辺の演算をどの値の形で行えば良いのか求めるメソッド.
	 * STRING+ANY => STRING
	 * DOUBLE
	 */
	private ValueType evalValueType(Value right, Value left) {
		List<ValueType> list = new ArrayList<>();
		
		list.add(right.getType());
		list.add(left.getType());
		
		if (list.contains(ValueType.STRING)) {
			return ValueType.STRING;
		}
		if (list.contains(ValueType.DOUBLE)) {
			return ValueType.DOUBLE;
		}
		if (list.contains(ValueType.INTEGER)) {
			return ValueType.INTEGER;
		}
		if (list.contains(ValueType.BOOL)) {
			return ValueType.BOOL;
		}
		return ValueType.VOID;
	}
	
	@Override
	public String toString() {
		// 5 -> 5 , a - 1 -> -[a, 1]
		
		// 末端
		if (isEnd()) {
			return expression.toString();
		}
		
		// 単項
		if (this.right == null) {
			return String.format("%s[%s]", ope.toString(), left.toString());
		}
		
		// 二項
		return String.format("%s[%s,%s]", ope.toString(), left.toString(), right.toString());
	}
	
}

/**
 *　Exprの末端ノード
 *
 */
class ExprEnd extends ExprNode {
	
	// 演算子Nodeか？
	boolean isOpe;
	
	// 単項演算子か？
	boolean isSingleOpe;
	
	// 字句
	LexicalUnit val;
	
	// 関数呼び出し値か？
	boolean isFcval;
	// 関数呼び出しNode
	FunctionCallNode fcval;
	
	public ExprEnd(LexicalUnit val, Environment env) {
		super(env);
		this.val = val;
		this.isOpe = isOpe(val.getType());
	}
	
	public ExprEnd(FunctionCallNode fcN, Environment env) {
		super(env);
		this.fcval = fcN;
		this.isFcval = true;
		isOpe = false;
	}
	
	@Override
	public String toString() {
		if (isFcval) {
			return fcval.toString();
		}
		if (isOpe) {
			return val.getType().name();
		}
		return val.getValue().getSValue();
	}
	
	@Override
	protected boolean isEnd() {
		return true;
	}
	
	@Override
	public Value eval() {
		if (isFcval) {
			return fcval.eval();
		}
		if (val.getType() == LexicalType.NAME) {
			Value res = env.getVarValue(val);
			return res;
		}
		
		return val.getValue();
	}
}
