/*******************************************************************************
 * Copyright (c) 2008 The University of York.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 ******************************************************************************/
package org.eclipse.epsilon.emc.neoemf;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A base List<T> implementation that is immutable.
 * 
 * @author Dimitris Kolovos
 *
 * @param <T>
 */
public abstract class ImmutableList<T> implements List<T> {

	@Override
	public boolean add(T arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int arg0, T arg1) {
		throw new UnsupportedOperationException();		
	}
	
	@Override
	public boolean addAll(int arg0, Collection<? extends T> arg1) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public T remove(int arg0) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public T set(int arg0, T arg1) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Iterator<T> iterator() {
		return listIterator();
	}
	
	@Override
	public Object[] toArray() {
		Iterator<T> iterator = iterator();
		int size = size();
		Object[] array = new Object[size];
		for (int i = 0; iterator.hasNext(); i++) {
			array[i] = iterator.next();
		}
		return array;
	}

	@Override
	public <A> A[] toArray(A[] arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ListIterator<T> listIterator(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}
}
