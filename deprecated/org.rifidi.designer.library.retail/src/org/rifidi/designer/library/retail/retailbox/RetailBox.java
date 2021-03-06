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
package org.rifidi.designer.library.retail.retailbox;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import org.rifidi.designer.entities.VisualEntity;
import org.rifidi.designer.entities.interfaces.INeedsPhysics;
import org.rifidi.designer.entities.interfaces.IProduct;
import org.rifidi.designer.entities.rifidi.ITagged;
import org.rifidi.designer.library.retail.shelf.Shelf;
import org.rifidi.tags.impl.RifidiTag;

import com.jme.input.InputHandler;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.PhysicsSpace;

/**
 * Box for retail scenarios.
 * 
 * @author Jochen Mader - jochen@pramari.com - Apr 3, 2008
 * 
 */
public class RetailBox extends VisualEntity implements INeedsPhysics, ITagged, IProduct<Shelf> {

	/** Reference to the current physics space. */
	@XmlTransient
	private PhysicsSpace physicsSpace;
	/** Model for shared meshes */
	@XmlTransient
	private static Node model = null;
	/** Translation for initial translation. Never used afterwards. */
	@XmlTransient
	private Vector3f startTranslation;
	/** Tag that's associated with this box. */
	@XmlIDREF
	private RifidiTag tag;
	/** Reference to the producer of this box. */
	@XmlIDREF
	private Shelf producer;
	
	/**
	 * Constructor. 
	 */
	public RetailBox() {
		super();
		setName("Retail box");
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
		URI modelpath = null;
		try {
			modelpath = getClass().getClassLoader().getResource(
					"org/rifidi/designer/library/retail/retailbox/box.jme")
					.toURI();
			model = (Node) BinaryImporter.getInstance().load(modelpath.toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		DynamicPhysicsNode phys = physicsSpace.createDynamicNode();
		phys.attachChild(model);
		phys.setLocalScale(0.5f);
		phys.setLocalTranslation(startTranslation);
		phys.updateModelBound();
		phys.generatePhysicsGeometry();
		phys.setActive(true);
		phys.setIsCollidable(true);
		setNode(phys);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#loaded()
	 */
	@Override
	public void loaded() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.designer.entities.interfaces.INeedsPhysics#setCollisionHandler
	 * (com.jme.input.InputHandler)
	 */
	@Override
	public void setCollisionHandler(InputHandler collisionHandler) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.designer.entities.interfaces.INeedsPhysics#setPhysicsSpace(
	 * com.jmex.physics.PhysicsSpace)
	 */
	@Override
	public void setPhysicsSpace(PhysicsSpace physicsSpace) {
		this.physicsSpace = physicsSpace;
	}

	/**
	 * @param startTranslation
	 *            the startTranslation to set
	 */
	public void setStartTranslation(Vector3f startTranslation) {
		this.startTranslation = startTranslation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.VisualEntity#setLOD(int)
	 */
	@Override
	public void setLOD(int lod) {
		// No LOD for this one.

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.VisualEntity#getBoundingNode()
	 */
	@Override
	public Node getBoundingNode() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.rifidi.designer.entities.rifidi.ITagged#getRifidiTag()
	 */
	@Override
	public RifidiTag getRifidiTag() {
		return tag;
	}

	/* (non-Javadoc)
	 * @see org.rifidi.designer.entities.rifidi.ITagged#setRifidiTag(org.rifidi.tags.impl.RifidiTag)
	 */
	@Override
	public void setRifidiTag(RifidiTag tag) {
		this.tag=tag;
	}

	/* (non-Javadoc)
	 * @see org.rifidi.designer.entities.interfaces.IProduct#getProducer()
	 */
	@Override
	public Shelf getProducer() {
		return producer;
	}

	/**
	 * @param producer the producer to set
	 */
	public void setProducer(Shelf producer) {
		this.producer = producer;
	}

}
