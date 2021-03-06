/*
 *  EPCLogin.java
 *
 *  Project:		RiFidi Emulator - A Software Simulation Tool for RFID Devices
 *  http://www.rifidi.org
 *  http://rifidi.sourceforge.net
 *  Copyright:	    Pramari LLC and the Rifidi Project
 *  License:		Lesser GNU Public License (LGPL)
 *  http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.emulator.reader.epc.commandhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rifidi.emulator.reader.command.CommandObject;
import org.rifidi.emulator.reader.module.abstract_.AbstractReaderSharedResources;
import org.rifidi.emulator.reader.sharedrc.properties.ReaderProperty;

/**
 * @author matt
 * 
 */
public class EPCLogin {
	
	/**
	 * Message logger
	 */
	private static Log logger = LogFactory.getLog(EPCLogin.class);

	/* Token to parse the current value with */
	private final String parseToken = "|";

	/* Constant for authenticate query string */
	private final String authenticate = "authenticate";

	/* Constant for getUsername method name */
	private final String getUsername = "getUsername";

	/* Constant for setUsername method name */
	private final String setUsername = "setUsername";

	/* Constant for getUsername method name */
	private final String getPassword = "getPassword";

	/* Constant for setUsername method name */
	private final String setPassword = "setPassword";

	/**
	 * This is the general Mapping class for all Credential related methods such
	 * as authenticate, get/set Username, get/set Password.
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @param asr
	 *            The SharedResource object which gives references to all shared
	 *            reader variables
	 * @return The CommandObject with the return error msg
	 * 
	 */
	public CommandObject credentialMap(CommandObject arg,
			AbstractReaderSharedResources asr) {

		CommandObject returnCommand = null;

		/*
		 * This method is called when the query name was authenticate and is
		 * used to authenticate the credentials
		 */
		if (arg.getCurrentQueryName().equalsIgnoreCase(authenticate))
			returnCommand = this.authenticate(arg, asr);

		/*
		 * This method is called when the query name was getUsername and is used
		 * to get the Username
		 */
		if (arg.getCurrentQueryName().equalsIgnoreCase(getUsername))
			returnCommand = this.getUsername(arg, asr);

		/*
		 * This method is called when the query name was setUsername and is used
		 * to set the Username
		 */
		if (arg.getCurrentQueryName().equalsIgnoreCase(setUsername))
			returnCommand = this.setUsername(arg, asr);

		/*
		 * This method is called when the query name was getPassword and is used
		 * to get the Password
		 */
		if (arg.getCurrentQueryName().equalsIgnoreCase(getPassword))
			returnCommand = this.getPassword(arg, asr);

		/*
		 * This method is called when the query name was setPassword and is used
		 * to set the Password
		 */
		if (arg.getCurrentQueryName().equalsIgnoreCase(setPassword))
			returnCommand = this.setPassword(arg, asr);

		return returnCommand;

	}

	/**
	 * Sends back the error message for an authenticate error
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @param asr
	 *            The SharedResource object which gives references to all shared
	 *            reader variables
	 * @return The CommandObject with the return error msg
	 */
	public CommandObject authenticationError(CommandObject arg,
			AbstractReaderSharedResources asr) {

		CommandObject returnCmd;
		if (!arg.getPromptSuppress()) {

			/* Call the Alien Common utility method to return the current value */
			returnCmd = EPCLogin.returnDefaultValue(arg, asr);

			/* Now format to remove the Ascii Characters */
			returnCmd = EPCLogin.formatEscapeCharacters(returnCmd);
		} else {
			// arg.getReturnValue().add(EPCLogin.ZEROCHAR);
			return arg;
		}

		// returnCmd.getReturnValue().add(EPCLogin.NEWLINE);

		return (returnCmd);
	}

	/**
	 * Sends back the authentication successful message
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @param asr
	 *            The SharedResource object which gives references to all shared
	 *            reader variables
	 * @return The CommandObject with the return success msg
	 */
	public CommandObject authenticationSuccess(CommandObject arg,
			AbstractReaderSharedResources asr) {
		CommandObject returnCmd;

		if (!arg.getPromptSuppress()) {

			returnCmd = EPCLogin.returnDefaultValue(arg, asr);

			/* Now add the correct padding */
			returnCmd.getReturnValue().add(EPCCommon.NEWLINE);
			returnCmd.getReturnValue().add(EPCCommon.NEWLINE);
		} else {
			return arg;
		}
		return (returnCmd);
	}

	/**
	 * Disconnects the user from the system.
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @param asr
	 *            The SharedResource object which gives references to all shared
	 *            reader variables
	 * @return The CommandObject with the disconnect message
	 */
	public CommandObject disconnectUser(CommandObject arg,
			AbstractReaderSharedResources asr) {

		return (EPCLogin.returnDefaultValue(arg, asr));
	}

	/**
	 * The method called to give the first command prompt
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @param asr
	 *            The SharedResource object which gives references to all shared
	 *            reader variables
	 * @return The CommandObject with the disconnect message
	 */
	public CommandObject commandPrompt(CommandObject arg,
			AbstractReaderSharedResources asr) {

		CommandObject returnCmd;

		if (!arg.getPromptSuppress()) {
			/* Call the Alien Common utility method to return the current value */

			returnCmd = EPCLogin.returnDefaultValue(arg, asr);

			returnCmd.getReturnValue().add(EPCCommon.PROMPT_STRING);
		} else {
			return arg;
		}
		return (returnCmd);

	}

	/**
	 * Prints out the prompt for the password
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @param asr
	 *            The SharedResource object which gives references to all shared
	 *            reader variables
	 * @return The CommandObject with the password prompt
	 */
	public CommandObject passwordPrompt(CommandObject arg,
			AbstractReaderSharedResources asr) {

		CommandObject returnCmd;
		if (!arg.getPromptSuppress()) {

			/* Call the Alien Common utility method to return the current value */
			returnCmd = EPCLogin.returnDefaultValue(arg, asr);

			/* Now format to remove the Ascii Characters */
			returnCmd = EPCLogin.formatEscapeCharacters(returnCmd);
		} else {
			return arg;
		}

		return (returnCmd);
	}

	/**
	 * Prints out the password masked
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @param asr
	 *            The SharedResource object which gives references to all shared
	 *            reader variables
	 * @return The CommandObject with the return value for password correctly
	 *         masked.
	 */
	public CommandObject showPassword(CommandObject arg,
			AbstractReaderSharedResources asr) {

		String retVal = "";

		String passwordValue = (String) arg.getArguments().get(0);
		ArrayList<Object> returnArray = new ArrayList<Object>();
		if (!arg.getPromptSuppress()) {
			/* Apply the mask to the value */
			for (int i = 0; i < passwordValue.length(); i++) {
				retVal += "*";
			}

			logger.debug("The mask is: " + retVal);

			returnArray.add(retVal);

			/* Add an end of reply to it */
			returnArray.add(EPCCommon.NEWLINE);

			arg.setReturnValue(returnArray);
		} else {
			HashMap<String, ReaderProperty> comMap = (HashMap<String, ReaderProperty>) asr
					.getPropertyMap();
			ReaderProperty passwordProp = comMap.get("password");

			String cmdPassword = passwordProp.getPropertyStringValue();

			if (passwordValue.equals(cmdPassword)) {
			}
			arg.setReturnValue(returnArray);
		}
		return arg;

	}

	/**
	 * Prints out the username that was typed in
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @param asr
	 *            The SharedResource object which gives references to all shared
	 *            reader variables
	 * @return The CommandObject with the username echoed
	 */
	public CommandObject showUsername(CommandObject arg,
			AbstractReaderSharedResources asr) {

		String retUsername = "";
		ArrayList<Object> returnArray = new ArrayList<Object>();
		/* Get the username from the argument that was passed in */
		retUsername = (String) arg.getArguments().get(0);
		if (!arg.getPromptSuppress()) {
			/* Create a Return Array with the return value */

			returnArray.add(retUsername);

			/* Add an end of reply to it */
			returnArray.add(EPCCommon.NEWLINE);
			returnArray.add(EPCCommon.NEWLINE);

			/* Set it as the return Value in the return CMDObject */
			arg.setReturnValue(returnArray);
		} else {
			// returnArray.add(EPCLogin.ZEROCHAR);
			arg.setReturnValue(returnArray);
		}
		return arg;
	}

	/**
	 * Prints out the prompt for the username
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @param asr
	 *            The SharedResource object which gives references to all shared
	 *            reader variables
	 * @return The CommandObject with the username prompt
	 */
	public CommandObject usernamePrompt(CommandObject arg,
			AbstractReaderSharedResources asr) {

		CommandObject returnCmd;
		if (!arg.getPromptSuppress()) {

			/* Call the Alien Common utility method to return the current value */
			returnCmd = EPCLogin.returnDefaultValue(arg, asr);
			/* Now format to remove the Ascii Characters */
			returnCmd = EPCLogin.formatEscapeCharacters(returnCmd);
		} else {
			return arg;
		}

		return (returnCmd);
	}

	/**
	 * This prints the Welcome Message
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @param asr
	 *            The SharedResource object which gives references to all shared
	 *            reader variables
	 * @return The CommandObject with the welcome message
	 */
	public CommandObject welcome(CommandObject arg,
			AbstractReaderSharedResources asr) {

		logger.debug("In the welcome message");

		/* Call the Alien Common utility method to return the current value */
		CommandObject returnCmd = EPCLogin.returnDefaultValue(arg, asr);

		/* Now format to remove the Ascii Characters */
		returnCmd = EPCLogin.formatEscapeCharacters(returnCmd);

		/* Add a Newline to the end of the message */
		returnCmd.getReturnValue().add(EPCCommon.NEWLINE);
		/* Add a Newline to the end of the message */
		returnCmd.getReturnValue().add(EPCCommon.NEWLINE);

		return (returnCmd);
	}

	/**
	 * Utility method for getting the Username Variable<br>
	 * A custom method is needed because this is implemented as a pipe delimited
	 * value in the xml.
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @return The CommandObject with the returnValue from the current Value.
	 */
	public CommandObject getUsername(CommandObject arg,
			AbstractReaderSharedResources asr) {

		/* username variables from command */
		String cmdCurrentValue = "";
		String cmdUsername = "";

		HashMap<String, ReaderProperty> comMap = (HashMap<String, ReaderProperty>) asr
				.getPropertyMap();
		ReaderProperty newProp = comMap.get(arg.getDisplayName().toLowerCase());

		/* get the current value (UN|PWD) so it can be parsed */
		cmdCurrentValue = newProp.getPropertyStringValue();

		/*
		 * Parse the username and password from the current value in xml
		 * currently this is implemented as a pipe delimited text value
		 */
		StringTokenizer st = new StringTokenizer(cmdCurrentValue, parseToken);
		cmdUsername = st.nextToken();

		/* Now add it back to the return value */
		ArrayList<Object> retVal = new ArrayList<Object>();
		logger.debug("adding command username: " +cmdUsername);
		retVal.add(cmdUsername);
		arg.setReturnValue(retVal);
		return arg;

	}

	/**
	 * Utility method for setting the Username Variable<br>
	 * A custom method is needed because this is implemented as a pipe delimited
	 * value in the xml.
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @return The CommandObject with the returnValue from the current Value.
	 */
	public CommandObject setUsername(CommandObject arg,
			AbstractReaderSharedResources asr) {
		ArrayList<Object> retVal = new ArrayList<Object>();

		HashMap<String, ReaderProperty> comMap = (HashMap<String, ReaderProperty>) asr
				.getPropertyMap();
		ReaderProperty newProp = comMap.get(arg.getDisplayName().toLowerCase());

		retVal.add(newProp.getPropertyStringValue());
		arg.setReturnValue(retVal);
		return arg;

	}

	/**
	 * Utility method for getting the Username Variable<br>
	 * A custom method is needed because this is implemented as a pipe delimited
	 * value in the xml.
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @return The CommandObject with the returnValue from the current Value.
	 */
	public CommandObject getPassword(CommandObject arg,
			AbstractReaderSharedResources asr) {

		/* username variables from command */
		String cmdCurrentValue = "";
		String cmdTempUsername = "";
		String cmdPassword = "";

		HashMap<String, ReaderProperty> comMap = (HashMap<String, ReaderProperty>) asr
				.getPropertyMap();
		ReaderProperty newProp = comMap.get(arg.getDisplayName().toLowerCase());

		/* get the current value (UN|PWD) so it can be parsed */
		cmdCurrentValue = newProp.getPropertyStringValue();

		/*
		 * Parse the username and password from the current value in xml
		 * currently this is implemented as a pipe delimited text value
		 */
		StringTokenizer st = new StringTokenizer(cmdCurrentValue, parseToken);
		cmdTempUsername = st.nextToken();
		cmdPassword = st.nextToken();
		cmdTempUsername.trim();

		/* Now add it back to the return value */
		ArrayList<Object> retVal = new ArrayList<Object>();
		retVal.add(cmdPassword);
		arg.setReturnValue(retVal);
		return arg;

	}

	/**
	 * Utility method for setting the Username Variable<br>
	 * A custom method is needed because this is implemented as a pipe delimited
	 * value in the xml.
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @return The CommandObject with the returnValue from the current Value.
	 */
	public CommandObject setPassword(CommandObject arg,
			AbstractReaderSharedResources asr) {
		ArrayList<Object> retVal = new ArrayList<Object>();

		HashMap<String, ReaderProperty> comMap = (HashMap<String, ReaderProperty>) asr
				.getPropertyMap();
		ReaderProperty newProp = comMap.get(arg.getDisplayName().toLowerCase());

		retVal.add(newProp.getPropertyStringValue());
		arg.setReturnValue(retVal);
		return arg;

	}

	/**
	 * Authenticates the User given the username and password this method is a
	 * custom implementation for the Alien
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @return The CommandObject with the return values as true or false
	 */
	public CommandObject authenticate(CommandObject arg,
			AbstractReaderSharedResources asr) {
		logger.debug("IN THE AUTHENTICATE");
		
		
		/* The message to return back */
		String authenticationReply = "false";

		/* username variables from arguments */
		String argUsername = "";
		String argPassword = "";

		/* username variables from command */
		// String cmdCurrentValue = "";
		String cmdUsername = "";
		String cmdPassword = "";
		
		logger.debug("ASR IS: " + asr);

		logger.debug("BEFORE THE GET PROPERTY MAP");
		HashMap<String, ReaderProperty> comMap = (HashMap<String, ReaderProperty>) asr
				.getPropertyMap();
		logger.debug("JUST BEFORE GET USERNAME/PASSWORD");
		ReaderProperty usernameProp = comMap.get("username");
		ReaderProperty passwordProp = comMap.get("password");
		logger.debug("JUST AFTER GET USERNAME/PASSWORD");

		/* Get the arguments ArrayList for debug purposes */
		ArrayList<Object> argumentsList = arg.getArguments();

		for (Object i : argumentsList) {
			logger.debug(i.toString());
		}

		/* get the username and passwords as arguments */
		argUsername = (String) argumentsList.get(0);
		argPassword = (String) argumentsList.get(1);

		argUsername = argUsername.trim();
		argPassword = argPassword.trim();

		/*
		 * Parse the username and password from the current value in xml
		 * currently this is implemented as a pipe delimited text value
		 */
		// StringTokenizer st = new StringTokenizer(cmdCurrentValue,
		// parseToken);
		// cmdUsername = st.nextToken();
		// cmdPassword = st.nextToken();
		try {
			cmdUsername = usernameProp.getPropertyStringValue();
			cmdPassword = passwordProp.getPropertyStringValue();

		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.debug(" Default username = " + cmdUsername);
		logger.debug(" Default password = " + cmdPassword);

		if (cmdUsername.equals(argUsername) && cmdPassword.equals(argPassword))
			/* This means that user authenticated correctly */
			authenticationReply = "true";
		else
			/* False is sent back when the user did not authenticate */
			authenticationReply = "false";

		logger.debug("Authentication return is: " + authenticationReply);
		/* Set the Return Array to return back the value */
		ArrayList<Object> returnArray = new ArrayList<Object>();
		returnArray.add(authenticationReply);

		/* Set authentication reply message as return value */
		arg.setReturnValue(returnArray);

		logger.debug("RETURNING THE AUTHENTICATE: " + arg);
		return (arg);
	}

	/**
	 * Returns the default value of the command.
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @return The CommandObject with the default value in the return value.
	 */
	private static CommandObject returnDefaultValue(CommandObject arg,
			AbstractReaderSharedResources asr) {
		arg.getReturnValue().clear();
		arg.getReturnValue().add(arg.getDefaultValue());
		return arg;
	}

	/**
	 * Returns the default value of the command.
	 * 
	 * @param arg
	 *            The CommandObject which contains the information from the
	 *            method.
	 * @return The CommandObject with the default value in the return value.
	 */
	private static CommandObject formatEscapeCharacters(CommandObject arg) {
		String retString = "";
		for (Object i : arg.getReturnValue()) {
			retString += (String) i;
		}
		retString = retString.replace(EPCCommon.NEWLINELITERAL,
				EPCCommon.NEWLINE);
		arg.getReturnValue().clear();
		arg.getReturnValue().add(retString);
		return arg;
	}
}
