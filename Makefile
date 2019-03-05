SRCS=src/at/tspi/tjson/JSONArray.java \
	src/at/tspi/tjson/JSONBool.java \
	src/at/tspi/tjson/JSONNull.java \
	src/at/tspi/tjson/JSONNumber.java \
	src/at/tspi/tjson/JSONObject.java \
	src/at/tspi/tjson/JSONParser.java \
	src/at/tspi/tjson/JSONParserException.java \
	src/at/tspi/tjson/JSONSerialize.java \
	src/at/tspi/tjson/JSONSerializeException.java \
	src/at/tspi/tjson/JSONString.java \
	src/at/tspi/tjson/JSONValue.java \
	src/at/tspi/tjson/annotations/JSONSerializeObject.java \
	src/at/tspi/tjson/annotations/JSONSerializeValue.java

all: dirs classes jar

dirs:

	-@mkdir -p bin
	-@mkdir -p classes/at/tspi/tjson/annotations

classes:

	javac -d classes/ $(SRCS)

jar:

	jar -cvf bin/TJSON.jar -C ./classes .

.PHONY: dirs classes jar