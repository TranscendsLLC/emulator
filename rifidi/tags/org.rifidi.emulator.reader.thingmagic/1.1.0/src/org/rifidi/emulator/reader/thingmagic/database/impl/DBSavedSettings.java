/*
 *  DBSavedSettings.java
 *
 *  Created:	August 11, 2008
 *  Project:	RiFidi Emulator - A Software Simulation Tool for RFID Devices
 *  				http://www.rifidi.org
 *  				http://rifidi.sourceforge.net
 *  Copyright:	Pramari LLC and the Rifidi Project
 *  License:	Lesser GNU Public License (LGPL)
 *  				http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.emulator.reader.thingmagic.database.impl;

import java.util.AbstractList;
import java.util.Map;

import org.rifidi.emulator.reader.thingmagic.database.IDBRow;
import org.rifidi.emulator.reader.thingmagic.database.IDBTable;
import org.rifidi.emulator.reader.thingmagic.database.impl.row.DBSavedSettingRow;

/**
 * @author Jerry Maine - jerry@pramari.com
 *
 */
public class DBSavedSettings extends AbstractList<IDBRow> implements IDBTable {

	private DBSavedSettingRow savedSettings = new DBSavedSettingRow();

	@Override
	public IDBRow get(int arg0) {
		if (arg0 != 0)
			throw new IndexOutOfBoundsException();
		return savedSettings;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public void preTableAccess(Map<String, String> params) {
		// TODO Auto-generated method stub

	}

}
