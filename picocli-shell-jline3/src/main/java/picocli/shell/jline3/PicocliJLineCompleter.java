package picocli.shell.jline3;

import org.jline.reader.LineReader;
import org.jline.reader.Completer;
import org.jline.reader.Candidate;
import org.jline.reader.ParsedLine;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.lang.CharSequence;
import java.util.Map;

/**
 * Implementation of the JLine 3 {@link Completer} interface that generates completion
 * candidates for the specified command line based on the {@link CommandSpec} that
 * this {@code PicocliJLineCompleter} was constructed with.
 *
 * @since 3.9
 */
public class PicocliJLineCompleter implements Completer {
    private final CommandSpec spec;

    /**
     * Constructs a new {@code PicocliJLineCompleter} for the given command spec.
     * @param spec the command specification to generate completions for. Must be non-{@code null}.
     */
    public PicocliJLineCompleter(CommandSpec spec) {
        if (spec == null) { throw new NullPointerException("spec"); }
        this.spec = spec;
    }

    /**
     * Populates <i>candidates</i> with a list of possible completions for the <i>command line</i>.
     *
     * The list of candidates will be sorted and filtered by the LineReader, so that
     * the list of candidates displayed to the user will usually be smaller than
     * the list given by the completer.  Thus it is not necessary for the completer
     * to do any matching based on the current buffer.  On the contrary, in order
     * for the typo matcher to work, all possible candidates for the word being
     * completed should be returned.
     *
     * @param reader        The line reader
     * @param line          The parsed command line
     * @param candidates    The {@link List} of candidates to populate
     */
    //@Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
       // let picocli generate completion candidates for the token where the cursor is at
        String[] words = new String[line.words().size()];
        words = line.words().toArray(words);
        complete(spec,
                words,
                line.wordIndex(),
                0,
                line.cursor(),
                candidates);
    }
    public static int complete(CommandSpec spec,
                               String[] args,
                               int argIndex,
                               int positionInArg,
                               int cursor,
                               List<Candidate> candidates) {
        if (spec == null)       { throw new NullPointerException("spec is null"); }
        if (args == null)       { throw new NullPointerException("args is null"); }
        if (candidates == null) { throw new NullPointerException("candidates list is null"); }
        if (argIndex == args.length) {
            String[] copy = new String[args.length + 1];
            System.arraycopy(args, 0, copy, 0, args.length);
            args = copy;
            args[argIndex] = "";
        }
        if (argIndex < 0      || argIndex >= args.length)                 { throw new IllegalArgumentException("Invalid argIndex " + argIndex + ": args array only has " + args.length + " elements."); }
        if (positionInArg < 0 || positionInArg > args[argIndex].length()) { throw new IllegalArgumentException("Invalid positionInArg " + positionInArg + ": args[" + argIndex + "] (" + args[argIndex] + ") only has " + args[argIndex].length() + " characters."); }

        String currentArg = args[argIndex];
        boolean reset = spec.parser().collectErrors();
        try {
            String committedPrefix = currentArg.substring(0, positionInArg);

            spec.parser().collectErrors(true);
            CommandLine parser = new CommandLine(spec);
            CommandLine.ParseResult parseResult = parser.parseArgs(args);
            if (argIndex >= parseResult.tentativeMatch.size()) {
                Object startPoint = findCompletionStartPoint(parseResult);
                addCandidatesForArgsFollowing(startPoint, candidates);
            } else {
                Object obj = parseResult.tentativeMatch.get(argIndex);
                if (obj instanceof CommandSpec) { // subcommand
                    addCandidatesForArgsFollowing(((CommandSpec) obj).parent(), candidates);

                } else if (obj instanceof OptionSpec) { // option
                    int sep = currentArg.indexOf(spec.parser().separator());
                    if (sep < 0 || positionInArg < sep) { // no '=' or cursor before '='
                        addCandidatesForArgsFollowing(findCommandFor((OptionSpec) obj, spec), candidates);
                    } else {
                        addCandidatesForArgsFollowing((OptionSpec) obj, candidates);

                        int sepLength = spec.parser().separator().length();
                        if (positionInArg < sep + sepLength) {
                            int posInSeparator = positionInArg - sep;
                            String prefix = spec.parser().separator().substring(posInSeparator);
                            for (int i = 0; i < candidates.size(); i++) {
                                Candidate old = candidates.get(i);
                                Candidate replace = new Candidate(prefix + old.value(),
                                        prefix + old.displ(),
                                        old.group(),
                                        old.descr(),
                                        old.suffix(),
                                        old.key(),
                                        old.complete());
                                candidates.set(i, replace);
                            }
                            committedPrefix = currentArg.substring(sep, positionInArg);
                        } else {
                            committedPrefix = currentArg.substring(sep + sepLength, positionInArg);
                        }
                    }

                } else if (obj instanceof CommandLine.Model.PositionalParamSpec) { // positional
                    //addCandidatesForArgsFollowing(obj, candidates);
                    addCandidatesForArgsFollowing(findCommandFor((CommandLine.Model.PositionalParamSpec) obj, spec), candidates);

                } else {
                    int i = argIndex - 1;
                    while (i > 0 && !isPicocliModelObject(parseResult.tentativeMatch.get(i))) {i--;}
                    if (i < 0) { return -1; }
                    addCandidatesForArgsFollowing(parseResult.tentativeMatch.get(i), candidates);
                }
            }
            // TODO filterAndTrimMatchingPrefix(committedPrefix, candidates);
            return candidates.isEmpty() ? -1 : cursor;
        } finally {
            spec.parser().collectErrors(reset);
        }
    }

    private static void filterAndTrimMatchingPrefix(String prefix, List<CharSequence> candidates) {
        List<CharSequence> replace = new ArrayList<CharSequence>();
        for (CharSequence seq : candidates) {
            if (seq.toString().startsWith(prefix)) {
                replace.add(seq.subSequence(prefix.length(), seq.length()));
            }
        }
        candidates.clear();
        candidates.addAll(replace);
    }
    private static Object findCompletionStartPoint(CommandLine.ParseResult parseResult) {
        List<Object> tentativeMatches = parseResult.tentativeMatch;
        for (int i = 1; i <= tentativeMatches.size(); i++) {
            Object found = tentativeMatches.get(tentativeMatches.size() - i);
            if (found instanceof CommandSpec) {
                return found;
            }
            if (found instanceof CommandLine.Model.ArgSpec) {
                CommandLine.Range arity = ((CommandLine.Model.ArgSpec) found).arity();
                if (i < arity.min) {
                    return found; // not all parameters have been supplied yet
                } else {
                    return findCommandFor((CommandLine.Model.ArgSpec) found, parseResult.commandSpec());
                }
            }
        }
        return parseResult.commandSpec();
    }

    private static CommandSpec findCommandFor(CommandLine.Model.ArgSpec arg, CommandSpec cmd) {
        return (arg instanceof OptionSpec) ? findCommandFor((OptionSpec) arg, cmd) : findCommandFor((CommandLine.Model.PositionalParamSpec) arg, cmd);
    }
    private static CommandSpec findCommandFor(OptionSpec option, CommandSpec commandSpec) {
        for (OptionSpec defined : commandSpec.options()) {
            if (defined == option) { return commandSpec; }
        }
        for (CommandLine sub : commandSpec.subcommands().values()) {
            CommandSpec result = findCommandFor(option, sub.getCommandSpec());
            if (result != null) { return result; }
        }
        return null;
    }
    private static CommandSpec findCommandFor(CommandLine.Model.PositionalParamSpec positional, CommandSpec commandSpec) {
        for (CommandLine.Model.PositionalParamSpec defined : commandSpec.positionalParameters()) {
            if (defined == positional) { return commandSpec; }
        }
        for (CommandLine sub : commandSpec.subcommands().values()) {
            CommandSpec result = findCommandFor(positional, sub.getCommandSpec());
            if (result != null) { return result; }
        }
        return null;
    }
    private static boolean isPicocliModelObject(Object obj) {
        return obj instanceof CommandSpec || obj instanceof OptionSpec || obj instanceof CommandLine.Model.PositionalParamSpec;
    }
    private static void addCandidatesForArgsFollowing(Object obj, List<Candidate> candidates) {
        if (obj == null) { return; }
        if (obj instanceof CommandSpec) {
            addCandidatesForArgsFollowing((CommandSpec) obj, candidates);
        } else if (obj instanceof OptionSpec) {
            addCandidatesForArgsFollowing((OptionSpec) obj, candidates);
        } else if (obj instanceof CommandLine.Model.PositionalParamSpec) {
            addCandidatesForArgsFollowing((CommandLine.Model.PositionalParamSpec) obj, candidates);
        }
    }
    private static void addCandidatesForArgsFollowing(CommandSpec commandSpec, List<Candidate> candidates) {
        if (commandSpec == null) { return; }

        // the commandSpec.subcommands() Map contains an entry for every alias of each subcommand
        for (Map.Entry<String, CommandLine> entry : commandSpec.subcommands().entrySet()) {
            CommandSpec subSpec = entry.getValue().getCommandSpec();
            Candidate cmd = new Candidate(entry.getKey(),
                    entry.getKey(), // display
                    subSpec.name(), // group
                    join(subSpec.usageMessage().description(), " "), //desc
                    null,
                    subSpec.name(),
                    true);
            candidates.add(cmd);
        }
        for (Map.Entry<String, OptionSpec> entry : commandSpec.optionsMap().entrySet()) {
            OptionSpec option = entry.getValue();
            Candidate optionCandidate = new Candidate(entry.getKey(),
                    entry.getKey(), // display
                    option.longestName(), // group
                    join(option.description(), " "), //desc
                    null,
                    option.longestName(),
                    option.arity().max < 1);
            candidates.add(optionCandidate);
        }
        for (CommandLine.Model.PositionalParamSpec positional : commandSpec.positionalParameters()) {
            addCandidatesForArgsFollowing(positional, candidates);
        }
    }

    private static String join(String[] parts, String sep) {

        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (result.length() > 0) {
                result.append(sep);
            }
            result.append(CommandLine.Help.Ansi.AUTO.string(part));
        }
        return result.toString();
    }

    private static void addCandidatesForArgsFollowing(OptionSpec optionSpec, List<Candidate> candidates) {
        if (optionSpec != null) {
            addCompletionCandidates(optionSpec, candidates);
        }
    }
    private static void addCandidatesForArgsFollowing(CommandLine.Model.PositionalParamSpec positionalSpec, List<Candidate> candidates) {
        if (positionalSpec != null) {
            addCompletionCandidates(positionalSpec, candidates);
        }
    }
    private static void addCompletionCandidates(CommandLine.Model.ArgSpec argSpec, List<Candidate> candidates) {
        if (argSpec.completionCandidates() != null) {
            for (String candidate : argSpec.completionCandidates()) {
                candidates.add(new Candidate(candidate,
                        candidate, // display
                        argSpec.paramLabel(), // group
                        null, //join(argSpec.description(), " "),
                        null,
                        null,
                        true));
            }
        }
    }
}
