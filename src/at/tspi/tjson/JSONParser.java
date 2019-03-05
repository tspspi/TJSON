package at.tspi.tjson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class JSONParser {
	abstract private class StateStackElement {
		abstract public void childParsed(JSONValue child);
		abstract public boolean processCluster(String nextCluster) throws JSONParserException;
	};
	private class StateStackObject extends StateStackElement {
		private static final int OBJECTSTATE_EXPECTKEY = 0;
		private static final int OBJECTSTATE_READKEY = 1;
		private static final int OBJECTSTATE_EXPECTOBJECT = 2;
		private static final int OBJECTSTATE_READOBJECT = 3;

		private int currentState;
		private String currentKey;
		HashMap<String, List<JSONValue>> hmMembers;
		Stack<StateStackElement> stk;

		public StateStackObject(Stack<StateStackElement> stk, String passedCluster) throws JSONParserException {
			this.currentKey 	= "";
			this.hmMembers 		= new HashMap<String, List<JSONValue>>();
			this.currentState 	= OBJECTSTATE_EXPECTKEY;
			this.stk 			= stk;

			this.stk.push(this);

			if(passedCluster != null) {
				processCluster(passedCluster);
			}
		}
		
		public boolean processCluster(String nextCluster) throws JSONParserException {
			if(nextCluster.length() > 1) { throw new JSONParserException(); }
			char c = nextCluster.charAt(0);

			switch(this.currentState) {
				case OBJECTSTATE_EXPECTKEY:
					switch(c) {
						case '"':
							this.currentState = OBJECTSTATE_READKEY;
							new StateStackString(stk, null);
							return true;
						case 0x09:
						case 0x0A:
						case 0x0D:
						case 0x20:
							return true;
						case '}':
							if(this.hmMembers.size() == 0) {
								break;
							}
							throw new JSONParserException();
						default:
							throw new JSONParserException("Unknown symbol \""+c+"\"");
					}
					break;
				case OBJECTSTATE_READKEY:
					switch(c) {
						case ':':
							this.currentState = OBJECTSTATE_EXPECTOBJECT;
							return true;
						case 0x09:
						case 0x0A:
						case 0x0D:
						case 0x20:
							return true;
						default:
							throw new JSONParserException();
					}
				case OBJECTSTATE_EXPECTOBJECT:
					switch(c) {
						case '{':
							this.currentState = OBJECTSTATE_READOBJECT;
							new StateStackObject(stk, null);
							return true;
						case '[':
							this.currentState = OBJECTSTATE_READOBJECT;
							new StateStackArray(stk, null);
							return true;
						case '"':
							this.currentState = OBJECTSTATE_READOBJECT;
							new StateStackString(stk, null);
							return true;
						case 't':
						case 'T':
						case 'f':
						case 'F':
						case 'n':
						case 'N':
							this.currentState = OBJECTSTATE_READOBJECT;
							new StateStackConstant(stk, nextCluster);
							return true;
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
						case '-':
							this.currentState = OBJECTSTATE_READOBJECT;
							new StateStackNumber(stk, nextCluster);
							return true;
						case 0x09:
						case 0x0A:
						case 0x0D:
						case 0x20:
							return true;
						default:
							throw new JSONParserException();
					}
				case OBJECTSTATE_READOBJECT:
					switch(c) {
						case ',':
							this.currentState = OBJECTSTATE_EXPECTKEY;
							return true;
						case '}':
							break;
						case 0x09:
						case 0x0A:
						case 0x0D:
						case 0x20:
							return true;
						default:
							throw new JSONParserException();
					}
					break;
				default:
					throw new RuntimeException("Implementation error");
			}
			
			JSONObject objNew = new JSONObject();
			for(Map.Entry<String, List<JSONValue>> ehm : this.hmMembers.entrySet()) {
				String currentKey = ehm.getKey();
				for(JSONValue el : ehm.getValue()) {
					objNew.add(currentKey, el);
				}
			}
			stk.pop();
			stk.peek().childParsed(objNew);
			return true;
		}

		@Override
		public void childParsed(JSONValue child) {
			switch(this.currentState) {
				case OBJECTSTATE_READKEY:
					this.currentKey = ((JSONString)child).get();
					return;
				case OBJECTSTATE_READOBJECT:
					List<JSONValue> lst = hmMembers.get(this.currentKey);
					if(lst == null) {
						lst = new ArrayList<JSONValue>();
						hmMembers.put(this.currentKey, lst);
					}
					lst.add(child);
					return;
				default:
					throw new RuntimeException("Implementation error");
			}
			
		}
	};
	private class StateStackArray extends StateStackElement {
		private boolean gotComma;
		private List<JSONValue> members;

		private Stack<StateStackElement> stk;
		
		public StateStackArray(Stack<StateStackElement> stk, String passedCluster) throws JSONParserException {
			this.stk = stk;
			this.members = new ArrayList<JSONValue>();
			this.gotComma = false;

			stk.push(this);

			if(passedCluster != null) {
				this.processCluster(passedCluster);
			}
		}
		public boolean processCluster(String nextCluster) throws JSONParserException {
			if(nextCluster.length() > 1) {
				throw new JSONParserException();
			}
			char c = nextCluster.charAt(0);

			switch(c) {
				case '{':
					this.gotComma = false;
					new StateStackObject(stk, null);
					return true;
				case '[':
					this.gotComma = false;
					new StateStackArray(stk, null);
					return true;
				case '"':
					this.gotComma = false;
					new StateStackString(stk, null);
					return true;
				case 't':
				case 'T':
				case 'f':
				case 'F':
				case 'n':
				case 'N':
					this.gotComma = false;
					new StateStackConstant(stk, nextCluster);
					return true;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case '-':
					this.gotComma = false;
					new StateStackNumber(stk, nextCluster);
					return true;
				case 0x0A:
				case 0x0D:
				case 0x20:
				case 0x09:
					return true;
				case ',':
					if((!this.gotComma) && (this.members.size() > 0)) {
						this.gotComma = true;
						return true;
					} else {
						throw new JSONParserException();
					}
				case ']':
					if(this.gotComma) {
						throw new JSONParserException();
					}
					stk.pop();
					JSONArray arNew = new JSONArray();
					for(JSONValue v : this.members) {
						arNew.push(v);
					}
					stk.peek().childParsed(arNew);
					return true;
				default:
					throw new JSONParserException("Invalid character \""+c+"\"");
			}
		}
		@Override
		public void childParsed(JSONValue child) {
			this.members.add(child);
		}
	};
	private class StateStackNumber extends StateStackElement {
		private static final int NUMBERSTATE_FIRSTSYMBOL = 0;
		private static final int NUMBERSTATE_INTEGERDIGITS = 1;
		private static final int NUMBERSTATE_FRACTIONALDIGITSFIRST = 2;
		private static final int NUMBERSTATE_FRACTIONALDIGITS = 3;
		private static final int NUMBERSTATE_EXPONENTFIRST = 4;
		private static final int NUMBERSTATE_EXPONENTFIRSTAFTERSIGN = 5;
		private static final int NUMBERSTATE_EXPONENT = 6;
		
		private int parseState;

		private double currentMultiplier;
		private long currentExponent;
		private long signBase;
		private long signExponent;
		private Number currentValue;
		
		private Stack<StateStackElement> stk;

		public StateStackNumber(Stack<StateStackElement> stk, String passedCluster) throws JSONParserException {
			this.stk = stk;
			this.parseState = NUMBERSTATE_FIRSTSYMBOL;
			this.currentMultiplier = 1;
			this.currentExponent = 0;
			this.signBase = 1;
			this.signExponent = 1;
			
			stk.push(this);
			
			if(passedCluster != null) {
				processCluster(passedCluster);
			}
		}
		public boolean processCluster(String nextCluster) throws JSONParserException {
			if(nextCluster.length() != 1) {
				throw new JSONParserException();
			}
			char c = nextCluster.charAt(0);

			switch(this.parseState) {
				case NUMBERSTATE_FIRSTSYMBOL:
					if((c >= '0') && (c <= '9')) {
						this.parseState = NUMBERSTATE_INTEGERDIGITS;
						this.currentValue = new Integer(c - '0');
					} else if(c == '-') {
						this.signBase = -1;
						this.parseState = NUMBERSTATE_INTEGERDIGITS;
					} else {
						throw new JSONParserException();						
					}
					return true;
				case NUMBERSTATE_INTEGERDIGITS:
					if((c >= '0') && (c <= '9')) {
						if(this.currentValue == null) {
							this.currentValue = new Integer(c - '0');
						} else if(this.currentValue.longValue() < Integer.MAX_VALUE/10) {
							this.currentValue = new Integer(this.currentValue.intValue() * 10 + (c - '0'));
						} else if(this.currentValue.doubleValue() < Long.MAX_VALUE/10) {
							this.currentValue = new Long(this.currentValue.longValue() * 10 + (c - '0'));
						} else {
							this.currentValue = new Double(this.currentValue.doubleValue() * 10.0 + (c - '0'));
						}
						return true;
					} else if(c == '.') {
						this.parseState = NUMBERSTATE_FRACTIONALDIGITSFIRST;
						this.currentValue = new Double(this.currentValue.doubleValue());
						return true;
					} else if((c == 'e') || (c == 'E')) {
						this.parseState = NUMBERSTATE_EXPONENTFIRST;
						this.currentValue = new Double(this.currentValue.doubleValue());
						return true;
					} else {
						stk.pop();
						JSONNumber newNode;
						if(this.currentValue.longValue() < Integer.MAX_VALUE) {
							 newNode = new JSONNumber(this.signBase * this.currentValue.intValue());
						} else if(this.currentValue.doubleValue() < Long.MAX_VALUE){
							newNode = new JSONNumber(this.signBase * this.currentValue.longValue());
						} else {
							newNode = new JSONNumber(this.signBase * this.currentValue.doubleValue());
						}
						stk.peek().childParsed(newNode);
						return false;
					}
				case NUMBERSTATE_FRACTIONALDIGITSFIRST:
					if((c >= '0') && (c <= '9')) {
						this.parseState = NUMBERSTATE_FRACTIONALDIGITS;
						this.currentValue = new Double(this.currentValue.doubleValue() * 10 + ((double)(c - '0')));
						this.currentMultiplier = this.currentMultiplier / 10.0;
						return true;
					} else {
						throw new JSONParserException();
					}
				case NUMBERSTATE_FRACTIONALDIGITS:
					if((c >= '0') && (c <= '9')) {
						this.currentValue = new Double(this.currentValue.doubleValue() * 10 + ((double)(c - '0')));
						this.currentMultiplier = this.currentMultiplier / 10.0;
						return true;
					} else if((c == 'e') || (c == 'E')) {
						this.parseState = NUMBERSTATE_EXPONENTFIRST;
						return true;
					} else {
						JSONNumber newNode = new JSONNumber(this.currentValue.doubleValue() * this.currentMultiplier * this.signBase);
						stk.pop();
						stk.peek().childParsed(newNode);
						return false;
					}
				case NUMBERSTATE_EXPONENTFIRST:
					if(c == '+') {
						this.parseState = NUMBERSTATE_EXPONENTFIRSTAFTERSIGN;
						return true;
					} else if(c == '-') {
						this.parseState = NUMBERSTATE_EXPONENTFIRSTAFTERSIGN;
						this.signExponent = -1;
						return true;
					} else if((c >= '0') && (c <= '9')) {
						this.parseState = NUMBERSTATE_EXPONENT;
						this.currentExponent = c - '0';
						return true;
					} else {
						throw new JSONParserException();
					}
				case NUMBERSTATE_EXPONENTFIRSTAFTERSIGN:
					if((c >= '0') && (c <= '9')) {
						this.parseState = NUMBERSTATE_EXPONENT;
						this.currentExponent = c - '0';
						return true;
					} else {
						throw new JSONParserException();
					}
				case NUMBERSTATE_EXPONENT:
					if((c >= '0') && (c <= '9')) {
						this.currentExponent = this.currentExponent * 10 + (c - '0');
						return true;
					}
					double dValue = this.currentValue.doubleValue() * this.currentMultiplier * this.signBase * Math.pow(10, this.currentExponent * this.signExponent);
					JSONNumber newNode = new JSONNumber(dValue);
					stk.pop();
					stk.peek().childParsed(newNode);
					return false;
				default:
					throw new RuntimeException("Implementation error");
			}
		}
		@Override
		public void childParsed(JSONValue child) {
			throw new RuntimeException("Implementation error");
		}
	};
	private class StateStackString extends StateStackElement {
		private static final int STRINGSTATE_NORMAL = 0;
		private static final int STRINGSTATE_ESCAPED = 1;
		private static final int STRINGSTATE_UTF16CODEPOINT = 2;

		private int parseState;
		private String uBytes;
		private StringBuilder builder;
		private Stack<StateStackElement> stk;

		public StateStackString(Stack<StateStackElement> stk, String passedCluster) throws JSONParserException {
			this.parseState = STRINGSTATE_NORMAL;
			this.uBytes = null;
			if(passedCluster != null) {
				this.builder = new StringBuilder(passedCluster);
			} else {
				this.builder = new StringBuilder();
			}
			this.stk = stk;
			
			stk.push(this);
		}

		public boolean processCluster(String nextCluster) throws JSONParserException {
			switch(this.parseState) {
				case STRINGSTATE_NORMAL:
					if(nextCluster.equals("\\")) {
						this.parseState = STRINGSTATE_ESCAPED;
						return true;
					}
					if(!nextCluster.equals("\"")) {
						this.builder.append(nextCluster);
						return true;
					}

					String content = this.builder.toString();
					this.builder = null;
					stk.pop();
					stk.peek().childParsed(new JSONString(content));
					return true;
				case STRINGSTATE_ESCAPED:
					if(nextCluster.length() > 1) {
						throw new JSONParserException();
					}
					char c = nextCluster.charAt(0);
					switch(c) {
						case '\\':
							this.builder.append("\\");
							break;
						case '/':
							this.builder.append("/");
							break;
						case 'b':
							this.builder.append("\b");
							break;
						case 'f':
							this.builder.append("\f");
							break;
						case 'n':
							this.builder.append("\n");
							break;
						case 'r':
							this.builder.append("\r");
							break;
						case 't':
							this.builder.append("\t");
							break;
						case 'u':
							this.parseState = STRINGSTATE_UTF16CODEPOINT;
							this.uBytes = "";
							break;
						default:
							throw new JSONParserException();
					}
					return true;
				case STRINGSTATE_UTF16CODEPOINT:
					this.uBytes = this.uBytes + nextCluster;
					if(this.uBytes.length() == 4) {
						this.builder.append(Character.toChars(Integer.parseInt(this.uBytes)));
						this.uBytes = null;
						this.parseState = STRINGSTATE_NORMAL;
					}
					return true;
				default:
					throw new RuntimeException("Implementation error");
			}
		}

		@Override
		public void childParsed(JSONValue child) {
			throw new RuntimeException("Implementation error");
		}
	};
	private class StateStackConstant extends StateStackElement {
		public static final int CONSTANT_NULL = 0;
		public static final int CONSTANT_TRUE = 1;
		public static final int CONSTANT_FALSE = 2;

		private int matchedSymbols;
		private String matchingSymbol;
		private int matchedType;
		private Stack<StateStackElement> stk;

		public StateStackConstant(Stack<StateStackElement> stk, String passedCluster) throws JSONParserException {
			String lower = passedCluster.toLowerCase();
			
			this.matchedSymbols = 1;
			this.stk = stk;
			
			stk.push(this);

			if(lower.equals("t")) {
				this.matchedType = CONSTANT_TRUE;
				this.matchingSymbol = "true";
			} else if(lower.equals("f")) {
				this.matchedType = CONSTANT_FALSE;
				this.matchingSymbol = "false";
			} else if(lower.equals("n")) {
				this.matchedType = CONSTANT_NULL;
				this.matchingSymbol = "null";
			} else {
				throw new JSONParserException();
			}
		}

		public boolean processCluster(String nextCluster) throws JSONParserException {
			if(this.matchingSymbol.substring(this.matchedSymbols, this.matchedSymbols+1).equals(nextCluster.toLowerCase())) {
				this.matchedSymbols++;
				if(this.matchedSymbols == this.matchingSymbol.length()) {
					/*
						We pop ourself from the stack
						and attach ourself to our parent
					*/
					stk.pop();
					switch(this.matchedType) {
						case CONSTANT_TRUE:
							stk.peek().childParsed(new JSONBool(true));
							break;
						case CONSTANT_FALSE:
							stk.peek().childParsed(new JSONBool(false));
							break;
						case CONSTANT_NULL:
							stk.peek().childParsed(new JSONNull());
							break;
						default:
							throw new RuntimeException("Invalid state");
					}
				}
				return true;
			}
			throw new JSONParserException();
		}

		@Override
		public void childParsed(JSONValue child) {
			throw new RuntimeException("Implementation error");
		}
	};
	private class StateStackRoot extends StateStackElement {
		private Stack<StateStackElement> stk;
		private JSONValue lastValue;

		public StateStackRoot(Stack<StateStackElement> stk) {
			this.stk = stk;
			stk.push(this);
			this.lastValue = null;
		}

		public JSONValue getChild() {
			return this.lastValue;
		}

		@Override
		public void childParsed(JSONValue child) {
			this.lastValue = child;
		}

		@Override
		public boolean processCluster(String nextCluster) throws JSONParserException {
			if(nextCluster.length() > 1) { throw new JSONParserException(); }
			char c = nextCluster.charAt(0);
			switch(c) {
				case '{':
					new StateStackObject(stk, null);
					return true;
				case '[':
					new StateStackArray(stk, null);
					return true;
				case '"':
					new StateStackString(stk, null);
					return true;
				case 't':
				case 'T':
				case 'f':
				case 'F':
				case 'n':
				case 'N':
					new StateStackConstant(stk, null);
					return true;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case '-':
					new StateStackNumber(stk, nextCluster);
					return true;
				case 0x09:
				case 0x0A:
				case 0x0D:
				case 0x20:
					return true;
				default:
					throw new JSONParserException();
			}
		}
	}

	private Stack<StateStackElement> stk;

	public JSONParser() {
		stk = new Stack<StateStackElement>();
		new StateStackRoot(stk);
	}
	public JSONValue processCluster(String nextCluster) throws JSONParserException {
		while(!stk.peek().processCluster(nextCluster)) { }
		if(stk.peek() instanceof StateStackRoot) {
			return ((StateStackRoot)stk.peek()).getChild();
		} else {
			return null;
		}
	}


	public static JSONValue parseString(String s) throws JSONParserException {
		JSONParser jp = new JSONParser();

		for(int i = 0; i < s.length(); i++) {
			String clus = s.substring(i, i+1);
			JSONValue res = jp.processCluster(clus);
			if(res != null) {
				return res;
			}
		}

		return null;
	}
}
