/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ipbhalle.metfusion.web.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.File;

/**
 *
 * @author mgerlich
 */
public class TempFileSessionCleaner implements HttpSessionListener {

    public static final Log log = LogFactory.getLog(TempFileSessionCleaner.class);

    public static final String FILE_UPLOAD_DIRECTORY = "upload";

    private static final String sep = System.getProperty("file.separator");
    /**
     * This method is called by the servlet container when the session
     * is about to expire. This method will attempt to delete all files that
     * where uploaded into the folder which has the same name as the session
     * id.
     *
     * @param event JSF session event.
     */
    public void sessionDestroyed(HttpSessionEvent event) {
        // get the session id, so we know which folder to remove
        String sessionId = event.getSession().getId();

        String applicationPath = event.getSession().getServletContext().getRealPath(
                event.getSession().getServletContext().getServletContextName());

        applicationPath = applicationPath.substring(0, applicationPath.lastIndexOf(sep));
        //String sessionFileUploadPath = applicationPath + sep + FILE_UPLOAD_DIRECTORY + sep + sessionId;

        System.out.println("session "+sessionId+" destroyed...");
        //System.out.println("path -> " + sessionFileUploadPath);
        //File sessionfileUploadDirectory = new File(sessionFileUploadPath);
        
        String tempPath = applicationPath + sep + "temp" + sep + sessionId;
        System.out.println("tempPath -> " + tempPath);
        File sessionTempDir = new File(tempPath);
        
//        if (sessionfileUploadDirectory.isDirectory()) {
//            try {
//                sessionfileUploadDirectory.delete();
//                deleteTempFiles(sessionfileUploadDirectory);
//            }
//            catch (SecurityException e) {
//                log.error("Error deleting file upload directory ["+sessionFileUploadPath+"]: ", e);
//            }
//        }
        
        if(sessionTempDir.isDirectory()) {
        	try {
        		sessionTempDir.delete();
        		boolean success = deleteTempFiles(sessionTempDir);
        		System.out.println("Deleting directory [" + sessionTempDir.getAbsolutePath() + "] -> " + success);
        	}
        	catch(SecurityException e) {
        		log.error("Error deleting temp directory ["+tempPath+"]: ", e);
        	}
        }

    }

    public void sessionCreated(HttpSessionEvent event) {
        // get the session id, so we know which folder to remove
        String sessionId = event.getSession().getId();

        String applicationPath = event.getSession().getServletContext().getRealPath(
                event.getSession().getServletContext().getServletContextName());

        applicationPath = applicationPath.substring(0, applicationPath.lastIndexOf(sep));
        
//        String sessionFileUploadPath = applicationPath + sep + FILE_UPLOAD_DIRECTORY + sep + sessionId;
        
        String tempPath = applicationPath + sep + "temp" + sep + sessionId;
        
        System.out.println("session "+sessionId+" created...");
//        System.out.println("path -> " + sessionFileUploadPath);
        System.out.println("tempPath -> " + tempPath);
        
//        File f = new File(sessionFileUploadPath);
//        if(!f.exists())
//        	f.mkdirs();
        
        File f = new File(tempPath);
        if(!f.exists())
        	f.mkdirs();
    }

    private boolean deleteTempFiles(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                File child = new File(dir, children[i]);
                child.delete();
            }
        }

        return dir.delete();
    }
}
