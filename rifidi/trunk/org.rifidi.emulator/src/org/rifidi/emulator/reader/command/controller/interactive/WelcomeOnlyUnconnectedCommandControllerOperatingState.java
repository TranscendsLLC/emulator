/**
 * 
 */
package org.rifidi.emulator.reader.command.controller.interactive;

import java.util.ArrayList;

import org.rifidi.emulator.io.comm.Communication;
import org.rifidi.emulator.io.comm.CommunicationException;
import org.rifidi.emulator.reader.command.controller.CommandController;
import org.rifidi.emulator.reader.command.controller.abstract_.AbstractCommandController;
import org.rifidi.emulator.reader.command.controller.abstract_.AbstractCommandControllerOperatingState;
import org.rifidi.emulator.reader.control.adapter.CommandAdapter;

/**
 * 
 * 
 * @author Matthew Dean - matt@pramari.com
 */
public class WelcomeOnlyUnconnectedCommandControllerOperatingState extends
		AbstractCommandControllerOperatingState {

	/**
	 * The command which is invoked by this state in order to return a welcome
	 * message.
	 */
	public static final byte[] WELCOME_COMMAND = "welcome".getBytes();

	/**
	 * The adapter to hold to eventually pass to the authenticated state.
	 */
	private CommandAdapter authenticatedAdapter;

	/**
	 * 
	 * @param adapter
	 */
	public WelcomeOnlyUnconnectedCommandControllerOperatingState(
			CommandAdapter unauthenticatedAdapter,
			CommandAdapter authenticatedAdapter) {
		super(unauthenticatedAdapter);
		this.authenticatedAdapter = authenticatedAdapter;
	}

	/**
	 * Processes a connection message, returning a welcome message and username
	 * prompt before switching to the username state.
	 * 
	 * @see org.rifidi.emulator.reader.command.controller.abstract_.AbstractCommandControllerOperatingState#processCommand(byte[],
	 *      org.rifidi.emulator.reader.command.controller.CommandController)
	 */
	@Override
	public ArrayList<Object> processCommand(byte[] command,
			CommandController controller) {
		ArrayList<Object> retList;
		String tempMessage = "";
		

		/* Combine the welcome command handler and login prompt handlers values */
		retList = this
				.getCommandAdapter()
				.executeCommand(
						LoginUnconnectedCommandControllerOperatingState.WELCOME_COMMAND);
		
		
		
		for (Object obj : retList) {
			tempMessage += (String) obj;
		}

		retList.clear();
		retList.add(tempMessage);

		/* Cast to an interactive controller */
		InteractiveCommandController interactiveController = (InteractiveCommandController) controller;

		/* Change state to username state, first attempt */
		interactiveController
				.changeCommandControllerOperatingState(new LoginAuthenticatedCommandControllerOperatingState(
						this.authenticatedAdapter));

		
		/* Return the welcome message / login prompt */
		return retList;
	}

	/**
	 * This method initializes the LoginUnconnected Operating State by sending
	 * the connect command.
	 */
	public void initialize(AbstractCommandController controller,
			Communication comm) {
		/* Generate a connection command */

		ArrayList<Object> respond = this.processCommand(
				AbstractCommandController.CONNECTION_COMMAND, controller);

		for (Object obj : respond) {
			try {
				comm.sendBytes(((String) obj).getBytes());
			} catch (CommunicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
