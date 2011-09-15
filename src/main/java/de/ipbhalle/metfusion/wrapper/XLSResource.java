/**
 * created by Michael Gerlich, Jul 20, 2011 - 12:19:16 PM
 */ 

package de.ipbhalle.metfusion.wrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;

import javax.faces.context.ExternalContext;

import com.icesoft.faces.context.Resource;


// TODO: Auto-generated Javadoc
/**
 * The Class XLSResource.
 */
public class XLSResource implements Resource, Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The resource name. */
	private String resourceName;
    
	private String folder;
	
    /** The input stream. */
    private InputStream inputStream;
    
    /** The last modified. */
    private final Date lastModified;
    
    /** The external context. */
    private ExternalContext extContext;
    
    public final String sep = System.getProperty("file.separator");
    
    /**
     * Instantiates a new output resource.
     * 
     * @param ec the ExternalContext to be used
     * @param resourceName the resource name
     */
    public XLSResource(ExternalContext ec, String resourceName, String folder) {
    	this.extContext = ec;
        this.resourceName = resourceName;
        this.folder = (folder.endsWith(sep)) ? folder : folder + sep;
        System.out.println("folder -> " + this.folder);
        this.lastModified = new Date();
	}
    
	/* (non-Javadoc)
	 * @see com.icesoft.faces.context.Resource#calculateDigest()
	 */
	public String calculateDigest() {
		return resourceName;
	}

	/* (non-Javadoc)
	 * @see com.icesoft.faces.context.Resource#lastModified()
	 */
	public Date lastModified() {
		return lastModified;
	}

	/**
	 * This intermediate step of reading in the files from the JAR, into a
	 * byte array, and then serving the Resource from the ByteArrayInputStream,
	 * is not strictly necessary, but serves to illustrate that the Resource
	 * content need not come from an actual file, but can come from any source,
	 * and also be dynamically generated. In most cases, applications need not
	 * provide their own concrete implementations of Resource, but can instead
	 * simply make use of com.icesoft.faces.context.ByteArrayResource,
	 * com.icesoft.faces.context.FileResource, com.icesoft.faces.context.JarResource.
	 * 
	 * @return the input stream
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public InputStream open() throws IOException {
		if (inputStream == null) {
//			HttpSession session = (HttpSession) extContext.getSession(false);
//			String sessionString = session.getId();
            //InputStream stream = extContext.getResourceAsStream("/temp/" + sessionString + "/" + resourceName);
			InputStream stream = extContext.getResourceAsStream(this.folder + resourceName);
            byte[] byteArray = toByteArray(stream);
            inputStream = new ByteArrayInputStream(byteArray);
        }
        return inputStream;
	}

	/* (non-Javadoc)
	 * @see com.icesoft.faces.context.Resource#withOptions(com.icesoft.faces.context.Resource.Options)
	 */
	public void withOptions(Options arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int len = 0;
        while ((len = input.read(buf)) > -1) output.write(buf, 0, len);
        return output.toByteArray();
    }
	
	public String getResourceName() {
		return resourceName;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getFolder() {
		return folder;
	}

}