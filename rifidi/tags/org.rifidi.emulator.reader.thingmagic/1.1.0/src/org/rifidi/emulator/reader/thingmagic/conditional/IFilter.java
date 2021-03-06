/*
 *  IFilter.java
 *
 *  Created:	August 7, 2008
 *  Project:	RiFidi Emulator - A Software Simulation Tool for RFID Devices
 *  				http://www.rifidi.org
 *  				http://rifidi.sourceforge.net
 *  Copyright:	Pramari LLC and the Rifidi Project
 *  License:	Lesser GNU Public License (LGPL)
 *  				http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.emulator.reader.thingmagic.conditional;

import java.util.List;

import org.rifidi.emulator.reader.thingmagic.database.IDBRow;

/**
 * @author Jerry Maine - jerry@pramari.com
 *
 */
public interface IFilter {
	List<IDBRow> filter(List<IDBRow> rows);
}
