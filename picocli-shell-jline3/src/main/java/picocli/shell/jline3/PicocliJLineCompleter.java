package picocli.shell.jline3;

import org.jline.reader.LineReader;
import org.jline.reader.Completer;
import org.jline.reader.Candidate;
import org.jline.reader.ParsedLine;
import picocli.AutoComplete;
import picocli.CommandLine.Model.CommandSpec;

import java.util.List;
import java.util.ArrayList;
import java.lang.CharSequence;

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
        List<CharSequence> cs = new ArrayList<CharSequence>();
        AutoComplete.complete(spec,
                words,
                line.wordIndex(),
                0,
                line.cursor(),
                cs);
        for(CharSequence c: cs){
            candidates.add(new Candidate((String)c)); 
        }
    }
}
