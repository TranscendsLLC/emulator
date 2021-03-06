/*
 *  PusharmEntity.java
 *
 *  Project:		RiFidi Designer - A Virtualization tool for 3D RFID environments
 *  http://www.rifidi.org
 *  http://rifidi.sourceforge.net
 *  Copyright:	    Pramari LLC and the Rifidi Project
 *  License:		Lesser GNU Public License (LGPL)
 *  http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.designer.library.basemodels.pusharm;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rifidi.designer.entities.VisualEntity;
import org.rifidi.designer.entities.annotations.Property;
import org.rifidi.designer.entities.databinding.annotations.MonitoredProperties;
import org.rifidi.designer.entities.gpio.IGPIO;
import org.rifidi.designer.entities.gpio.GPIPort;
import org.rifidi.designer.entities.gpio.GPOPort;
import org.rifidi.designer.entities.gpio.GPOPort.State;
import org.rifidi.designer.entities.interfaces.INeedsPhysics;
import org.rifidi.designer.entities.interfaces.IHasSwitch;
import org.rifidi.designer.entities.interfaces.ITrigger;

import com.jme.animation.SpatialTransformer;
import com.jme.bounding.BoundingBox;
import com.jme.input.InputHandler;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.input.util.SyntheticButton;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.SharedNode;
import com.jme.scene.Spatial;
import com.jme.scene.Spatial.CullHint;
import com.jme.scene.shape.Box;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.BlendState.DestinationFunction;
import com.jme.scene.state.BlendState.SourceFunction;
import com.jme.system.DisplaySystem;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.physics.PhysicsNode;
import com.jmex.physics.PhysicsSpace;
import com.jmex.physics.StaticPhysicsNode;
import com.jmex.physics.contact.ContactInfo;
import com.jmex.physics.material.Material;

/**
 * @author Jochen Mader Oct 8, 2007
 * @author Dan West
 */
@MonitoredProperties(names = { "name" })
@XmlRootElement
public class PusharmEntity extends VisualEntity implements IHasSwitch, ITrigger,
		INeedsPhysics, IGPIO, PropertyChangeListener {
	/** Logger for this class. */
	@XmlTransient
	private static Log logger = LogFactory.getLog(PusharmEntity.class);
	/** Speed of the pusharm. */
	private float speed;
	/** Transformer for the arm movement. */
	@XmlTransient
	private SpatialTransformer st;
	/** Not extended position. */
	@XmlTransient
	private Vector3f minpos = new Vector3f(-1.75f, 6, 0);
	/** Extended position. */
	@XmlTransient
	private Vector3f maxpos = minpos.add(new Vector3f(-4, 0, 0));
	/** IHasSwitch state. */
	private boolean running = false;
	/** Paused state. */
	private boolean paused = true;
	/** Infrared trigger. */
	@XmlTransient
	private StaticPhysicsNode triggerSpace = null;
	/** Physics of the push arm. */
	@XmlTransient
	private StaticPhysicsNode armPhysics = null;
	/** Reference to the physics space. */
	@XmlTransient
	private PhysicsSpace physicsSpace;
	/** Reference to the sollision handler. */
	@XmlTransient
	private InputHandler collisionHandler;
	/** Stack for activation signals. */
	@XmlTransient
	private Stack<Boolean> activationStack;
	/** Shared node for the body geometry. */
	@XmlTransient
	private static Node sharedbodyNode;
	/** Shared node for the arm geometry. */
	@XmlTransient
	private static Node sharedarmNode;
	/** GPI for the pusher. */
	private GPIPort port;

	/**
	 * Constructor
	 */
	public PusharmEntity() {
		setName("Pusharm");
		activationStack = new Stack<Boolean>();
		this.speed = 2;
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
	@Property(displayName = "Speed", description = "Speed the pusharm moves at", readonly = false, unit = "")
	public void setSpeed(float speed) {
		this.speed = speed;
		initController();
		activate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#init()
	 */
	@Override
	public void init() {
		port = new GPIPort();
		port.setNr(0);
		port.setId(getEntityId() + "-gpi-0");
		Node mainNode = new Node();
		mainNode.setModelBound(new BoundingBox());
		Node node = new Node("maingeometry");
		node.setModelBound(new BoundingBox());
		try {
			if (sharedbodyNode == null) {
				URI body = null;
				URI arm = null;
				try {
					arm = getClass()
							.getClassLoader()
							.getResource(
									"org/rifidi/designer/library/basemodels/pusharm/pusher_arm.jme")
							.toURI();
					body = getClass()
							.getClassLoader()
							.getResource(
									"org/rifidi/designer/library/basemodels/pusharm/pusher_body.jme")
							.toURI();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}

				sharedbodyNode = (Node) BinaryImporter.getInstance().load(
						body.toURL());
				sharedarmNode = (Node) BinaryImporter.getInstance().load(
						arm.toURL());
			}
			Node bodyNode = new SharedNode(sharedbodyNode);
			Node armNode = new SharedNode(sharedarmNode);
			bodyNode.setModelBound(new BoundingBox());
			bodyNode.updateModelBound();
			for (Spatial sp : bodyNode.getChildren()) {
				sp.clearRenderState(RenderState.RS_TEXTURE);
			}
			bodyNode.setLocalRotation(new Quaternion(new float[] {
					(float) Math.toRadians(270), 0, 0 }));
			bodyNode.setLocalTranslation(new Vector3f(0, 3.5f, 0));
			bodyNode.updateRenderState();
			armNode.setLocalRotation(new Quaternion(new float[] {
					(float) Math.toRadians(90), 0, 0 }));
			armPhysics = physicsSpace.createStaticNode();
			armPhysics.attachChild(armNode);
			armPhysics.setName("armPhysics");
			armPhysics.generatePhysicsGeometry();
			armPhysics.setLocalTranslation(minpos);
			armNode.setModelBound(new BoundingBox());
			armNode.updateModelBound();

			node.attachChild(bodyNode);
			node.attachChild(armPhysics);
			node.updateModelBound();

			// store pusharm dimensions for ease of calculation
			float xCent = armPhysics.getWorldBound().getCenter().x;
			float yCent = armPhysics.getWorldBound().getCenter().y;
			float xExt = ((BoundingBox) armPhysics.getWorldBound()).xExtent;
			float yExt = ((BoundingBox) armPhysics.getWorldBound()).yExtent;
			float len = 2.5f; // length of the trigger area

			// Create the material and alpha states for the trigger area
			MaterialState ms = DisplaySystem.getDisplaySystem().getRenderer()
					.createMaterialState();
			ms.setDiffuse(new ColorRGBA(1, 1, 1, .6f));
			BlendState as = DisplaySystem.getDisplaySystem().getRenderer()
					.createBlendState();
			as.setBlendEnabled(true);
			as.setSourceFunction(SourceFunction.SourceAlpha);
			as.setDestinationFunction(DestinationFunction.One);
			as.setEnabled(true);

			// create the trigger area
			Box irGeom = new Box("triggerSpace_geom", new Vector3f(-xExt - len
					+ xCent, yCent, 0f).add(minpos), len, yExt, .15f);
			irGeom.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
			irGeom.setRenderState(ms);
			irGeom.setRenderState(as);
			triggerSpace = physicsSpace.createStaticNode();
			triggerSpace.setName("triggerSpace");
			triggerSpace.attachChild(irGeom);
			triggerSpace.setModelBound(new BoundingBox());
			triggerSpace.updateModelBound();
			triggerSpace.updateRenderState();
			node.attachChild(triggerSpace);

			setNode(mainNode);
			getNode().attachChild(node);

			getNode().updateGeometricState(0, true);
			getNode().updateWorldBound();
			Node _node = new Node("hiliter");
			Box box = new Box("hiliter", ((BoundingBox) bodyNode
					.getWorldBound()).getCenter().clone().subtractLocal(
					getNode().getLocalTranslation()), 3f, 3.5f, 2.0f);
			box.setModelBound(new BoundingBox());
			box.updateModelBound();
			_node.attachChild(box);
			_node.setModelBound(new BoundingBox());
			_node.updateModelBound();
			_node.setCullHint(CullHint.Always);
			getNode().attachChild(_node);

		} catch (IOException e) {
			logger.error("Unable to load jme: " + e);
		}

		prepare();
		// initialize controller for moving the pusharm
		initController();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#loaded()
	 */
	@Override
	public void loaded() {
		port.addPropertyChangeListener("state", this);
		triggerSpace = (StaticPhysicsNode) getNode().getChild("triggerSpace");
		armPhysics = (StaticPhysicsNode) getNode().getChild("armPhysics");
		prepare();
		if (running)
			turnOn();
	}

	@XmlTransient
	private Node oldCol = null;

	private void prepare() {
		// set up collisions to trigger the pusharm
		triggerSpace.generatePhysicsGeometry();
		triggerSpace.setMaterial(Material.GHOST);
		InputAction triggerAction = new InputAction() {
			public void performAction(InputActionEvent evt) {
				Node collider = ((ContactInfo) evt.getTriggerData()).getNode1()
						.equals(triggerSpace) ? ((ContactInfo) evt
						.getTriggerData()).getNode2() : ((ContactInfo) evt
						.getTriggerData()).getNode1();

				if (collider != armPhysics && !collider.equals(oldCol)) {
					oldCol = collider;
					trigger(collider);
				}

			}
		};
		SyntheticButton intersect = triggerSpace.getCollisionEventHandler();
		collisionHandler.addAction(triggerAction, intersect, false);
		collisionHandler.setEnabled(true);

		// initialize controller for moving the pusharm
		initController();
		port.addPropertyChangeListener("state", this);
	}

	/**
	 * Creates the controller that moves the pusharm
	 */
	private void initController() {
		if (getNode() != null && st == null) {
			getNode().removeController(st);
			st = new SpatialTransformer(1);
			st.setObject(getNode().getChild("armPhysics"), 0, -1);
			st.setPosition(0, 0.0f, minpos);
			st.setPosition(0, 0.8f * speed, maxpos);
			st.setPosition(0, speed, minpos);
			st.interpolateMissing();
			st.setCurTime(st.getMaxTime());
			getNode().addController(st);
			st.setActive(false);
			return;
		}
		if (st != null) {

			st.setCurTime(st.getMaxTime());
			getNode().removeController(st);
			boolean active = st.isActive();
			st.setActive(false);
			st = new SpatialTransformer(1);
			st.setObject(getNode().getChild("armPhysics"), 0, -1);
			st.setPosition(0, 0.0f, minpos);
			st.setPosition(0, 0.8f * speed, maxpos);
			st.setPosition(0, speed, minpos);
			st.interpolateMissing();
			st.setCurTime(st.getMaxTime());
			getNode().addController(st);
			st.setActive(active);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.IHasSwitch#turnOn()
	 */
	public void turnOn() {
		running = true;
		activate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#start()
	 */
	public void start() {
		paused = false;
		activate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#pause()
	 */
	public void pause() {
		deactivate();
		st.setActive(false);
		paused = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#reset()
	 */
	public void reset() {
		// initController();
		st.setCurTime(st.getMaxTime());
		// armPhysics.setLocalTranslation(minpos);
		paused = true;
	}

	/**
	 * Starts the pusharm (whether turned on then started, or v.v.)
	 */
	private void activate() {
		if (running && !paused) {
			st.setActive(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.designer.entities.interfaces.ITrigger#trigger(java.lang.Object
	 * )
	 */
	public void trigger(Object source) {

		if (running && !paused
				&& (!activationStack.isEmpty() && activationStack.pop())) {
			if (st.getCurTime() == st.getMaxTime())
				st.setCurTime(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.IHasSwitch#turnOff()
	 */
	public void turnOff() {
		deactivate();
		running = false;
		activationStack.clear();
	}

	/**
	 * Stops the pusharm from moving
	 */
	private void deactivate() {
		if (running && !paused) {
			st.setActive(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.IHasSwitch#isRunning()
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Sets whether the new running state of the pusharm
	 * 
	 * @param newrunning
	 *            the new state of running-ness
	 */
	public void setRunning(boolean newrunning) {
		running = newrunning;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#destroy()
	 */
	@Override
	public void destroy() {
		((PhysicsNode) getNode().getChild("armPhysics")).setActive(false);
		getNode().removeFromParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.designer.entities.interfaces.INeedsPhysics#setCollisionHandler
	 * (com.jme.input.InputHandler)
	 */
	public void setCollisionHandler(InputHandler collisionHandler) {
		this.collisionHandler = collisionHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.designer.entities.interfaces.INeedsPhysics#setPhysicsSpace(
	 * com.jmex.physics.PhysicsSpace)
	 */
	public void setPhysicsSpace(PhysicsSpace physicsSpace) {
		this.physicsSpace = physicsSpace;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.gpio.IGPIO#getGPIPorts()
	 */
	@Override
	public List<GPIPort> getGPIPorts() {
		List<GPIPort> ret = new ArrayList<GPIPort>();
		ret.add(port);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.gpio.IGPIO#getGPOPorts()
	 */
	@Override
	public List<GPOPort> getGPOPorts() {
		return Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
	 * PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (State.HIGH.equals(port.getState())) {
			activationStack.push(true);
		}
	}

	/**
	 * @return the port
	 */
	public GPIPort getPort() {
		return this.port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(GPIPort port) {
		this.port = port;
	}

}