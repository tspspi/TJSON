package at.tspi.tjson;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import at.tspi.tjson.annotations.JSONSerializeObject;
import at.tspi.tjson.annotations.JSONSerializeValue;

public class JSONSerialize {
	private static String serializeJSONValue(JSONValue v) throws JSONSerializeException {
		if(v instanceof JSONArray) {
			StringBuilder builder = new StringBuilder();
			boolean bFirst = true;

			builder.append("[");
			for(JSONValue val : (JSONArray)v) {
				if(bFirst) {
					bFirst = false;
				} else {
					builder.append(",");
				}
				builder.append(serializeJSONValue(val));
			}
			builder.append("]");
			return builder.toString();
		} else if(v instanceof JSONBool) {
			return ((JSONBool)v).get() ? "true" : "false";
		} else if(v instanceof JSONNull) {
			return "null";
		} else if(v instanceof JSONObject) {
			StringBuilder builder = new StringBuilder();
			boolean bFirst = true;
			builder.append("{");

			for(Map.Entry<String, List<JSONValue>> mapEnt : (JSONObject)v) {
				String currentKey = mapEnt.getKey();
				if(mapEnt.getValue() != null) {
					for(JSONValue val : mapEnt.getValue()) {
						if(bFirst) {
							bFirst = false;
						} else {
							builder.append(",");
						}

						builder.append("\"" + currentKey + "\":");
						builder.append(serializeJSONValue(val));
					}
				}
			}
			builder.append("}");
			return builder.toString();
		} else if(v instanceof JSONString) {
			return
				"\""
				+ ((JSONString)v).get().replace("\\", "\\\\").replace("\"", "\\\"")
				+ "\"";
		} else if(v instanceof JSONNumber) {
			return ((JSONNumber)v).toString();
		} else {
			throw new JSONSerializeException("Unknown element type "+v.getClass().getCanonicalName());
		}
	}

	public static String toJSONString(JSONValue v) throws JSONSerializeException {
		return serializeJSONValue(v);
	}

	


	@SuppressWarnings("rawtypes")
	private static void seralizeFieldValue(Object fieldValue, StringBuilder strBuilder) throws JSONSerializeException, IllegalAccessException {
		if(fieldValue == null) {
			strBuilder.append("null");
		} else if(fieldValue.getClass().isArray()) {
			int arLen = Array.getLength(fieldValue);
			strBuilder.append("[");
			boolean bFirst = true;
			for(int i = 0; i < arLen; i++) {
				if(bFirst) { bFirst = false; } else { strBuilder.append(","); }
				seralizeFieldValue(Array.get(fieldValue, i), strBuilder);
			}
			strBuilder.append("]");
		} else if(fieldValue instanceof List) {
			strBuilder.append("[");
			boolean bFirst = true;
			for(Object listMember : ((List)fieldValue)) {
				if(bFirst) { bFirst = false; } else { strBuilder.append(","); }
				seralizeFieldValue(listMember, strBuilder);
			}
			strBuilder.append("]");
		} else if(fieldValue instanceof String) {
			strBuilder.append("\""
					+ ((String)fieldValue).replace("\\", "\\\\").replace("\"", "\\\"")
					+ "\"");
		} else if(fieldValue instanceof Number) {
			strBuilder.append(fieldValue.toString());
		} else if(fieldValue instanceof Boolean) {
			strBuilder.append(((Boolean)fieldValue).booleanValue() ? "true" : "false");
		} else if(fieldValue instanceof Object) {
			strBuilder.append(toJSONString(fieldValue));
		} else {
			throw new JSONSerializeException("Unknown type: "+fieldValue.getClass().getCanonicalName());
		}
		return;
	}

	public static String toJSONString(Object o) throws JSONSerializeException, IllegalAccessException {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("{");
		/*
			First we check if the object we should serialize is
			marked as JSONSerializeObject
		 */
		JSONSerializeObject classAnnotation = o.getClass().getAnnotation(JSONSerializeObject.class);
		if(classAnnotation == null) {
			throw new JSONSerializeException("All serialized classes have to be JSONSerializeObject annotated"); 
		}

		/*
			The fetch all fields declared in this class
		 */
		boolean bFirst = true;
		
		@SuppressWarnings("rawtypes")
		Class objClass = o.getClass();
		while(objClass != null) {
			Field[] memberFields = objClass.getDeclaredFields();
			for(Field currentField : memberFields) {
				JSONSerializeValue fieldAnnotation = currentField.getAnnotation(JSONSerializeValue.class);
				if(fieldAnnotation == null) {
					continue; // Ignore fields missing the annotation
				}
	
				String fieldSerializedName = currentField.getName();
				if(!fieldAnnotation.name().equals("")) {
					fieldSerializedName = fieldAnnotation.name();
				}
				if(bFirst) { bFirst = false; } else { strBuilder.append(","); }
	
				strBuilder.append("\""
						+ fieldSerializedName.replace("\\", "\\\\").replace("\"", "\\\"")
						+ "\":");
	
				boolean wasAccessible = currentField.isAccessible();
				currentField.setAccessible(true);
				Object fieldValue = currentField.get(o);
				currentField.setAccessible(wasAccessible);

				seralizeFieldValue(fieldValue, strBuilder);
			}
			objClass = objClass.getSuperclass();
		}

		strBuilder.append("}");
		return strBuilder.toString();
	}
}
