package org.eclipse.epsilon.emc.neoemf;

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

import fr.inria.atlanmod.neoemf.data.PersistenceBackendFactoryRegistry;
import fr.inria.atlanmod.neoemf.data.blueprints.BlueprintsPersistenceBackendFactory;
import fr.inria.atlanmod.neoemf.data.blueprints.neo4j.option.BlueprintsNeo4jOptionsBuilder;
import fr.inria.atlanmod.neoemf.data.blueprints.neo4j.option.BlueprintsNeo4jResourceOptions;
import fr.inria.atlanmod.neoemf.data.blueprints.util.BlueprintsURI;
import fr.inria.atlanmod.neoemf.data.mapdb.option.MapDbOptionsBuilder;
import fr.inria.atlanmod.neoemf.option.AbstractPersistenceOptionsBuilder;
import fr.inria.atlanmod.neoemf.option.PersistentResourceOptions;
import fr.inria.atlanmod.neoemf.option.PersistentStoreOptions;
import fr.inria.atlanmod.neoemf.resource.DefaultPersistentResource;
import fr.inria.atlanmod.neoemf.resource.PersistentResource;
import fr.inria.atlanmod.neoemf.resource.PersistentResourceFactory;

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
		
		AbstractPersistenceOptionsBuilder builder = null;
		if(resourceType.equals("Graph")) {
			builder = BlueprintsNeo4jOptionsBuilder.newBuilder();
		}
		else if(resourceType.equals("Map")) {
			builder = MapDbOptionsBuilder.newBuilder();
		}
		
		Map<String,Object> options = new HashMap<String, Object>();
		List<PersistentStoreOptions> storeOptions = new ArrayList<PersistentStoreOptions>();
		options.put(PersistentResourceOptions.STORE_OPTIONS, storeOptions);
		// set core-level options
		if(cacheSize)
			builder.cacheSizes();
		if(cacheIsSet)
			builder.cacheIsSet();
		if(cacheEStructuralFeatures)
			builder.cacheFeatures();
		if(logging)
			builder.log();
		if(resourceType.equals("Graph")) {
			BlueprintsNeo4jOptionsBuilder neoBuilder = (BlueprintsNeo4jOptionsBuilder)builder;
			if(!PersistenceBackendFactoryRegistry.isRegistered(BlueprintsURI.SCHEME)) {
				PersistenceBackendFactoryRegistry.register(BlueprintsURI.SCHEME, BlueprintsPersistenceBackendFactory.getInstance());
			}
			rSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(BlueprintsURI.SCHEME, PersistentResourceFactory.getInstance());
			this.modelImpl = rSet.createResource(BlueprintsURI.createFileURI(new File(neoemfPath)));
			
			// set graph-specific options
			if(autocommit) {
				if(autocommitChunk > 0) {
					neoBuilder.autocommit(autocommitChunk);
				}
				else {
					neoBuilder.autocommit();
				}
			}
			else {
				neoBuilder.directWrite();
			}
			if(cacheType.equals(BlueprintsNeo4jResourceOptions.CacheType.SOFT.toString())) {
				neoBuilder.softCache();
			} else if(cacheType.equals(BlueprintsNeo4jResourceOptions.CacheType.NONE.toString())) {
				neoBuilder.noCache();
			} else if(cacheType.equals(BlueprintsNeo4jResourceOptions.CacheType.STRONG.toString())) {
				neoBuilder.strongCache();
			} else if(cacheType.equals(BlueprintsNeo4jResourceOptions.CacheType.WEAK.toString())) {
				neoBuilder.weakCache();
			}
//			options.put(BlueprintsNeo4jResourceOptions.CACHE_TYPE, cacheType);
//			if(nodeCache > 0)
//				options.put(BlueprintsNeo4jResourceOptions.NODES_MAPPED_MEMORY, nodeCache + "M");
//			if(relationshipCache > 0)
//				options.put(BlueprintsNeo4jResourceOptions.RELATIONSHIPS_MAPPED_MEMORY, relationshipCache + "M");
//			if(propertyCache > 0)
//				options.put(BlueprintsNeo4jResourceOptions.PROPERTIES_MAPPED_MEMORY, propertyCache + "M");
//			if(stringCache > 0)
//				options.put(BlueprintsNeo4jResourceOptions.STRINGS_MAPPED_MEMORY, stringCache + "M");
//			if(arrayCache > 0)
//				options.put(BlueprintsNeo4jResourceOptions.ARRAYS_MAPPED_MEMORY, arrayCache + "M");
//			options.put(BlueprintsNeo4jResourceOptions.USE_MEMORY_MAPPED_BUFFERS, useMemoryMappedBuffers);
		}
		if(resourceType.equals("Map")) {
			throw new UnsupportedOperationException("NeoEMF EMC does not support Map backend for now");
//			if(!PersistenceBackendFactoryRegistry.isRegistered(MapDbURI.SCHEME)) {
//				PersistenceBackendFactoryRegistry.register(MapDbURI.SCHEME, MapDbPersistenceBackendFactory.getInstance());
//			}
//			rSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(MapDbURI.SCHEME, PersistentResourceFactory.getInstance());
//			this.modelImpl = rSet.createResource(MapDbURI.createFileURI(new File(neoemfPath)));
//			
//			// set map-specific options
//			if(autocommit) {
//				storeOptions.add(MapDbStoreOptions.AUTOCOMMIT);
//				storeOptions.add(MapDbStoreOptions.DIRECT_WRITE);
//				// Autocommit chunk size not supported in Map for now
//			} 
//			else {
//				storeOptions.add(MapDbStoreOptions.DIRECT_WRITE);
//			}
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
		((DefaultPersistentResource)getNeoEMFResource()).close();
		super.disposeModel();
	}
	
	protected PersistentResource getNeoEMFResource() {
		return (PersistentResource)modelImpl;
	}
	
	@Override
	protected Collection<EObject> getAllOfTypeFromModel(String type) throws EolModelElementTypeNotFoundException {
		System.out.println("Computing allOfType");
		if(modelImpl instanceof PersistentResource) {
			return ((PersistentResource)modelImpl).getAllInstances(classForName(type,getPackageRegistry()), true);
		}
		return super.getAllOfTypeFromModel(type);
	}
	
	@Override
	protected Collection<EObject> getAllOfKindFromModel(String kind) throws EolModelElementTypeNotFoundException {
		System.out.println("Computing allOfKind");
		if(modelImpl instanceof PersistentResource) {
			return ((PersistentResource)modelImpl).getAllInstances(classForName(kind,getPackageRegistry()));
		}
		return super.getAllOfKindFromModel(kind);
	}
	
}
