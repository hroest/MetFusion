/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.ipbhalle.MetFlow.io;

import com.icesoft.faces.component.inputfile.FileInfo;

import de.ipbhalle.MetFlow.web.controller.UploadBean;
import java.io.File;

/**
 *
 * @author mgerlich
 */
public class InputFileData {

    // file info attributes
    private FileInfo fileInfo;
    // file that was uploaded
    private File file;
    // boolean indicating if file was uploaded successfully
    private boolean uploaded;
    
    /**
     * Create a new InputFileDat object.
     *
     * @param fileInfo fileInfo object created by the inputFile component for
     *                 a given File object.
     */
    public InputFileData(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
        this.file = fileInfo.getFile();
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Method to return the file size as a formatted string
     * For example, 4000 bytes would be returned as 4kb
     *
     *@return formatted file size
     */
    public String getSizeFormatted() {
        long ourLength = file.length();

        // Generate formatted label, such as 4kb, instead of just a plain number
        if (ourLength >= UploadBean.MEGABYTE_LENGTH_BYTES) {
            return ourLength / UploadBean.MEGABYTE_LENGTH_BYTES + " MB";
        }
        else if (ourLength >= UploadBean.KILOBYTE_LENGTH_BYTES) {
            return ourLength / UploadBean.KILOBYTE_LENGTH_BYTES + " KB";
        }
        else if (ourLength == 0) {
            return "0";
        }
        else if (ourLength < UploadBean.KILOBYTE_LENGTH_BYTES) {
            return ourLength + " B";
        }

        return Long.toString(ourLength);
    }

	public void setUploaded(boolean uploaded) {
		this.uploaded = uploaded;
	}

	public boolean isUploaded() {
		return uploaded;
	}
}
