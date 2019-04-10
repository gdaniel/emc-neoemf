package org.eclipse.epsilon.emc.neoemf;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.emf.ecore.EClass;
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

import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;
import com.tinkerpop.pipes.transform.InEdgesPipe;
import com.tinkerpop.pipes.transform.OutVertexPipe;

import fr.inria.atlanmod.neoemf.data.PersistenceBackendFactoryRegistry;
import fr.inria.atlanmod.neoemf.data.blueprints.BlueprintsPersistenceBackend;
import fr.inria.atlanmod.neoemf.data.blueprints.BlueprintsPersistenceBackendFactory;
import fr.inria.atlanmod.neoemf.data.blueprints.neo4j.option.BlueprintsNeo4jOptionsBuilder;
import fr.inria.atlanmod.neoemf.data.blueprints.neo4j.option.BlueprintsNeo4jResourceOptions;
import fr.inria.atlanmod.neoemf.data.blueprints.util.BlueprintsURI;
import fr.inria.atlanmod.neoemf.data.mapdb.option.MapDbOptionsBuilder;
import fr.inria.atlanmod.neoemf.option.AbstractPersistenceOptionsBuilder;
import fr.inria.atlanmod.neoemf.resource.DefaultPersistentResource;
import fr.inria.atlanmod.neoemf.resource.PersistentResource;
import fr.inria.atlanmod.neoemf.resource.PersistentResourceFactory;

public class NeoEMFModel extends AbstractEmfModel {

	// Core properties
	public static final String PROPERTY_NEOEMF_PATH = "neoemf.path";
	public static final String PROPERTY_METAMODEL_URI = "metamodel.uri";
	public static final String PROPERTY_GREMLIN = "native.gremlin";
	public static final String PROPERTY_NEOEMF_RESOURCE_TYPE = "neoemf.resource.type";
	public static final String PROPERTY_AUTOCOMMIT = "neoemf.autocommit";
	public static final String PROPERTY_AUTOCOMMIT_CHUNK = "neoemf.autocommit.chunk";
	public static final String PROPERTY_CACHE_SIZE = "neoemf.cache.size";
	public static final String PROPERTY_CACHE_ISSET = "neoemf.cache.isset";
	public static final String PROPERTY_CACHE_ESTRUCTURALFEATURES = "neoemf.cache.estructuralfeatures";
	public static final String PROPERTY_LOGGING = "neoemf.logging";

	// Neo4j properties
	public static final String PROPERTY_NEO4J_CACHE_TYPE = "neoemf.blueprints.neo4j.cache.type";


	private String neoemfPath, metamodelURI, resourceType, cacheType;
	private boolean nativeGremlin, autocommit, cacheSize, cacheIsSet, cacheEStructuralFeatures, logging;
	private int autocommitChunk;

	private BlueprintsPersistenceBackend blueprintsBackend;

	private ResourceSet rSet;

	private IdGraph<KeyIndexableGraph> graph;

	private Index<Vertex> metaclassIndex;

//	private EPackage metamodel;

	@Override
	public void load(StringProperties properties, IRelativePathResolver resolver) throws EolModelLoadingException {
		super.load(properties, resolver);

		this.neoemfPath = properties.getProperty(PROPERTY_NEOEMF_PATH);
		this.metamodelURI = properties.getProperty(PROPERTY_METAMODEL_URI);
		this.nativeGremlin = properties.hasProperty(PROPERTY_GREMLIN);
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
			if(properties.hasProperty(PROPERTY_NEO4J_CACHE_TYPE)) {
				cacheType = properties.getProperty(PROPERTY_NEO4J_CACHE_TYPE);
			}
		}
//		loadMetamodel();
		load();
	}

//	private void loadMetamodel() {
//		metamodel = getPackageRegistry().getEPackage(metamodelURI);
//	}

//	public EPackage getMetamodel() {
//		return metamodel;
//	}

	public BlueprintsPersistenceBackend getBackend() {
		return blueprintsBackend;
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
		}
		if(resourceType.equals("Map")) {
			throw new UnsupportedOperationException("NeoEMF EMC does not support Map backend for now");
		}
		try {
			this.modelImpl.load(builder.asMap());
		} catch(IOException e) {
			throw new EolModelLoadingException(e, this);
		}
		initBackend();
	}

	public void initBackend() {
		Field backendField;
		try {
			backendField = modelImpl.getClass().getDeclaredField("backend");
			backendField.setAccessible(true);
			blueprintsBackend = (BlueprintsPersistenceBackend) backendField.get(modelImpl);
		} catch(NoSuchFieldException | SecurityException | IllegalAccessException e) {
			throw new RuntimeException("Cannot retrieve the Blueprints backend, see attached exception", e);
		}
		graph = blueprintsBackend.getGraph();
		metaclassIndex = graph.getIndex("metaclasses", Vertex.class);
	}

	// required to configure the model without loading the resource
//	public void setMetamodelURI(String uri) {
//		this.metamodelURI = uri;
//	}

	// required to configure the model without loading the resource
	public void setGremlinSupport(boolean gremlin) {
		this.nativeGremlin = gremlin;
	}

	@Override
	protected Registry getPackageRegistry() {
	  // Use the local registry if it exists, fallback on global if not
	  Registry r = modelImpl.getResourceSet().getPackageRegistry();
	  if (r == null)
	    r = EPackage.Registry.INSTANCE;
	  return r;
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
	public boolean owns(Object instance) {
		if(instance instanceof GremlinPipelineListWrapper) {
			return ((GremlinPipelineListWrapper)instance).getModel() == this;
		}
		return super.owns(instance);
	}

	@Override
	protected Collection<EObject> getAllOfTypeFromModel(String type) throws EolModelElementTypeNotFoundException {
		System.out.println("Computing allOfType");
		if(nativeGremlin) {
			return getAllOfTypeFromModelGremlin(type);
		} else {
			return super.getAllOfTypeFromModel(type);
		}
	}

	private Collection<EObject> getAllOfTypeFromModelGremlin(String type) throws EolModelElementTypeNotFoundException {
		if(modelImpl instanceof PersistentResource) {
			System.out.println("Using Gremlin native connector to compute allOfType");
//			EClassifier typeClassifier = metamodel.getEClassifier(type);
			EClass typeClass = classForName(type);
			Iterable<Vertex> metaclass = metaclassIndex.get("name", type);
			GremlinPipelineListWrapper pipeline = GremlinPipelineListWrapper.pipelineOf(this, metaclass, typeClass);
			pipeline.getPipeline().add(new InEdgesPipe("kyanosInstanceOf"));
			pipeline.getPipeline().add(new OutVertexPipe());
			return pipeline;
		}
		return super.getAllOfTypeFromModel(type);
	}

	@Override
	protected Collection<EObject> getAllOfKindFromModel(String kind) throws EolModelElementTypeNotFoundException {
		System.out.println("AllOfKind not supported, computing AllOfType");
		return getAllOfTypeFromModel(kind);
	}

}
