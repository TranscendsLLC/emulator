/*
 *  Clothing.java
 *
 *  Project:		RiFidi Designer - A Virtualization tool for 3D RFID environments
 *  http://www.rifidi.org
 *  http://rifidi.sourceforge.net
 *  Copyright:	    Pramari LLC and the Rifidi Project
 *  License:		Lesser GNU Public License (LGPL)
 *  http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.designer.library.retail.shelf;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.rifidi.designer.entities.VisualEntity;
import org.rifidi.designer.entities.interfaces.NeedsPhysics;
import org.rifidi.designer.entities.interfaces.VisualEntityHolder;
import org.rifidi.designer.entities.placement.BinaryPattern;
import org.rifidi.designer.library.retail.Position;
import org.rifidi.designer.library.retail.retailbox.RetailBox;

import com.jme.input.InputHandler;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.PhysicsSpace;

/**
 * 
 * 
 * @author Jochen Mader - jochen@pramari.com - Apr 3, 2008
 * 
 */
public class Shelf extends VisualEntity implements VisualEntityHolder,
		NeedsPhysics {

	/**
	 * Reference to the collision input handler.
	 */
	private InputHandler inputHandler;
	/**
	 * Reference to the current physics space.
	 */
	private PhysicsSpace physicsSpace;
	/**
	 * Container for entities inside the holder.
	 */
	private List<VisualEntity> entities;
	/**
	 * List of available positions.
	 */
	private List<Position> positions;
	/**
	 * Capacity of the container.
	 */
	private int capacity = 9;
	/**
	 * Number of currently stored items.
	 */
	private int itemCount = 0;
	/**
	 * Model for shared meshes
	 */
	private static Node model = null;

	/**
	 * 
	 */
	public Shelf() {
		super();
		setName("Shelf");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#destroy()
	 */
	@Override
	public void destroy() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#init()
	 */
	@Override
	public void init() {
		BinaryPattern pattern = new BinaryPattern();
		pattern.setPattern(new boolean[][] {
				{ true, true, true, true, true, true, true, true },
				{ true, true, true, true, true, true, true, true } });
		setPattern(pattern);
		URI modelpath = null;
		Node node = new Node();
		try {
			modelpath = getClass().getClassLoader().getResource(
					"org/rifidi/designer/library/retail/shelf/shelf.jme")
					.toURI();
			model = (Node) BinaryImporter.getInstance().load(modelpath.toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		model.setLocalTranslation(new Vector3f(0, 3.7f, 0));
		model.setLocalScale(5.0f);
		node.attachChild(model);
		entities = new ArrayList<VisualEntity>();
		positions = new ArrayList<Position>();
		positions
				.add(new Position(new Vector3f(-1.9f, 6f, 0), new Quaternion()));
		positions.add(new Position(new Vector3f(-0f, 6f, 0), new Quaternion()));
		positions
				.add(new Position(new Vector3f(1.9f, 6f, 0), new Quaternion()));
		positions.add(new Position(new Vector3f(-1.9f, 3.5f, 0),
				new Quaternion()));
		positions
				.add(new Position(new Vector3f(-0f, 3.5f, 0), new Quaternion()));
		positions.add(new Position(new Vector3f(1.9f, 3.5f, 0),
				new Quaternion()));
		positions
				.add(new Position(new Vector3f(-1.9f, 1f, 0), new Quaternion()));
		positions.add(new Position(new Vector3f(-0f, 1f, 0), new Quaternion()));
		positions
				.add(new Position(new Vector3f(1.9f, 1f, 0), new Quaternion()));
		for (int count = 0; count < capacity; count++) {
			RetailBox box = new RetailBox();
			box.setStartTranslation((Vector3f) positions.get(count).translation
					.clone());
			entities.add(box);
			itemCount++;
		}
		setNode(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#loaded()
	 */
	@Override
	public void loaded() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.VisualEntityHolder#addVisualEntity(org.rifidi.designer.entities.VisualEntity)
	 */
	@Override
	public void addVisualEntity(final VisualEntity visualEntity) {
		if (accepts(visualEntity) && !isFull()) {
			entities.add(visualEntity);
			GameTaskQueueManager.getManager().update(new Callable<Object>() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.concurrent.Callable#call()
				 */
				@Override
				public Object call() throws Exception {
					visualEntity.getNode().removeFromParent();
					int count = 0;
					while (count < capacity) {
						if (entities.get(count) == null) {
							break;
						}
						count++;
					}
					getNode().attachChild(visualEntity.getNode());
					visualEntity.getNode()
							.setLocalTranslation(
									(Vector3f) positions.get(count).translation
											.clone());
					visualEntity.getNode().setLocalRotation(
							new Quaternion(positions.get(count).rotation));
					visualEntity.getNode().setIsCollidable(false);
					((DynamicPhysicsNode) visualEntity.getNode())
							.setActive(false);
					entities.set(count, visualEntity);
					return null;
				}

			});
			itemCount++;
			return;
		}
		throw new RuntimeException("Stupid!! Wrong type or full: "
				+ accepts(visualEntity) + " " + isFull());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.VisualEntityHolder#getVisualEntity()
	 */
	@Override
	public VisualEntity getVisualEntity() {
		VisualEntity ret = null;
		int count = 0;
		for (VisualEntity vs : entities) {
			if (vs != null) {
				ret = vs;
				ret.getNode().setIsCollidable(true);
				break;
			}
			count++;
		}
		count = count == capacity ? count - 1 : count;
		entities.set(count, null);
		itemCount--;
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.VisualEntityHolder#getVisualEntity(org.rifidi.designer.entities.VisualEntity)
	 */
	@Override
	public VisualEntity getVisualEntity(VisualEntity visualEntity) {
		if (entities.contains(visualEntity)) {
			entities.set(entities.indexOf(visualEntity), null);
			itemCount--;
			visualEntity.getNode().setIsCollidable(true);
			return visualEntity;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.VisualEntityHolder#getVisualEntitySet()
	 */
	@Override
	public List<VisualEntity> getVisualEntityList() {
		return Collections.unmodifiableList(entities);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.VisualEntityHolder#isFull()
	 */
	@Override
	public boolean isFull() {
		return capacity <= itemCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.NeedsPhysics#setCollisionHandler(com.jme.input.InputHandler)
	 */
	@Override
	public void setCollisionHandler(InputHandler collisionHandler) {
		this.inputHandler = collisionHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.NeedsPhysics#setPhysicsSpace(com.jmex.physics.PhysicsSpace)
	 */
	@Override
	public void setPhysicsSpace(PhysicsSpace physicsSpace) {
		this.physicsSpace = physicsSpace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.VisualEntityHolder#accepts(org.rifidi.designer.entities.VisualEntity)
	 */
	@Override
	public boolean accepts(VisualEntity visualEntity) {
		return visualEntity instanceof RetailBox;
	}

}
