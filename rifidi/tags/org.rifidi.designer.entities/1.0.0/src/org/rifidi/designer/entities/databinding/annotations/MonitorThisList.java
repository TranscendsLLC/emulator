/*
 *  MonitorThisList.java
 *
 *  Project:		RiFidi Designer - A Virtualization tool for 3D RFID environments
 *  http://www.rifidi.org
 *  http://rifidi.sourceforge.net
 *  Copyright:	    Pramari LLC and the Rifidi Project
 *  License:		Lesser GNU Public License (LGPL)
 *  http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.designer.entities.databinding.annotations;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for telling the databinding which list to monitor for changes.
 * 
 * @author Jochen Mader Jan 17, 2008
 * 
 */
@Target({ TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MonitorThisList {
	/**
	 * Name of the list to monitor.
	 * @return
	 */
	String name();
}
