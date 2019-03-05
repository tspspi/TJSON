package at.tspi.tjson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JSONObject implements JSONValue, Iterable<Map.Entry<String, List<JSONValue>>>{
	private HashMap<String, List<JSONValue>> hmMembers;
	
	public JSONObject() {
		this.hmMembers = new HashMap<String, List<JSONValue>>();
	}
	public JSONObject(JSONObject other) {
		this.hmMembers = new HashMap<String, List<JSONValue>>();
		for(Map.Entry<String, List<JSONValue>> lst : other.hmMembers.entrySet()) {
			String key = lst.getKey();
			List<JSONValue> lstOriginal = lst.getValue();
			List<JSONValue> lstNew = new ArrayList<JSONValue>();
			this.hmMembers.put(key, lstNew);

			for(JSONValue v : lstOriginal) {
				lstNew.add(v.clone());
			}
		}
	}
	
	@Override
	public JSONValue clone() { return new JSONObject(this); }

	public JSONObject put(String key, JSONValue v) {
		List<JSONValue> lstNew = new ArrayList<JSONValue>();
		lstNew.add(v);
		hmMembers.put(key, lstNew);
		return this;
	}
	public JSONValue get(String key) {
		List<JSONValue> lst = hmMembers.get(key);
		if(lst == null) {
			return null;
		}

		if(lst.size() < 1) {
			return null;
		} else {
			return lst.get(0); // Always return the FIRST element on single object getter
		}
	}
	public int size(String key) {
		List<JSONValue> lst = hmMembers.get(key);
		if(lst == null) {
			return 0;
		}
		return lst.size();
	}
	public JSONObject add(String key, JSONValue v) {
		List<JSONValue> lst = hmMembers.get(key);
		if(lst == null) {
			lst = new ArrayList<JSONValue>();
			hmMembers.put(key, lst);
		}
		lst.add(v);
		return this;
	}
	public List<JSONValue> getAll(String key) {
		return hmMembers.get(key);
	}
	@Override
	public Iterator<Entry<String, List<JSONValue>>> iterator() {
		return hmMembers.entrySet().iterator();
	}
	
	public String toString() {
		String res = "{";
		boolean bFirst = true;
		for(Map.Entry<String, List<JSONValue>> ent : this.hmMembers.entrySet()) {
			if(ent.getValue() == null) {
				continue;
			}
			for(JSONValue v : ent.getValue()) {
				if(bFirst) {
					bFirst = false;
				} else {
					res = res + ",";
				}

				res = res + "\""+ent.getKey()+"\":" + v; 
			}
		}
		return res + "}";
	}
}
