# groktools

[![Java CI with Maven](https://github.com/bernhardhuber/groktools/actions/workflows/maven.yml/badge.svg)](https://github.com/bernhardhuber/groktools/actions/workflows/maven.yml)

## Overview

A simple command line tool reading unstructured text.

This tool uses the java library GROK.

## Usage

```
Usage: grokMain [-hV] [--[no-]named-only] [--[no-]register-default-patterns]
                [--output-matchresult-as-csv] [--output-matchresult-as-json]
                [--show-pattern-definitions] [-f=<inputFile>] [-p=<pattern>]
                [--pattern-definition=<patternDefinition>]
                [--pattern-definitions-classpath=<patternDefinitionsClasspath>]
                [--pattern-definitions-file=<patternDefinitionsFile>]
                [--read-max-lines-count=<readMaxLinesCount>]
parse unstructured  files
  -f, --file=<inputFile>    read from file, if not specified read from stdin
  -h, --help                Show this help message and exit.
      --[no-]named-only     Provide only named matches. True by default.
      --[no-]register-default-patterns
                            Register default patterns. True by default.
      --output-matchresult-as-csv
                            output match results as csv
      --output-matchresult-as-json
                            output match results as json
  -p, --pattern=<pattern>   grok pattern
      --pattern-definition=<patternDefinition>
                            define pattern name pattern and pattern definition
      --pattern-definitions-classpath=<patternDefinitionsClasspath>
                            read pattern definition from classpath
      --pattern-definitions-file=<patternDefinitionsFile>
                            read pattern definition from a file
      --read-max-lines-count=<readMaxLinesCount>
                            read maximum number lines
      --show-pattern-definitions
                            show grok pattern definitions
  -V, --version             Print version information and exit.
```

## Example Wildfly

Parse wildfly server.log file and convert it to csv file format.

Content of server.log:

```
2019-03-04 22:30:15,465 INFO  [org.jboss.modules] (main) JBoss Modules version 1.9.0.Final
2019-03-04 22:30:16,324 INFO  [org.jboss.msc] (main) JBoss MSC version 1.4.5.Final
2019-03-04 22:30:16,340 INFO  [org.jboss.threads] (main) JBoss Threads version 2.3.3.Final
2019-03-04 22:30:16,563 INFO  [org.jboss.as] (MSC service thread 1-2) WFLYSRV0049: WildFly Full 16.0.0.Final (WildFly Core 8.0.0.Final) starting
```

Launching grooktools:

```
java --pattern-definitions-classpath=//groktoolspatterns/server_log \
  --read-max-lines-count=5 \
  --output-matchresult-as-csv \
  --pattern=%{WILDFLY_SERVERLOG_2} \
  --file=server.log \
```

Generated CSV:

```
"lineno","category","level","message","thread","timestampIso8601"
"1","org.jboss.modules","INFO","JBoss Modules version 1.9.0.Final","main","2019-03-04 22:30:15,465"
"2","org.jboss.msc","INFO","JBoss MSC version 1.4.5.Final","main","2019-03-04 22:30:16,324"
"3","org.jboss.threads","INFO","JBoss Threads version 2.3.3.Final","main","2019-03-04 22:30:16,340"
"4","org.jboss.as","INFO","WFLYSRV0049: WildFly Full 16.0.0.Final (WildFly Core 8.0.0.Final) starting","MSC service thread 1-2","2019-03-04 22:30:16,563"
"5","org.jboss.as.config","DEBUG","Configured system properties:","MSC service thread 1-2","2019-03-04 22:30:16,563"
```

## Example Activemq 

Parse activemq.log file and convert it to csv file format.

Content of activemq.log:

```
2020-05-02 07:26:22,294 | INFO  | Refreshing org.apache.activemq.xbean.XBeanBrokerFactory$1@6b09bb57: startup date [Sat May 02 07:26:22 CEST 2020]; root of context hierarchy | org.apache.activemq.xbean.XBeanBrokerFactory$1 | main
2020-05-02 07:26:23,516 | INFO  | Using Persistence Adapter: KahaDBPersistenceAdapter[C:\Users\berni3\Downloads\apache-activemq-5.15.12-bin\apache-activemq-5.15.12\bin\..\data\kahadb] | org.apache.activemq.broker.BrokerService | main
2020-05-02 07:26:24,567 | INFO  | PListStore:[C:\Users\berni3\Downloads\apache-activemq-5.15.12-bin\apache-activemq-5.15.12\bin\..\data\localhost\tmp_storage] started | org.apache.activemq.store.kahadb.plist.PListStoreImpl | main
2020-05-02 07:26:24,817 | INFO  | Apache ActiveMQ 5.15.12 (localhost, ID:DESKTOP-GHTTUME-64231-1588397184645-0:1) is starting | org.apache.activemq.broker.BrokerService | main
2020-05-02 07:26:24,871 | INFO  | Listening for connections at: tcp://DESKTOP-GHTTUME:61616?maximumConnections=1000&wireFormat.maxFrameSize=104857600 | org.apache.activemq.transport.TransportServerThreadSupport | main
```

Launching grooktools:

```
java --pattern-definitions-classpath=//groktoolspatterns/server_log \
  --read-max-lines-count=5 \
  --output-matchresult-as-csv \
  --pattern=%{ACTIVEMQ_ACTIVEMQLOG_2} \
  --file=activemq.log \
```

Generated CSV:

```
"lineno","category","level","message","thread","timestampIso8601"
"1","org.apache.activemq.xbean.XBeanBrokerFactory$1","INFO","Refreshing org.apache.activemq.xbean.XBeanBrokerFactory$1@6b09bb57: startup date [Sat May 02 07:26:22 CEST 2020]; root of context hierarchy","main","2020-05-02 07:26:22,294"
"2","org.apache.activemq.broker.BrokerService","INFO","Using Persistence Adapter: KahaDBPersistenceAdapter[C:\Users\berni3\Downloads\apache-activemq-5.15.12-bin\apache-activemq-5.15.12\bin\..\data\kahadb]","main","2020-05-02 07:26:23,516"
"3","org.apache.activemq.store.kahadb.plist.PListStoreImpl","INFO","PListStore:[C:\Users\berni3\Downloads\apache-activemq-5.15.12-bin\apache-activemq-5.15.12\bin\..\data\localhost\tmp_storage] started","main","2020-05-02 07:26:24,567"
"4","org.apache.activemq.broker.BrokerService","INFO","Apache ActiveMQ 5.15.12 (localhost, ID:DESKTOP-GHTTUME-64231-1588397184645-0:1) is starting","main","2020-05-02 07:26:24,817"
"5","org.apache.activemq.transport.TransportServerThreadSupport","INFO","Listening for connections at: tcp://DESKTOP-GHTTUME:61616?maximumConnections=1000&wireFormat.maxFrameSize=104857600","main","2020-05-02 07:26:24,871"
```

## Example Logstash 

Parse logstash-plain.log file and convert it to csv file format.

Content of logstash-plain.log:

```
[2020-01-30T22:26:57,775][INFO ][logstash.modules.scaffold] Initializing module {:module_name=>"fb_apache", :directory=>"D:/projects/elkstack/logstash-5.6.4/modules/fb_apache/configuration"}
[2020-01-30T22:26:57,822][INFO ][logstash.modules.scaffold] Initializing module {:module_name=>"netflow", :directory=>"D:/projects/elkstack/logstash-5.6.4/modules/netflow/configuration"}
[2020-01-30T22:27:00,256][INFO ][logstash.pipeline        ] Starting pipeline {"id"=>"main", "pipeline.workers"=>4, "pipeline.batch.size"=>125, "pipeline.batch.delay"=>5, "pipeline.max_inflight"=>500}
[2020-01-30T22:27:00,991][ERROR][logstash.pipeline        ] Error registering plugin {:plugin=>"<LogStash::Inputs::File path=>[\"test.log\"], start_position=>\"beginning\", codec=><LogStash::Codecs::Multiline pattern=>\"^%{TIMESTAMP_ISO8601}\", negate=>true, what=>\"previous\", id=>\"5214ce8cd6b5e57d516944cb1e2b93fb03be178c-1\", enable_metric=>true, charset=>\"UTF-8\", multiline_tag=>\"multiline\", max_lines=>500, max_bytes=>10485760>, id=>\"5214ce8cd6b5e57d516944cb1e2b93fb03be178c-2\", enable_metric=>true, stat_interval=>1, discover_interval=>15, sincedb_write_interval=>15, delimiter=>\"\\n\", close_older=>3600>", :error=>"File paths must be absolute, relative path specified: test.log"}
[2020-01-30T22:27:01,116][ERROR][logstash.agent           ] Pipeline aborted due to error {:exception=>#<ArgumentError: File paths must be absolute, relative path specified: test.log>, :backtrace=>["D:/projects/elkstack/logstash-5.6.4/vendor/bundle/jruby/1.9/gems/logstash-input-file-4.0.3/lib/logstash/inputs/file.rb:187:in `register'", "org/jruby/RubyArray.java:1613:in `each'", "D:/projects/elkstack/logstash-5.6.4/vendor/bundle/jruby/1.9/gems/logstash-input-file-4.0.3/lib/logstash/inputs/file.rb:185:in `register'", "D:/projects/elkstack/logstash-5.6.4/logstash-core/lib/logstash/pipeline.rb:290:in `register_plugin'", "D:/projects/elkstack/logstash-5.6.4/logstash-core/lib/logstash/pipeline.rb:301:in `register_plugins'", "org/jruby/RubyArray.java:1613:in `each'", "D:/projects/elkstack/logstash-5.6.4/logstash-core/lib/logstash/pipeline.rb:301:in `register_plugins'", "D:/projects/elkstack/logstash-5.6.4/logstash-core/lib/logstash/pipeline.rb:456:in `start_inputs'", "D:/projects/elkstack/logstash-5.6.4/logstash-core/lib/logstash/pipeline.rb:348:in `start_workers'", "D:/projects/elkstack/logstash-5.6.4/logstash-core/lib/logstash/pipeline.rb:235:in `run'", "D:/projects/elkstack/logstash-5.6.4/logstash-core/lib/logstash/agent.rb:408:in `start_pipeline'"]}
```

Launching grooktools:

```
java --pattern-definitions-classpath=//groktoolspatterns/server_log \
  --read-max-lines-count=5 \
  --output-matchresult-as-csv \
  --pattern=%{ELKSTACK_LOGSTASHLOG} \
  --file=logstash-plain.log \
```

Generated CSV:

```
"lineno","category","level","message","timestampIso8601"
"1","logstash.modules.scaffold","INFO","Initializing module {:module_name=>""fb_apache"", :directory=>""D:/projects/elkstack/logstash-5.6.4/modules/fb_apache/configuration""}","2020-01-30T22:26:57,775"
"2","logstash.modules.scaffold","INFO","Initializing module {:module_name=>""netflow"", :directory=>""D:/projects/elkstack/logstash-5.6.4/modules/netflow/configuration""}","2020-01-30T22:26:57,822"
"3","logstash.pipeline","INFO","Starting pipeline {""id""=>""main"", ""pipeline.workers""=>4, ""pipeline.batch.size""=>125, ""pipeline.batch.delay""=>5, ""pipeline.max_inflight""=>500}","2020-01-30T22:27:00,256"
"4","logstash.pipeline","ERROR","Error registering plugin {:plugin=>""<LogStash::Inputs::File path=>[\""test.log\""], start_position=>\""beginning\"", codec=><LogStash::Codecs::Multiline pattern=>\""^%{TIMESTAMP_ISO8601}\"", negate=>true, what=>\""previous\"", id=>\""5214ce8cd6b5e57d516944cb1e2b93fb03be178c-1\"", enable_metric=>true, charset=>\""UTF-8\"", multiline_tag=>\""multiline\"", max_lines=>500, max_bytes=>10485760>, id=>\""5214ce8cd6b5e57d516944cb1e2b93fb03be178c-2\"", enable_metric=>true, stat_interval=>1, discover_interval=>15, sincedb_write_interval=>15, delimiter=>\""\\n\"", close_older=>3600>"", :error=>""File paths must be absolute, relative path specified: test.log""}","2020-01-30T22:27:00,991"
"5","logstash.agent","ERROR","Pipeline aborted due to error {:exception=>#<ArgumentError: File paths must be absolute, relative path specified: test.log>, :backtrace=>[""D:/projects/elkstack/logstash-5.6.4/vendor/bundle/jruby/1.9/gems/logstash-input-file-4.0.3/lib/logstash/inputs/file.rb:187:in `register'"", ""org/jruby/RubyArray.java:1613:in `each'"", ""D:/projects/elkstack/logstash-5.6.4/vendor/bundle/jruby/1.9/gems/logstash-input-file-4.0.3/lib/logstash/inputs/file.rb:185:in `register'"", ""D:/projects/elkstack/logstash-5.6.4/logstash-core/lib/logstash/pipeline.rb:290:in `register_plugin'"", ""D:/projects/elkstack/logstash-5.6.4/logstash-core/lib/logstash/pipeline.rb:301:in `register_plugins'"", ""org/jruby/RubyArray.java:1613:in `each'"", ""D:/projects/elkstack/logstash-5.6.4/logstash-core/lib/logstash/pipeline.rb:301:in `register_plugins'"", ""D:/projects/elkstack/logstash-5.6.4/logstash-core/lib/logstash/pipeline.rb:456:in `start_inputs'"", ""D:/projects/elkstack/logstash-5.6.4/logstash-core/lib/logstash/pipeline.rb:348:in `start_workers'"", ""D:/projects/elkstack/logstash-5.6.4/logstash-core/lib/logstash/pipeline.rb:235:in `run'"", ""D:/projects/elkstack/logstash-5.6.4/logstash-core/lib/logstash/agent.rb:408:in `start_pipeline'""]}","2020-01-30T22:27:01,116"
```

## References

* GROK : https://github.com/thekrakken/java-grok
* PICOCLI : https://picocli.info/
