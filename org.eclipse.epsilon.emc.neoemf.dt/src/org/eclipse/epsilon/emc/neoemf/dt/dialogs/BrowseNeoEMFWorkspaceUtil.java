package org.eclipse.epsilon.emc.neoemf.dt.dialogs;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public class BrowseNeoEMFWorkspaceUtil {

	public static String browseResourcePath(Shell shell, String title, String message, String filter, Image image){
		IResource resource = browseResource(shell, title, message, filter, image);
		if (resource != null) {
			return resource.getRawLocation().toString();
		}
		else {
			return null;
		}
	}
	
	public static IResource browseResource(Shell shell, String title, String message, String filter, Image image){
		NeoEMFResourceListSelectionDialog elementSelector = new NeoEMFResourceListSelectionDialog(shell, ResourcesPlugin.getWorkspace().getRoot(), IResource.DEPTH_INFINITE | IResource.FILE );
		//elementSelector.setElements(ResourcesPlugin.getWorkspace().getRoot().get)
		//elementSelector.setInput(ResourcesPlugin.getWorkspace().getRoot());
		elementSelector.setTitle(title);
		elementSelector.setMessage(message);
		//elementSelector.setAllowMultiple(false);
		//elementSelector.setImage(image);
		elementSelector.open();
		
		if (elementSelector.getReturnCode() == Window.OK){
			IResource r = (IResource) elementSelector.getResult()[0];
			//return f.getLocation().toOSString();
			return r;
		}
		else {
			return null;
		}
	}	
}
