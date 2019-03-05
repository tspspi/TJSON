package at.tspi.tjson;

public class JSONBool implements JSONValue {
	boolean value;

	public JSONBool() { this.value = false; }
	public JSONBool(boolean value) { this.value = value; }
	public JSONBool(JSONBool other) { this.value = other.value; }
	
	public boolean get() { return this.value; }
	public JSONBool set(boolean value) { this.value = value; return this; }

	@Override
	public String toString() { return value ? "true" : "false"; }
	
	@Override
	public JSONValue clone() { return new JSONBool(this); }
}
