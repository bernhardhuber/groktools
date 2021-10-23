#!/bin/sh

PATTERN=%{WILDFLY_SERVERLOG_2}

env -v MSYS_NO_PATHCONV=1 \
	java -jar target/groktools-1.0-SNAPSHOT-grokmain.jar \
	--pattern-definitions-classpath=/groktoolspatterns/server_log \
        --pattern=$PATTERN \
	--file=server.log 

