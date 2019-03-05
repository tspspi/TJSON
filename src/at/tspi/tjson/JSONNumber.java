package at.tspi.tjson;

public class JSONNumber implements JSONValue {
	Number n;

	public JSONNumber() {
		this.n = new Integer(0);
	}
	public JSONNumber(JSONNumber o) {
		if(o.n instanceof Integer) {
			this.n = new Integer(o.n.intValue());
		} else if(o.n instanceof Long) {
			this.n = new Long(o.n.longValue());
		} else if(o.n instanceof Double) {
			this.n = new Double(o.n.doubleValue());
		} else if(o.n instanceof Float) {
			this.n = new Float(o.n.floatValue());
		}
	}
	public JSONNumber(Number n) {
		if(n instanceof Integer) {
			this.n = new Integer(n.intValue());
		} else if(n instanceof Long) {
			this.n = new Long(n.longValue());
		} else if(n instanceof Double) {
			this.n = new Double(n.doubleValue());
		} else if(n instanceof Float) {
			this.n = new Float(n.floatValue());
		}
	}
	public JSONValue clone() { return new JSONNumber(this); }

	public JSONNumber(int v) 	{ this.n = new Integer(v); 	}
	public JSONNumber(long v) 	{ this.n = new Long(v); 	}
	public JSONNumber(double v) { this.n = new Double(v); 	}
	public JSONNumber(float v) 	{ this.n = new Float(v); 	}

	public JSONNumber set(int v) 	{ this.n = new Integer(v); return this; 	}
	public JSONNumber set(long v) 	{ this.n = new Long(v); return this; 		}
	public JSONNumber set(double v) { this.n = new Double(v); return this; 		}
	public JSONNumber set(float v) 	{ this.n = new Float(v); return this; 		}

	public Number get() { return this.n; }
	public int getInt() { return this.n.intValue(); }
	public long getLong() { return this.n.longValue(); }
	public double getDouble() { return this.n.doubleValue(); }
	public float getFloat() { return this.n.floatValue(); }

	public String toString() {
		return this.n.toString();
	}
}
