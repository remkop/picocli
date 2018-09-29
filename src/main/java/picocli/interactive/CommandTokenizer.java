package picocli.interactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.io.OutputStream;

/**
 * A class that parses a {@link java.lang.String String}
 * or {@link java.util.Scanner Scanner} into tokens. 
 * It uses whitespace characters as delimiters, and supports
 * quoting and escaping said characters. 
 * In addition, it supports comment characters, which comment out
 * everything up to the next EOL string. 
 * All special character types are customizable, as well as the EOL
 * strings (note that the latter are Strings, NOT regex).
 * The syntax is meant to resemble that of
 * {@link java.io.StreamTokenizer StreamTokenizer}. 
 * <br>
 * By default, the {@link #presetSh() Bourne shell} syntax is used.
 */
public class CommandTokenizer {

	private String[] tokens;
	private String cmd;
	private Scanner input;
	private OutputStream output;
	private int position = 0;
	// Strings that comment out until a newline
	private List<String> commentStrings = new ArrayList<String>(Arrays.asList("#"));
	// Characters to use to represent a new line
	private List<String> eolStrings = new ArrayList<String>(Arrays.asList("\r\n", "\r", "\n"));
	// Characters that escape other special characters
	private List<String> escapeStrings = new ArrayList<String>(Arrays.asList("\\"));
	// Characters to be escaped automatically
	private List<String> escapedStrings = new ArrayList<String>();
	// Characters that begin/end quotes
	private List<String> quoteStrings = new ArrayList<String>(Arrays.asList("\"", "'"));
	// Characters to be treated as whitespace
	private List<String> whitespaceStrings = new ArrayList<String>(Arrays.asList(" ", "\t"));
	// Remove blanks at start and end of a string
	private boolean trimBlanks = true;

	/**
	 * Creates an instance of {@code CommandTokenizer} to be used
	 * with {@link #parse(String)} or {@link parse(Scanner, OutputStream)}.
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
	public CommandTokenizer(String command) {
		cmd = command;
	}

	/**
	 * Creates an instance of {@code CommandTokenizer} with
	 * {@code input} as the input.
	 * Note that it does not parse until {@link #nextToken()} or
	 * {@link #getTokens()} is called. 
	 */
	public CommandTokenizer(Scanner in, OutputStream out) {
		input = in;
		output = out;
	}

	/**
	 * Splits a {@link java.lang.String String} into tokens, according
	 * to the customization options. 
	 */
	public String[] parse(String cmd) {
		String[] cmdSplit = new String[]{""};
		boolean inQuote = false;															// Stores if currently quoted
		String quoteChar = "";																// Stores which char ends quotes
		for (int i = 0; i < cmd.length(); i++) {
			String curChar = cmd.substring(i, i + 1);
			String preChar = (i == 0) ? "" : cmd.substring(i - 1, i);
			String curStr = cmdSplit[cmdSplit.length - 1];
			if (quoteStrings.contains(curChar)) {											// Check if has quoting char
				if (i == 0 || !escapeStrings.contains(preChar) && 
					(curChar.equals(quoteChar) || !inQuote)) {								// Make sure quote is unescaped and the right quote
					inQuote = !inQuote;														// Toggle quoted text
					if (inQuote) {
						quoteChar = curChar;												// Set quote character to current char
					}
				}
				else {
					if (escapeStrings.contains(preChar)) {									// If previous char escapes it
						curStr = curStr.substring(0, curStr.length() - 1);					// Cut off last backslash
					}
					curStr += curChar;														// Add quote to string
				}
			}
			else if (commentStrings.contains(curChar)) {									// Check if has commenting char
				if (i == 0 || (!escapeStrings.contains(preChar) && !inQuote)) {
					break;
				}
				else if (escapeStrings.contains(preChar)) {								// If previous char escapes it
					curStr = curStr.substring(0, curStr.length() - 1);						// Cut off escape character
					curStr += curChar;														// Add quote to string
				}
			}
			else if (!inQuote && whitespaceStrings.contains(curChar)) {					// If char is whitespace and not in quote
				if (escapeStrings.contains(preChar)) {										// If previous char escapes
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
		for (int i = 0; i < cmdSplit.length; i++) {											// Loop through the tokens
			for (String escaped : escapedStrings) {									// Loop through the characters t escape
				cmdSplit[i].replace(escaped, "\\" + escaped);								// Escape the characters
			}
		}
		if (trimBlanks) {
			while (cmdSplit.length > 0 &&
				   (cmdSplit[0].equals("")) ||
				   cmdSplit[0] == null) {
				String[] cmdTmp = new String[cmdSplit.length - 1];
				System.arraycopy(cmdSplit, 1, cmdTmp, 0, cmdTmp.length);
				cmdSplit = cmdTmp;
			}
			while (cmdSplit.length > 0 &&
				   (cmdSplit[cmdSplit.length - 1].equals("") ||
				   cmdSplit[cmdSplit.length - 1] == null)) {
				String[] cmdTmp = new String[cmdSplit.length - 1];
				System.arraycopy(cmdSplit, 0, cmdTmp, 0, cmdTmp.length);
				cmdSplit = cmdTmp;
			}
		}
		return cmdSplit;																	// Return the tokens
	}

	/**
	 * Splits tokens from a {@link java.util.Scanner Scanner}, according
	 * to the customization options. Adds newline escaping functionality
	 * to the {@link #parse(String)} method. The mthod needs an 
	 * {@link java.io.OutputStream OutputStream} to print the prompt to. 
	 * 
	 * @param input    The {@link java.util.Scanner} to read from
	 * @param output   The {@link java.io.OutputStream} to write prompt
	 * @return         This tokenizer, for method chaining. 
	 */
	public String[] parse(Scanner input, OutputStream output) {
		String delimiters = "";
		for (String string : eolStrings) {
			if (!delimiters.equals("")) {
				delimiters += "|";
			}
			delimiters += string;
		}
		input.useDelimiter(delimiters);
		String[] cmdSplit = new String[0];
		if (input.hasNext()) {
			String cmd = input.next();
			if (escapeStrings.contains(cmd.substring(cmd.length() - 1))) {				// If new line is escaped
				System.out.print("> ");													// Print new prompt
				cmd = cmd.substring(0, cmd.length() - 1);								// Cut off escape character
				cmdSplit = parse(cmd);													// Parse the new String
				String[] moreCmd = parse(input, output);								// Get more input
				String[] cmdTmp = new String[cmdSplit.length + moreCmd.length];			// Create temporary array
				System.arraycopy(cmdSplit, 0, cmdTmp, 0, cmdSplit.length);				// Copy first old array to temp one
				System.arraycopy(moreCmd, 0, cmdTmp, cmdSplit.length, moreCmd.length);	// Copy second old array to temo one
				cmdSplit = cmdTmp;														// Copy temp array to real one
			}
			else {
				cmdSplit = parse(cmd);													// If newline isn't escaped, go ahead and parse
			}
		}
		return cmdSplit;																// Return array
	}

	/**
	 * Returns all tokens as a {@link java.lang.String String[]}.
	 *
	 * @return         Tokens as a {@link java.lang.String String[]}.
	 */
	public String[] getTokens() {
		if (tokens == null) {
			if (cmd != null) {
				tokens = parse(cmd);
			}
			else {
				tokens = parse(input, output);
			}
		}
		return tokens;
	}

	/**
	 * Returns the next token and advances the position.
	 *
	 * @return         Next token.
	 */
	public String nextToken() {
		String str = getTokens()[position];
		position++;
		return str;
	}

	/**
	 * Specifies that char {@code ch} starts a comment.
	 *
	 * @param ch       The character to start comments.
	 * @return         This tokenizer, for method chaining. 
	 */
	public CommandTokenizer commentChar(char ch) {
		ordinaryChar(ch);
		commentStrings.add(String.valueOf(ch));
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
		for (char ch : chars) {
			commentChar(ch);
		}
		return this;
	}

	/**
	 * Specifies that String string {@code string} ends lines.
	 *
	 * @param ch       The character to end lines.
	 * @return         This tokenizer, for method chaining. 
	 */
	public CommandTokenizer eolChar(char ch) {
		ordinaryChar(ch);
		eolStrings.add(String.valueOf(ch));
		return this;
	}
	
	/**
	 * Specifies that all characters passed end lines.
	 * 
	 * @param chars    The characters to end lines.
	 * @return         This tokenizer, for method chaining.
	 * @see            #eolChar(char)
	 */
	public CommandTokenizer eolChars(char... chars) {
		for (char ch : chars) {
			eolChar(ch);
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
		ordinaryChar(ch);
		escapeStrings.add(String.valueOf(ch));
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
			escapeChar(ch);
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
		ordinaryChar(ch);
		escapedStrings.add(String.valueOf(ch));
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
			escapedChar(ch);
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
		char chChar = ch;
		if (escapeStrings.contains(chChar)) {
			escapeStrings.remove(chChar);
		}
		if (escapedStrings.contains(chChar)) {
			escapedStrings.remove(chChar);
		}
		if (quoteStrings.contains(chChar)) {
			quoteStrings.remove(chChar);
		}
		if (whitespaceStrings.contains(chChar)) {
			whitespaceStrings.remove(chChar);
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
			ordinaryChar(ch);
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
		ordinaryChar(ch);
		quoteStrings.add(String.valueOf(ch));
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
			quoteChar(ch);
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
		ordinaryChar(ch);
		whitespaceStrings.add(String.valueOf(ch));
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
			commentChar(ch);
		}
		return this;
	}

	/**
	 * Specifies that all characters {@code low ≤ ch ≤ hi} are whitespace
	 * 
	 * @param low      The minimum character.
	 * @param hi       The maximum character.
	 * @return         This tokenizer, for method chaining.
	 * @see            #whitespaceChar(char)
	 */
	public CommandTokenizer whitespaceChars(char low, char hi) {
		for (int ch = low; ch <= hi; ch++) {
			ordinaryChar((char) ch);
			whitespaceChar((char) ch);
		}
		return this;
	}

	/**
	 * Specifies that char {@code ch} starts a comment.
	 *
	 * @param string   The string to start comments.
	 * @return         This tokenizer, for method chaining. 
	 */
	public CommandTokenizer commentString(String string) {
		ordinaryString(string);
		commentStrings.add(string);
		return this;
	}

	/**
	 * Specifies that all characters passed start comments.
	 * 
	 * @param strings  The strings to be treated as comment starters.
	 * @return         This tokenizer, for method chaining.
	 * @see            #commentChar(char)
	 */
	public CommandTokenizer commentStrings(String... strings) {
		for (String string : strings) {
			commentString(string);
		}
		return this;
	}

	/**
	 * Specifies that String string {@code string} ends lines.
	 *
	 * @param string  The character to start comments.
	 * @return         This tokenizer, for method chaining. 
	 */
	public CommandTokenizer eolString(String string) {
		ordinaryString(string);
		eolStrings.add(string);
		return this;
	}

	/**
	 * Specifies that all characters passed escape other characters.
	 * 
	 * @param strings  The strings to end lines.
	 * @return         This tokenizer, for method chaining.
	 * @see            #escapeChar(char)
	 */
	public CommandTokenizer eolStrings(String... strings) {
		for (String string : strings) {
			eolString(string);
		}
		return this;
	}
	
	/**
	 * Specifies that char {@code ch} escapes other characters.
	 *
	 * @param string   The string to escape other characters.
	 * @return         This tokenizer, for method chaining. 
	 */
	public CommandTokenizer escapeString(String string) {
		ordinaryString(string);
		escapeStrings.add(string);
		return this;
	}

	/**
	 * Specifies that all characters passed escape other characters.
	 * 
	 * @param strings  The strings to be treated as escape characters.
	 * @return         This tokenizer, for method chaining.
	 * @see            #escapeChar(char)
	 */
	public CommandTokenizer escapeStrings(String... strings) {
		for (String string : strings) {
			escapeString(string);
		}
		return this;
	}

	/**
	 * Specifies that char {@code ch} should be automatically escaped.
	 *
	 * @param string   The string to be escaped.
	 * @return         This tokenizer, for method chaining. 
	 */
	public CommandTokenizer escapedString(String string) {
		ordinaryString(string);
		escapedStrings.add(string);
		return this;
	}

	/**
	 * Specifies that all characters passed will be escaped.
	 * 
	 * @param strings  The strings to be escaped.
	 * @return         This tokenizer, for method chaining.
	 * @see            #escapedChar(char)
	 */
	public CommandTokenizer escapedStrings(String... strings) {
		for (String string : strings) {
			escapedString(string);
		}
		return this;
	}

	/**
	 * Removes all special meaning for char {@code ch}.
	 *
	 * @param string   The string to be treated normally.
	 * @return         This tokenizer, for method chaining. 
	 */
	private CommandTokenizer ordinaryString(String string) {
		if (escapeStrings.contains(string)) {
			escapeStrings.remove(string);
		}
		if (escapedStrings.contains(string)) {
			escapedStrings.remove(string);
		}
		if (quoteStrings.contains(string)) {
			quoteStrings.remove(string);
		}
		if (whitespaceStrings.contains(string)) {
			whitespaceStrings.remove(string);
		}
		return this;
	}

	/**
	 * Removes all special meaning for all passed characters.
	 * 
	 * @param strings  The strings to be treated normally.
	 * @return         This tokenizer, for method chaining.
	 * @see            #ordinaryChar(char)
	 */
	public CommandTokenizer ordinaryStrings(String... strings) {
		for (String string : strings) {
			ordinaryString(string);
		}
		return this;
	}

	/**
	 * Specifies that char {@code ch} starts/ends quotes.
	 *
	 * @param string   The string to be treated as quotes.
	 * @return         This tokenizer, for method chaining. 
	 */
	public CommandTokenizer quoteString(String string) {
		ordinaryString(string);
		quoteStrings.add(string);
		return this;
	}

	/**
	 * Specifies that all characters passed start/end quotes.
	 * 
	 * @param strings  The strings to be treated as quotes.
	 * @return         This tokenizer, for method chaining.
	 * @see            #quoteChar(char)
	 */
	public CommandTokenizer quoteStrings(String... strings) {
		for (String string : strings) {
			quoteString(string);
		}
		return this;
	}

	/**
	 * Specifies that char {@code ch} is whitespace.
	 *
	 * @param string   The string to be treated as whitespace.
	 * @return         This tokenizer, for method chaining. 
	 */
	public CommandTokenizer whitespaceString(String string) {
		ordinaryString(string);
		whitespaceStrings.add(string);
		return this;
	}

	/**
	 * Specifies that all characters passed are whitespace.
	 * 
	 * @param strings  The strings to be treated as whitespace
	 * @see            #whitespaceChar(char)
	 */
	public CommandTokenizer whitespaceStrings(String... strings) {
		for (String string : strings) {
			whitespaceString(string);
		}
		return this;
	}

	/**
	 * If {@code true}, trims blanks from the start and end. If 
	 * {@code false}, keeps them.
	 * 
	 * @param enabled  Whether to trim or keep.
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer trimBlanks(boolean enabled) {
		trimBlanks = enabled;
		return this;
	}

	/**
	 * Loads the Linux Bourne Shell preset. <br />
	 * <table summary="Bourne preset special characters/strings">
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
	 * 
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer presetSh() {
		clearAll();
		commentString("#");
		escapeString("\\");
		quoteStrings("\"", "'");
		whitespaceStrings(" ", "\t");
		eolStrings("\r\n", "\r", "\n");
		trimBlanks = true;
		return this;
	}

	/**
	 * Loads the Windows batch preset. <br />
	 * <table summary="Batch preset special characters/strings">
	 * 	<tr>
	 * 		<td>comments</td>
	 * 		<td>{@code ::}</td>
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
	 * 
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer presetBatch() {
		clearAll();
		commentString("::");
		escapeString("^");
		quoteStrings("\"", "'");
		whitespaceStrings(" ", "\t", ",", ";", "=");
		eolStrings("\r\n", "\r", "\n");
		trimBlanks = true;
		return this;
	}
	
	/*
	 * Clears all comment strings.
	 *
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer clearCommentStrings() {
		commentStrings.clear();
		return this;
	}
	
	/*
	 * Clears all EOL strings.
	 *
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer clearEolStrings() {
		eolStrings.clear();
		return this;
	}
	
	/*
	 * Clears all escape strings.
	 *
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer clearEscapeStrings() {
		escapeStrings.clear();
		return this;
	}
	
	/*
	 * Clears all escaped strings.
	 *
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer clearEscapedStrings() {
		escapedStrings.clear();
		return this;
	}
	
	/*
	 * Clears all quote strings.
	 *
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer clearQuoteStrings() {
		quoteStrings.clear();
		return this;
	}
	
	/*
	 * Clears all whitespace strings.
	 *
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer clearWhitespaceStrings() {
		whitespaceStrings.clear();
		return this;
	}
	
	/*
	 * Clears all strings.
	 *
	 * @return         This tokenizer, for method chaining.
	 */
	public CommandTokenizer clearAll() {
		clearCommentStrings();
		clearEolStrings();
		clearEscapeStrings();
		clearEscapedStrings();
		clearQuoteStrings();
		clearWhitespaceStrings();
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
		String str = "Token['" + tokens[position] + "'], position " + position;
		return str;
	}

}
