package picocli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class CommandTokenizer {

		private String[] tokens;
		private int position = 0;
		// Characters that cimment out until a newline
		private List<Character> commentChars = new ArrayList<Character>(Arrays.asList('#'));
		// Characters that escape other special characters
		private List<Character> escapeChars = new ArrayList<Character>(Arrays.asList('\\'));
		// Characters to be escaped automatically
		private List<Character> escapedChars = new ArrayList<Character>();
		// Characters that begin/end quotes
		private List<Character> quoteChars = new ArrayList<Character>(Arrays.asList('"', '\''));
		// Characters to be treated as whitespace
		private List<Character> whitespaceChars = new ArrayList<Character>(Arrays.asList(' '));
		// Characters to use to represent a new line
		private List<String> eolPatterns = new ArrayList<String>(Arrays.asList("\r\n", "\r", "\n"));
		// Remove blanks at start and end of a string
		private boolean trimBlanks = true;

		public CommandTokenizer(String cmd) {
			this.tokens = parse(cmd);
		}

		public CommandTokenizer(Scanner input) {
			this.tokens = parse(input);
		}

		private String[] parse(String cmd) {
			String[] cmdSplit = new String[]{""};
			boolean inQuote = false;												// Stores if currently quoted
			char quoteChar = 0;														// Stores which char ends quotes
			for (int i = 0; i < cmd.length(); i++) {
				char curChar = cmd.charAt(i);
				char preChar = (i == 0) ? 0 : cmd.charAt(i - 1);
				String curStr = cmdSplit[cmdSplit.length - 1];
				if (quoteChars.contains(curChar)) {									// Check if has quoting char
					if (i == 0 || !escapeChars.contains(preChar) && 
						(curChar == quoteChar || !inQuote)) {						// Make sure quote is unescaped and the right quote
						inQuote = !inQuote;											// Toggle quoted text
						if (inQuote) {
							quoteChar = curChar;									// Set quote character to current char
						}
					}
					else {
						if (escapeChars.contains(preChar)) {						// If previous char escapes it
							curStr = curStr.substring(0, curStr.length() - 1);		// Cut off last backslash
						}
						curStr += curChar;											// Add quote to string
					}
				}
				else if (commentChars.contains(curChar)) {							// Check if has commenting char
					if (i == 0 || !escapeChars.contains(preChar) && (!inQuote)) {
						break;
					}
					else if (escapeChars.contains(preChar)) {						// If previous char escapes it
						curStr = curStr.substring(0, curStr.length() - 1);			// Cut off escape character
						curStr += curChar;											// Add quote to string
					}
				}
				else if (!inQuote && whitespaceChars.contains(curChar)) {			// If char is whitespace and not in quote
					if (escapeChars.contains(preChar)) {							// If previous char escapes
						curStr = curStr.substring(0, curStr.length() - 1);			// Cut off escape character
						curStr += curChar;											// Add the space in
					}
					else {
						String[] cmdTmp = new String[cmdSplit.length + 1];			// Make array one bigger
						System.arraycopy(cmdSplit, 0, cmdTmp, 0, cmdSplit.length);	// Copy to bigger array
						cmdSplit = cmdTmp;											// Replace old array
						curStr = "";												// Empty current string
					}
				}
				else {
					curStr += curChar;												// Add char to string
				}
				cmdSplit[cmdSplit.length - 1] = curStr;								// Copy back to array
			}
			return cmdSplit;
		}

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
					if (escapeChars.contains(cmd.charAt(cmd.length() - 1))) {										// If new line is escaped
						System.out.print("> ");
						cmd = cmd.substring(0, cmd.length() - 1);
						if (cmd.length() > 0 &&
							whitespaceChars.contains(cmd.charAt(cmd.length() - 1)) &&
							!escapeChars.contains(cmd.charAt(cmd.length() - 2))) {
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
				for (int j = 0; j < this.escapedChars.size(); j++) {
					String escaped = this.escapeChars.get(j).toString();
					cmdSplit[i].replace(escaped, "\\" + escaped);
				}
			}
			return cmdSplit;
		}

		public String[] tokens() {
			return this.tokens;
		}

		public String nextToken() {
			String str = this.tokens[this.position];
			this.position++;
			return str;
		}

		public CommandTokenizer commentChar(int ch) {
			if (ch < 0) {
				this.quoteChars.clear();
			}
			else {
				this.ordinaryChar(ch);
				this.commentChars.add((char) ch);
			}
			return this;
		}

		public CommandTokenizer commentChars(int low, int hi) {
			for (int i = low; i <= hi; i++) {
				this.ordinaryChar(i);
				this.commentChar(i);
			}
			return this;
		}

		public CommandTokenizer commentChars(boolean fl) {
			if (fl) {
				this.quoteChars = new ArrayList<Character>(Arrays.asList('#'));
			}
			else {
				this.commentChar(-1);
			}
			return this;
		}
		
		public CommandTokenizer escapeChar(int ch) {
			if (ch < 0) {
				this.quoteChars.clear();
			}
			else {
				this.ordinaryChar(ch);
				this.escapeChars.add((char) ch);
			}
			return this;
		}

		public CommandTokenizer escapeChars(int low, int hi) {
			for (int i = low; i <= hi; i++) {
				this.ordinaryChar(i);
				this.escapeChar(i);
			}
			return this;
		}
		
		public CommandTokenizer escapeChars(boolean fl) {
			if (fl) {
				this.quoteChars = new ArrayList<Character>(Arrays.asList('\\'));
			}
			else {
				this.escapeChar(-1);
			}
			return this;
		}

		public CommandTokenizer escapedChar(int ch) {
			if (ch < 0) {
				this.escapedChars.clear();
			}
			else {
				this.ordinaryChar(ch);
				this.escapedChars.add((char) ch);
			}
			return this;
		}

		public CommandTokenizer escapedChars(int low, int hi) {
			for (int i = low; i <= hi; i++) {
				this.ordinaryChar(i);
				this.escapedChar(i);
			}
			return this;
		}
		
		public CommandTokenizer escapedChars(boolean fl) {
			if (fl) {
				this.escapedChars = new ArrayList<Character>();
			}
			else {
				this.escapedChar(-1);
			}
			return this;
		}

		// Removes a character from all lists
		private CommandTokenizer ordinaryChar(int ch) {
			char chChar = (char) ch;
			if (this.escapeChars.contains(chChar)) {
				this.escapeChars.remove(chChar);
			}
			if (this.escapedChars.contains(chChar)) {
				this.escapedChars.remove(chChar);
			}
			if (this.quoteChars.contains(chChar)) {
				this.quoteChars.remove(chChar);
			}
			if (this.whitespaceChars.contains(chChar)) {
				this.whitespaceChars.remove(chChar);
			}
			return this;
		}

		public CommandTokenizer ordinaryChars(int low, int hi) {
			for (int i = low; i <= hi; i++) {
				this.ordinaryChar(i);
			}
			return this;
		}

		public CommandTokenizer quoteChar(int ch) {
			if (ch < 0) {
				this.quoteChars.clear();
			}
			else {
				this.ordinaryChar(ch);
				this.quoteChars.add((char) ch);
			}
			return this;
		}

		public CommandTokenizer quoteChars(int low, int hi) {
			for (int i = low; i <= hi; i++) {
				this.ordinaryChar(i);
				this.quoteChar(i);
			}
			return this;
		}
		
		public CommandTokenizer quoteChars(boolean fl) {
			if (fl) {
				this.quoteChars = new ArrayList<Character>(Arrays.asList('"', '\''));
			}
			else{
				this.quoteChar(-1);
			}
			return this;
		}

		public CommandTokenizer whitespaceChar(int ch) {
			if (ch < 0) {
				this.whitespaceChars.clear();
			}
			else {
				this.ordinaryChar(ch);
				this.whitespaceChars.add((char) ch);
			}
			return this;
		}

		public CommandTokenizer whitespaceChars(int low, int hi) {
			for (int i = low; i <= hi; i++) {
				this.ordinaryChar(i);
				this.whitespaceChar(i);
			}
			return this;
		}
		
		public CommandTokenizer whitespaceChars(boolean fl) {
			if (fl) {
				this.whitespaceChars = new ArrayList<Character>(Arrays.asList(' '));
			}
			else {
				this.whitespaceChar(-1);
			}
			return this;
		}
		
		public CommandTokenizer eolPattern(String pattern) {
			if (pattern == null) {
				this.eolPatterns.clear();
			}
			else {
				this.eolPatterns.add(pattern);
			}
			return this;
		}
		
		public CommandTokenizer eolPatterns(boolean fl) {
			if (fl) {
				this.eolPatterns = new ArrayList<String>(Arrays.asList("\r\n", "\r", "\n"));
			}
			else {
				this.eolPatterns.clear();
			}
			return this;
		}

		// Restores defaults
		public CommandTokenizer resetSyntax() {
			this.escapeChars = new ArrayList<Character>(Arrays.asList('\\'));
			this.escapedChars = new ArrayList<Character>();
			this.quoteChars = new ArrayList<Character>(Arrays.asList('"', '\''));
			this.whitespaceChars = new ArrayList<Character>(Arrays.asList(' '));
			this.eolPatterns = new ArrayList<String>(Arrays.asList("\r\n", "\r", "\n"));
			this.trimBlanks = true;
			return this;
		}

		// Based off the "typical" syntax in the StreamTokenizer docs
		@Override
		public String toString() {
			String str = "Token[\"" + this.tokens[this.position] + "\"], position " + this.position;
			return str;
		}

	}