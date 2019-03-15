package org.eclipse.epsilon.emc.neoemf;

import static java.text.MessageFormat.format;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Utility methods to retrieve metamodel-level information.
 */
public class MetamodelUtils {

	/**
	 * Returns the {@link EStructuralFeature} associated to the given {@code featureName} from the provided
	 * {@code classifier}.
	 * 
	 * @param classifier  the {@link EClassifier} to retrieve the feature from
	 * @param featureName the name of the feature to retrieve
	 * @return the retrieved {@link EStructuralFeature} if it exists
	 * 
	 * @throws IllegalArgumentException if the provided {@code classifier} is not an {@link EClass}
	 */
	public static EStructuralFeature getFeature(EClassifier classifier, String featureName) {
		if (classifier instanceof EClass) {
			EClass eClass = (EClass) classifier;
			return eClass.getEStructuralFeature(featureName);
		} else {
			throw new IllegalArgumentException(
					format("Cannot retrieve the feature of the provided classifier {0} the classifier is not an {1}",
							classifier, EClass.class.getSimpleName()));
		}
	}

	/**
	 * Returns the type of the feature associated to the given {@code featureName} from the provided {@code classifier}.
	 * 
	 * @param classifier  the {@link EClassifier} to retrieve the feature's type from
	 * @param featureName the name of the feature to retrieve
	 * @return the retrieve {@link EStructuralFeature} if it exists
	 */
	public static EClassifier getFeatureClassifier(EClassifier classifier, String featureName) {
		EStructuralFeature feature = getFeature(classifier, featureName);
		return feature.getEType();
	}

	/**
	 * Returns whether the feature associated to the given {@code featureName} from the provided {@code classifier} is
	 * an attribute.
	 * 
	 * @param classifier  the {@link EClassifier} to retrieve the feature from
	 * @param featureName the name of the feature to retrieve
	 * @return {@code true} if the retrieved feature is an attribute, {@code false} otherwise
	 */
	public static boolean isAttributeFeature(EClassifier classifier, String featureName) {
		EStructuralFeature feature = getFeature(classifier, featureName);
		return feature instanceof EAttribute;
	}

	/**
	 * Returns whether the feature associated to the given {@code featureName} from the provided {@code classifier} is a
	 * reference.
	 * 
	 * @param classifier  the {@link EClassifier} to retrieve the feature from
	 * @param featureName the name of the feature to retrieve
	 * @return {@code true} if the retrieved feature is a reference, {@code false} otherwise
	 */
	public static boolean isReferenceFeature(EClassifier classifier, String featureName) {
		EStructuralFeature feature = getFeature(classifier, featureName);
		return feature instanceof EReference;
	}
}
