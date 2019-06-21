package org.eclipse.epsilon.emc.neoemf;

import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.epsilon.eol.execute.operations.AbstractOperation;
import org.eclipse.epsilon.eol.execute.operations.declarative.IAbstractOperationContributor;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.gremlin.java.GremlinStartPipe;
import fr.inria.atlanmod.neoemf.util.logging.NeoLogger;

/**
 * A {@link GremlinPipeline} wrapper that allows to manipulate the pipeline contents as an {@link ImmutableList}.
 */
public class GremlinPipelineListWrapper extends ImmutableList implements IAbstractOperationContributor {

	private NeoEMFModel model;

	/**
	 * The {@link GremlinPipeline} used to define the query.
	 * <p>
	 * The pipeline's output type cannot be known in advance, it depends on the steps that are added to it through the
	 * ast navigation. Possible outputs include vertices, edges, as well as literals.
	 */
	private GremlinPipeline<Vertex, Object> pipeline;

	/**
	 * The {@link EClassifier} representing the EMF-related type of the pipeline output.
	 * <p>
	 * This type can be either an {@link EClass} if the pipeline returns serialized {@link EObject}s, or an
	 * {@link EDataType} if it returns a literal (e.g. an attribute value).
	 */
	private EClassifier pipelineEndClassifier;

	/**
	 * The {@link List} caching the results of the pipeline's iteration.
	 * <p>
	 * This list is used to allow multiple iterations of the pipeline's results. Pipes default behavior exhausts its
	 * inputs and does not allow to iterate multiple times the same pipeline.
	 */
	private List<Object> fetchedPipeline;

	/**
	 * Builds a new {@link GremlinPipelineListWrapper} to traverse the provided {@code model}.
	 * <p>
	 * The pipeline is initialized with an set of vertices (i.e. the starting point of the traversal), and an
	 * {@link EClassifier} representing the EMF-related type of the provided vertices.
	 *
	 * @param model                 the {@link NeoEMFModel} to traverse
	 * @param vertices              the starting point of the traversal
	 * @param pipelineEndClassifier the {@link EClassifier} representing the EMF-related type of the provided vertices
	 * @return the created {@link GremlinPipelineListWrapper}
	 */
	public static GremlinPipelineListWrapper pipelineOf(NeoEMFModel model, Iterable<Vertex> vertices,
			EClassifier pipelineEndClassifier) {
		GremlinPipeline<Vertex, Object> pipeline = new GremlinPipeline<>(new GremlinStartPipe(vertices));
		pipeline.setStarts(vertices);
		return new GremlinPipelineListWrapper(model, pipeline, pipelineEndClassifier);
	}

	/**
	 * Builds a new {@link GremlinPipelineListWrapper} to traverse the provided {@code model}.
	 * <p>
	 * The pipeline is initialized with a {@link GremlinPipeline} holding the traversal logic, and an
	 * {@link EClassifier} representing the EMF-related type of the pipeline's output.
	 *
	 * @param model                 the {@link NeoEMFModel} to traverse
	 * @param pipeline              the {@link GremlinPipeline} holding the traversal logic
	 * @param pipelineEndClassifier the {@link EClassifier} representing the EMF-related type of the pipeline's output.
	 */
	public GremlinPipelineListWrapper(NeoEMFModel model, GremlinPipeline<Vertex, Object> pipeline,
			EClassifier pipelineEndClassifier) {
		this.model = model;
		this.pipeline = pipeline;
		this.pipelineEndClassifier = pipelineEndClassifier;
	}

	/**
	 * Returns the internal {@link GremlinPipeline}.
	 * <p>
	 * This method is used to add computation steps to an existing {@link GremlinPipeline}.
	 *
	 * @return the internal {@link GremlinPipeline}
	 */
	public GremlinPipeline<Vertex, Object> getPipeline() {
		return pipeline;
	}

	/**
	 * Returns the {@link NeoEMFModel} traversed by this pipeline.
	 *
	 * @return the {@link NeoEMFModel} traversed by this pipeline
	 */
	public NeoEMFModel getModel() {
		return model;
	}

	/**
	 * Returns the {@link EClassifier} representing the EMF-related type of the pipeline's output.
	 *
	 * @return the {@link EClassifier} representing the EMF-related type of the pipeline's output
	 */
	public EClassifier getPipelineEndEClassifier() {
		return pipelineEndClassifier;
	}

	/**
	 * Sets the type of the pipeline's output with the provided {@code newEClassifier}.
	 * <p>
	 * This method is used when a new computation steps are added to an existing {@link GremlinPipeline} (see
	 * {@link #getPipeline()}), and when those steps change the pipeline's output type.
	 *
	 * @param newEClassifier the {@link EClassifier} to set
	 */
	public void setPipelineEndEClassifier(EClassifier newEClassifier) {
		this.pipelineEndClassifier = newEClassifier;
	}

	/**
	 * Provides Gremlin implementations of abstract operations.
	 * <p>
	 * This method is called internally by the EOL engine, and returns specific operations optimized for the underlying
	 * Gremlin engine.
	 */
	@Override
	public AbstractOperation getAbstractOperation(String name) {
		if ("collect".equals(name)) {
			return new GremlinPipelineCollectOperation();
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * Fetches the content of the pipeline.
	 * <p>
	 * This method caches the results of iterating the pipeline into a list in order to allow multiple iterations of the
	 * pipeline's results.
	 * <p>
	 * <b>Note:</b> if a vertex is returned by the pipeline this method will attempt to reify it into a regular
	 * {@link EObject}.
	 */
	private void fetchPipeline() {
		this.fetchedPipeline = new ArrayList<>();
		for (Object o : this.pipeline) {
			if (o instanceof Vertex) {
				fetchedPipeline.add(model.getBackend().reifyVertex((Vertex) o));
			} else {
				fetchedPipeline.add(o);
			}
		}
	}

	@Override
	public int size() {
		if (isNull(fetchedPipeline)) {
			fetchPipeline();
		}
		return fetchedPipeline.size();
//		return Math.toIntExact(this.pipeline.count());
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EObject get(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator iterator() {
		NeoLogger.info("Getting an iterator on the Gremlin Pipeline (iterating a full pipeline can be costly)");
		if (isNull(fetchedPipeline)) {
			fetchPipeline();
		}
		return fetchedPipeline.iterator();
		// iterator is exhausted when pretty printing huhu
//		return pipeline.iterator();
	}

	@Override
	public ListIterator listIterator() {
		throw new UnsupportedOperationException();
	}

}
