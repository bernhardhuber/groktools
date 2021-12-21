#! /bin/sh

#set -x

#-----------------------------------------------------------------------------
# Run grok on 
# * wildfly log
# * activemq log
# * logstash log
# * flume log
#
#-----------------------------------------------------------------------------

#-----------------------------------------------------------------------------
BASEDIR=$(dirname $0)
VERSION=0.3.0-SNAPSHOT

#-----------------------------------------------------------------------------
JAVA_OPTIONS=""
#JAVA_OPTIONS="-agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=y "

#JAVA_COMMAND=${BASEDIR}/target/groktools-${VERSION}-executable 
JAVA_COMMAND="${JAVA_HOME}/bin/java \
  ${JAVA_OPTIONS} \
  -jar ${BASEDIR}/target/groktools-${VERSION}-grokmain.jar \
  "

#-----------------------------------------------------------------------------
READ_MAX_LINES_COUNT=10
#READ_MAX_LINES_COUNT=100
#READ_MAX_LINES_COUNT=600

#-----------------------------------------------------------------------------
#OUTPUT_MATCHRESULT="--output-matchresult=asIs"
OUTPUT_MATCHRESULT="--output-matchresult=asCsv"
#OUTPUT_MATCHRESULT="--output-matchresult=asJson"

#-----------------------------------------------------------------------------
#MATCHING_LINE_MODE="--matching-line-mode=singleLineMode"
MATCHING_LINE_MODE="--matching-line-mode=multiLinesMode"

#-----------------------------------------------------------------------------
# log-files

LOG_FILES_BASEDIR=${BASEDIR}/src/main/resources/examples
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
# ${WILDFLY_SERVERLOG_FILE}

-EOF-

    GROK_OPTIONS="\
 --pattern-definitions-classpath=//groktoolspatterns/server_log \
 --read-max-lines-count=${READ_MAX_LINES_COUNT} \
 ${OUTPUT_MATCHRESULT} \
 --match-pattern=%{WILDFLY_SERVERLOG} \
 --file=${WILDFLY_SERVERLOG_FILE} \
 ${MATCHING_LINE_MODE} "

    $JAVA_COMMAND ${GROK_OPTIONS} \
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
# ${ACTIVEMQ_ACTIVEMQLOG_FILE}

-EOF-

    GROK_OPTIONS="\
 --pattern-definitions-classpath=//groktoolspatterns/server_log \
 --read-max-lines-count=${READ_MAX_LINES_COUNT} \
 ${OUTPUT_MATCHRESULT} \
 --match-pattern=%{ACTIVEMQ_ACTIVEMQLOG} \
 --file=${ACTIVEMQ_ACTIVEMQLOG_FILE} \
 ${MATCHING_LINE_MODE} "

    $JAVA_COMMAND ${GROK_OPTIONS} 2>&1 
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
# ${ELKSTACK_LOGSTASHLOG}

-EOF-

    GROK_OPTIONS="\
 --pattern-definitions-classpath=//groktoolspatterns/server_log \
 --read-max-lines-count=${READ_MAX_LINES_COUNT} \
 ${OUTPUT_MATCHRESULT} \
 --match-pattern=%{ELKSTACK_LOGSTASHLOG} \
 --file=${ELKSTACK_LOGSTASHLOG} \
 ${MATCHING_LINE_MODE} "

    $JAVA_COMMAND ${GROK_OPTIONS} 2>&1 
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
# ${FLUME_FLUMELOG}

-EOF-

    GROK_OPTIONS="\
 --pattern-definitions-classpath=//groktoolspatterns/server_log \
 --read-max-lines-count=${READ_MAX_LINES_COUNT} \
 ${OUTPUT_MATCHRESULT} \
 --match-pattern=%{FLUME_FLUMELOG} \
 --file=${FLUME_FLUMELOG} \
 ${MATCHING_LINE_MODE} "

    $JAVA_COMMAND ${GROK_OPTIONS} 2>&1 
}


#-----------------------------------------------------------------------------
#
grok_wildfly
grok_activemq
grok_elkstack
grok_flume

