package at.tspi.tjson;

public class JSONString implements JSONValue {
	String value;
	
	public JSONString() { this.value = null; }
	public JSONString(String value) { this.value = value; }
	public JSONString(JSONString other) { this.value = other.value; }

	public String get() { return this.value; }
	public JSONString set(String value) { this.value = value; return this; }

	@Override
	public JSONValue clone() { return new JSONString(this); }

	@Override
	public String toString() { return '"' + this.value + '"'; }
}
