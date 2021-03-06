/*
 *  Command.java
 *
 *  Created:	August 7, 2008
 *  Project:	RiFidi Emulator - A Software Simulation Tool for RFID Devices
 *  				http://www.rifidi.org
 *  				http://rifidi.sourceforge.net
 *  Copyright:	Pramari LLC and the Rifidi Project
 *  License:	Lesser GNU Public License (LGPL)
 *  				http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.emulator.reader.thingmagic.commandobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jerry Maine - jerry@pramari.com
 * 
 */
public abstract class Command {

	final static public String A_WORD = "\\w+";

	final static public String WHITE_SPACE = "\\s+";

	final static public String COMMA_WITH_WS = "\\s*,\\s*";

	final static public String EQUALS_WITH_WS = "\\s*=\\s*";

	final static public String GREATER_THAN_WITH_WS = "\\s*>\\s*";

	final static public String LESS_THAN_WITH_WS = "\\s*<\\s*";

	final static public String GREATER_THAN_EQUALS_W_WS = "\\s*>=\\s*";

	final static public String LESS_THAN_EQUALS_W_WS = "\\s*<=\\s*";

	final static public String NOT_EQUALS_W_WS = "\\s*<>\\s*";

	/*
	 * This regex looks for a Word, or a series of spaces on either side of any
	 * single comparison operator or comma, or a single parentheses (opening or
	 * closing). At the last ... any dangling spaces not attached to the above
	 * groups and then anything else as a single group.
	 * 
	 * This makes it really easy to parse the command string as it becomes
	 * really predictable tokens.
	 */
	static private Pattern TOKENIZER = Pattern.compile(
			/*NOTE: Order of groups here is *very* important! 
			 * Evaluates first left to right!
			 */
			// anything less...
			"[^\\s\\w,<>=\\(\\)\\u0027]|"
					+
					// groups we are looking for...
					//TODO: Use the constants above for this.
					"\\w+|" + "\\u0027|" + "\\s*<>\\s*|" + "\\s*>=\\s*|"
					+ "\\s*<=\\s*|" + "\\s*=\\s*|" + "\\s*,\\s*|"
					+ "\\s*>\\s*|" + "\\s*<\\s*|" + "\\s+|" + "\\(|" + "\\)|",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	/*
	 * common method to tokenize the command used by most command objects.
	 */
	protected List<String> tokenizer(String command) {
		List<String> tokens = new ArrayList<String>();
		Matcher tokenFinder = TOKENIZER.matcher(command.trim());

		while (tokenFinder.find()) {
			String temp = tokenFinder.group();
			/*
			 * no need to add empty strings at tokens.
			 */
			// TODO: Figure out why we are getting empty stings as tokens.
			if (temp.equals(""))
				continue;
			tokens.add(temp);
		}
		return tokens;
	}

	public abstract ArrayList<Object> execute();

	/**
	 * 
	 * @return the original unmodified command sent to the constructor of the
	 *         command object
	 */
	public abstract String toCommandString();
}
