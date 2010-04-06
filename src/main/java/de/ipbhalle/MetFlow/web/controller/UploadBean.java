/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ipbhalle.MetFlow.web.controller;

import com.icesoft.faces.component.inputfile.InputFile;
import com.icesoft.faces.component.inputfile.FileInfo;
import com.icesoft.faces.component.inputfile.InputFileProgressEvent;
import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;

import com.icesoft.faces.webapp.xmlhttp.RenderingException;
import de.ipbhalle.MetFlow.io.InputFileData;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import java.util.*;
import java.io.File;
import java.io.Serializable;
import javax.el.ELResolver;
import javax.faces.application.FacesMessage;

/**
 *
 * @author mgerlich
 */
public class UploadBean implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final Log log = LogFactory.getLog(UploadBean.class);

    // File sizes used to generate formatted label
    public static final long MEGABYTE_LENGTH_BYTES = 1048000l;
    public static final long KILOBYTE_LENGTH_BYTES = 1024l;
   
    // PersistentFacesState used to force render() calls to JSF
    private PersistentFacesState state;

    // files associated with the current user
    private final List<InputFileData> fileList = Collections.synchronizedList(new ArrayList<InputFileData>());
    // latest file uploaded by client
    private InputFileData currentFile;
    // file upload completed percent (Progress)
    private int fileProgress;

    // String constants that restrict allowed file uploads based on ending
    // and MIME type
    public static final String EXTENSION_T1 = "xml";
    public static final String EXTENSION_T2 = "t2flow";
    public static final String CONTENT_TYPE_T1_xml = "text/xml";
    public static final String CONTENT_TYPE_T1_plain = "text/plain";
    public static final String CONTENT_TYPE_T2 = "application/octet-stream";
    public static final String CONTENT_TYPE_SERVER1 = "application/vnd.taverna.t2flow+xml";
    public static final String CONTENT_TYPE_SERVER2 = "application/xml";

    // list that contains all allowed extensions
    private static final List<String> allowedExtensions;
    static {allowedExtensions = new ArrayList<String>();
            allowedExtensions.add(EXTENSION_T1);
            allowedExtensions.add(EXTENSION_T2);
            allowedExtensions.add(CONTENT_TYPE_T1_xml);
            allowedExtensions.add(CONTENT_TYPE_T1_plain);
            allowedExtensions.add(CONTENT_TYPE_T2);
            allowedExtensions.add(CONTENT_TYPE_SERVER1);
            allowedExtensions.add(CONTENT_TYPE_SERVER2);
            };

    /**
     * standard constructor, creates a new instance of PersistentFacesState
     */
    public UploadBean() {
        System.out.println("UploadBean constructor call...");
        state = PersistentFacesState.getInstance();
        if (state == null) {
            System.out.println("PersistentFacesState NULL");
        }
//
//        System.out.println("synch mode -> " + state.isSynchronousMode());
//        FacesContext fcontext = FacesContext.getCurrentInstance();
//        ELResolver el = fcontext.getApplication().getELResolver();
//        TavernaRESTBean trb = (TavernaRESTBean) el.getValue(fcontext.getELContext(), null, "tavernaRESTBean");
//        try {
//            trb.testREST();
//        } catch (IOException ex) {
//            Logger.getLogger(UploadBean.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(UploadBean.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    /**
     * <p>Action event method which is triggered when a user clicks on the
     * upload file button.  Uploaded files are added to a list so that user have
     * the option to delete them programatically.  Any errors that occurs
     * during the file uploaded are added the messages output.</p>
     *
     * @param event jsf action event.
     */
    public void uploadFile(ActionEvent event) {
    	if(event.getSource() == null)
    		return;
    	
        InputFile inputFile = (InputFile) event.getSource();
        if (inputFile == null) {
			System.out.println("inputfile == null");
			return;
		}
        FileInfo fileInfo = inputFile.getFileInfo();
        String contentType = fileInfo.getContentType().toLowerCase();
        System.out.println("ContentType -> " + fileInfo.getContentType());
        System.out.println("FileName pattern -> " + inputFile.getFileNamePattern());
        System.out.println("FileName -> " + fileInfo.getFileName());
        String fileName = fileInfo.getFileName();
        String ending = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        System.out.println("ending -> " + ending);
        
        if (allowedExtensions.contains(ending)
                && allowedExtensions.contains(contentType)
                && fileInfo.getStatus() == FileInfo.SAVED
                && fileInfo.getSize() > 0) {
            // reference our newly updated file for display purposes and
            // added it to our history file list.
            currentFile = new InputFileData(fileInfo);

            synchronized (fileList) {
                fileList.add(currentFile);
            }

        } // One of the several failure possibilities. Use fileInfo.getStatus() to see which
        else if (fileInfo.getStatus() == FileInfo.INVALID) {
            System.out.println("invalid...");
        }
        else if (fileInfo.isFailed()) {
            fileInfo.getException().printStackTrace();
        }
        else {  // if file does not have correct content type and/or ending, delete
            System.out.println("File removed due to incorrect content type and/or ending!");
            fileInfo.getFile().delete();
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("File rejected!",
                    new FacesMessage(FacesMessage.SEVERITY_WARN,"Warning ",
                    "Rejected file for further use - no valid ending and/or content type!"));
        }
    }

    /**
     * <p>This method is bound to the inputFile component and is executed
     * multiple times during the file upload process.  Every call allows
     * the user to finds out what percentage of the file has been uploaded.
     * This progress information can then be used with a progressBar component
     * for user feedback on the file upload progress. </p>
     *
     * @param event holds a InputFile object in its source which can be probed
     *              for the file upload percentage complete.
     */
    public void fileUploadProgress(EventObject event) {
        InputFile ifile = (InputFile) event.getSource();
        fileProgress = ifile.getFileInfo().getPercent();
    }
    
    public void fileUploadProgress(InputFileProgressEvent event) {
    	InputFile ifile = (InputFile) event.getSource();
        fileProgress = ifile.getFileInfo().getPercent();
    }
    /**
     * <p>Allows a user to remove a file from a list of uploaded files.  This
     * methods assumes that a request param "fileName" has been set to a valid
     * file name that the user wishes to remove or delete</p>
     *
     * @param event jsf action event
     */
    public void removeUploadedFile(ActionEvent event) {
        // Get the inventory item ID from the context.
//        FacesContext context = FacesContext.getCurrentInstance();
//        Map map = context.getExternalContext().getRequestParameterMap();
//        String fileName = (String) map.get("fileName");

        String fileName = (String) event.getComponent().getAttributes().get("fileName");
        System.out.println("filename remove -> " + fileName);
        synchronized (fileList) {
            InputFileData inputFileData;
            for (int i = 0; i < fileList.size(); i++) {
                inputFileData = (InputFileData) fileList.get(i);
                // remove our file
                System.out.println("inputfilename -> " + inputFileData.getFileInfo().getFileName());
                if (inputFileData.getFileInfo().getFileName().equals(fileName)) {
                    fileList.remove(i);
                    File temp = new File(inputFileData.getFileInfo().getPhysicalPath());
                    temp.delete();
                    break;
                }
            }
        }
    }

    public InputFileData getCurrentFile() {
        return currentFile;
    }

    public int getFileProgress() {
        return fileProgress;
    }

    public List getFileList() {
        return fileList;
    }
}
