/*
 *  GateEntity.java
 *
 *  Project:		RiFidi Designer - A Virtualization tool for 3D RFID environments
 *  http://www.rifidi.org
 *  http://rifidi.sourceforge.net
 *  Copyright:	    Pramari LLC and the Rifidi Project
 *  License:		Lesser GNU Public License (LGPL)
 *  http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.designer.library.basemodels.gate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rifidi.designer.entities.RMIManager;
import org.rifidi.designer.entities.VisualEntity;
import org.rifidi.designer.entities.annotations.Property;
import org.rifidi.designer.entities.databinding.annotations.MonitoredProperties;
import org.rifidi.designer.entities.interfaces.GPI;
import org.rifidi.designer.entities.interfaces.GPO;
import org.rifidi.designer.entities.interfaces.ParentEntity;
import org.rifidi.designer.entities.interfaces.RifidiEntity;
import org.rifidi.designer.entities.interfaces.Switch;
import org.rifidi.designer.library.basemodels.antennafield.AntennaFieldEntity;
import org.rifidi.designer.services.core.cabling.CablingService;
import org.rifidi.emulator.rmi.server.ReaderModuleManagerInterface;
import org.rifidi.services.annotations.Inject;
import org.rifidi.ui.common.reader.UIReader;

import com.jme.bounding.BoundingBox;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.SharedNode;
import com.jme.scene.SwitchNode;
import com.jme.scene.Spatial.CullHint;
import com.jme.scene.shape.Box;
import com.jme.util.export.binary.BinaryImporter;

/**
 * This entity is a gateway with an attached reader, through which a
 * conveyorbelt can pass. One or two antennas are attached to the sides for tag
 * detection depending on the number of antennas the associated reader has.
 * 
 * @author Jochen Mader Oct 8, 2007
 * @author Dan West - dan@pramari.com
 */
@MonitoredProperties(names = { "name" })
@XmlRootElement
public class GateEntity extends VisualEntity implements RifidiEntity, Switch,
		ParentEntity, GPO, GPI {

	/**
	 * logger for this class.
	 */
	private static Log logger = LogFactory.getLog(GateEntity.class);
	/**
	 * Model for shared meshes
	 */
	private static Node[] lod = null;
	/**
	 * Left antenna entity.
	 */
	private AntennaFieldEntity antennaL;
	/**
	 * Right antenna entity.
	 */
	private AntennaFieldEntity antennaR;
	/**
	 * Reader associated with this gate.
	 */
	private UIReader reader;
	/**
	 * List of ChildEntites connected to this gate.
	 */
	private List<VisualEntity> children;
	/**
	 * The multiplication factor for the field size
	 */
	private float factor = 1.0f;
	/**
	 * Connection manager for rifidi.
	 */
	private RMIManager rmimanager;
	/**
	 * Reference to the reader management interface.
	 */
	private ReaderModuleManagerInterface readerModuleManagerInterface;
	/**
	 * State of the switch.
	 */
	private boolean running = false;
	/**
	 * Reference to the cablingservice.
	 */
	private CablingService cablingService;
	/**
	 * Node that contains the different lods.
	 */
	private SwitchNode switchNode;

	/**
	 * Constructor.
	 */
	public GateEntity() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#init()
	 */
	@Override
	public void init() {
		prepare();
		Node mainNode=new Node();
		mainNode.setModelBound(new BoundingBox());
		switchNode = new SwitchNode("maingeometry");
		switchNode.setLocalScale(new Vector3f(0.9f,1.1f,1f));
		switchNode.attachChildAt(new SharedNode("shared_gate", lod[0]), 0);
		switchNode.attachChildAt(new SharedNode("shared_gate", lod[1]), 1);
		switchNode.attachChildAt(new SharedNode("shared_gate", lod[2]), 2);
		switchNode.attachChildAt(new SharedNode("shared_gate", lod[2]), 3);
		switchNode.setActiveChild(0);
		
		mainNode.attachChild(switchNode);
		setNode(mainNode);
		
		// find the snappoints
		children = new ArrayList<VisualEntity>();
		if (reader.getNumAntennas() > 0) {
			// instantiate the right antenna
			antennaR = new AntennaFieldEntity(0, readerModuleManagerInterface);
			antennaR.setName("Right antenna");
			Vector3f baseTrans = new Vector3f(2.9f, 6f, 0f);
			Vector3f baseRot = new Vector3f(0, (float) (Math.PI * 1.5f), 0);
			antennaR.setBaseTranslation(baseTrans);
			antennaR.setBaseRotation(baseRot);
			children.add(antennaR);
		}

		if (reader.getNumAntennas() > 1) {
			// instantiate the left antenna
			antennaL = new AntennaFieldEntity(1, readerModuleManagerInterface);
			antennaL.setName("Left antenna");
			Vector3f baseTrans = new Vector3f(-2.9f, 6f, 0f);
			Vector3f baseRot = new Vector3f(0, (float) (Math.PI * 0.5f), 0);
			antennaL.setBaseTranslation(baseTrans);
			antennaL.setBaseRotation(baseRot);
			children.add(antennaL);
		}
		getNode().updateGeometricState(0, true);
		getNode().updateWorldBound();
		
		Node _node=new Node("hiliter");
		Box box = new Box("top", ((BoundingBox) getNode()
				.getWorldBound()).getCenter().clone().subtractLocal(
				getNode().getLocalTranslation()).add(new Vector3f(0,5,0)), 3f, 1f, .5f);
		box.setModelBound(new BoundingBox());
		box.updateModelBound();
		_node.attachChild(box);
		box = new Box("left", ((BoundingBox) getNode()
				.getWorldBound()).getCenter().clone().subtractLocal(
				getNode().getLocalTranslation()).add(new Vector3f(-2.5f,-.5f,0)), .5f, 4.5f, .5f);
		box.setModelBound(new BoundingBox());
		box.updateModelBound();
		_node.attachChild(box);
		box = new Box("leftFoot", ((BoundingBox) getNode()
				.getWorldBound()).getCenter().clone().subtractLocal(
				getNode().getLocalTranslation()).add(new Vector3f(-2.5f,-5.5f,0)), .5f, .5f, 2f);
		box.setModelBound(new BoundingBox());
		box.updateModelBound();
		_node.attachChild(box);
		box = new Box("right", ((BoundingBox) getNode()
				.getWorldBound()).getCenter().clone().subtractLocal(
				getNode().getLocalTranslation()).add(new Vector3f(2.5f,-.5f,0)), .5f, 4.5f, .5f);
		box.setModelBound(new BoundingBox());
		box.updateModelBound();
		_node.attachChild(box);
		box = new Box("rightFoot", ((BoundingBox) getNode()
				.getWorldBound()).getCenter().clone().subtractLocal(
				getNode().getLocalTranslation()).add(new Vector3f(2.5f,-5.5f,0)), .5f, .5f, 2f);
		box.setModelBound(new BoundingBox());
		box.updateModelBound();
		_node.attachChild(box);
		
		
		
		_node.setModelBound(new BoundingBox());
		_node.updateModelBound();
		_node.setCullHint(CullHint.Always);
		getNode().attachChild(_node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#loaded()
	 */
	@Override
	public void loaded() {
		prepare();
		switchNode=(SwitchNode)getNode().getChild("maingeometry");
		for (VisualEntity antennaFieldEntity : children) {
			((AntennaFieldEntity) antennaFieldEntity)
					.setReaderInterface(readerModuleManagerInterface);
		}
		if (running)
			turnOn();
	}

	private void prepare() {
		if (lod == null) {
			lod = new Node[4];
			URI modelpath = null;
			for (int count = 0; count < 4; count++) {
				try {
					modelpath = getClass().getClassLoader().getResource(
							"org/rifidi/designer/library/basemodels/gate/gate"
									+ count + ".jme").toURI();
				} catch (URISyntaxException e) {
					logger.debug(e);
				}
				try {
					lod[count] = (Node) BinaryImporter.getInstance().load(
							modelpath.toURL());
					lod[count].setLocalRotation(new Quaternion().fromAngleAxis(
							270 * FastMath.DEG_TO_RAD, Vector3f.UNIT_X));
					lod[count].setModelBound(new BoundingBox());
					lod[count].updateGeometricState(0f, true);
					lod[count].updateModelBound();
					lod[count].updateWorldBound();
					lod[count].setLocalScale(new Vector3f(0.87f,1f,1f));
				} catch (MalformedURLException e) {
					logger.debug(e);
				} catch (IOException e) {
					logger.debug(e);
				}
			}
		}
		try {
			readerModuleManagerInterface = rmimanager.createReader(reader
					.getGeneralReaderPropertyHolder());
		} catch (ClassNotFoundException e) {
			logger.fatal("Unable to create reader: "+e);
		} catch (InstantiationException e) {
			logger.fatal("Unable to create reader: "+e);
		} catch (IllegalAccessException e) {
			logger.fatal("Unable to create reader: "+e);
		} catch (MalformedURLException e) {
			logger.fatal("Unable to create reader: "+e);
		} catch (NotBoundException e) {
			logger.fatal("Unable to create reader: "+e);
		} catch (RemoteException e) {
			logger.fatal("Unable to create reader: "+e);
		} catch (IOException e) {
			logger.fatal("Unable to create reader: "+e);
		}
	}

	/**
	 * Turns on the reader associated with the gate.
	 */
	public void turnOn() {
		Thread thr = new Thread(new Runnable() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {

				try {
					readerModuleManagerInterface.turnReaderOn();
					running = true;
					// attach antenna fields to the gate
					for (VisualEntity antenna : children) {
						((AntennaFieldEntity) antenna).turnOn();
					}
				} catch (Exception e) {
					logger.error("Problem turning on gate: " + e);
				}
			}

		});
		thr.run();
	}

	/**
	 * Turns off the reader associated with the gate.
	 */
	public void turnOff() {
		try {

			Thread thr = new Thread(new Runnable() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				@Override
				public void run() {

					try {
						readerModuleManagerInterface.turnReaderOff();
						running = false;
					} catch (Exception e) {
						logger.error("Problem turning on gate: " + e);
					}
				}

			});
			thr.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (VisualEntity entity : children) {
			((AntennaFieldEntity) entity).turnOff();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.Entity#destroy()
	 */
	@Override
	public void destroy() {
		getNode().removeFromParent();
		try {
			readerModuleManagerInterface.turnReaderOff();
		} catch (Exception e) {
			logger.warn("Unable to turn off reader: " + e);
		}
		rmimanager.removeReader(reader.getReaderName());
	}

	public void setRMIManager(RMIManager rmimanager) {
		this.rmimanager = rmimanager;
	}

	/**
	 * Set the running state of the entity.
	 * 
	 * @param newrunning
	 *            the running state
	 */
	public void setRunning(boolean newrunning) {
		running = newrunning;
	}

	/**
	 * Check if the entity is running.
	 * 
	 * @return the running state
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * @return the reader
	 */
	public UIReader getReader() {
		return reader;
	}

	/**
	 * @param reader
	 *            the reader to set
	 */
	public void setReader(UIReader reader) {
		this.reader = reader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.ParentEntity#getChildEntites()
	 */
	@Override
	public List<VisualEntity> getChildEntites() {
		return children;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.ParentEntity#setChildEntites(java.util.List)
	 */
	@XmlIDREF
	@Override
	public void setChildEntites(List<VisualEntity> children) {
		this.children = children;
	}

	/**
	 * @return the factor
	 */
	public float getFactor() {
		return this.factor;
	}

	/**
	 * @param factor
	 *            the factor to set
	 */
	@Property(displayName = "Factor", description = "field mnultiplicator", readonly = false, unit = "")
	public void setFactor(float factor) {
		this.factor = factor;
		if (children != null) {
			for (VisualEntity antenna : children) {
				((AntennaFieldEntity) antenna).updateFactor(factor);
			}
		}
	}

	@XmlTransient
	@Property(displayName = "Reader", description = "type of emulated reader", readonly = true, unit = "")
	public void setReaderType(String name) {

	}

	public String getReaderType() {
		return reader.getReaderType();
	}
	
	@XmlTransient
	@Property(displayName = "Reader Connection", description = "connection details for the reader", readonly = true, unit = "")
	public void setConnectionDetails(String readerDetails){
		
	}
	
	public String getConnectionDetails(){
		if(reader.getProperty("inet_address")!=null){
			return reader.getProperty("inet_address");
		}
		return "no connection info available";
	}
	
	@Inject
	public void setCablingService(CablingService cablingService) {
		this.cablingService = cablingService;
	}

	/**
	 * Used to tell the gate that a tag just passed through it.
	 */
	public void tagSeen() {
		cablingService.setHigh(this, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.interfaces.RifidiEntity#getReaderInterface()
	 */
	@Override
	public ReaderModuleManagerInterface getReaderInterface() {
		return this.readerModuleManagerInterface;
	}

	@Override
	public void setHigh(int portNum) {
		try {
			readerModuleManagerInterface.setGPIHigh(portNum);
		} catch (Exception e) {
			logger
					.error("Unable to set GPI port " + portNum + " to high: "
							+ e);
		}
	}

	@Override
	public void setLow(int portNum) {
		try {
			readerModuleManagerInterface.setGPILow(portNum);
		} catch (Exception e) {
			logger
					.error("Unable to set GPI port " + portNum + " to high: "
							+ e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rifidi.designer.entities.VisualEntity#setLOD(int)
	 */
	@Override
	public void setLOD(int lod) {
		if(switchNode!=null){
			switchNode.setActiveChild(lod);	
		}
	}

	/* (non-Javadoc)
	 * @see org.rifidi.designer.entities.VisualEntity#getBoundingNode()
	 */
	@Override
	public Node getBoundingNode() {
		return (Node)getNode().getChild("hiliter");
	}

}