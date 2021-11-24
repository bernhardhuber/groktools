#! /bin/sh

#-----------------------------------------------------------------------------
# Run grok on 
# * wildfly log
# * activemq log
# * logstash log
#
#-----------------------------------------------------------------------------

#-----------------------------------------------------------------------------
CMD=./target/groktools-1.0-SNAPSHOT-executable 
READ_MAX_LINES_COUNT=10
OUTPUT_MODE=--output-matchresult-as-csv
#OUTPUT_MODE=--output-matchresult-as-json

#-----------------------------------------------------------------------------
# log-files
WILDFLY_SERVERLOG_FILE=./server.log
ACTIVEMQ_ACTIVEMQLOG_FILE=./activemq.log
ELKSTACK_LOGSTASHLOG=logstash-plain.log

#-----------------------------------------------------------------------------
# grok wildfly server.log
function grok_wildfly () {
cat << -EOF-

# Wildfly server.log

-EOF-
$CMD --pattern-definitions-classpath=//groktoolspatterns/server_log \
  --read-max-lines-count=${READ_MAX_LINES_COUNT} \
  ${OUTPUT_MODE} \
  --pattern=%{WILDFLY_SERVERLOG_2} \
  --file=${WILDFLY_SERVERLOG_FILE} \
  2>&1 
}

#-----------------------------------------------------------------------------
# grok activemq activemq.log
function grok_activemq () {
cat << -EOF-

# Activemq activemq.log

-EOF-
$CMD --pattern-definitions-classpath=//groktoolspatterns/server_log \
  --read-max-lines-count=${READ_MAX_LINES_COUNT} \
  ${OUTPUT_MODE} \
  --pattern=%{ACTIVEMQ_ACTIVEMQLOG_2} \
  --file=${ACTIVEMQ_ACTIVEMQLOG_FILE} \
  2>&1 
}

#-----------------------------------------------------------------------------
# grok logstash logstash-plain.log
function grok_elkstack () {
cat << -EOF-

# Logstash logstash-plain.log

-EOF-
$CMD --pattern-definitions-classpath=//groktoolspatterns/server_log \
  --read-max-lines-count=${READ_MAX_LINES_COUNT} \
  ${OUTPUT_MODE} \
  --pattern=%{ELKSTACK_LOGSTASHLOG} \
  --file=${ELKSTACK_LOGSTASHLOG} \
  2>&1 
}

#-----------------------------------------------------------------------------
#
grok_wildfly
grok_activemq
grok_elkstack

