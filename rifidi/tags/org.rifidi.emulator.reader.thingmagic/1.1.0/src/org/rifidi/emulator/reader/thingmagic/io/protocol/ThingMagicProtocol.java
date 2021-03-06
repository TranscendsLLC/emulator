/*
 *  ThingMagicProtocol.java
 *
 *  Created:	May 5, 2008
 *  Project:	RiFidi Emulator - A Software Simulation Tool for RFID Devices
 *  				http://www.rifidi.org
 *  				http://rifidi.sourceforge.net
 *  Copyright:	Pramari LLC and the Rifidi Project
 *  License:	Lesser GNU Public License (LGPL)
 *  				http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.emulator.reader.thingmagic.io.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rifidi.emulator.io.protocol.Protocol;
import org.rifidi.emulator.io.protocol.ProtocolValidationException;

/**
 * @author Jerry Maine - jerry@pramari.com
 *
 */
public class ThingMagicProtocol implements Protocol {
	
	private static Log logger = LogFactory.getLog(ThingMagicProtocol.class);

	/* (non-Javadoc)
	 * @see org.rifidi.emulator.io.protocol.Protocol#addProtocol(byte[])
	 */
	@Override
	public final byte[] addProtocol(final byte[] data) {
		// TODO Auto-generated method stub
		logger.debug("ThingMagicProtocol.addProtocol() called: " + new String(data));
		return data;
	}

	//TODO Send the semicolon onward. It will help with handling errors in syntax.
	/* (non-Javadoc)
	 * @see org.rifidi.emulator.io.protocol.Protocol#removeProtocol(byte[])
	 */
	@Override
	public final List<byte[]> removeProtocol(final byte[] data)
			throws ProtocolValidationException {
		logger.debug("ThingMagicProtocal.removeProtocol() called: " + new String(data));
		List<byte[]> removed = new ArrayList<byte[]>();
		String stringData = new String(data);
		
		List<String> removedString = new ArrayList<String>();
		Pattern tokenizer = Pattern.compile(
				"[^;]+;|[^;]+", Pattern.CASE_INSENSITIVE
								| Pattern.DOTALL);
				Matcher tokenFinder = tokenizer.matcher(stringData);

				while (tokenFinder.find()) {
					String temp = tokenFinder.group();
					/*
					 * no need to add empty strings at tokens or
					 * strings with just white space.
					 */
					if (temp.equals("") || temp.matches("\\s+"))
						continue;
					logger.debug(temp);
					removedString.add(temp);
				}
		

		
	
		for (String s: removedString){
			removed.add(s.getBytes());
		}
		
		return removed;
	}

}
