package org.eclipse.epsilon.emc.neoemf.graph;

import java.io.File;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.emc.emf.AbstractEmfModel;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;

import fr.inria.atlanmod.neoemf.datastore.PersistenceBackendFactoryRegistry;
import fr.inria.atlanmod.neoemf.graph.blueprints.datastore.BlueprintsPersistenceBackendFactory;
import fr.inria.atlanmod.neoemf.graph.blueprints.util.NeoBlueprintsURI;
import fr.inria.atlanmod.neoemf.map.datastore.MapPersistenceBackendFactory;
import fr.inria.atlanmod.neoemf.map.util.NeoMapURI;
import fr.inria.atlanmod.neoemf.resources.PersistentResource;
import fr.inria.atlanmod.neoemf.resources.PersistentResourceFactory;
import fr.inria.atlanmod.neoemf.resources.impl.PersistentResourceImpl;

public class NeoEMFModel extends AbstractEmfModel {

	public static final String PROPERTY_NEOEMF_PATH = "neoemf.path";
	
	private String neoemfPath;
	private ResourceSet rSet;
	
	@Override
	public void load(StringProperties properties, IRelativePathResolver resolver) throws EolModelLoadingException {
		super.load(properties, resolver);
		this.neoemfPath = (String)properties.getProperty(PROPERTY_NEOEMF_PATH);
		if(!PersistenceBackendFactoryRegistry.isRegistered(NeoBlueprintsURI.NEO_GRAPH_SCHEME)) {
			PersistenceBackendFactoryRegistry.register(NeoBlueprintsURI.NEO_GRAPH_SCHEME, BlueprintsPersistenceBackendFactory.getInstance());
		}
		if(!PersistenceBackendFactoryRegistry.isRegistered(NeoMapURI.NEO_MAP_SCHEME)) {
			PersistenceBackendFactoryRegistry.register(NeoMapURI.NEO_MAP_SCHEME, MapPersistenceBackendFactory.getInstance());
		}
		rSet = new ResourceSetImpl();
		rSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(NeoBlueprintsURI.NEO_GRAPH_SCHEME, PersistentResourceFactory.eINSTANCE);
		rSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(NeoMapURI.NEO_MAP_SCHEME, PersistentResourceFactory.eINSTANCE);
		
		load();
	}
	
	@Override
	protected void loadModel() throws EolModelLoadingException {
		this.modelImpl = rSet.createResource(NeoBlueprintsURI.createNeoGraphURI(new File(neoemfPath)));
	}
	
	@Override
	public void disposeModel() {
		PersistentResourceImpl.shutdownWithoutUnload((PersistentResourceImpl)getNeoEMFResource());
		super.disposeModel();
	}
	
	protected PersistentResource getNeoEMFResource() {
		return (PersistentResource)modelImpl;
	}
	
}
