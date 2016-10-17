package org.eclipse.epsilon.emc.neoemf.dt;

import java.util.Arrays;

import org.eclipse.epsilon.common.dt.launching.dialogs.AbstractModelConfigurationDialog;
import org.eclipse.epsilon.emc.neoemf.dt.dialogs.BrowseNeoEMFWorkspaceUtil;
import org.eclipse.epsilon.emc.neoemf.graph.NeoEMFModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import fr.inria.atlanmod.neoemf.graph.blueprints.neo4j.resources.BlueprintsNeo4jResourceOptions;

public class NeoEMFModelConfigurationDialog extends AbstractModelConfigurationDialog {

	private String[] cacheTypeList = {
			BlueprintsNeo4jResourceOptions.CacheType.NONE.toString(),
			BlueprintsNeo4jResourceOptions.CacheType.WEAK.toString(),
			BlueprintsNeo4jResourceOptions.CacheType.SOFT.toString(),
			BlueprintsNeo4jResourceOptions.CacheType.STRONG.toString()
			};
	
	private Text pathText;
	private Button autocommitCheck;
	private Text autocommitChunkText;
	private Button cacheSizeCheck;
	private Button cacheIsSetCheck;
	private Button cacheEStructuralFeaturesCheck;
	private Button loggingCheck;
	
	private Button graphRadio;
	private Button mapRadio;
	
	private Combo cacheTypeCombo;
	private Button useMemoryMapCheck;
	private Text nodeMemText;
	private Text relMemText;
	private Text propMemText;
	private Text stringMemText;
	private Text arrayMemText;
	
	private Composite parent;
	private Composite storeOptionContent;
	private Composite graphOptionContent;
	
	@Override
	protected String getModelName() {
		return "NeoEMF Model";
	}
	
	@Override
	protected String getModelType() {
		return "neoemf";
	}
	
	@Override
	protected void createGroups(Composite parent) {
		super.createGroups(parent);
		this.parent = parent;
		createAccessGroup(parent);
		createStoreOptionGroup(parent);
		createGraphOptionGroup(parent);
		createLoadStoreOptionsGroup(parent);
		// TODO integrate MapDB options
//		createMapOptionGroup(parent);
	}
	
	protected void createAccessGroup(Composite parent) {
		final Composite groupContent = createGroupContainer(parent, "Resource Access", 3);
		
		final Label lblPath = new Label(groupContent, SWT.NONE);
		lblPath.setText("Path:");
		pathText = new Text(groupContent, SWT.BORDER);
		pathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		final Button browseModelFile = new Button(groupContent, SWT.NONE);
		browseModelFile.setText("Browse Worskpace...");
		browseModelFile.addListener(SWT.Selection, 
			new BrowseWorkspaceForNeoEMFModelsListener(pathText, "NeoEMF models in the workspace", "Select a NeoEMF model"));
		
		final Label backendType = new Label(groupContent, SWT.NONE);
		backendType.setText("Backend");
		graphRadio = new Button(groupContent, SWT.RADIO);
		graphRadio.setSelection(true);
		graphRadio.setText("Graph");
		graphRadio.addSelectionListener(backendListener);
		mapRadio = new Button(groupContent, SWT.RADIO);
		mapRadio.setText("Map");
		mapRadio.addSelectionListener(backendListener);
		
	}
	
	protected void createStoreOptionGroup(Composite parent) {
		storeOptionContent = createGroupContainer(parent, "Store Options", 2);
		
		final Label storeTypeLabel = new Label(storeOptionContent, SWT.NONE);
		storeTypeLabel.setText("Autocommit:");
		
		autocommitCheck = new Button(storeOptionContent, SWT.CHECK);
		autocommitCheck.addSelectionListener(autocommitListener);
		
		final Label autocommitChunk = new Label(storeOptionContent, SWT.NONE);
		autocommitChunk.setText("Autocommit Chunk");
		autocommitChunkText = new Text(storeOptionContent, SWT.BORDER);
		autocommitChunkText.setText("10000");
		autocommitChunkText.setEnabled(false);
		
		final Label cacheSizeLabel = new Label(storeOptionContent, SWT.NONE);
		cacheSizeLabel.setText("Cache size() results:");
		cacheSizeCheck = new Button(storeOptionContent, SWT.CHECK);
		
		final Label cacheIsSetLabel = new Label(storeOptionContent, SWT.NONE);
		cacheIsSetLabel.setText("Cache isSet() results:");
		cacheIsSetCheck = new Button(storeOptionContent, SWT.CHECK);
		
		final Label cacheEStructuralFeaturesLabel = new Label(storeOptionContent, SWT.NONE);
		cacheEStructuralFeaturesLabel.setText("Cache EStructuralFeatures:");
		cacheEStructuralFeaturesCheck = new Button(storeOptionContent, SWT.CHECK);
		
		final Label loggingLabel = new Label(storeOptionContent, SWT.NONE);
		loggingLabel.setText("Logging:");
		loggingCheck = new Button(storeOptionContent, SWT.CHECK);
	}
	
	protected void createGraphOptionGroup(Composite parent) {
		graphOptionContent = createGroupContainer(parent, "Neo4j Options", 2);
		
		final Label cacheTypeLabel = new Label(graphOptionContent, SWT.NONE);
		cacheTypeLabel.setText("Cache");
		
		cacheTypeCombo = new Combo(graphOptionContent, SWT.BORDER | SWT.READ_ONLY);
		cacheTypeCombo.setItems(cacheTypeList);
		cacheTypeCombo.select(2);
		cacheTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		useMemoryMapCheck = new Button(graphOptionContent, SWT.CHECK);
		useMemoryMapCheck.setText("Use Memory-Mapped Buffers");
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		useMemoryMapCheck.setLayoutData(gd);
		
		final Label nodeMem = new Label(graphOptionContent, SWT.NONE);
		nodeMem.setText("Node Cache (MB)");
		nodeMemText = new Text(graphOptionContent, SWT.BORDER);
		nodeMemText.setText("64");
		nodeMemText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		final Label relMem = new Label(graphOptionContent, SWT.NONE);
		relMem.setText("Relationship Cache (MB)");
		relMemText = new Text(graphOptionContent, SWT.BORDER);
		relMemText.setText("64");
		relMemText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		final Label propMem = new Label(graphOptionContent, SWT.NONE);
		propMem.setText("Property Cache (MB)");
		propMemText = new Text(graphOptionContent, SWT.BORDER);
		propMemText.setText("64");
		propMemText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		final Label stringMem = new Label(graphOptionContent, SWT.NONE);
		stringMem.setText("String Cache (MB)");
		stringMemText = new Text(graphOptionContent, SWT.BORDER);
		stringMemText.setText("64");
		stringMemText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		final Label arrayMem = new Label(graphOptionContent, SWT.NONE);
		arrayMem.setText("Array Cache (MB)");
		arrayMemText = new Text(graphOptionContent, SWT.BORDER);
		arrayMemText.setText("64");
		arrayMemText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	}
	
	@Override
	protected void loadProperties() {
		super.loadProperties();
		if(properties == null) return;
		
		pathText.setText(properties.getProperty(NeoEMFModel.PROPERTY_NEOEMF_PATH));
		if(properties.hasProperty(NeoEMFModel.PROPERTY_NEOEMF_RESOURCE_TYPE)) {
			String resourceType = properties.getProperty(NeoEMFModel.PROPERTY_NEOEMF_RESOURCE_TYPE);
			if(resourceType.equals("Map")) {
				mapRadio.setSelection(true);
				graphRadio.setSelection(false);
			}
			else if(resourceType.equals("Graph")) {
				mapRadio.setSelection(false);
				graphRadio.setSelection(true);
			}
			else {
				throw new RuntimeException("Unknown resource type: " + resourceType);
			}
		}
		autocommitCheck.setSelection(properties.hasProperty(NeoEMFModel.PROPERTY_AUTOCOMMIT));
		if(properties.hasProperty(NeoEMFModel.PROPERTY_AUTOCOMMIT_CHUNK)) {
			autocommitChunkText.setText((String) properties.getProperty(NeoEMFModel.PROPERTY_AUTOCOMMIT_CHUNK));
		}
		cacheSizeCheck.setSelection(properties.hasProperty(NeoEMFModel.PROPERTY_CACHE_SIZE));
		cacheIsSetCheck.setSelection(properties.hasProperty(NeoEMFModel.PROPERTY_CACHE_ISSET));
		cacheEStructuralFeaturesCheck.setSelection(properties.hasProperty(NeoEMFModel.PROPERTY_CACHE_ESTRUCTURALFEATURES));
		loggingCheck.setSelection(properties.hasProperty(NeoEMFModel.PROPERTY_LOGGING));
		
		if(properties.hasProperty(NeoEMFModel.PROPERTY_NEO4J_CACHE_TYPE)) {
			cacheTypeCombo.select(Arrays.asList(cacheTypeList).indexOf(properties.getProperty(NeoEMFModel.PROPERTY_NEO4J_CACHE_TYPE)));
		}
		useMemoryMapCheck.setSelection(properties.hasProperty(NeoEMFModel.PROPERTY_NEO4J_USE_MEMORY_MAPPED_BUFFERS));
		if(properties.hasProperty(NeoEMFModel.PROPERTY_NEO4J_NODE_CACHE_SIZE)) {
			nodeMemText.setText(properties.getProperty(NeoEMFModel.PROPERTY_NEO4J_NODE_CACHE_SIZE));
		}
		if(properties.hasProperty(NeoEMFModel.PROPERTY_NEO4J_RELATIONSHIP_CACHE_SIZE)) {
			relMemText.setText(properties.getProperty(NeoEMFModel.PROPERTY_NEO4J_RELATIONSHIP_CACHE_SIZE));
		}
		if(properties.hasProperty(NeoEMFModel.PROPERTY_NEO4J_PROPERTY_CACHE_SIZE)) {
			propMemText.setText(properties.getProperty(NeoEMFModel.PROPERTY_NEO4J_PROPERTY_CACHE_SIZE));
		}
		if(properties.hasProperty(NeoEMFModel.PROPERTY_NEO4J_STRING_CACHE_SIZE)) {
			stringMemText.setText(properties.getProperty(NeoEMFModel.PROPERTY_NEO4J_STRING_CACHE_SIZE));
		}
		if(properties.hasProperty(NeoEMFModel.PROPERTY_NEO4J_ARRAY_CACHE_SIZE)) {
			arrayMemText.setText(properties.getProperty(NeoEMFModel.PROPERTY_NEO4J_ARRAY_CACHE_SIZE));
		}
	}
	
	@Override
	protected void storeProperties() {
		super.storeProperties();
		
		properties.put(NeoEMFModel.PROPERTY_NEOEMF_PATH, pathText.getText());
		if(mapRadio.getSelection()) {
			properties.put(NeoEMFModel.PROPERTY_NEOEMF_RESOURCE_TYPE, "Map");
		}
		if(graphRadio.getSelection()) {
			properties.put(NeoEMFModel.PROPERTY_NEOEMF_RESOURCE_TYPE, "Graph");
		}
		if(autocommitCheck.getSelection()) {
			properties.put(NeoEMFModel.PROPERTY_AUTOCOMMIT, "1");
			properties.put(NeoEMFModel.PROPERTY_AUTOCOMMIT_CHUNK, autocommitChunkText.getText());
		}
		if(cacheSizeCheck.getSelection()) {
			properties.put(NeoEMFModel.PROPERTY_CACHE_SIZE, "1");
		}
		if(cacheIsSetCheck.getSelection()) {
			properties.put(NeoEMFModel.PROPERTY_CACHE_ISSET, "1");
		}
		if(cacheEStructuralFeaturesCheck.getSelection()) {
			properties.put(NeoEMFModel.PROPERTY_CACHE_ESTRUCTURALFEATURES, "1");
		}
		if(loggingCheck.getSelection()) {
			properties.put(NeoEMFModel.PROPERTY_LOGGING, "1");
		}
		
		if(graphRadio.getSelection()) {
			// Store Graph options only if the accessed model is mapped to a graph
			properties.put(NeoEMFModel.PROPERTY_NEO4J_CACHE_TYPE, cacheTypeCombo.getItem(cacheTypeCombo.getSelectionIndex()));
			if(useMemoryMapCheck.getSelection()) {
				properties.put(NeoEMFModel.PROPERTY_NEO4J_USE_MEMORY_MAPPED_BUFFERS, "1");
			}
			if(!nodeMemText.getText().isEmpty()) {
				properties.put(NeoEMFModel.PROPERTY_NEO4J_NODE_CACHE_SIZE, nodeMemText.getText());
			}
			if(relMemText.getText().isEmpty()) {
				properties.put(NeoEMFModel.PROPERTY_NEO4J_RELATIONSHIP_CACHE_SIZE, relMemText.getText());
			}
			if(propMemText.getText().isEmpty()) {
				properties.put(NeoEMFModel.PROPERTY_NEO4J_PROPERTY_CACHE_SIZE, propMemText.getText());
			}
			if(stringMemText.getText().isEmpty()) {
				properties.put(NeoEMFModel.PROPERTY_NEO4J_STRING_CACHE_SIZE, stringMemText.getText());
			}
			if(arrayMemText.getText().isEmpty()) {
				properties.put(NeoEMFModel.PROPERTY_NEO4J_ARRAY_CACHE_SIZE, arrayMemText.getText());
			}
		}
	}
	
	private SelectionListener backendListener = new SelectionListener() {	
		@Override
		public void widgetSelected(SelectionEvent e) {
			Button backendRadio = (Button)e.getSource();
			boolean isSelected = backendRadio.getSelection();
			if(isSelected) {
				if(backendRadio.getText().equals("Map")) {
					graphOptionContent.getParent().setVisible(false);
					((GridData)graphOptionContent.getParent().getLayoutData()).exclude = true;
					parent.layout();
				}
				else if(backendRadio.getText().equals("Graph")) {
					graphOptionContent.getParent().setVisible(true);
					((GridData)graphOptionContent.getParent().getLayoutData()).exclude = false;
					parent.layout();
				}
			}
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {}
	};
	
	private SelectionListener autocommitListener = new SelectionListener() {
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			Button autocommitCheck = (Button)e.getSource();
			boolean isSelected = autocommitCheck.getSelection();
			if(isSelected) {
				autocommitChunkText.setEnabled(true);
			}
			else {
				autocommitChunkText.setEnabled(false);
			}
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {}
	};
	
	private class BrowseWorkspaceForNeoEMFModelsListener implements Listener{
		
		private Text text = null;
		private String title = "";
		private String prompt = "";
		
		public BrowseWorkspaceForNeoEMFModelsListener(Text text, String title, String prompt){
			this.text = text;
		}
		
		public void handleEvent(Event event) {
			String resource = BrowseNeoEMFWorkspaceUtil.browseResourcePath(getShell(), 
					title, prompt, "", null);
			if (resource != null){
				text.setText(resource);
			}
		}
	}
}
