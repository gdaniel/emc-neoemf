package org.eclipse.epsilon.emc.neoemf;

import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.epsilon.eol.dom.Expression;
import org.eclipse.epsilon.eol.dom.NameExpression;
import org.eclipse.epsilon.eol.dom.Parameter;
import org.eclipse.epsilon.eol.dom.PropertyCallExpression;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.execute.context.IEolContext;
import org.eclipse.epsilon.eol.execute.operations.declarative.CollectOperation;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.transform.InVertexPipe;
import com.tinkerpop.pipes.transform.OutEdgesPipe;
import com.tinkerpop.pipes.transform.PropertyPipe;

import static java.text.MessageFormat.format;

/**
 * A Gremlin implementation of the <i>collect</i> operation.
 * <p>
 * This class computes the result of a <i>collect</i> operation on top of a {@link GremlinPipelineListWrapper} by
 * creating a set of Gremlin steps expressing the collection logic as a traversal sequence.
 */
public class GremlinPipelineCollectOperation extends CollectOperation {

	@Override
	public Object execute(Object target, NameExpression operationNameExpression, List<Parameter> iterators,
			List<Expression> expressions, IEolContext context) throws EolRuntimeException {
		GremlinPipelineListWrapper wrapper = (GremlinPipelineListWrapper) target;
		GremlinPipeline<Vertex, Object> pipeline = wrapper.getPipeline();
		// TODO this should be a generic translation method
		if (expressions.size() == 1) { // multiple expressions not handled for now
			if (expressions.get(0) instanceof PropertyCallExpression) {
				PropertyCallExpression propertyCall = (PropertyCallExpression) expressions.get(0);
				String propertyName = propertyCall.getPropertyNameExpression().getName();
				EStructuralFeature feature = MetamodelUtils.getFeature(wrapper.getPipelineEndEClassifier(),
						propertyName);
				if (feature instanceof EAttribute) {
					pipeline.add(new PropertyPipe<Vertex, String>(propertyCall.getPropertyNameExpression().getName()));
					// we can set it safely, it is an EDataType
					wrapper.setPipelineEndEClassifier(
							MetamodelUtils.getFeatureClassifier(wrapper.getPipelineEndEClassifier(), propertyName));
				} else if (feature instanceof EReference) {
					pipeline.add(new OutEdgesPipe(propertyName));
					pipeline.add(new InVertexPipe());
					wrapper.setPipelineEndEClassifier(
							MetamodelUtils.getFeatureClassifier(wrapper.getPipelineEndEClassifier(), propertyName));
				} else {
					throw new IllegalArgumentException(format(
							"The provided feature {0} ({1}) is not an {2} or {3} instance", propertyName, propertyName,
							feature.getName(), EAttribute.class.getSimpleName(), EReference.class.getSimpleName()));
				}
				return wrapper;
			} else {
				throw new UnsupportedOperationException(format("Expected a {0} in the collect's expression, found {1}",
						PropertyCallExpression.class.getSimpleName(), expressions.get(0).getClass().getName()));
			}
		} else {
			throw new UnsupportedOperationException(format("Collect with multiple expressions are not supported"));
		}
	}

}
