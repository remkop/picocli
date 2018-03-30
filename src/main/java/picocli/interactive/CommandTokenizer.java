package picocli.interactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * A class that parses a {@link java.lang.String String}
 * or {@link java.util.Scanner Scanner} into tokens. 
 * It uses whitespace characters as delimiters, and supports
 * quoting and escaping said characters. 
 * In addition, it supports comment characters, which comment out
 * everything up to the next EOL pattern. 
 * All special character types are customizable, as well as the EOL
 * patterns (note that the latter are Strings, NOT regex).
 * The syntax is meant to resemble that of
 * {@link java.io.StreamTokenizer StreamTokenizer}. 
 * <br>
 * Default patterns/characters:<br>
 * <table summary="Default special characters/patterns">
 * 	<tr>
 * 		<td>comments</td>
 * 		<td>{@code #}</td>
 * 	</tr>
 * 	<tr>
 * 		<td>eol</td>
 * 		<td>{@code \r\n,\r,\n}</td>
 * 	</tr>
 * 	<tr>
 * 		<td>escape</td>
 * 		<td>{@code \}</td>
 * 	</tr>
 * 	<tr>
 * 		<td>escaped</td>
 * 		<td>none</td>
 * 	</tr>
 * 	<tr>
 * 		<td>quote</td>
 * 		<td>{@code ",'}</td>
 * 	</tr>
 * 	<tr>
 * 		<td>whitespace</td>
 * 		<td>{@code  ,\t}</td>
 * 	</tr>
 * </table>
 */
public class CommandTokenizer {

	private String[] tokens;
	private Scanner input;
	private int position = 0;
	// Patterns that comment out until a newline
	private List<String> commentPatterns = new ArrayList<String>(Arrays.asList("#"));
	// Characters to use to represent a new line
	private List<String> eolPatterns = new ArrayList<String>(Arrays.asList("\r\n", "\r", "\n"));
	// Characters that escape other special characters
	private List<String> escapePatterns = new ArrayList<String>(Arrays.asList("\\"));
	// Characters to be escaped automatically
	private List<String> escapedPatterns = new ArrayList<String>();
	// Characters that begin/end quotes
	private List<String> quotePatterns = new ArrayList<String>(Arrays.asList("\"", "'"));
	// Characters to be treated as whitespace
	private List<String> whitespacePatterns = new ArrayList<String>(Arrays.asList(" ", "\t"));
	// Remove blanks at start and end of a string
	private boolean trimBlanks = true;

	/**
	 * Creates an instance of {@code CommandTokenizer} to be used
	 * with {@link #parse(String cmd)} or {@link parse(Scanner input)}.
	 * 
	 * @see #parse(String cmd)
	 * @see #parse(Scanner input)
	 */
	public CommandTokenizer() {}
	
	/**
	 * Creates an instance of {@code CommandTokenizer} with {@code cmd}
	 * as the input.
	 * Note that it does not parse until {@link #nextToken()} or
	 * {@link #getTokens()} is called. 
	 */
	public CommandTokenizer(String cmd) {
		this.input = new Scanner(cmd);
	}

	/**
	 * Creates an instance of {@code CommandTokenizer} with
	 * {@code input} as the input.
	 * Note that it does not parse until {@link #nextToken()} or
	 * {@link #getTokens()} is called. 
	 */
	public CommandTokenizer(Scanner input) {
		this.input = input;
	}

	/**
	 * Splits a {@link java.lang.String String} into tokens, according
	 * to the customization options. 
	 */
	public String[] parse(String cmd) {
		String[] cmdSplit = new String[]{""};
		boolean inQuote = false;															// Stores if currently quoted
		char quoteChar = 0;																	// Stores which char ends quotes
		for (int i = 0; i < cmd.length(); i++) {
			char curChar = cmd.charAt(i);
			char preChar = (i == 0) ? 0 : cmd.charAt(i - 1);
			String curStr = cmdSplit[cmdSplit.length - 1];
			if (quotePatterns.contains(curChar)) {											// Check if has quoting char
				if (i == 0 || !escapePatterns.contains(preChar) && 
					(curChar == quoteChar || !inQuote)) {									// Make sure quote is unescaped and the right quote
					inQuote = !inQuote;														// Toggle quoted text
					if (inQuote) {
						quoteChar = curChar;												// Set quote character to current char
					}
				}
				else {
					if (escapePatterns.contains(preChar)) {									// If previous char escapes it
						curStr = curStr.substring(0, curStr.length() - 1);					// Cut off last backslash
					}
					curStr += curChar;														// Add quote to string
				}
			}
			else if (commentPatterns.contains(curChar)) {									// Check if has commenting char
				if (i == 0 || !escapePatterns.contains(preChar) && (!inQuote)) {
					break;
				}
				else if (escapePatterns.contains(preChar)) {								// If previous char escapes it
					curStr = curStr.substring(0, curStr.length() - 1);						// Cut off escape character
					curStr += curChar;														// Add quote to string
				}
			}
			else if (!inQuote && whitespacePatterns.contains(curChar)) {					// If char is whitespace and not in quote
				if (escapePatterns.contains(preChar)) {										// If previous char escapes
					curStr = curStr.substring(0, curStr.length() - 1);						// Cut off escape character
					curStr += curChar;														// Add the space in
				}
				else {
					String[] cmdTmp = new String[cmdSplit.length + 1];						// Make array one bigger
					System.arraycopy(cmdSplit, 0, cmdTmp, 0, cmdSplit.length);				// Copy to bigger array
					cmdSplit = cmdTmp;														// Replace old array
					curStr = "";															// Empty current string
				}
			}
			else {
				curStr += curChar;															// Add char to string
			}
			cmdSplit[cmdSplit.length - 1] = curStr;											// Copy back to array
		}
		return cmdSplit;
	}
	
	/**
	 * Splits tokens from a {@link java.util.Scanner Scanner}, according
	 * to the customization options. 
	 * 
	 * @return    
	 */
	public String[] parse(Scanner input) {
		String delimiters = "";
		for (String pattern : eolPatterns) {
			if (!delimiters.equals("")) {
				delimiters += "|";
			}
			delimiters += pattern;
		}
		input.useDelimiter(delimiters);
		String[] cmdSplit = new String[0];
		while (true) {
			if (input.hasNext()) {
				String cmd = input.next();
				if (escapePatterns.contains(cmd.charAt(cmd.length() - 1))) {				// If new line is escaped
					System.out.print("> ");
					cmd = cmd.substring(0, cmd.length() - 1);
					if (cmd.length() > 0 &&
						whitespacePatterns.contains(cmd.charAt(cmd.length() - 1)) &&
						!escapePatterns.contains(cmd.charAt(cmd.length() - 2))) {
						cmd = cmd.substring(0, cmd.length() - 1);
					}
					cmdSplit = parse(cmd);
					String[] moreCmd = parse(input);
					String[] newCmd = new String[cmdSplit.length + moreCmd.length];
					System.arraycopy(cmdSplit, 0, newCmd, 0, cmdSplit.length);
					System.arraycopy(moreCmd, 0, newCmd, cmdSplit.length, moreCmd.length);
					cmdSplit = newCmd;
					break;
				}
				else {
					cmdSplit = parse(cmd);
					break;
				}
			}
		}
		while (trimBlanks) {
			if (cmdSplit.length > 0 &&
				(cmdSplit[0].equals("")) ||
				cmdSplit[0] == null) {
				String[] newCmd = new String[cmdSplit.length - 1];
				System.arraycopy(cmdSplit, 1, newCmd, 0, newCmd.length);
				cmdSplit = newCmd;
			}
			if (cmdSplit.length > 0 &&
				(cmdSplit[cmdSplit.length - 1].equals("") ||
				cmdSplit[cmdSplit.length - 1] == null)) {
				String[] newCmd = new String[cmdSplit.length - 1];
				System.arraycopy(cmdSplit, 0, newCmd, 0, newCmd.length);
				cmdSplit = newCmd;
			}
			else {
				break;
			}
		}
		for (int i = 0; i< cmdSplit.length; i++) {
			for (int j = 0; j < this.escapedPatterns.size(); j++) {
				String escaped = this.escapePatterns.get(j).toString();
				cmdSplit[i].replace(escaped, "\\" + escaped);
			}
		}
		return cmdSplit;
	}

	/**
	 * Returns all tokens as a {@link java.lang.String String[]}.
	 *
	 * @return         Tokens as a {@link java.lang.String String[]}.
	 */
	public String[] getTokens() {
		if (this.tokens == null) {
			this.tokens = parse(this.input);
		}
		return this.tokens;
	}

	/**
	 * Returns the next token and advances the position.
	 *
	 * @return         Next token.
	 */
	public String nextToken() {
		String str = this.getTokens()[this.position];
		this.position++;
		return str;
	}

	/**
	 * Specifies that char {@code ch} starts a comment.
	 *
	 * @param ch       The character to start comments.
	 * @return         This tokenizer, for method chaining. 
	 */
	public CommandTokenizer commentChar(char ch) {
		this.ordinaryChar(ch);
		this.commentPatterns.add(String.valueOf(ch));
		return this;
	}

	/**
	 * Specifies that all characters passed start comments.
	 * 
	 * @param chars    The characters to be treated as comment starters.
	 * @return         This tokenizer, for method chaining.
	 * @see            #commentChar(char)
	 */
	public CommandTokenizer commentChars(char... chars) {
		for (char c : chars) {
			this.commentChar(c);
		}
		return this;
	}

	/**
	 * If {@code true}, resets to defaults. If {@code false}, clears all.
	 * 
	 * @param enabled  Whether to reset or clear.
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer commentChars(boolean enabled) {
		if (enabled) {
			this.quotePatterns = new ArrayList<String>(Arrays.asList("#"));
		}
		else {
			this.commentPatterns.clear();
		}
		return this;
	}

	/**
	 * Specifies that char {@code ch} escapes other characters.
	 *
	 * @param ch       The character to escape other characters.
	 * @return         This tokenizer, for method chaining. 
	 */
	public CommandTokenizer escapeChar(char ch) {
		if (ch < 0) {
			this.quotePatterns.clear();
		}
		else {
			this.ordinaryChar(ch);
			this.escapePatterns.add(String.valueOf(ch));
		}
		return this;
	}

	/**
	 * Specifies that all characters passed escape other characters.
	 * 
	 * @param chars    The characters to be treated as escape characters.
	 * @return         This tokenizer, for method chaining.
	 * @see            #escapeChar(char)
	 */
	public CommandTokenizer escapeChars(char... chars) {
		for (char ch : chars) {
			this.escapeChar(ch);
		}
		return this;
	}
	
	/**
	 * If {@code true}, resets to defaults. If {@code false}, clears all.
	 * 
	 * @param enabled  Whether to reset or clear.
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer escapeChars(boolean enabled) {
		if (enabled) {
			this.quotePatterns = new ArrayList<String>(Arrays.asList("\\"));
		}
		else {
			this.escapePatterns.clear();
		}
		return this;
	}

	/**
	 * Specifies that char {@code ch} should be automatically escaped.
	 *
	 * @param ch       The character to be escaped.
	 * @return         This tokenizer, for method chaining. 
	 */
	public CommandTokenizer escapedChar(char ch) {
		if (ch < 0) {
			this.escapedPatterns.clear();
		}
		else {
			this.ordinaryChar(ch);
			this.escapedPatterns.add(String.valueOf(ch));
		}
		return this;
	}

	/**
	 * Specifies that all characters passed will be escaped.
	 * 
	 * @param chars    The characters to be escaped.
	 * @return         This tokenizer, for method chaining.
	 * @see            #escapedChar(char)
	 */
	public CommandTokenizer escapedChars(char... chars) {
		for (char ch : chars) {
			this.escapedChar(ch);
		}
		return this;
	}

	/**
	 * If {@code true}, resets to defaults. If {@code false}, clears all.
	 * 
	 * @param enabled  Whether to reset or clear.
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer escapedChars(boolean enabled) {
		if (enabled) {
			this.escapedPatterns = new ArrayList<String>();
		}
		else {
			this.escapedPatterns.clear();
		}
		return this;
	}

	/**
	 * Removes all special meaning for char {@code ch}.
	 *
	 * @param ch       The character to be treated normally.
	 * @return         This tokenizer, for method chaining. 
	 */
	private CommandTokenizer ordinaryChar(char ch) {
		char chChar = (char) ch;
		if (this.escapePatterns.contains(chChar)) {
			this.escapePatterns.remove(chChar);
		}
		if (this.escapedPatterns.contains(chChar)) {
			this.escapedPatterns.remove(chChar);
		}
		if (this.quotePatterns.contains(chChar)) {
			this.quotePatterns.remove(chChar);
		}
		if (this.whitespacePatterns.contains(chChar)) {
			this.whitespacePatterns.remove(chChar);
		}
		return this;
	}
	
	/**
	 * Removes all special meaning for all passed characters.
	 * 
	 * @param chars    The characters to be treated normally.
	 * @return         This tokenizer, for method chaining.
	 * @see            #ordinaryChar(char)
	 */
	public CommandTokenizer ordinaryChars(char... chars) {
		for (char ch : chars) {
			this.ordinaryChar(ch);
		}
		return this;
	}

	/**
	 * Specifies that char {@code ch} starts/ends quotes.
	 *
	 * @param ch       The character to be treated as quotes.
	 * @return         This tokenizer, for method chaining. 
	 */
	public CommandTokenizer quoteChar(char ch) {
		if (ch < 0) {
			this.quotePatterns.clear();
		}
		else {
			this.ordinaryChar(ch);
			this.quotePatterns.add(String.valueOf(ch));
		}
		return this;
	}

	/**
	 * Specifies that all characters passed start/end quotes.
	 * 
	 * @param chars    The characters to be treated as quotes.
	 * @return         This tokenizer, for method chaining.
	 * @see            #quoteChar(char)
	 */
	public CommandTokenizer quoteChars(char... chars) {
		for (char ch : chars) {
			this.quoteChar(ch);
		}
		return this;
	}

	/**
	 * If {@code true}, resets to defaults. If {@code false}, clears all.
	 * 
	 * @param enabled  Whether to reset or clear.
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer quoteChars(boolean enabled) {
		if (enabled) {
			this.quotePatterns = new ArrayList<String>(Arrays.asList("\"", "'"));
		}
		else{
			this.quotePatterns.clear();
		}
		return this;
	}

	/**
	 * Specifies that char {@code ch} is whitespace.
	 *
	 * @param ch       The character to be treated as whitespace.
	 * @return         This tokenizer, for method chaining. 
	 */
	public CommandTokenizer whitespaceChar(char ch) {
		if (ch < 0) {
			this.whitespacePatterns.clear();
		}
		else {
			this.ordinaryChar(ch);
			this.whitespacePatterns.add(String.valueOf(ch));
		}
		return this;
	}

	/**
	 * Specifies that all characters passed are whitespace.
	 * 
	 * @param chars    The characters to be treated as whitespace.
	 * @return         This tokenizer, for method chaining.
	 * @see            #whitespaceChar(char)
	 */
	public CommandTokenizer whitespaceChars(char... chars) {
		for (char ch : chars) {
			this.commentChar(ch);
		}
		return this;
	}
	
	
	/**
	 * Specifies that all characters {@code low ≤ ch ≤ hi} are whitespace.
	 * Takes {@code int}s because of {@link #whitespaceChars(char...)}.
	 * 
	 * @param low      The minimum character.
	 * @param hi       The maximum character.
	 * @return         This tokenizer, for method chaining.
	 * @see            #whitespaceChar(char)
	 */
	public CommandTokenizer whitespaceChars(int low, int hi) {
		for (int ch = low; ch <= hi; ch++) {
			this.ordinaryChar((char) ch);
			this.whitespaceChar((char) ch);
		}
		return this;
	}

	/**
	 * If {@code true}, resets to defaults. If {@code false}, clears all.
	 * 
	 * @param enabled  Whether to reset or clear.
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer whitespaceChars(boolean enabled) {
		if (enabled) {
			this.whitespacePatterns = new ArrayList<String>(Arrays.asList(" "));
		}
		else {
			this.whitespacePatterns.clear();
		}
		return this;
	}

	/**
	 * Specifies that String pattern {@code pattern} ends lines.
	 *
	 * @param pattern  The character to start comments.
	 * @return         This tokenizer, for method chaining. 
	 */
	public CommandTokenizer eolPattern(String pattern) {
		if (pattern == null) {
			this.eolPatterns.clear();
		}
		else {
			this.eolPatterns.add(pattern);
		}
		return this;
	}

	/**
	 * If {@code true}, resets to defaults. If {@code false}, clears all.
	 * 
	 * @param enabled  Whether to reset or clear.
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer eolPatterns(boolean enabled) {
		if (enabled) {
			this.eolPatterns = new ArrayList<String>(Arrays.asList("\r\n", "\r", "\n"));
		}
		else {
			this.eolPatterns.clear();
		}
		return this;
	}
	
	/**
	 * Restores default patterns and characters.
	 * 
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer resetSyntax() {
		this.escapePatterns = new ArrayList<String>(Arrays.asList("\\"));
		this.escapedPatterns = new ArrayList<String>();
		this.quotePatterns = new ArrayList<String>(Arrays.asList("\"", "'"));
		this.whitespacePatterns = new ArrayList<String>(Arrays.asList(" ", "\t"));
		this.eolPatterns = new ArrayList<String>(Arrays.asList("\r\n", "\r", "\n"));
		this.trimBlanks = true;
		return this;
	}
	
	/**
	 * Returns the current token in a form similar to the "typical" syntax 
	 * for {@link java.io.StreamTokenizer StreamTokenizer}.
	 * Looks like {@code Token['token'], position 0}.
	 * 
	 * @return         This tokenizer, for method chaining.
	 */
	@Override
	public String toString() {
		String str = "Token['" + this.tokens[this.position] + "'], position " + this.position;
		return str;
	}

}
