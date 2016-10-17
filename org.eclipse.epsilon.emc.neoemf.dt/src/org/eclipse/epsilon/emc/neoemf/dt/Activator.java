package org.eclipse.epsilon.emc.neoemf.dt;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	// The plug-in ID
		public static final String PLUGIN_ID = "org.eclipse.epsilon.emc.neoemf.dt"; //$NON-NLS-1$

		// The shared instance
		private static Activator plugin;
		
		/**
		 * The constructor
		 */
		public Activator() {
			plugin = this;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
		 */
		public void start(BundleContext context) throws Exception {
			super.start(context);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
		 */
		public void stop(BundleContext context) throws Exception {
			plugin = null;
			super.stop(context);
		}

		/**
		 * Returns the shared instance
		 *
		 * @return the shared instance
		 */
		public static Activator getDefault() {
			return plugin;
		}
		
		ImageRegistry imageRegistry = null; //new ImageRegistry();
		
		public ImageDescriptor getImageDescriptor(String path) {
			
			if (imageRegistry == null) imageRegistry = new ImageRegistry();
			
			ImageDescriptor descriptor = null;
			if (imageRegistry.getDescriptor(path) != null) {
				descriptor = imageRegistry.getDescriptor(path);
			}
			else {
				descriptor = imageDescriptorFromPlugin(PLUGIN_ID, path);
				imageRegistry.put(path, descriptor);
			}
			return descriptor;
		}
	
}
