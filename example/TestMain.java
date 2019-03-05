import java.util.ArrayList;
import java.util.List;

import at.tspi.tjson.JSONParser;
import at.tspi.tjson.JSONParserException;
import at.tspi.tjson.JSONSerialize;
import at.tspi.tjson.JSONValue;
import at.tspi.tjson.annotations.JSONSerializeObject;
import at.tspi.tjson.annotations.JSONSerializeValue;

public class TestMain {
	private static final String jsonTest1 = "{"
			+ " \"abc\" : \"def\","
			+ " \"geh\" : \"ijk\","
			+ " \"lmn\" : 123,"
			+ " \"opq\" : 123.234,"
			+ " \"a\" : [ 12, 34, \"ab\", \"cd\" ]"
			+ "}";
	private static final String jsonTest2 = "["
			+ " \"abc\", "
			+ " 234, "
			+ " 12.34, "
			+ " -12.34e-12, "
			+ " -12.34e12, "
			+ " 12.34e-12 "
			+ "]";

	@JSONSerializeObject
	private class SerTest1 {
		@JSONSerializeValue
		String tst1 = "abc";
		@JSONSerializeValue
		String tst2 = "def";
		
		@JSONSerializeValue(name = "publicTst1")
		public String tst3 = "abcPUBLIC";
		@JSONSerializeValue(name = "publicTst2")
		public String tst4 = "defPUBLIC";
		
		@JSONSerializeValue
		int tst5 = 123;
		@JSONSerializeValue
		double tst6 = 12.34;

		@JSONSerializeValue
		SerTest2 innerObject = new SerTest2();
	}
	@JSONSerializeObject
	private class SerTest2Base {
		@JSONSerializeValue
		private double inheritedValue = 98.76e-10;

		@JSONSerializeValue
		private int[] ar1 = { 1, 2, 3 };
		
		@JSONSerializeValue
		private List<String> testList;

		public SerTest2Base() {
			testList = new ArrayList<String>();
			testList.add("test1");
			testList.add("test2");
			testList.add("test3");
		}
	}
	@JSONSerializeObject
	private class SerTest2 extends SerTest2Base {
		@JSONSerializeValue
		private String innerTest = "ab";
		@JSONSerializeValue
		private String nullTest = null;
		@JSONSerializeValue
		private boolean bTrue = true;
		@JSONSerializeValue
		private boolean bFalse = false;
	}
	public static void main(String args[]) {
		TestMain tstMain = new TestMain();
		tstMain.objMain(args);
	}
	private void objMain(String args[]) {
		try {
			JSONValue v1 = JSONParser.parseString(jsonTest1);
			JSONValue v2 = JSONParser.parseString(jsonTest2);
			System.out.println(v1);
			System.out.println(v2);
			
			
			System.out.println(JSONSerialize.toJSONString(v1));
			System.out.println(JSONSerialize.toJSONString(v2));
			
			String serializeTest1 = JSONSerialize.toJSONString(new SerTest1()); 
			System.out.println(serializeTest1);
			System.out.println(JSONParser.parseString(serializeTest1));
		} catch(JSONParserException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
