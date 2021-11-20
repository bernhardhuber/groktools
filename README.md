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

## References

* GROK : https://github.com/thekrakken/java-grok
* PICOCLI : https://picocli.info/
