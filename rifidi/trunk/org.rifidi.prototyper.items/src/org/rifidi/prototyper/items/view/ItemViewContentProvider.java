/**
 * 
 */
package org.rifidi.prototyper.items.view;

import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.rifidi.prototyper.items.model.ItemModel;

/**
 * The content provider for the ItemView.
 * 
 * @author Kyle Neumeier - kyle@pramari.com
 * 
 */
public class ItemViewContentProvider implements ITreeContentProvider,
		ItemModelProviderListener {

	private Viewer viewer;

	public ItemViewContentProvider() {
		ItemModelProviderSingleton.getModelProvider().registerListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.
	 * Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Collection<?>) {
			return ((Collection<?>) parentElement).toArray();
		}
		return new Object[] {};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object
	 * )
	 */
	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.
	 * Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof Collection<?>) {
			return !((Collection<?>) element).isEmpty();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java
	 * .lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(ItemModelProviderSingleton.getModelProvider()
				.getItems());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		ItemModelProviderSingleton.getModelProvider().unregisterListener(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
	 * .viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;

	}

	@Override
	public void ItemAdded(ItemModel item) {
		viewer.refresh();
	}

	@Override
	public void ItemRemoved(ItemModel item) {
		viewer.refresh();
	}

}
