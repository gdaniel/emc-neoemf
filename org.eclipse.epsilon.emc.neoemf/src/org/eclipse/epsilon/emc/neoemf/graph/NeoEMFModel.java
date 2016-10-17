package org.eclipse.epsilon.emc.neoemf.graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.emc.emf.AbstractEmfModel;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;

import fr.inria.atlanmod.neoemf.datastore.PersistenceBackendFactoryRegistry;
import fr.inria.atlanmod.neoemf.graph.blueprints.datastore.BlueprintsPersistenceBackendFactory;
import fr.inria.atlanmod.neoemf.graph.blueprints.neo4j.resources.BlueprintsNeo4jResourceOptions;
import fr.inria.atlanmod.neoemf.graph.blueprints.resources.BlueprintsResourceOptions;
import fr.inria.atlanmod.neoemf.graph.blueprints.util.NeoBlueprintsURI;
import fr.inria.atlanmod.neoemf.map.datastore.MapPersistenceBackendFactory;
import fr.inria.atlanmod.neoemf.map.resources.MapResourceOptions;
import fr.inria.atlanmod.neoemf.map.util.NeoMapURI;
import fr.inria.atlanmod.neoemf.resources.PersistentResource;
import fr.inria.atlanmod.neoemf.resources.PersistentResourceFactory;
import fr.inria.atlanmod.neoemf.resources.PersistentResourceOptions;
import fr.inria.atlanmod.neoemf.resources.PersistentResourceOptions.StoreOption;
import fr.inria.atlanmod.neoemf.resources.impl.PersistentResourceImpl;

public class NeoEMFModel extends AbstractEmfModel {

	// Core properties
	public static final String PROPERTY_NEOEMF_PATH = "neoemf.path";
	public static final String PROPERTY_NEOEMF_RESOURCE_TYPE = "neoemf.resource.type";
	public static final String PROPERTY_AUTOCOMMIT = "neoemf.autocommit";
	public static final String PROPERTY_AUTOCOMMIT_CHUNK = "neoemf.autocommit.chunk";
	public static final String PROPERTY_CACHE_SIZE = "neoemf.cache.size";
	public static final String PROPERTY_CACHE_ISSET = "neoemf.cache.isset";
	public static final String PROPERTY_CACHE_ESTRUCTURALFEATURES = "neoemf.cache.estructuralfeatures";
	public static final String PROPERTY_LOGGING = "neoemf.logging";
	
	// Neo4j properties
	public static final String PROPERTY_NEO4J_CACHE_TYPE = "neoemf.blueprints.neo4j.cache.type";
	public static final String PROPERTY_NEO4J_USE_MEMORY_MAPPED_BUFFERS = "neoemf.blueprints.neo4j.use_memory_mapped_buffers";
	public static final String PROPERTY_NEO4J_NODE_CACHE_SIZE = "neoemf.blueprints.neo4j.cache.node.size";
	public static final String PROPERTY_NEO4J_RELATIONSHIP_CACHE_SIZE = "neoemf.blueprints.neo4j.cache.relationships.size";
	public static final String PROPERTY_NEO4J_PROPERTY_CACHE_SIZE = "neoemf.blueprints.neo4j.cache.property.size";
	public static final String PROPERTY_NEO4J_STRING_CACHE_SIZE = "neoemf.blueprints.neo4j.cache.string.size";
	public static final String PROPERTY_NEO4J_ARRAY_CACHE_SIZE = "neoemf.blueprints.neo4j.cache.array.size";
	
	
	private String neoemfPath, resourceType, cacheType;
	private boolean autocommit, cacheSize, cacheIsSet, cacheEStructuralFeatures, logging, useMemoryMappedBuffers;
	private int autocommitChunk, nodeCache, relationshipCache, propertyCache, stringCache, arrayCache;
	private ResourceSet rSet;
	
	@Override
	public void load(StringProperties properties, IRelativePathResolver resolver) throws EolModelLoadingException {
		super.load(properties, resolver);

		this.neoemfPath = properties.getProperty(PROPERTY_NEOEMF_PATH);
		this.resourceType = properties.getProperty(PROPERTY_NEOEMF_RESOURCE_TYPE);
		this.autocommit = properties.hasProperty(PROPERTY_AUTOCOMMIT);
		if(this.autocommit) {
			autocommitChunk = Integer.valueOf(properties.getProperty(PROPERTY_AUTOCOMMIT_CHUNK));
		}
		this.cacheSize = properties.hasProperty(PROPERTY_CACHE_SIZE);
		this.cacheIsSet = properties.hasProperty(PROPERTY_CACHE_ISSET);
		this.cacheEStructuralFeatures = properties.hasProperty(PROPERTY_CACHE_ESTRUCTURALFEATURES);
		this.logging = properties.hasProperty(PROPERTY_LOGGING);
		
		if(this.resourceType.equals("Graph")) {
			useMemoryMappedBuffers = properties.hasProperty(PROPERTY_NEO4J_USE_MEMORY_MAPPED_BUFFERS);
			if(properties.hasProperty(PROPERTY_NEO4J_CACHE_TYPE)) {
				cacheType = properties.getProperty(PROPERTY_NEO4J_CACHE_TYPE);
			}
			if(properties.hasProperty(PROPERTY_NEO4J_NODE_CACHE_SIZE)) {
				nodeCache = Integer.valueOf(properties.getProperty(PROPERTY_NEO4J_NODE_CACHE_SIZE));
			}
			if(properties.hasProperty(PROPERTY_NEO4J_RELATIONSHIP_CACHE_SIZE)) {
				relationshipCache = Integer.valueOf(properties.getProperty(PROPERTY_NEO4J_RELATIONSHIP_CACHE_SIZE));
			}
			if(properties.hasProperty(PROPERTY_NEO4J_PROPERTY_CACHE_SIZE)) {
				propertyCache = Integer.valueOf(properties.getProperty(PROPERTY_NEO4J_PROPERTY_CACHE_SIZE));
			}
			if(properties.hasProperty(PROPERTY_NEO4J_STRING_CACHE_SIZE)) {
				stringCache = Integer.valueOf(properties.getProperty(PROPERTY_NEO4J_STRING_CACHE_SIZE));
			}
			if(properties.hasProperty(PROPERTY_NEO4J_ARRAY_CACHE_SIZE)) {
				arrayCache = Integer.valueOf(properties.getProperty(PROPERTY_NEO4J_ARRAY_CACHE_SIZE));
			}
		}		
		load();
	}
	
	@Override
	protected void loadModel() throws EolModelLoadingException {
		// Each model is loaded in a dedicated ResourceSet
		// TODO see if we can use a CachedResourceSet
		rSet = new ResourceSetImpl();
		
		Map<String,Object> options = new HashMap<String, Object>();
		List<StoreOption> storeOptions = new ArrayList<StoreOption>();
		options.put(PersistentResourceOptions.STORE_OPTIONS, storeOptions);
		// set core-level options
		if(cacheSize)
			storeOptions.add(PersistentResourceOptions.EStoreOption.SIZE_CACHING);
		if(cacheIsSet)
			storeOptions.add(PersistentResourceOptions.EStoreOption.IS_SET_CACHING);
		if(cacheEStructuralFeatures)
			storeOptions.add(PersistentResourceOptions.EStoreOption.ESTRUCUTRALFEATURE_CACHING);
		if(logging)
			storeOptions.add(PersistentResourceOptions.EStoreOption.LOGGING);
		if(resourceType.equals("Graph")) {
			if(!PersistenceBackendFactoryRegistry.isRegistered(NeoBlueprintsURI.NEO_GRAPH_SCHEME)) {
				PersistenceBackendFactoryRegistry.register(NeoBlueprintsURI.NEO_GRAPH_SCHEME, BlueprintsPersistenceBackendFactory.getInstance());
			}
			rSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(NeoBlueprintsURI.NEO_GRAPH_SCHEME, PersistentResourceFactory.eINSTANCE);
			this.modelImpl = rSet.createResource(NeoBlueprintsURI.createNeoGraphURI(new File(neoemfPath)));
			
			// set graph-specific options
			if(autocommit) {
				storeOptions.add(BlueprintsResourceOptions.EStoreGraphOption.AUTOCOMMIT);
				options.put(BlueprintsResourceOptions.OPTIONS_BLUEPRINTS_AUTOCOMMIT_CHUNK, autocommitChunk);
			}
			else {
				storeOptions.add(BlueprintsResourceOptions.EStoreGraphOption.DIRECT_WRITE);
			}
			options.put(BlueprintsNeo4jResourceOptions.OPTIONS_BLUEPRINTS_NEO4J_CACHE_TYPE, cacheType);
			if(nodeCache > 0)
				options.put(BlueprintsNeo4jResourceOptions.OPTIONS_BLUEPRINTS_NEO4J_NODES_MAPPED_MEMORY, nodeCache + "M");
			if(relationshipCache > 0)
				options.put(BlueprintsNeo4jResourceOptions.OPTIONS_BLUEPRINTS_NEO4J_RELATIONSHIPS_MAPPED_MEMORY, relationshipCache + "M");
			if(propertyCache > 0)
				options.put(BlueprintsNeo4jResourceOptions.OPTIONS_BLUEPRINTS_NEO4J_PROPERTIES_MAPPED_MEMORY, propertyCache + "M");
			if(stringCache > 0)
				options.put(BlueprintsNeo4jResourceOptions.OPTIONS_BLUEPRINTS_NEO4J_STRINGS_MAPPED_MEMORY, stringCache + "M");
			if(arrayCache > 0)
				options.put(BlueprintsNeo4jResourceOptions.OPTIONS_BLUEPRINTS_NEO4J_ARRAYS_MAPPED_MEMORY, arrayCache + "M");
			options.put(BlueprintsNeo4jResourceOptions.OPTIONS_BLUEPRINTS_NEO4J_USE_MEMORY_MAPPED_BUFFERS, useMemoryMappedBuffers);
		}
		if(resourceType.equals("Map")) {
			if(!PersistenceBackendFactoryRegistry.isRegistered(NeoMapURI.NEO_MAP_SCHEME)) {
				PersistenceBackendFactoryRegistry.register(NeoMapURI.NEO_MAP_SCHEME, MapPersistenceBackendFactory.getInstance());
			}
			rSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(NeoMapURI.NEO_MAP_SCHEME, PersistentResourceFactory.eINSTANCE);
			this.modelImpl = rSet.createResource(NeoMapURI.createNeoMapURI(new File(neoemfPath)));
			
			// set map-specific options
			if(autocommit) {
				storeOptions.add(MapResourceOptions.EStoreMapOption.AUTOCOMMIT);
				// Autocommit chunk size not supported in Map for now
			} 
			else {
				storeOptions.add(MapResourceOptions.EStoreMapOption.DIRECT_WRITE);
			}
		}
		try {
			this.modelImpl.load(options);
		} catch(IOException e) {
			throw new EolModelLoadingException(e, this);
		}
	}
	
	/*
	 *  For now the EPackages are retrieved in the global 
	 *  registry, and have to be registered somehow before
	 *  accessing the model.
	 */
	@Override
	protected Registry getPackageRegistry() {
		return EPackage.Registry.INSTANCE;
	}
	
	@Override
	public boolean store() {
		if (modelImpl == null) return false;
		
		try {
			modelImpl.save(Collections.emptyMap());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public void disposeModel() {
		PersistentResourceImpl.shutdownWithoutUnload((PersistentResourceImpl)getNeoEMFResource());
		super.disposeModel();
	}
	
	protected PersistentResource getNeoEMFResource() {
		return (PersistentResource)modelImpl;
	}
	
	@Override
	protected Collection<EObject> getAllOfTypeFromModel(String type) throws EolModelElementTypeNotFoundException {
		if(modelImpl instanceof PersistentResource) {
			return ((PersistentResource)modelImpl).getAllInstances(classForName(type,getPackageRegistry()), true);
		}
		return super.getAllOfTypeFromModel(type);
	}
	
	@Override
	protected Collection<EObject> getAllOfKindFromModel(String kind) throws EolModelElementTypeNotFoundException {
		if(modelImpl instanceof PersistentResource) {
			return ((PersistentResource)modelImpl).getAllInstances(classForName(kind,getPackageRegistry()));
		}
		return super.getAllOfKindFromModel(kind);
	}
	
}
