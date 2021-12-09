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
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.huberb.groktools.GrokIt.GrokMatchResult;
import org.huberb.groktools.MatchGatherOutput.Result;
import org.huberb.groktools.MatchGatherOutput.Wrapper;
import org.huberb.groktools.OutputGrokResultConverters.IOutputGrokResultConverter;
import org.huberb.groktools.OutputGrokResultConverters.OutputGrokResultAsCsv;
import org.huberb.groktools.OutputGrokResultConverters.OutputGrokResultAsIs;
import org.huberb.groktools.OutputGrokResultConverters.OutputGrokResultAsJson;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

/**
 * Simple app for using grok library from the command line.
 *
 * @author berni3
 */
@Command(name = "grokMain",
        mixinStandardHelpOptions = true,
        showDefaultValues = true,
        version = "grokMain 1.0-SNAPSHOT",
        description = "parse unstructured  files")
public class GrokMain implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    private SystemErrOutPrinter systemErrOutPrinter;

    @Option(names = {"-f", "--file"},
            description = "read from file, if not specified read from stdin")
    private File inputFile;
    @Option(names = {"--read-max-lines-count"},
            defaultValue = "-1",
            description = "read maximum number lines")
    private int readMaxLinesCount = -1;
    @Option(names = {"-p", "--pattern"},
            description = "grok pattern")
    private String pattern;

    @Option(names = "--no-register-default-patterns",
            negatable = true,
            description = "Register default patterns. True by default.")
    private boolean registerDefaultPatterns = true;

    @Option(names = "--no-named-only",
            negatable = true,
            description = "Provide only named matches. True by default.")
    private boolean namedOnly = true;

    @Option(names = {"--pattern-definitions-file"},
            description = "read pattern definition from a file")
    private File patternDefinitionsFile;
    @Option(names = {"--pattern-definitions-classpath"},
            description = "read pattern definition from classpath")
    private String patternDefinitionsClasspath;

    @Option(names = {"--pattern-definition"},
            description = "define pattern name pattern and pattern definition")
    private String patternDefinition;

    @Option(names = {"--show-pattern-definitions"},
            description = "show grok pattern definitions")
    private boolean showPatternDefinitions;

    @Option(names = {"--matching-line-mode"},
            defaultValue = "singleLineMode",
            description = "match single line or mutli lines; valid values: \"${COMPLETION-CANDIDATES}\""
    )
    private MatchingLineMode matchingLineMode;

    @Option(names = {"--output-matchresult-as-csv"},
            description = "output match results as csv")
    private boolean outputMatchResultAsCsv;
    @Option(names = {"--output-matchresult-as-json"},
            description = "output match results as json")
    private boolean outputMatchResultAsJson;

    /**
     * Command line entry point.
     *
     * @param args
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new GrokMain()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Picocli entry point.
     *
     * @return exitCode
     * @throws Exception
     */
    @Override
    public Integer call() throws Exception {
        try {
            // use stdout, and stderr from picocli
            this.systemErrOutPrinter = new SystemErrOutPrinter(
                    this.spec.commandLine().getErr(),
                    this.spec.commandLine().getOut()
            );
            //---
            // setup Grok using a GrokBuilder
            final GrokBuilder grokBuilder = new GrokBuilder()
                    .registerDefaultPatterns(registerDefaultPatterns)
                    .namedOnly(namedOnly);
            if (pattern != null) {
                grokBuilder.pattern(pattern);
            } else {
                // Hack: GrokCompiler wants a pattern anyway
                grokBuilder.pattern(".*");
            }
            // register more pattern definitions
            if (patternDefinition != null) {
                systemErrOutPrinter.printErr(
                        String.format(
                                "register pattern name and definition: %s%n",
                                patternDefinition)
                );
                grokBuilder.patternDefinitionsFromString(patternDefinition);
            }
            if (patternDefinitionsClasspath != null) {
                systemErrOutPrinter.printErr(
                        String.format(
                                "register pattern definitions from classpath: %s%n",
                                patternDefinitionsClasspath)
                );
                grokBuilder.patternDefinitionsFromClasspath(patternDefinitionsClasspath);
            }
            if (patternDefinitionsFile != null) {
                systemErrOutPrinter.printErr(
                        String.format(
                                "register pattern definitions from file: %s%n",
                                patternDefinitionsFile)
                );
                grokBuilder.patternDefinitionsFromFile(patternDefinitionsFile);
            }
            //---
            // execute commands
            final Grok grok = grokBuilder.build();
            if (showPatternDefinitions) {
                executeShowPatterndefinitions(grok);
            } else {
                executeMatching(grok);
            }
            return 0;
        } finally {
            this.spec.commandLine().getOut().flush();
            this.spec.commandLine().getErr().flush();
        }
    }

    /**
     * Execute command "show pattern definitions".
     *
     * @param grok
     */
    void executeShowPatterndefinitions(Grok grok) {
        final GrokIt grokIt = new GrokIt();
        final Map<String, String> defaultPatterns = grokIt.retrievePatterndefinitions(grok);
        //---
        systemErrOutPrinter.printOut(
                String.format("grok pattern definitions:%n"));
        defaultPatterns.entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .forEach((e) -> {
                    systemErrOutPrinter.printOut(String.format("%s: %s%n", e.getKey(), e.getValue()));
                });
    }

    /**
     * Execute default command: "match lines".
     *
     * @param grok
     * @throws IOException
     */
    void executeMatching(Grok grok) throws IOException {
        final GrokIt grokIt = new GrokIt();

        try (final Reader logReader = new ReaderFactory(inputFile).createUtf8Reader();
                final BufferedReader br = new BufferedReader(logReader)) {
            //---
            final IOutputGrokResultConverter outputGrokResultConverter;
            if (outputMatchResultAsCsv) {
                outputGrokResultConverter = new OutputGrokResultAsCsv(this.spec.commandLine().getOut());
            } else if (outputMatchResultAsJson) {
                outputGrokResultConverter = new OutputGrokResultAsJson(this.spec.commandLine().getOut());
            } else {
                outputGrokResultConverter = new OutputGrokResultAsIs(this.spec.commandLine().getOut());
            }
            int mode = 1;
            if (mode == 0) {
                // context: grokIt, matchingLineMode, outputGrokResultConverter, br
                try {
                    final MatchGatherOutput matchGatherOutput = new MatchGatherOutput();

                    outputGrokResultConverter.start();
                    //---
                    int readLineCount = 0;
                    for (String line; (line = br.readLine()) != null;) {
                        readLineCount += 1;
                        if (this.readMaxLinesCount >= 0 && readLineCount > this.readMaxLinesCount) {
                            break;
                        }
                        final GrokMatchResult grokResult = grokIt.match(grok, line);
                        //---
                        if (matchingLineMode == MatchingLineMode.singleLineMode) {
                            boolean skip = false;
                            skip = skip || grokResult == null;
                            skip = skip || (grokResult.start == 0 && grokResult.end == 0);
                            skip = skip || grokResult.m == null;
                            skip = skip || grokResult.m.isEmpty();
                            if (skip) {
                                continue;
                            }
                            outputGrokResultConverter.output(readLineCount, grokResult);
                        } else if (matchingLineMode == MatchingLineMode.multiLinesMode) {
                            final Optional<Result> resultOpt = matchGatherOutput.gatherMatch(
                                    readLineCount,
                                    line,
                                    grokResult.start,
                                    grokResult.end,
                                    grokResult.m);
                            if (resultOpt.isPresent()) {
                                Wrapper w = resultOpt.get().wrapperWithExtraToMap();
                                int readLineCount2 = w.readLineCount;
                                GrokMatchResult grokResult2 = new GrokMatchResult(
                                        w.subject,
                                        w.start,
                                        w.end,
                                        w.m
                                );
                                outputGrokResultConverter.output(readLineCount2, grokResult2);
                            }
                        }
                    }
                    // retrieve last Optional<Result> still gathered, but
                    // not yet output
                    final Optional<Result> resultOpt = matchGatherOutput.retrieveResult();
                    if (resultOpt.isPresent()) {
                        Wrapper w = resultOpt.get().wrapperWithExtraToMap();
                        GrokMatchResult grokResult2 = new GrokMatchResult(
                                w.subject,
                                w.start,
                                w.end,
                                w.m
                        );
                        outputGrokResultConverter.output(readLineCount, grokResult2);
                    }

                    outputGrokResultConverter.end();
                } finally {
                    outputGrokResultConverter.close();
                }
            } else if (mode == 1) {
                final InputLineProcessor inputLineProcessor = new InputLineProcessor(
                        grok,
                        this.matchingLineMode,
                        outputGrokResultConverter,
                        this.readMaxLinesCount
                );
                inputLineProcessor.processLines(br);
            }
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

    /**
     * Wrap printing to stdout, and stderr.
     * <p>
     * Picocli defines the stdout, and stderr {@link PrintWriter}.
     */
    static class SystemErrOutPrinter {

        private final PrintWriter pwErr;
        private final PrintWriter pwOut;

        SystemErrOutPrinter(PrintWriter pwErr, PrintWriter pwOut) {
            this.pwErr = pwErr;
            this.pwOut = pwOut;
        }

        void printErr(String str) {
            final PrintWriter pw = this.pwErr;
            pw.print(str);
        }

        void printOut(String str) {
            final PrintWriter pw = this.pwOut;
            pw.print(str);
        }
    }

    static enum MatchingLineMode {
        singleLineMode,
        multiLinesMode
    }

    static class InputLineProcessor {

        final Grok grok;
        final MatchingLineMode matchingLineMode;
        final IOutputGrokResultConverter outputGrokResultConverter;

        final int readMaxLinesCount;

        public InputLineProcessor(
                Grok grok,
                MatchingLineMode matchingLineMode,
                IOutputGrokResultConverter outputGrokResultConverter,
                int readMaxLinesCount) {
            this.grok = grok;
            this.matchingLineMode = matchingLineMode;
            this.outputGrokResultConverter = outputGrokResultConverter;
            this.readMaxLinesCount = readMaxLinesCount;
        }

        /**
         * entry for processing all lines from a {@link BufferedReader}.
         */
        void processLines(final BufferedReader br) throws IOException {
            final GrokIt grokIt = new GrokIt();

            // context: grokIt, matchingLineMode, outputGrokResultConverter, br
            try {
                final MatchGatherOutput matchGatherOutput = new MatchGatherOutput();

                outputGrokResultConverter.start();
                //---
                int readLineCount = 0;
                for (String line; (line = br.readLine()) != null;) {
                    readLineCount += 1;
                    if (readMaxLinesCount >= 0 && readLineCount > readMaxLinesCount) {
                        break;
                    }
                    final GrokMatchResult grokResult = grokIt.match(grok, line);
                    //---
                    if (matchingLineMode == MatchingLineMode.singleLineMode) {
                        singleLineMode(readLineCount, grokResult);
                    } else if (matchingLineMode == MatchingLineMode.multiLinesMode) {
                        multiLinesMode(line,
                                readLineCount,
                                matchGatherOutput,
                                grokResult
                        );
                    }
                }
                // retrieve last Optional<Result> still gathered, but
                // not yet output
                multiLineModeLast(
                        readLineCount,
                        matchGatherOutput);
                outputGrokResultConverter.end();
            } finally {
                outputGrokResultConverter.close();
            }
        }

        /**
         * process only matched lines, ignore non matched lines
         */
        void singleLineMode(int readLineCount, GrokMatchResult grokResult) {
            boolean skip = false;
            skip = skip || grokResult == null;
            skip = skip || (grokResult.start == 0 && grokResult.end == 0);
            skip = skip || grokResult.m == null;
            skip = skip || grokResult.m.isEmpty();
            if (!skip) {
                outputGrokResultConverter.output(readLineCount, grokResult);
            }

        }

        /**
         * process matched lines, append non-matched lines to last matched lines
         * as map-entry "extra"
         */
        void multiLinesMode(String line,
                int readLineCount,
                MatchGatherOutput matchGatherOutput,
                GrokMatchResult grokResult
        ) {
            final Optional<Result> resultOpt = matchGatherOutput.gatherMatch(
                    readLineCount,
                    line,
                    grokResult.start,
                    grokResult.end,
                    grokResult.m);
            if (resultOpt.isPresent()) {
                Wrapper w = resultOpt.get().wrapperWithExtraToMap();
                int readLineCount2 = w.readLineCount;
                GrokMatchResult grokResult2 = new GrokMatchResult(
                        w.subject,
                        w.start,
                        w.end,
                        w.m
                );
                outputGrokResultConverter.output(readLineCount2, grokResult2);
            }
        }

        /**
         * tail end processing of multilinesMode processing
         */
        void multiLineModeLast(
                int readLineCount,
                MatchGatherOutput matchGatherOutput
        ) {
            // retrieve last Optional<Result> still gathered, but
            // not yet output
            final Optional<Result> resultOpt = matchGatherOutput.retrieveResult();
            if (resultOpt.isPresent()) {
                Wrapper w = resultOpt.get().wrapperWithExtraToMap();
                GrokMatchResult grokResult2 = new GrokMatchResult(
                        w.subject,
                        w.start,
                        w.end,
                        w.m
                );
                outputGrokResultConverter.output(readLineCount, grokResult2);
            }
        }
    }
}
