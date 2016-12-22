package core;

public interface Value {
// 実装すべきコンストラクタ
//    public Value(String s);
//    public Value(int i);
//    public Value(double d);
//    public Value(boolean b);
//    public String get_sValue();
	public String getSValue();
	// ストリング型で値を取り出す。必要があれば、型変換を行う。
    public int getIValue();
    	// 整数型で値を取り出す。必要があれば、型変換を行う。
    public double getDValue();
    	// 小数点型で値を取り出す。必要があれば、型変換を行う。
    public boolean getBValue();
    	// 論理型で値を取り出す。必要があれば、型変換を行う。
    public ValueType getType();
}
