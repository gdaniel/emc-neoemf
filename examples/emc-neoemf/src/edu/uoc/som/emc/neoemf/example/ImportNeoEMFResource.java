package edu.uoc.som.emc.neoemf.example;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.gmt.modisco.java.JavaPackage;

import fr.inria.atlanmod.neoemf.data.PersistenceBackendFactoryRegistry;
import fr.inria.atlanmod.neoemf.data.blueprints.BlueprintsPersistenceBackendFactory;
import fr.inria.atlanmod.neoemf.data.blueprints.neo4j.option.BlueprintsNeo4jOptionsBuilder;
import fr.inria.atlanmod.neoemf.data.blueprints.util.BlueprintsURI;
import fr.inria.atlanmod.neoemf.resource.PersistentResource;
import fr.inria.atlanmod.neoemf.resource.PersistentResourceFactory;

/**
 * Creates a new NeoEMF resource and fills it with the content of the defined
 * XMI file.
 */
public class ImportNeoEMFResource {

	/**
	 * The path of the XMI file to import in the NeoEMF resource.
	 */
	private static String XMI_MODEL_PATH = "models/sample.xmi";

	/**
	 * The path of the NeoEMF resource to create.
	 */
	private static String NEOEMF_MODEL_PATH = "models/sample.graphdb";

	/**
	 * Creates the NeoEMF resource and imports the content of the defined XMI file
	 * in it.
	 * <p>
	 * This method iterates in the content of the provided XMI file to check it has
	 * been loaded properly. Invalid file loading may occur when the NeoEMF EPackage
	 * is not properly registered, or when a regular EPackage is used instead of a
	 * NeoEMF one.
	 * 
	 * @param args
	 * @throws IOException if an error occurred when loading the XMI file or
	 *                     creating the NeoEMF resource
	 */
	public static void main(String[] args) throws IOException {
		ResourceSet rSet = new ResourceSetImpl();
		rSet.getPackageRegistry().put(JavaPackage.eNS_URI, JavaPackage.eINSTANCE);
		rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		Resource resource = rSet.getResource(URI.createURI(XMI_MODEL_PATH), true);
		int modelSize = ModelUtils.countEObjects(resource);
		System.out.println(MessageFormat.format("Loaded {0} elements", modelSize));
		File neoemfFile = new File(NEOEMF_MODEL_PATH);
		if (!neoemfFile.exists()) {
			/*
			 * Register NeoEMF persistence backend and protocol to enable NeoEMF resource
			 * loading.
			 */
			PersistenceBackendFactoryRegistry.register(BlueprintsURI.SCHEME,
					BlueprintsPersistenceBackendFactory.getInstance());
			rSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(BlueprintsURI.SCHEME,
					PersistentResourceFactory.getInstance());
			Resource neoemfResource = rSet.createResource(BlueprintsURI.createFileURI(neoemfFile));
			Map<String, Object> options = BlueprintsNeo4jOptionsBuilder.newBuilder().asMap();
			neoemfResource.save(options);
			neoemfResource.getContents().addAll(resource.getContents());
			neoemfResource.save(options);
			/*
			 * Closing the persistent resource is not mandatory (it is closed on JVM
			 * shutdown anyway), but is a good practice to avoid database lock issues.
			 */
			((PersistentResource) neoemfResource).close();
		} else {
			throw new IOException(MessageFormat.format("Cannot create the NeoEMF database: the file {0} already exists",
					neoemfFile.toString()));
		}
	}

}
