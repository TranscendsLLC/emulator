/*
 *  BoxproducerEntityGID96.java
 *
 *  Project:		RiFidi Designer - A Virtualization tool for 3D RFID environments
 *  http://www.rifidi.org
 *  http://rifidi.sourceforge.net
 *  Copyright:	    Pramari LLC and the Rifidi Project
 *  License:		Lesser GNU Public License (LGPL)
 *  http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.designer.library.basemodels.boxproducer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.monklypse.core.NodeHelper;
import org.rifidi.designer.entities.Entity;
import org.rifidi.designer.entities.VisualEntity;
import org.rifidi.designer.entities.annotations.Property;
import org.rifidi.designer.entities.databinding.IEntityObservable;
import org.rifidi.designer.entities.databinding.annotations.MonitoredProperties;
import org.rifidi.designer.entities.interfaces.IHasSwitch;
import org.rifidi.designer.entities.interfaces.IProducer;
import org.rifidi.designer.entities.internal.RifidiTagWithParent;
import org.rifidi.designer.library.basemodels.cardbox.CardboxEntity;
import org.rifidi.designer.services.core.entities.EntitiesService;
import org.rifidi.services.annotations.Inject;
import org.rifidi.services.tags.IRifidiTagService;
import org.rifidi.services.tags.exceptions.RifidiTagNotAvailableException;
import org.rifidi.services.tags.model.IRifidiTagContainer;
import org.rifidi.tags.impl.RifidiTag;

import com.jme.bounding.BoundingBox;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.SharedNode;
import com.jme.scene.Spatial.CullHint;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.shape.Disk;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.BlendState.DestinationFunction;
import com.jme.scene.state.BlendState.SourceFunction;
import com.jme.system.DisplaySystem;

/**
 * BoxproducerContinuousEntity: Used for generating boxes.
 * 
 * @author Jochen Mader Oct 8, 2007
 * @author Dan West
 */
@MonitoredProperties(names = { "name" })
@XmlAccessorType(XmlAccessType.FIELD)
public class BoxproducerEntity extends VisualEntity implements IHasSwitch,
		IRifidiTagContainer, IEntityObservable, PropertyChangeListener,
		IProducer<CardboxEntity> {

	/** Logger for this class. */
	@XmlTransient
	private static Log logger = LogFactory.getLog(BoxproducerEntity.class);
	/** Seconds per box. */
	private float speed;
	/** Production thread. */
	@XmlTransient
	private BoxproducerEntityThread thread;
	/** State of the switch. */
	private boolean running = false;
	/** Is the entity paused. */
	private boolean paused = true;
	/** Source for shared meshes. */
	@XmlTransient
	private Node model;
	/** Reference to the product service. */
	@XmlTransient
	private EntitiesService entitiesService;
	/** Stack shared with the boxproducer thread. */
	@XmlTransient
	private Stack<RifidiTag> tagStack;
	/** Set containing all available tags. */
	@XmlIDREF
	private List<RifidiTag> tags;
	/** Reference to the tag service. */
	@XmlTransient
	private IRifidiTagService tagService;
	/** List of wrapper objects that bind tags and container together. */
	@XmlTransient
	private WritableList wrappers;
	@XmlIDREF
	private List<CardboxEntity> products;
	private Boolean continuous = false;

	/**
	 * Constructor
	 */
	public BoxproducerEntity() {
		this.speed = 4;
		this.tagStack = new Stack<RifidiTag>();
		this.tags = new ArrayList<RifidiTag>();
		this.wrappers = new WritableList();
		this.products = new ArrayList<CardboxEntity>();
		setName("Batch Boxproducer");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.rifidi.designer.entities.interfaces.IEntityObservable#
	 * addListChangeListener
	 * (org.eclipse.core.databinding.observable.list.IListChangeListener)
	 */
	@Override
	public void addListChangeListener(IListChangeListener changeListener) {
		wrappers.addListChangeListener(changeListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.rifidi.designer.entities.interfaces.IEntityObservable#
	 * removeListChangeListener
	 * (org.eclipse.core.databinding.observable.list.IListChangeListener)
	 */
	@Override
	public void removeListChangeListener(IListChangeListener changeListener) {
		wrappers.removeListChangeListener(changeListener);
	}

	/**
	 * @return the speed
	 */
	public float getSpeed() {
		return speed;
	}

	/**
	 * @param speed
	 *            the speed to set
	 */
	@Property(displayName = "Production speed", description = "production rate of boxes", readonly = false, unit = "sec/box")
	public void setSpeed(float speed) {
		this.speed = speed;
		if (thread != null) {
			thread.setInterval((int) speed * 1000);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#init()
	 */
	@Override
	public void init() {
		if (model == null) {
			model = new Node();
			attachCylinder(model);
		}
		setCollides(false);

		BlendState as = DisplaySystem.getDisplaySystem().getRenderer()
				.createBlendState();
		as.setBlendEnabled(true);
		as.setSourceFunction(SourceFunction.SourceAlpha);
		as.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
		as.setBlendEnabled(true);
		as.setEnabled(true);

		MaterialState ms = DisplaySystem.getDisplaySystem().getRenderer()
				.createMaterialState();
		ms.setDiffuse(new ColorRGBA(.2f, .75f, .8f, 1).multLocal(.7f));
		ms.setEnabled(true);
		model.setRenderState(ms);

		Node node = new Node(getEntityId());
		Node sharednode = new SharedNode("maingeometry", model);
		node.attachChild(sharednode);

		sharednode.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
		sharednode.setRenderState(as);

		sharednode.setModelBound(new BoundingBox());
		sharednode.updateModelBound();

		setNode(node);

		Node _node = new Node("hiliter");
		attachCylinder(_node);
		_node.setModelBound(new BoundingBox());
		_node.updateModelBound();
		_node.setCullHint(CullHint.Always);
		getNode().attachChild(_node);

		logger.debug(NodeHelper.printNodeHierarchy(getNode(), 3));

		thread = new BoxproducerEntityThread(this, entitiesService, products,
				tagStack);
		thread.setInterval((int) speed * 1000);
		thread.start();
		for (RifidiTag tag : getTags()) {
			RifidiTagWithParent r = new RifidiTagWithParent();
			r.parent = this;
			r.tag = tag;
			wrappers.add(r);
		}
	}

	private void attachCylinder(Node node) {
		Cylinder cyl = new Cylinder("cyl", 32, 32, 1.5f, 0.4f);
		cyl.setModelBound(new BoundingBox());
		cyl.updateModelBound();
		Disk disk1 = new Disk("top", 32, 32, 1.5f);
		node.setLocalTranslation(new Vector3f(0, 12f, 0));
		node.setLocalRotation(new Quaternion(new float[] {
				(float) Math.toRadians(270), 0, 0 }));
		node.attachChild(cyl);
		node.attachChild(disk1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#loaded()
	 */
	@Override
	public void loaded() {
		if (model == null) {
			model = new Node();
			attachCylinder(model);
		}

		Set<RifidiTag> temptags = new HashSet<RifidiTag>(tags);
		for (CardboxEntity vis : products) {
			temptags.remove(vis.getRifidiTag());
		}
		tagStack.addAll(temptags);
		thread = new BoxproducerEntityThread(this, entitiesService, products,
				tagStack);
		thread.setInterval((int) speed * 1000);
		thread.setPaused(paused);
		thread.start();
		if (running)
			turnOn();
		for (RifidiTag tag : tags) {
			try {
				tagService.takeRifidiTag(tag, this);
			} catch (RifidiTagNotAvailableException e) {
				logger.error(e);
			}
			tag.addPropertyChangeListener(this);
		}
		for (RifidiTag tag : getTags()) {
			RifidiTagWithParent r = new RifidiTagWithParent();
			r.parent = this;
			r.tag = tag;
			wrappers.add(r);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Switch#turnOff()
	 */
	public void turnOff() {
		thread.setPaused(true);
		running = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Switch#turnOn()
	 */
	public void turnOn() {
		if (!paused) {
			thread.setPaused(false);
		}
		running = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.IHasSwitch#isRunning()
	 */
	public boolean isRunning() {
		return running;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#start()
	 */
	public void start() {
		paused = false;
		if (running) {
			thread.setPaused(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#stop()
	 */
	public void reset() {
		paused = true;
		thread.setPaused(true);
		entitiesService.deleteEntities(new ArrayList<Entity>(products));
		tagStack.clear();
		tagStack.addAll(tags);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#pause()
	 */
	public void pause() {
		paused = true;
		if (thread != null) {
			thread.setPaused(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#destroy()
	 */
	@Override
	public void destroy() {
		thread.interrupt();
		getNode().removeFromParent();
	}

	/**
	 * Used to control the running state of the producer
	 * 
	 * @param newrunning
	 */
	public void setRunning(boolean newrunning) {
		running = newrunning;
	}

	/**
	 * Set the entities service.
	 * 
	 * @param entitiesService
	 */
	@Inject
	public void setEntitiesService(EntitiesService entitiesService) {
		this.entitiesService = entitiesService;
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
		return (Node) getNode().getChild("hiliter");
	}

	/**
	 * Get a string representation of the tags this producer owns.
	 * 
	 * @return
	 */
	public String getTagList() {
		StringBuffer buf = new StringBuffer();
		for (RifidiTag tag : tags) {
			buf.append(tag + "\n");
		}
		return buf.toString();
	}

	@Property(displayName = "Tags", description = "tags assigned to this producer", readonly = true, unit = "")
	public void setTagList(String tagList) {

	}

	/**
	 * @return the tags
	 */
	public List<RifidiTag> getTags() {
		return tags;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.designer.entities.interfaces.IProducer#productDestroied(org
	 * .rifidi.designer.entities.interfaces.IProduct)
	 */
	@Override
	public void productDestroied(CardboxEntity product) {
		products.remove(product);
		if (continuous
				&& tags.contains(((CardboxEntity) product).getRifidiTag())) {
			tagStack.push(((CardboxEntity) product).getRifidiTag());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
	 * PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("deleted".equals(evt.getPropertyName())) {
			tags.remove(((RifidiTag) evt.getSource()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.services.tags.model.IRifidiTagContainer#addTags(java.util.
	 * Collection)
	 */
	@Override
	public void addTags(Collection<RifidiTag> tags) {
		Set<RifidiTagWithParent> add = new HashSet<RifidiTagWithParent>();
		for (RifidiTag tag : tags) {
			try {
				tagService.takeRifidiTag(tag, this);
				tag.addPropertyChangeListener(this);
				RifidiTagWithParent r = new RifidiTagWithParent();
				r.parent = this;
				r.tag = tag;
				add.add(r);
			} catch (RifidiTagNotAvailableException e) {
				logger.error("Tried to take unavailable tag: "+tag);
			}
		}
		this.tags.addAll(tags);
		tagStack.addAll(tags);
		wrappers.addAll(add);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.services.tags.model.IRifidiTagContainer#removeTags(java.util
	 * .Collection)
	 */
	@Override
	public void removeTags(Collection<RifidiTag> tags) {
		//only remove tags that are currently available.
		//TODO: Wuhu, behold, It's a race condition. let's see who wins.
		if(this.tagStack.containsAll(tags)){
			this.tags.removeAll(tags);
			tagStack.removeAll(tags);
			Set<RifidiTagWithParent> rem = new HashSet<RifidiTagWithParent>();
			for (Object wrapper : wrappers) {
				if (tags.contains(((RifidiTagWithParent) wrapper).tag)) {
					((RifidiTagWithParent) wrapper).tag
							.removePropertyChangeListener(this);
					rem.add((RifidiTagWithParent) wrapper);
				}
			}
			wrappers.removeAll(rem);
			tagService.releaseRifidiTags(tags, this);
		}
	}

	/**
	 * @param tagService
	 *            the tagService to set
	 */
	@Inject
	public void setTagService(IRifidiTagService tagService) {
		this.tagService = tagService;
	}

	/**
	 * @return the wrappers
	 */
	public WritableList getWrappers() {
		return this.wrappers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.IProducer#getProducts()
	 */
	@Override
	public List<CardboxEntity> getProducts() {
		return new ArrayList<CardboxEntity>(products);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.designer.entities.interfaces.IProducer#setProducts(java.util
	 * .List)
	 */
	@Override
	public void setProducts(List<CardboxEntity> entities) {
		products.clear();
		products.addAll(entities);
	}

	/**
	 * @return the continuous
	 */
	public Boolean getContinuous() {
		return this.continuous;
	}

	/**
	 * @param continuous
	 *            the continuous to set
	 */
	@Property(displayName = "Continuous", description = "read destroied tags", readonly = false, unit = "")
	public void setContinuous(Boolean continuous) {
		this.continuous = continuous;
	}

}
