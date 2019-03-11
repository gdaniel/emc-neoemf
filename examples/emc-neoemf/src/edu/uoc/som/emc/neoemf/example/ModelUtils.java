package edu.uoc.som.emc.neoemf.example;
import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * Provides utility methods to query and manipulate EMF {@link Resource}s.
 */
public class ModelUtils {

	/**
	 * Returns the number of {@link EObject}s contained in the provided
	 * {@code resource}.
	 * 
	 * @param resource the {@link Resource} to count the element of
	 * @return the number of {@link EObject}s contained in the provided
	 *         {@code resource}
	 */
	public static int countEObjects(Resource resource) {
		Iterator<EObject> it = resource.getAllContents();
		int count = 0;
		while (it.hasNext()) {
			it.next();
			count++;
		}
		return count;
	}

}
