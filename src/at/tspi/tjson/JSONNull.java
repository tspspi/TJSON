package at.tspi.tjson;

public class JSONNull implements JSONValue {
	public JSONNull() { }
	public JSONValue clone() { return new JSONNull(); }

	public String toString() { return "null"; }
}
