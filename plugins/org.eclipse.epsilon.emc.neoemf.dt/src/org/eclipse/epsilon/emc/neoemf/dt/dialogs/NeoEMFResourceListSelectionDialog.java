package org.eclipse.epsilon.emc.neoemf.dt.dialogs;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;

import fr.inria.atlanmod.neoemf.data.PersistenceBackendFactory;

public class NeoEMFResourceListSelectionDialog extends ResourceListSelectionDialog {

	public NeoEMFResourceListSelectionDialog(Shell parentShell, IResource[] resources) {
		super(parentShell,resources);
	}
	
	public NeoEMFResourceListSelectionDialog(Shell parentShell, IContainer container,
            int typeMask) {
		super(parentShell,container,typeMask);
	}
	
	/**
	 * Filter resources that actually represent NeoEMF resources
	 * @param resource the resource to filter
	 * @return true if the resource is a NeoEMF model, false otherwise
	 */
	@Override
	protected boolean select(IResource resource) {
		if(resource instanceof IFolder) {
			return ((IFolder)resource).getFile(PersistenceBackendFactory.CONFIG_FILE).exists();
		}
		return false;
	}
	
}
