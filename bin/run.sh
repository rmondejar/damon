#!/bin/sh

JAVA_COMMAND=../../jdk1.6.0_05/bin/java
AW_HOME=..

if [ -z "$CLASSPATH" ]; then
	CLASSPATH="."
fi


AW_LIBS=$AW_HOME/lib/dom4j.jar:$AW_HOME/lib/qdox-1.4.jar:$AW_HOME/lib/concurrent-1.3.1.jar:$AW_HOME/lib/trove-1.0.2.jar:$AW_HOME/lib/jrexx-1.1.1.jar
AW_PATH=$AW_HOME/lib/aspectwerkz-jdk5-2.0.jar
AW_BOOTPATH=$AW_HOME/lib/aspectwerkz-extensions-2.0.jar:$AW_HOME/lib/aspectwerkz-core-2.0.jar:$AW_HOME/lib/aspectwerkz-2.0.jar:$AW_HOME/lib/aspectwerkz-jdk5-2.0.jar:$AW_LIBS:$AW_HOME/lib/piccolo-1.03.jar

DAMON_LIB=$AW_HOME/lib/damon.jar:$AW_HOME/lib/easypastry.jar:$AW_HOME/lib/log4j.jar:$AW_HOME/lib/asm-all-3.1.jar:$AW_HOME/lib/jug.jar:$AW_HOME/lib/pastry.jar:$AW_HOME/lib/bunshin.jar:$AW_HOME/lib/junit.jar:$AW_HOME/lib/jdom.jar:$AW_HOME/lib/xstream.jar:$AW_HOME/lib/sbbi-upnplib-1.0.4.jar

$JAVA_COMMAND -javaagent:$AW_PATH -Xbootclasspath/p:"$AW_BOOTPATH" -Daspectwerkz.home="$AW_HOME" -cp $DAMON_LIB: "$@"
