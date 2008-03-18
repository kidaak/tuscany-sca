package org.apache.tuscany.sca.osgi.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;

import org.apache.tuscany.sca.extensibility.ServiceDiscovery;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * OSGi bundle activator, which is run when Tuscany is run inside an OSGi runtime.
 *
 */
public class OSGiBundleActivator implements BundleActivator, BundleListener {
	
	private static final String TUSCANY_SCA_BUNDLE_PREFIX = "org.apache.tuscany.sca";
	private static final String TUSCANY_3RD_PARTY_BUNDLE_PREFIX = "org.apache.tuscany.sca.3rdparty";
	private OSGiRuntime runtime;
	private BundleClassLoader serviceDiscoveryClassLoader;
	private BundleClassLoader threadContextClassLoader;
	private ClassLoader origTCCL;
	private Bundle thisBundle;

	public void start(BundleContext bundleContext) throws Exception {
		
		initializeTuscanyClassLoaders(bundleContext);
		
		runtime = OSGiRuntime.findRuntime();
		runtime.setBundleContext(bundleContext);
		runtime.initialize();
		
	}

	public void stop(BundleContext bundleContext) throws Exception {
		runtime.shutdown();
		
        if (Thread.currentThread().getContextClassLoader() == threadContextClassLoader)
            Thread.currentThread().setContextClassLoader(origTCCL);
	}
	
	/**
	 * Create a bundle classloader which has visibility of all Tuscany related
	 * bundles. Use this classloader as TCCL, and the Tuscany service discovery
	 * classloader.
	 * 
	 * @param bundleContext
	 */
	private void initializeTuscanyClassLoaders(BundleContext bundleContext) {
		
		thisBundle = bundleContext.getBundle();

		origTCCL = Thread.currentThread().getContextClassLoader();
		
		threadContextClassLoader = new BundleClassLoader(thisBundle, origTCCL);
		
		Thread.currentThread().setContextClassLoader(threadContextClassLoader);
		
		serviceDiscoveryClassLoader = new BundleClassLoader(thisBundle, origTCCL);

		Bundle[] bundles = bundleContext.getBundles();
		for (Bundle bundle : bundles) {
			updateBundleClassLoader(bundle);			
		}
		bundleContext.addBundleListener(this);
		
		ServiceDiscovery.getInstance().registerClassLoader(serviceDiscoveryClassLoader);
		
		
		
	}
	
	/**
	 * Add an installed bundle to the list of bundles in the service discovery classpath
	 * if the installed bundle is a Tuscany bundle using the same Core-SPI bundle.
	 * Since the bundle containing this activator is in Tuscany Runtime, this bundle
	 * will provide access to Runtime and its dependencies (SPI, SCA-API). Third party
	 * bundles dont need to be in the service discovery classpath, since they will be automatically
	 * imported by bundles which require them. That leaves Tuscany extension bundles,
	 * which are added to the service discovery classpath here.
	 * 
	 * 3rd party bundle should be explicity added to TCCL since classes from these
	 * bundles use TCCL to load other classes from the bundle.
	 * 
	 * Load one class from the bundle to check if the new bundle matches this runtime bundle.
	 * 
	 * @param bundle
	 */
	private void updateBundleClassLoader(Bundle bundle) {
		
		if (bundle.getSymbolicName().startsWith(TUSCANY_SCA_BUNDLE_PREFIX)) {
			

			// This may be the third party bundle.
		    if (bundle.getSymbolicName().startsWith(TUSCANY_3RD_PARTY_BUNDLE_PREFIX)) {
					
				try {
					String xmlInputFactory = "javax.xml.stream.XMLInputFactory";
					if (thisBundle.loadClass(xmlInputFactory) == bundle.loadClass(xmlInputFactory)) {
							
					    threadContextClassLoader.addBundle(bundle);
					}
				} catch (ClassNotFoundException e) {
					// Ignore
				}
			} 
		    else {
			
			    String moduleActivator = "org.apache.tuscany.sca.core.ModuleActivator";
			
			    try {
				    if (thisBundle.loadClass(moduleActivator) == bundle.loadClass(moduleActivator)) {
					
				        serviceDiscoveryClassLoader.addBundle(bundle);
				        threadContextClassLoader.addBundle(bundle);
				    }
			    } catch (ClassNotFoundException e) {
				    // Ignore				
			    }
		    }
		}
	}


	/**
	 * Handle bundle install/uninstall events 
	 */
	public void bundleChanged(BundleEvent event) {
		
		Bundle bundle = event.getBundle();
		if (event.getType() == BundleEvent.UNINSTALLED) {
			serviceDiscoveryClassLoader.removeBundle(bundle);
			threadContextClassLoader.removeBundle(bundle);
		}
		else if (event.getType() == BundleEvent.INSTALLED) {
			updateBundleClassLoader(bundle);
		}
	}




	/**
	 * Bundle classloader that searches a bundle classpath consisting of
	 * a list of bundles. The parent classloader is searched only if a class
	 * cannot be loaded from the bundle classpath. Tuscany bundles are
	 * dynamically added and removed from the bundle classpath when the bundles
	 * are installed and uninstalled.
	 * 
	 * No ordering of bundles is maintained at the moment.
	 *
	 */
	private static class BundleClassLoader extends ClassLoader {
		
		private HashSet<Bundle> bundles;
		
		BundleClassLoader(Bundle bundle, ClassLoader parent) {
			super(parent);
			this.bundles = new HashSet<Bundle>();
			bundles.add(bundle);
		}
		
		private synchronized void addBundle(Bundle bundle) {
			bundles.add(bundle);
		}
		
		private synchronized void removeBundle(Bundle bundle) {
			if (bundles.contains(bundle))
			    bundles.remove(bundle);
		}
		
        
		@Override
        protected Class<?> findClass(String className) throws ClassNotFoundException {
            Class<?> clazz = null;
            synchronized (this) {
                for (Bundle bundle : bundles) {
                    try {
                        clazz = bundle.loadClass(className);
                        break;
                    } catch (ClassNotFoundException e) {
                    } catch (NoClassDefFoundError e) {
                    }

                }
            }           
            if (clazz != null) {
                return clazz;
            }
            return super.findClass(className);
        }


		@Override
		@SuppressWarnings("unchecked")
		public Enumeration<URL> getResources(String resName) throws IOException {
			HashSet<URL> urlSet = new HashSet<URL>();
			Enumeration<URL> urls = null;
			synchronized (this) {
				for (Bundle bundle : bundles) {
					urls = bundle.getResources(resName);
					if (urls != null) {
						while (urls.hasMoreElements()) {
							urlSet.add(urls.nextElement());
						}
					}
				}
			}			
			if (urlSet.size() > 0)
				return Collections.enumeration(urlSet);
			return super.getResources(resName);
		}

		@Override
		public URL getResource(String resName) {
			URL url = null;
			synchronized (this) {
				for (Bundle bundle : bundles) {
					url = bundle.getResource(resName);
					if (url != null)
						return url;
				}
			}			
			return super.getResource(resName);
		}

		@Override
		public String toString() {
			return "Tuscany BundleClassLoader " + bundles.iterator().next();
	    }
		
		
		
	}
}
