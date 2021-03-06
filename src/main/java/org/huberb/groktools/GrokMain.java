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
import java.util.function.Function;
import java.util.stream.Collectors;
import org.huberb.groktools.GrokIt.GrokMatchResult;
import org.huberb.groktools.GrokMain.Executors.ExecuteDiscover;
import org.huberb.groktools.GrokMain.Executors.ExecuteMatching;
import org.huberb.groktools.GrokMain.Executors.ExecuteShowPatterndefinitions;
import org.huberb.groktools.GrokMain.InputLineProcessor.MatchingLineMode;
import org.huberb.groktools.MatchGatherOutput.Result;
import org.huberb.groktools.MatchGatherOutput.Wrapper;
import org.huberb.groktools.OutputGrokResultFormatters.IOutputGrokResultFormatter;
import org.huberb.groktools.OutputGrokResultFormatters.OutputMatchResultMode;
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

    /**
     * Command line entry point.
     *
     * @param args
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new GrokMain()).execute(args);
        System.exit(exitCode);
    }

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

    @Option(names = {"-p", "--match-pattern"},
            description = "grok pattern")
    private String matchPattern;

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

    @Option(names = {"--discover-input-line"},
            description = "find pattern for discover input line")
    private String discoverInputLine;

    @Option(names = {"--matching-line-mode"},
            defaultValue = "singleLineMode",
            description = "match single line or mutli lines; valid values: \"${COMPLETION-CANDIDATES}\""
    )
    private MatchingLineMode matchingLineMode;

    @Option(names = {"--output-matchresult"},
            defaultValue = "asCsv",
            description = "output match results; valid values: \"${COMPLETION-CANDIDATES}\"")
    private OutputMatchResultMode outputMatchResultMode;

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
            if (this.matchPattern != null) {
                grokBuilder.pattern(this.matchPattern);
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
                new ExecuteShowPatterndefinitions(this).execute(grok);
            } else if (this.discoverInputLine != null && this.discoverInputLine.length() > 0) {
                new ExecuteDiscover(this).execute(grok);
            } else {
                new ExecuteMatching(this).execute(grok);
            }
            return 0;
        } finally {
            this.spec.commandLine().getOut().flush();
            this.spec.commandLine().getErr().flush();
        }
    }

    static class Executors {

        static interface Executor {

            void execute(Grok grok) throws Exception;
        }

        /**
         * Execute show pattern definitions.
         */
        static class ExecuteShowPatterndefinitions implements Executor {

            private final GrokMain grokMain;

            ExecuteShowPatterndefinitions(GrokMain grokMain) {
                this.grokMain = grokMain;
            }

            /**
             * Execute command "show pattern definitions".
             *
             * @param grok
             */
            public void execute(Grok grok) {
                //---
                final Function<Map<String, String>, String> f = (m) -> m.entrySet()
                        .stream()
                        .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                        .map((e) -> String.format("%s: %s%n", e.getKey(), e.getValue()))
                        .collect(Collectors.toList())
                        .toString();

                final String namedRegex = grok.getNamedRegex();
                final Map<String, String> namedRegexCollection = grok.getNamedRegexCollection();
                final String originalGrokPattern = grok.getOriginalGrokPattern();
                final Map<String, String> patternsMap = grok.getPatterns();
                final String savedPattern = grok.getSaved_pattern();

                final String formatted = String.format(""
                        + "namedRegex: %s%n"
                        + "namedRegexCollection: %s%n"
                        + "originalGrokPattern: %s%n"
                        + "patterns: %s%n"
                        + "savedPattern: %s%n",
                        namedRegex,
                        f.apply(namedRegexCollection),
                        originalGrokPattern,
                        f.apply(patternsMap),
                        savedPattern
                );
                grokMain.systemErrOutPrinter.printOut(formatted);
            }
        }

        /**
         * Execute matching.
         */
        static class ExecuteMatching implements Executor {

            private final GrokMain grokMain;

            ExecuteMatching(GrokMain grokMain) {
                this.grokMain = grokMain;
            }

            /**
             * Execute default command: "match lines".
             *
             * @param grok
             * @throws IOException
             */
            public void execute(Grok grok) throws IOException {
                final GrokIt grokIt = new GrokIt();

                try (final Reader logReader = new ReaderFactory(grokMain.inputFile).createUtf8Reader();
                        final BufferedReader br = new BufferedReader(logReader)) {
                    //---
                    final PrintWriter pw = grokMain.spec.commandLine().getOut();
                    final IOutputGrokResultFormatter outputGrokResultConverter
                            = OutputGrokResultFormatters.createOutputGrokResultConverter(grokMain.outputMatchResultMode, pw);

                    final InputLineProcessor inputLineProcessor = new InputLineProcessor(
                            grok,
                            grokMain.matchingLineMode,
                            outputGrokResultConverter,
                            grokMain.readMaxLinesCount
                    );
                    inputLineProcessor.processLines(br);
                }
            }
        }

        /**
         * Execute {@link Grok#discover(java.lang.String)
         */
        static class ExecuteDiscover implements Executor {

            private final GrokMain grokMain;

            ExecuteDiscover(GrokMain grokMain) {
                this.grokMain = grokMain;
            }

            /**
             * discover grok pattern from an input line.
             *
             * @param grok
             */
            public void execute(Grok grok) {
                final String input = grokMain.discoverInputLine;
                final String grokPattern = grok.discover(input);
                final String formatted = String.format("discover%n"
                        + "input: %s%n"
                        + "grokPattern: %s%n",
                        input,
                        grokPattern);
                grokMain.systemErrOutPrinter.printOut(formatted);
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

    /**
     * Processor input lines in single-line or multi-lines mode.
     */
    static class InputLineProcessor {

        /**
         * Describe processing lines mode.
         */
        static enum MatchingLineMode {
            singleLineMode,
            multiLinesMode
        }

        final Grok grok;
        final MatchingLineMode matchingLineMode;
        final IOutputGrokResultFormatter outputGrokResultConverter;

        final int readMaxLinesCount;

        /**
         * Create an setup.
         *
         * @param grok
         * @param matchingLineMode
         * @param outputGrokResultConverter
         * @param readMaxLinesCount
         */
        public InputLineProcessor(
                Grok grok,
                MatchingLineMode matchingLineMode,
                IOutputGrokResultFormatter outputGrokResultConverter,
                int readMaxLinesCount) {
            this.grok = grok;
            this.matchingLineMode = matchingLineMode;
            this.outputGrokResultConverter = outputGrokResultConverter;
            this.readMaxLinesCount = readMaxLinesCount;
        }

        /**
         * Entry point for processing all lines from a {@link BufferedReader}.
         */
        public void processLines(final BufferedReader br) throws IOException {
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
         * process only matched lines, ignore non matched lines (
         * {@link MatchingLineMode#singleLineMode} ).
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
         * as map-entry "extra" {@link MatchingLineMode#multiLinesMode} ).
         *
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
         * Tail end processing of {@link MatchingLineMode#multiLinesMode}
         * processing.
         */
        public void multiLineModeLast(
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
