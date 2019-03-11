package edu.uoc.som.emc.neoemf.example;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.gmt.modisco.java.JavaPackage;

import fr.inria.atlanmod.neoemf.data.PersistenceBackendFactoryRegistry;
import fr.inria.atlanmod.neoemf.data.blueprints.BlueprintsPersistenceBackendFactory;
import fr.inria.atlanmod.neoemf.data.blueprints.util.BlueprintsURI;
import fr.inria.atlanmod.neoemf.resource.PersistentResource;
import fr.inria.atlanmod.neoemf.resource.PersistentResourceFactory;

/**
 * Checks that the given NeoEMF resource contains the same number of EObjects as
 * the provided XMI file.
 * <p>
 * This check is defined in a separated class to ensure that all the NeoEMF
 * caches have been erased before comparing the content of the resources.
 */
public class CheckNeoEMFModel {

	/**
	 * The path of the XMI file imported in the NeoEMF resource.
	 */
	private static String XMI_MODEL_PATH = "models/sample.xmi";

	/**
	 * The path of the NeoEMF resource to check.
	 */
	private static String NEOEMF_MODEL_PATH = "models/sample.graphdb";

	/**
	 * Checks the provided NeoEMF resource and compares its content with the
	 * provided XMI file.
	 * <p>
	 * This method performs a naive comparison by checking that the provided NeoEMF
	 * resource contains as many elements as the provided XMI one. This comparison
	 * should be enough for our example.
	 * 
	 * @param args
	 * @throws IOException if an error occurred when loading the XMI file or the
	 *                     NeoEMF resource
	 */
	public static void main(String[] args) throws IOException {
		ResourceSet rSet = new ResourceSetImpl();
		rSet.getPackageRegistry().put(JavaPackage.eNS_URI, JavaPackage.eINSTANCE);
		rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		/*
		 * Register NeoEMF persistence backend and protocol to enable NeoEMF resource
		 * loading.
		 */
		rSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(BlueprintsURI.SCHEME,
				PersistentResourceFactory.getInstance());
		PersistenceBackendFactoryRegistry.register(BlueprintsURI.SCHEME,
				BlueprintsPersistenceBackendFactory.getInstance());

		Resource xmiResource = rSet.getResource(URI.createURI(XMI_MODEL_PATH), true);
		int xmiSize = ModelUtils.countEObjects(xmiResource);

		File neoemfFile = new File(NEOEMF_MODEL_PATH);
		Resource neoemfResource = rSet.createResource(BlueprintsURI.createFileURI(neoemfFile));
		neoemfResource.load(Collections.emptyMap());
		int neoemfSize = ModelUtils.countEObjects(neoemfResource);

		if (xmiSize != neoemfSize) {
			throw new RuntimeException(MessageFormat.format(
					"The NeoEMF model does not match the xmi one: expected {0} elements, found {1}", xmiSize,
					neoemfSize));
		}

		System.out.println("The NeoEMF model matches the xmi one");
		/*
		 * Closing the persistent resource is not mandatory (it is closed on JVM
		 * shutdown anyway), but is a good practice to avoid database lock issues.
		 */
		((PersistentResource) neoemfResource).close();
	}

}
