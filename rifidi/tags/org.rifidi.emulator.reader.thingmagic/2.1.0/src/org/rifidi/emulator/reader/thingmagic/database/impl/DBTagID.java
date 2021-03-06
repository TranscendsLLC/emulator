/*
 *  DBTagID.java
 *
 *  Created:	August 7, 2008
 *  Project:	RiFidi Emulator - A Software Simulation Tool for RFID Devices
 *  				http://www.rifidi.org
 *  				http://rifidi.sourceforge.net
 *  Copyright:	Pramari LLC and the Rifidi Project
 *  License:	Lesser GNU Public License (LGPL)
 *  				http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.emulator.reader.thingmagic.database.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rifidi.emulator.reader.sharedrc.radio.generic.GenericRadio;
import org.rifidi.emulator.reader.sharedrc.tagmemory.TagMemory;
import org.rifidi.emulator.reader.thingmagic.database.IDBRow;
import org.rifidi.emulator.reader.thingmagic.database.IDBTable;
import org.rifidi.emulator.reader.thingmagic.database.impl.row.DBTagIDRow;
import org.rifidi.services.tags.impl.RifidiTag;

/**
 * @author Jerry Maine - jerry@pramari.com
 * 
 * This is the tag memory of the ThingMagic Reader emulator.
 * It looks like a 'table' to mirror how it logically looks when 
 * a command to get or update tag information is sent to the emulator.
 */
public class DBTagID implements IDBTable, TagMemory {
	private static Log logger = LogFactory.getLog(DBTagID.class);
	private List<DBTagIDRow> tags = new ArrayList<DBTagIDRow>();
	private boolean suspended = false;
	private GenericRadio radio;

	public DBTagID() {
		logger.debug("Creating Tag Memory...");
		clear();
	}

	@Override
	public IDBRow get(int index) {
		logger.debug("Getting tag at tag memmory location " + index);
		if (suspended)
			throw new ArrayIndexOutOfBoundsException(
					"Trying to use any tag memory index while tag memory is suspended");

		return tags.get(index);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		if (suspended) {
			return 0;
		} else {
			return tags.size();	
		}
	}

	@Override
	public Collection<RifidiTag> getTagReport() {
		logger.debug("Getting tag report.");
		List<RifidiTag> tagsToReturn = new ArrayList<RifidiTag>();
		if (!suspended) {
			for (DBTagIDRow t : tags) {
				tagsToReturn.add(t.getTag());
			}
		}
		return tagsToReturn;
	}

	@Override
	public void resume() {
		//TODO Should we inform shared resources of this??
		this.suspended = false;
	}

	@Override
	public void suspend() {
		//TODO Should we inform shared resources of this??
		this.suspended = true;
	}

	@Override
	public void updateMemory(Collection<RifidiTag> tagsToAdd) {
		logger.debug("Updating tag memory with tags: " + tagsToAdd);
		// TODO Think of a better way of doing this.
		for (RifidiTag t : tagsToAdd) {
			DBTagIDRow tagRowData = new DBTagIDRow(t);
			if (tags.contains(tagRowData)) {
				// TODO increment read count
			} else {
				tags.add(tagRowData);
			}
		}

	}

	@Override
	public void clear() {
		logger.debug("Clearing tag memory.");
		tags.clear();
	}

	@Override
	public void preTableAccess(Map<String, String> params) {
		//TODO: Add support for user supplied timeout values here.
		/*
		 * default timeout for getting tags.
		 */
		long timeout = 250;

		/* clear all previously accumulated tags. */
		//TODO: There may be a subtle minor bug here.
		clear();
		try {

			Thread.sleep(timeout); // let the tags gather for a moment.
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/* Force the radio to scan now. */
		radio.scan(null, this);
	}

	public void setRadio(GenericRadio radio) {
		// TODO Auto-generated method stub
		this.radio = radio;
	}

}
