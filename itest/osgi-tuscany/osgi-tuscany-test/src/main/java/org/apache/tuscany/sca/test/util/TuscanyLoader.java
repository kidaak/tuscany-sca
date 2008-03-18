package org.apache.tuscany.sca.test.util;


import java.io.File;
import java.io.FilenameFilter;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Load Tuscany into an OSGi runtime
 *
 */
public class TuscanyLoader {
    
    private static final String scaApiDir = "sca-api";
    private static final String tuscanySpiDir = "tuscany-spi";
    private static final String tuscanyRuntimeDir = "tuscany-runtime";
    private static final String tuscanyExtensionsDir = "tuscany-extensions";
    private static final String thirdPartyDir = "tuscany-3rdparty";
    
    private static Bundle tuscanyRuntimeBundle;
    
    
    private static String findBundle(String subDirName) throws Exception {
        
        File dir = new File("../" + subDirName + "/target");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
                
            });
            
            if (files != null && files.length > 0)
                return files[0].toURI().toURL().toString();
        }
        return null;
    }
    
    
    /**
     * Load four Tuscany bundles (API, Core-SPI, Runtime, Extensions) and 
     * one 3rd party bundle
     * 
     * @param bundleContext
     */
    public static Bundle loadTuscanyIntoOSGi(BundleContext bundleContext) throws Exception {
        
        if (tuscanyRuntimeBundle != null)
            return tuscanyRuntimeBundle;
        
        long startTime = System.currentTimeMillis();
        
        String thirdPartyBundleName = findBundle(thirdPartyDir);
        Bundle thirdPartyBundle = bundleContext.installBundle(thirdPartyBundleName);        
        thirdPartyBundle.start();
            

        String scaApiBundleName = findBundle(scaApiDir);
        Bundle scaApiBundle = bundleContext.installBundle(scaApiBundleName);            
        scaApiBundle.start();            

        String tuscanySpiBundleName = findBundle(tuscanySpiDir);
        Bundle tuscanySpiBundle = bundleContext.installBundle(tuscanySpiBundleName);    
        
        String tuscanyRuntimeBundleName = findBundle(tuscanyRuntimeDir);
        Bundle tuscanyRuntimeBundle = bundleContext.installBundle(tuscanyRuntimeBundleName);
        
        String tuscanyExtensionsBundleName = findBundle(tuscanyExtensionsDir);
        Bundle tuscanyExtensionsBundle = bundleContext.installBundle(tuscanyExtensionsBundleName);
        

        tuscanySpiBundle.start();      
        tuscanyExtensionsBundle.start();    
        
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("Loaded Tuscany, time taken = " + (endTime-startTime) + " ms" );
        
        return tuscanyRuntimeBundle;
    
    }
    
}
