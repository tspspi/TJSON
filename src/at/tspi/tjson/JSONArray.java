package at.tspi.tjson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JSONArray implements JSONValue, Iterable<JSONValue> {
	private List<JSONValue> members;
	
	public JSONArray() { this.members = new ArrayList<JSONValue>(); }

	public JSONArray(JSONArray other) {
		this.members = new ArrayList<JSONValue>();
		for(JSONValue v : other.members) {
			this.members.add(v.clone());
		}
	}

	public int size() { return this.members.size(); }
	public JSONValue get(int idx) {
		if((idx < 0) || (idx >= members.size())) {
			throw new IndexOutOfBoundsException();
		}
		return members.get(idx);
	}
	public JSONArray set(int idx, JSONValue v) {
		if((idx < 0) || (idx > members.size())) {
			throw new IndexOutOfBoundsException();
		}
		members.set(idx, v);
		return this;
	}
	
	public JSONArray push(JSONValue v) {
		this.members.add(v);
		return this;
	}
	public JSONValue pop() {
		if(this.members.size() == 0) {
			return null;
		}
		return this.members.remove(this.members.size()-1);
	}
	

	@Override
	public JSONValue clone() { return new JSONArray(this); }

	@Override
	public String toString() {
		String res = null;
		for(JSONValue v : this.members) {
			if(res == null) {
				res = "[";
			} else {
				res = res + ",";
			}
			res = res + v.toString();
		}
		return res + "]";
	}

	@Override
	public Iterator<JSONValue> iterator() {
		return this.members.iterator();
	}
}
