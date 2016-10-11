package org.eclipse.epsilon.emc.neoemf.graph.dt;

import org.eclipse.epsilon.common.dt.launching.dialogs.AbstractModelConfigurationDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import fr.inria.atlanmod.neoemf.graph.blueprints.neo4j.resources.BlueprintsNeo4jResourceOptions;

public class NeoEMFModelConfigurationDialog extends AbstractModelConfigurationDialog {

	private Text txtPath;
	
	@Override
	protected String getModelName() {
		return "NeoEMF/Graph Model";
	}
	
	@Override
	protected String getModelType() {
		return "neoemf-graph";
	}
	
	@Override
	protected void createGroups(Composite parent) {
		super.createGroups(parent);
		createAccessGroup(parent);
		createStoreOptionGroup(parent);
		createGraphOptionGroup(parent);
	}
	
	protected void createAccessGroup(Composite parent) {
		final Composite groupContent = createGroupContainer(parent, "Resource Access", 3);
		
		final Label lblPath = new Label(groupContent, SWT.NONE);
		lblPath.setText("Path:");
		txtPath = new Text(groupContent, SWT.BORDER);
		txtPath.setText("models/myResource.graphdb");
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		txtPath.setLayoutData(gd);
		
		final Label backendType = new Label(groupContent, SWT.NONE);
		backendType.setText("Backend");
		final Button graphRadio = new Button(groupContent, SWT.RADIO);
		graphRadio.setSelection(true);
		graphRadio.setText("Graph");
		final Button mapRadio = new Button(groupContent, SWT.RADIO);
		mapRadio.setText("Map");
		
	}
	
	protected void createStoreOptionGroup(Composite parent) {
		final Composite groupContent = createGroupContainer(parent, "Store Options", 3);
		
		final Label storeTypeLabel = new Label(groupContent, SWT.NONE);
		storeTypeLabel.setText("Store Type");
		
		final Button directWriteRadio = new Button(groupContent, SWT.RADIO);
		directWriteRadio.setText("Direct Write");
		directWriteRadio.setSelection(true);
		final Button autocommitRadio = new Button(groupContent, SWT.RADIO);
		autocommitRadio.setText("Autocommit");
		
		final Label autocommitChunk = new Label(groupContent, SWT.NONE);
		autocommitChunk.setText("Autocommit Chunk");
		final Text autocommitChunkText = new Text(groupContent, SWT.BORDER);
		autocommitChunkText.setText("10000");
		autocommitChunkText.setEnabled(false);
	}
	
	protected void createGraphOptionGroup(Composite parent) {
		final Composite groupContent = createGroupContainer(parent, "Neo4j Options", 2);
		
		final Label cacheTypeLabel = new Label(groupContent, SWT.NONE);
		cacheTypeLabel.setText("Cache");
		
		final Combo cacheTypeCombo = new Combo(groupContent, SWT.BORDER | SWT.READ_ONLY);
		cacheTypeCombo.add(BlueprintsNeo4jResourceOptions.CacheType.NONE.toString());
		cacheTypeCombo.add(BlueprintsNeo4jResourceOptions.CacheType.WEAK.toString());
		cacheTypeCombo.add(BlueprintsNeo4jResourceOptions.CacheType.SOFT.toString());
		cacheTypeCombo.add(BlueprintsNeo4jResourceOptions.CacheType.STRONG.toString());
		cacheTypeCombo.select(2);
		cacheTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		final Button useMemoryMapCheck = new Button(groupContent, SWT.CHECK);
		useMemoryMapCheck.setText("Use Memory-Mapped Buffers");
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		useMemoryMapCheck.setLayoutData(gd);
		
		final Label nodeMem = new Label(groupContent, SWT.NONE);
		nodeMem.setText("Node Cache (MB)");
		final Text nodeMemText = new Text(groupContent, SWT.BORDER);
		nodeMemText.setText("64");
		nodeMemText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		final Label relMem = new Label(groupContent, SWT.NONE);
		relMem.setText("Relationship Cache (MB)");
		final Text relMemText = new Text(groupContent, SWT.BORDER);
		relMemText.setText("64");
		relMemText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		final Label propMem = new Label(groupContent, SWT.NONE);
		propMem.setText("Property Cache (MB)");
		final Text propMemText = new Text(groupContent, SWT.BORDER);
		propMemText.setText("64");
		propMemText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		final Label stringMem = new Label(groupContent, SWT.NONE);
		stringMem.setText("String Cache (MB)");
		final Text stringMemText = new Text(groupContent, SWT.BORDER);
		stringMemText.setText("64");
		stringMemText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		final Label arrayMem = new Label(groupContent, SWT.NONE);
		arrayMem.setText("Array Cache (MB)");
		final Text arrayMemText = new Text(groupContent, SWT.BORDER);
		arrayMemText.setText("64");
		arrayMemText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}
}
