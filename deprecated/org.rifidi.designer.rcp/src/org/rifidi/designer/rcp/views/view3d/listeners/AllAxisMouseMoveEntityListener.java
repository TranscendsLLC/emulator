/*
 *  MousePickListener.java
 *
 *  Project:		RiFidi Designer - A Virtualization tool for 3D RFID environments
 *  http://www.rifidi.org
 *  http://rifidi.sourceforge.net
 *  Copyright:	    Pramari LLC and the Rifidi Project
 *  License:		Lesser GNU Public License (LGPL)
 *  http://www.opensource.org/licenses/lgpl-license.html
 */
package org.rifidi.designer.rcp.views.view3d.listeners;

import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.widgets.Display;
import org.monklypse.core.JMECanvasImplementor2;
import org.rifidi.designer.entities.VisualEntity;
import org.rifidi.designer.entities.interfaces.IContainer;
import org.rifidi.designer.entities.interfaces.IProduct;
import org.rifidi.designer.rcp.game.DesignerGame;
import org.rifidi.designer.rcp.game.ZoomableLWJGLCamera;
import org.rifidi.designer.rcp.views.view3d.View3D;
import org.rifidi.designer.services.core.entities.EntitiesService;
import org.rifidi.designer.services.core.entities.FinderService;
import org.rifidi.designer.services.core.entities.SceneDataService;
import org.rifidi.services.annotations.Inject;
import org.rifidi.services.registry.ServiceRegistry;

import com.jme.bounding.BoundingBox;
import com.jme.intersection.BoundingPickResults;
import com.jme.intersection.PickResults;
import com.jme.math.Matrix3f;
import com.jme.math.Ray;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.system.DisplaySystem;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.PhysicsNode;
import com.jmex.physics.material.Material;

/**
 * A listener for moving entites along all 3 axis.
 * 
 * @author Jochen Mader Oct 30, 2007
 */
public class AllAxisMouseMoveEntityListener implements MouseListener,
		MouseMoveListener, MouseWheelListener {
	/** The currently picked entity. */
	private VisualEntity pickedEntity = null;
	/** Reference to the 3d view. */
	private View3D view3D;
	/** Center of the screen. */
	private Point center;
	/** Movement delta on the mouse x axis. */
	float deltaX = 0;
	/** Movement delta on the mouse y axis. */
	float deltaY = 0;
	/** Mouse movement sensitivity. */
	private float sensitivity = 5;
	/** Reference to the scene data service. */
	private SceneDataService sceneDataService;
	/** Reference to the finder service. */
	private FinderService finderService;
	/** Reference to the entities service. */
	private EntitiesService entitiesService;
	/** Translation of the last hit object. */
	private Vector3f lastHit;
	/** Reference to the implementor. */
	private DesignerGame implementor;
	/** Used to check if the picked entity is already initialized. */
	private boolean init = false;

	/**
	 * Constructor.
	 * 
	 * @param view3D
	 *            the 3d view
	 */
	public AllAxisMouseMoveEntityListener(View3D view3D) {
		ServiceRegistry.getInstance().service(this);
		this.view3D = view3D;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt
	 * .events.MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events
	 * .MouseEvent)
	 */
	public void mouseDown(MouseEvent e) {
		// check what we got
		if (e.button == 1 || e.button == 3) {
			pickedEntity = null;
			Camera cam = DisplaySystem.getDisplaySystem().getRenderer()
					.getCamera();
			// create ray
			int canvasY = ((GLCanvas) ((JMECanvasImplementor2<?>) implementor)
					.getCanvas()).getSize().y;
			Vector3f coord = cam.getWorldCoordinates(new Vector2f(e.x, canvasY
					- e.y), 0);
			Vector3f coord2 = cam.getWorldCoordinates(new Vector2f(e.x, canvasY
					- e.y), 1);
			Ray ray = new Ray(coord, coord2.subtractLocal(coord)
					.normalizeLocal());
			PickResults pickResults = new BoundingPickResults();
			// shoot
			sceneDataService.getCurrentSceneData().getRootNode().findPick(ray,
					pickResults);
			Node node = null;
			// loop[ through the results to find an entity
			// this has to be done as for some reasons a bounding box appears
			// around the room and is hit first, darn
			pickedEntity = null;
			for (int count = 0; count < pickResults.getNumber(); count++) {
				node = pickResults.getPickData(count).getTargetMesh()
						.getParent().getParent();
				pickedEntity = finderService.getVisualEntityByNode(node);
				if (pickedEntity != null) {
					break;
				}
			}
		}
		// if it's a container get a child of the container
		lastHit = (Vector3f) pickedEntity.getNode().getWorldTranslation()
				.clone();
		if (pickedEntity != null && pickedEntity instanceof IContainer) {
			pickedEntity = ((IContainer) pickedEntity).getVisualEntity();
			init = false;
		} else if (!(pickedEntity instanceof IProduct)) {
			pickedEntity = null;
			init = true;
		}

		ignore = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.
	 * MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {
		view3D.showMousePointer();
		center = null;
		if (pickedEntity != null) {
			Set<VisualEntity> colls = entitiesService
					.getColliders(pickedEntity);
			// check if the picked entity collides with anything
			if (pickedEntity != null && colls.size() != 0) {
				for (VisualEntity ent : colls) {
					if (ent instanceof IContainer) {
						// if it vollides with an EntityHolder then try dropping
						// it into it
						if (((IContainer) ent).accepts(pickedEntity)) {
							((IContainer) ent).addVisualEntity(pickedEntity);
							pickedEntity = null;
						}
					}
				}
			}
			// drop it
			if (pickedEntity != null
					&& pickedEntity.getNode() instanceof PhysicsNode) {
				implementor.update(new ActivationCallable(
						(PhysicsNode) pickedEntity.getNode()));
				pickedEntity = null;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events
	 * .MouseEvent)
	 */
	private boolean ignore = false;

	@Override
	public void mouseMove(MouseEvent e) {
		if (pickedEntity != null && pickedEntity.getNode() != null
				&& init == false) {
			implementor.update(new Callable<Object>() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.concurrent.Callable#call()
				 */
				@Override
				public Object call() throws Exception {
					lastHit.setY(((BoundingBox) pickedEntity.getNode()
							.getWorldBound()).yExtent + 0.1f);
					pickedEntity.getNode().setLocalTranslation(lastHit);
					pickedEntity.getNode().setLocalRotation(new Matrix3f());
					init = true;
					return null;
				}

			});
		}
		// if it is a physics node prevent it from colliding.
		if (pickedEntity != null && init == true
				&& pickedEntity.getNode() instanceof DynamicPhysicsNode
				&& center == null) {
			Rectangle bounds = Display.getCurrent().getActiveShell()
					.getBounds();
			center = new Point(bounds.x + bounds.width / 2, bounds.y
					+ bounds.height / 2);
			Display.getCurrent().setCursorLocation(center);
			deltaX = 0;
			deltaY = 0;
			// deactivate collisions
			((PhysicsNode) pickedEntity.getNode()).setMaterial(Material.GHOST);
			((PhysicsNode) pickedEntity.getNode()).setActive(false);
		}

		if (pickedEntity != null && ignore == false && init == true) {
			// calculate how far to move the object(s)
			view3D.hideMousePointer();
			Point loc = Display.getCurrent().getCursorLocation();
			deltaX += ((float) loc.x - (float) center.x) / sensitivity;
			deltaY += ((float) loc.y - (float) center.y) / sensitivity;
			Vector3f up = Vector3f.UNIT_Z;
			Vector3f right = Vector3f.UNIT_X;

			Vector3f vec = up.mult((int) deltaY).add(right.mult((int) deltaX));
			vec.x = (int) vec.x;
			vec.z = (int) vec.z;

			// remove whatever amount has been used for calculating motion this
			// round
			deltaY -= (int) deltaY;
			deltaX -= (int) deltaX;
			pickedEntity.getNode().getLocalTranslation().addLocal(vec);
			// a series of checks to keep the entity in the bounds of the scene
			float xDelta = pickedEntity.getNode().getLocalTranslation().x
					- ((BoundingBox) pickedEntity.getNode().getWorldBound()).xExtent;
			if (xDelta < 0) {
				pickedEntity.getNode().getLocalTranslation()
						.setX(
								((BoundingBox) pickedEntity.getNode()
										.getWorldBound()).xExtent + 0.5f);
			}
			xDelta = pickedEntity.getNode().getLocalTranslation().x
					+ ((BoundingBox) pickedEntity.getNode().getWorldBound()).xExtent;
			if (xDelta > ((BoundingBox) sceneDataService.getCurrentSceneData()
					.getRootNode().getWorldBound()).xExtent * 2) {
				pickedEntity.getNode().getLocalTranslation().setX(
						((BoundingBox) sceneDataService.getCurrentSceneData()
								.getRootNode().getWorldBound()).xExtent
								* 2
								- ((BoundingBox) pickedEntity.getNode()
										.getWorldBound()).xExtent * 2);
			}
			float zDelta = pickedEntity.getNode().getLocalTranslation().z
					- ((BoundingBox) pickedEntity.getNode().getWorldBound()).yExtent;
			if (zDelta < 0) {
				pickedEntity.getNode().getLocalTranslation()
						.setZ(
								((BoundingBox) pickedEntity.getNode()
										.getWorldBound()).zExtent + 0.5f);
			}
			zDelta = pickedEntity.getNode().getLocalTranslation().z
					+ ((BoundingBox) pickedEntity.getNode().getWorldBound()).zExtent;
			if (zDelta > ((BoundingBox) sceneDataService.getCurrentSceneData()
					.getRootNode().getWorldBound()).zExtent * 2) {
				pickedEntity.getNode().getLocalTranslation().setZ(
						((BoundingBox) sceneDataService.getCurrentSceneData()
								.getRootNode().getWorldBound()).zExtent
								* 2
								- ((BoundingBox) pickedEntity.getNode()
										.getWorldBound()).zExtent);
			}
			// recenter the cursor
			Display.getCurrent().setCursorLocation(center);
			ignore = true;
		} else if (ignore == true) {
			ignore = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseWheelListener#mouseScrolled(org.eclipse.swt
	 * .events.MouseEvent)
	 */
	@Override
	public void mouseScrolled(MouseEvent e) {
		if (pickedEntity != null) {
			if (pickedEntity.getNode().getLocalTranslation().y + e.count > 0) {
				pickedEntity.getNode().getLocalTranslation().addLocal(
						new Vector3f(0, e.count, 0));
			}
			return;
		}
		if (e.count > 0) {
			((ZoomableLWJGLCamera) implementor.getRenderer().getCamera())
					.zoomIn(e.x, ((GLCanvas) e.widget).getSize().y - e.y);
			return;
		}
		((ZoomableLWJGLCamera) implementor.getRenderer().getCamera()).zoomOut(
				e.x, ((GLCanvas) e.widget).getSize().y - e.y);
	}

	/**
	 * @param sceneDataService
	 *            the sceneDataService to set
	 */
	@Inject
	public void setSceneDataService(SceneDataService sceneDataService) {
		this.sceneDataService = sceneDataService;
	}

	/**
	 * @param finderService
	 *            the finderService to set
	 */
	@Inject
	public void setFinderService(FinderService finderService) {
		this.finderService = finderService;
	}

	/**
	 * @param entitiesService
	 *            the entitiesService to set
	 */
	@Inject
	public void setEntitiesService(EntitiesService entitiesService) {
		this.entitiesService = entitiesService;
	}

	/**
	 * @param implementor
	 *            the implementor to set
	 */
	@Inject
	public void setImplementor(DesignerGame implementor) {
		this.implementor = implementor;
	}

	/**
	 * Callable for activating the physics properties of the given entity
	 * 
	 * 
	 * @author Jochen Mader - jochen@pramari.com - Jul 31, 2008
	 * 
	 */
	private class ActivationCallable implements Callable<Object> {
		/**
		 * The node to activate.
		 */
		private PhysicsNode physicsNode;

		/**
		 * @param physicsNode
		 */
		public ActivationCallable(PhysicsNode physicsNode) {
			super();
			this.physicsNode = physicsNode;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public Object call() throws Exception {
			if (physicsNode instanceof DynamicPhysicsNode) {

				((DynamicPhysicsNode) physicsNode).clearDynamics();
				((DynamicPhysicsNode) physicsNode).clearForce();
				((DynamicPhysicsNode) physicsNode).setMaterial(Material.WOOD);
			}
			physicsNode.setActive(true);
			return null;
		}

	}
}