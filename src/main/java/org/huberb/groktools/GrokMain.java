/*
 * Copyright 2021 berni3.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.huberb.groktools;

import io.krakens.grok.api.Grok;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.huberb.groktools.GrokIt.GrokMatchResult;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *
 * @author berni3
 */
@Command(name = "grokMain",
        mixinStandardHelpOptions = true,
        version = "grokMain 1.0-SNAPSHOT",
        description = "parse unstructured  files")
public class GrokMain implements Callable<Integer> {

    @Option(names = {"-f", "--file"},
            description = "read from log file, if not specified read log from stdin")
    private File logFile;
    @Option(names = {"--max-read-lines"},
            description = "maximum number of read lines")
    private int maxReadLineCount = -1;
    @Option(names = {"-p", "--pattern"},
            description = "grok pattern")
    private String pattern;

    @Option(names = {"--patterndefinitions-file"},
            description = "read pattern definition from a file")
    private File patterndefinitionsFile;
    @Option(names = {"--patterndefinitions-classpath"},
            description = "read pattern definition from classpath")
    private String patterndefinitionsClasspath;

    private String patterndefinitions;

    @Option(names = {"--show-default-patterns"},
            description = "show grok default patterns")
    private boolean showDefaultPatterns;

    @Option(names = {"--output-matchresult-as-csv"},
            description = "output match results as csv")
    private boolean outputMatchResultAsCsv;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GrokMain()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        final GrokBuilder grokBuilder = new GrokBuilder()
                .namedOnly(true);
        if (pattern != null) {
            grokBuilder.pattern(pattern);
        } else {
            // Hack: GrokCompiler wants a pattern anyway
            grokBuilder.pattern("%{SPACE:UNWANTED}");
        }
        if (patterndefinitionsClasspath != null) {
            System_out_format("register pattern definitions from classpath: %s%n", patterndefinitionsClasspath);
            grokBuilder.patternDefinitionsFromClasspath(patterndefinitionsClasspath);
        }
        if (patterndefinitionsFile != null) {
            System_out_format("register pattern definitions from file: %s%n", patterndefinitionsFile);
            grokBuilder.patternDefinitionsFromFile(patterndefinitionsFile);

        }
        final Grok grok = grokBuilder.build();

        if (showDefaultPatterns) {
            executeShowPatterndefinitions(grok);
        } else {
            executeMatching(grok);
        }
        return 0;
    }

    //
    void executeShowPatterndefinitions(Grok grok
    ) {
        final GrokIt grokIt = new GrokIt();
        final Map<String, String> defaultPatterns = grokIt.retrievePatterndefinitions(grok);
        System_out_format("grok pattern definitions:%n");
        defaultPatterns.entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .forEach((e) -> {
                    System_out_format("%s: %s%n", e.getKey(), e.getValue());
                });
    }

    void executeMatching(Grok grok) throws IOException {
        final GrokIt grokIt = new GrokIt();
        try (final Reader logReader = new ReaderFactory(logFile).createUtf8Reader();
                final BufferedReader br = new BufferedReader(logReader)) {
            int readLineCount = 0;
            for (String line; (line = br.readLine()) != null;) {
                readLineCount += 1;
                if (maxReadLineCount >= 0 && readLineCount > maxReadLineCount) {
                    break;
                }
                final GrokMatchResult grokResult = grokIt.match(grok, line);
                executePostMatching(readLineCount, grokResult);
            }
        }
    }

    void executePostMatching(int readLineCount, GrokMatchResult grokResult
    ) {
        boolean skip = false;
        skip = skip || grokResult == null;
        skip = skip || (grokResult.start == 0 && grokResult.end == 0);
        skip = skip || grokResult.m == null;
        skip = skip || grokResult.m.isEmpty();
        if (skip) {
            return;
        }
        if (outputMatchResultAsCsv) {
            new OutputGrokResult()
                    .outputGrokResultAsCsv(readLineCount, grokResult);
        } else {
            new OutputGrokResult()
                    .outputGrokResultAsIs(readLineCount, grokResult);
        }
    }

    static class OutputGrokResult {

        void outputGrokResultAsIs(int readLineCount, GrokMatchResult grokResult) {
            System_out_format("%d %s%n", readLineCount, grokResult);
        }

        void outputGrokResultAsCsv(int readLineCount, GrokMatchResult grokResult) {
            final List<String> keysSortedList = grokResult.m.keySet().stream()
                    .sorted()
                    .collect(Collectors.toList());
            if (readLineCount == 1) {
                final StringBuilder sb = new StringBuilder();
                int cols = 0;
                for (String k : keysSortedList) {
                    final Object o = k;
                    final String v = convertObjectToString(o);
                    if (cols > 0) {
                        sb.append(",");
                    }
                    sb.append(String.format("\"%s\"", v));
                    cols += 1;
                }
                System_out_format("%s%n", sb.toString());
            }
            {
                final StringBuilder sb = new StringBuilder();
                int cols = 0;
                for (String k : keysSortedList) {
                    final Object o = grokResult.m.getOrDefault(k, "");
                    final String v = convertObjectToString(o);
                    if (cols > 0) {
                        sb.append(",");
                    }
                    sb.append(String.format("\"%s\"", v));
                    cols += 1;
                }
                System_out_format("%s%n", sb.toString());
            }
        }

        String convertObjectToString(Object o) {
            final String v;
            if (o == null) {
                v = "";
            } else if (o instanceof String) {
                v = (String) o;
            } else {
                v = String.valueOf(o);
            }
            return v;
        }

        void System_out_format(String format, Object... args) {
            System.out.format(format, args);
        }
    }

    /**
     * Factory for creating a {@link Reader}.
     *
     * <p>
     * Use this reader for reading TGF data.
     */
    static class ReaderFactory {

        private final File f;

        public ReaderFactory(File f) {
            this.f = f;
        }

        Reader createUtf8Reader() throws FileNotFoundException {
            final Reader r;
            if (f != null) {
                final FileInputStream fis = new FileInputStream(this.f);
                final InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                r = isr;
            } else {
                final InputStream is = new java.io.BufferedInputStream(System.in);
                final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                r = isr;
            }
            return r;
        }
    }

    void System_out_format(String format, Object... args) {
        System.out.format(format, args);
    }
}
