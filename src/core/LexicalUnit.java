package core;

public class LexicalUnit {
	LexicalType type;
	Value value;
	LexicalUnit link;
	
	public LexicalUnit(LexicalType this_type) {
		type = this_type;
	}
	
	public LexicalUnit(LexicalType this_type, Value this_value) {
		type = this_type;
		value = this_value;
	}
	
	public Value getValue() {
		return value;
	}
	
	public LexicalType getType() {
		return type;
	}
	
	public String toString() {
		switch (type) {
		case LITERAL:
			return "LITERAL:\t" + value.getSValue();
		case NAME:
			return "NAME:\t" + value.getSValue();
		case DOUBLEVAL:
			return "DOUBLEVAL:\t" + value.getSValue();
		case INTVAL:
			return "INTVAL:\t" + value.getSValue();
		case IF:
			return ("IF");
		case THEN:
			return ("THEN");
		case ELSE:
			return ("ELSE");
		case FOR:
			return ("FOR");
		case FORALL:
			return ("FORALL");
		case NEXT:
			return ("NEXT");
		case SUB:
			return ("SUB");
		case DIM:
			return ("DIM");
		case AS:
			return ("AS");
		case END:
			return ("END");
		case EOF:
			return ("EOF");
		case NL:
			return ("NL");
		case EQ:
			return ("EQ");
		case LT:
			return ("LT");
		case GT:
			return ("GT");
		case LE:
			return ("LE");
		case GE:
			return ("GE");
		case DOT:
			return ("DOT");
		case WHILE:
			return ("WHILE");
		case DO:
			return ("DO");
		case UNTIL:
			return ("UNTIL");
		case ADD:
			return ("ADD");
		case MUL:
			return ("MUL");
		case DIV:
			return ("DIV");
		case LP:
			return ("LP");
		case RP:
			return ("RP");
		case COMMA:
			return ("COMMA");
		case LOOP:
			return ("LOOP");
		case TO:
			return ("TO");
		case WEND:
			return ("WEND");
		case ELSEIF:
			return ("ELSEIF");
		case NE:
			return ("NE");
		case ENDIF:
			return ("ENDIF");
		default:
			return "おい謎のタイプだぞこら.";
		}
	}
}
