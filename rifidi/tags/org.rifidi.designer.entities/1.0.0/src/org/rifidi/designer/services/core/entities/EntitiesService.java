/*
 *  EntitiesService.java
 *
 *  Project:		RiFidi Designer - A Virtualization tool for 3D RFID environments
 *  http://www.rifidi.org
 *  http://rifidi.sourceforge.net
 *  Copyright:	    Pramari LLC and the Rifidi Project
 *  License:		Lesser GNU Public License (LGPL)
 *  http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.designer.services.core.entities;

import java.util.List;

import org.rifidi.designer.entities.Entity;
import org.rifidi.designer.entities.grouping.EntityGroup;

/**
 * This service is responsible for manipulating entities and their groups,
 * 
 * @author Jochen Mader Jan 25, 2008
 * @tags
 * 
 */
public interface EntitiesService {

	/**
	 * Add a new entity to the scene.
	 * 
	 * @param ent
	 * @param center true if the entity should be centered in the scene.
	 */
	void addEntity(Entity ent, Boolean center);
	
	/**
	 * Delete entities from the scene
	 * 
	 * @param entities
	 *            the entities to be deleted from the scene
	 */
	void deleteEntities(final List<Entity> entities);

	/**
	 * Add the given entities to the group.
	 * 
	 * @param entityGroup
	 * @param entityIds
	 *            a \n separated list of entity ids
	 */
	void addEntitiesToGroup(EntityGroup entityGroup, String entityIds);

	/**
	 * @return a list of all the entity names
	 */
	List<String> getEntityNames();

	/**
	 * Add a new EntityGroup to the scene
	 * 
	 * @param entityGroup
	 */
	void addEntityGroup(EntityGroup entityGroup);

	/**
	 * Remove an EntityGroup from the scene. Doesn't delete the entities in the
	 * group.
	 * 
	 * @param entityGroup
	 */
	void removeEntityGroup(EntityGroup entityGroup);

	/**
	 * Returns a write protexted list of entity groups
	 * 
	 * @return
	 */
	List<EntityGroup> getEntityGroups();

	/**
	 * Remove an entity from its group.
	 * 
	 * @param entity
	 */
	void ungroupEntity(Entity entity);
}
