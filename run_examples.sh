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
#OUTPUT_MODE=--output-matchresult-as-csv
OUTPUT_MODE=--output-matchresult-as-json

#-----------------------------------------------------------------------------
# log-files
LOG_FILES_BASEDIR=./src/main/resources/examples
WILDFLY_SERVERLOG_FILE=${LOG_FILES_BASEDIR}/server.log
ACTIVEMQ_ACTIVEMQLOG_FILE=${LOG_FILES_BASEDIR}/activemq.log
ELKSTACK_LOGSTASHLOG=${LOG_FILES_BASEDIR}/logstash-plain.log
FLUME_FLUMELOG=${LOG_FILES_BASEDIR}/flume.log

#-----------------------------------------------------------------------------
# grok wildfly server.log
function grok_wildfly () {
    if [ ! -f ${WILDFLY_SERVERLOG_FILE} ]
    then
      echo "Missing file: ${WILDFLY_SERVERLOG_FILE}"
    fi

cat << -EOF-

# Wildfly server.log

-EOF-
$CMD --pattern-definitions-classpath=//groktoolspatterns/server_log \
  --read-max-lines-count=${READ_MAX_LINES_COUNT} \
  ${OUTPUT_MODE} \
  --pattern=%{WILDFLY_SERVERLOG} \
  --file=${WILDFLY_SERVERLOG_FILE} \
  2>&1 
}

#-----------------------------------------------------------------------------
# grok activemq activemq.log
function grok_activemq () {
    if [ ! -f ${ACTIVEMQ_ACTIVEMQLOG_FILE} ]
    then
      echo "Missing file: ${ACTIVEMQ_ACTIVEMQLOG_FILE}"
    fi

    cat << -EOF-

# Activemq activemq.log

-EOF-

    $CMD --pattern-definitions-classpath=//groktoolspatterns/server_log \
      --read-max-lines-count=${READ_MAX_LINES_COUNT} \
      ${OUTPUT_MODE} \
      --pattern=%{ACTIVEMQ_ACTIVEMQLOG} \
      --file=${ACTIVEMQ_ACTIVEMQLOG_FILE} \
      2>&1 
}

#-----------------------------------------------------------------------------
# grok logstash logstash-plain.log
function grok_elkstack () {

    if [ ! -f ${ELKSTACK_LOGSTASHLOG} ]
    then
      echo "Missing file: ${ELKSTACK_LOGSTASHLOG}"
    fi

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
# grok flume flume.log
function grok_flume () {
    if [ ! -f ${FLUME_FLUMELOG} ]
    then
      echo "Missing file: ${FLUME_FLUMELOG}"
    fi

    cat << -EOF-

# Flume flume.log

-EOF-

    $CMD --pattern-definitions-classpath=//groktoolspatterns/server_log \
      --read-max-lines-count=${READ_MAX_LINES_COUNT} \
      ${OUTPUT_MODE} \
      --pattern=%{FLUME_FLUMELOG} \
      --file=${FLUME_FLUMELOG} \
      2>&1 
}


#-----------------------------------------------------------------------------
#
grok_wildfly
grok_activemq
grok_elkstack
grok_flume

