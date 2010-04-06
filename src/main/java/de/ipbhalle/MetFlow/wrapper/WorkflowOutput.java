package de.ipbhalle.MetFlow.wrapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.axis.encoding.Base64;

import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ErrorDocumentService;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ListService;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.ReferenceSetService;
import net.sf.taverna.t2.reference.StackTraceElementBean;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;


public class WorkflowOutput implements Comparable<WorkflowOutput> {

	private String name; // name of output port
	private int depth; // depth of output port
	private String value; // value of output port -> use only if depth == 0
	private String message; // String message for port information
	private ArrayList<WorkflowOutput> elements; // list of list elements from output, if output reference is of type IdentifiedList

	private List<WorkflowOutputAlignment> aligns;
	
	private ReferenceService rs;
	private T2Reference ref;
	private String path;
	private static int spacesPerIndent = 3;
	private boolean image = false;
	private boolean escape = true;
	private String workingDir;
	private final String sep = System.getProperty("file.separator");
	
	private String imageW;
	private String imageH;
	private String sessionString;
	
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode rootTreeNode;
	private NodeOutputObject rootObject;
	private NodeOutputObject branchObject;
	private NodeOutputObject leafObject;
	
	// default node icons for rime theme
    private static final String RIME_BRANCH_CONTRACTED_ICON = "./xmlhttp/css/rime/css-images/tree_folder_open.gif";
    private static final String RIME_BRANCH_EXPANDED_ICON = "./xmlhttp/css/rime/css-images/tree_folder_close.gif";
    private static final String RIME_BRANCH_LEAF_ICON = "./xmlhttp/css/rime/css-images/tree_document.gif";
	private int treeDepth = 0;
    
	public WorkflowOutput() {
		this.name = "";
		this.depth = 0;
		this.value = "";
		this.message = "";
		this.elements = null;
		this.ref = null;
		this.rs = null;
	}

	public WorkflowOutput(String name, int depth, String value) {
		this.name = name;
		this.depth = depth;
		this.value = value;
		this.message = "";
		this.elements = null;
		this.ref = null;
		this.rs = null;
	}
	
	public WorkflowOutput(String name, int depth, String value, String path) {
		this.name = name;
		this.depth = depth;
		this.value = value;
		this.path = path;
		this.message = "";
		this.elements = null;
		this.ref = null;
		this.rs = null;
	}
	
	public WorkflowOutput(String name, int depth, String value, String path, boolean graphic) {
		this.name = name;
		this.depth = depth;
		this.value = value;
		this.path = path;
		this.message = "";
		this.elements = null;
		this.ref = null;
		this.rs = null;
		this.image = graphic;
	}
	
	public WorkflowOutput(String name, int depth, String value, String path, boolean graphic, int width, int height) {
		this.name = name;
		this.depth = depth;
		this.value = value;
		this.path = path;
		this.message = "";
		this.elements = null;
		this.ref = null;
		this.rs = null;
		this.image = graphic;
		this.imageW = String.valueOf(width);
		this.imageH = String.valueOf(height);
	}
	
	public WorkflowOutput(String name, int depth, String value, String path, boolean graphic, boolean escape) {
		this.name = name;
		this.depth = depth;
		this.value = value;
		this.path = path;
		this.message = "";
		this.elements = null;
		this.ref = null;
		this.rs = null;
		this.image = graphic;
		this.escape = escape;
	}
	
	public WorkflowOutput(String name, int depth, String value, String path, boolean graphic, boolean escape, int width, int height) {
		this.name = name;
		this.depth = depth;
		this.value = value;
		this.path = path;
		this.message = "";
		this.elements = null;
		this.ref = null;
		this.rs = null;
		this.image = graphic;
		this.escape = escape;
		this.imageW = String.valueOf(width);
		this.imageH = String.valueOf(height);
	}

	public WorkflowOutput(String name, T2Reference ref, ReferenceService rs, String dirPath) {
		this.name = (name == null ? "" : name);
		this.ref = (ref == null ? null : ref);
		this.rs = (rs == null ? null : rs);
		this.workingDir = dirPath;
		
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		HttpSession session = (HttpSession) ec.getSession(false);
		this.sessionString = session.getId();
		System.out.println("sessionString in WorkflowOutput = " + sessionString);
		
		//this.elements = convert();
		this.elements = new ArrayList<WorkflowOutput>();
		this.aligns = new ArrayList<WorkflowOutputAlignment>();
		
		// TODO create tree representation
		this.rootTreeNode = new DefaultMutableTreeNode();
		this.rootObject = new NodeOutputObject(rootTreeNode);
		rootObject.setText(name + "-" + ref.getLocalPart());
	    rootObject.setExpanded(true);
	    rootObject.setBranchContractedIcon(RIME_BRANCH_CONTRACTED_ICON);
        rootObject.setBranchExpandedIcon(RIME_BRANCH_EXPANDED_ICON);
        rootObject.setLeafIcon(RIME_BRANCH_LEAF_ICON);
	    rootTreeNode.setUserObject(rootObject);
	    
	    this.treeModel = new DefaultTreeModel(rootTreeNode);
		convert(ref, rootTreeNode);		
	}

	public WorkflowOutput(String name, int depth, String value, String msg,
			T2Reference ref, ReferenceService rs) {
		this.name = (name == null ? "" : name);
		this.depth = (depth < 0 ? 0 : depth);
		this.value = (value == null ? "" : value);
		this.message = (msg == null ? "" : msg);
		this.ref = (ref == null ? null : ref);
		this.rs = (rs == null ? null : rs);
	}


	public WorkflowOutput(String name, int depth, String value, String msg,
			ArrayList<WorkflowOutput> elements) {
		this.name = (name == null ? "" : name);
		this.depth = (depth < 0 ? 0 : depth);
		this.value = (value == null ? "" : value);
		this.message = (msg == null ? "" : msg);
		this.elements = (elements.equals(null) ? null : elements);
	}


	/**
	 * converts T2References to readable presentations
	 */
	public ArrayList<WorkflowOutput> convert() {
		T2ReferenceType type = ref.getReferenceType();
		StringBuffer sb = new StringBuffer();
		String indentString = getIndent(2);
		
		// nur ein ergebnis
		if (type.equals(T2ReferenceType.ErrorDocument)) {
			ErrorDocumentService eds = rs.getErrorDocumentService();
			ErrorDocument ed = eds.getError(ref);

			sb.append(indentString + " message = \"" + ed.getMessage()
							+ "\"\n");
			sb.append(indentString + " exception message = \""
					+ ed.getExceptionMessage() + "\"\n");

			List<StackTraceElementBean> stackFrames = ed.getStackTraceStrings();
			if (!stackFrames.isEmpty()) {
				boolean first = true;
				for (StackTraceElementBean frame : stackFrames) {
					sb.append(indentString
							+ (first ? " stack trace : " : "               ")
							+ frame.getClassName() + ":"
							+ frame.getMethodName() + ":"
							+ frame.getLineNumber() + "\n");
					first = false;
				}
			}
		} 
		// nur ein ergebnis
		else if (type.equals(T2ReferenceType.ReferenceSet)) {
			ReferenceSetService rss = rs.getReferenceSetService();
			ReferenceSet refSet = rss.getReferenceSet(ref);
			for (ExternalReferenceSPI extRef : refSet.getExternalReferences()) {
				if (extRef.getClass().getSimpleName().equals("InlineStringReference")) {
					String output = (String) rs.renderIdentifier(ref, String.class, null);
					this.value = output;
				} else if (extRef.getClass().getSimpleName().equals("InlineByteArrayReference")) {
					String output = (String) rs.renderIdentifier(ref, String.class, null);
					byte[] out = Base64.decode(output);
					this.value = out.toString();
				} else {

				}
			}
		} 
		// mehrere ergebnisse
		else if (type.equals(T2ReferenceType.IdentifiedList)) {
			this.value = ref.toString();
		}
		return elements;
	}


	/**
	 * converts T2References into readable presentations
	 */
	public void convert(T2Reference ref) {
		T2ReferenceType type = ref.getReferenceType();
		StringBuffer sb = new StringBuffer();
		String indentString = getIndent(2);
		
		// nur ein ergebnis
		if (type.equals(T2ReferenceType.ErrorDocument)) {
			ErrorDocumentService eds = rs.getErrorDocumentService();
			ErrorDocument ed = eds.getError(ref);

			sb.append(indentString + " message = \"" + ed.getMessage()
							+ "\"\n");
			sb.append(indentString + " exception message = \""
					+ ed.getExceptionMessage() + "\"\n");

			List<StackTraceElementBean> stackFrames = ed.getStackTraceStrings();
			if (!stackFrames.isEmpty()) {
				boolean first = true;
				for (StackTraceElementBean frame : stackFrames) {
					sb.append(indentString
							+ (first ? " stack trace : " : "               ")
							+ frame.getClassName() + ":"
							+ frame.getMethodName() + ":"
							+ frame.getLineNumber() + "\n");
					first = false;
				}
			}
			
			this.elements.add(new WorkflowOutput(ref.toString(), ref.getDepth(), sb.toString(), "", false));
		} 
		// nur ein ergebnis
		else if (type.equals(T2ReferenceType.ReferenceSet)) {
			ReferenceSetService rss = rs.getReferenceSetService();
			ReferenceSet refSet = rss.getReferenceSet(ref);
			for (ExternalReferenceSPI extRef : refSet.getExternalReferences()) {
				if (extRef.getClass().getSimpleName().equals("InlineStringReference")) {
					String output = (String) rs.renderIdentifier(ref, String.class, null);
					
					// xml tags escapen wenn vorhanden
					if(output.contains("</") | output.contains("/>")) {
						escape = false;
						//output = output.replaceAll("> <", "><br/><");
						output = output.replaceAll("><", "><br/><");
						output = "<pre>" + output + "</pre>";						
					}
					else escape = false;
					System.out.println("output -> \n" + output);
					
					// zeilenumbrueche durch <br> tags ersetzen
					output = replaceCRLF(output);
					
					this.elements.add(new WorkflowOutput(ref.toString(), ref.getDepth(), output, "", false, escape));			        
				} 
				else if (extRef.getClass().getSimpleName().equals("InlineByteArrayReference")) {
					Object output = rs.renderIdentifier(ref, Object.class, null);
					//byte[] out = Base64.decode(output);
					if(output instanceof byte[]) {
						//System.out.println("byte[]");
						byte[] arr = (byte[]) output;
												
						Date d = new Date();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd-HH-mm-ss");
						String time = sdf.format(d);
						
						File dir = new File(workingDir + sep + "temp" + sep + sessionString + sep);
//						try {
//							System.out.println("workingDir -> " + dir.getCanonicalPath());
//						} catch (IOException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
						dir.mkdirs();
						//File f = new File(name + "_" + ref.toString().substring(ref.toString().indexOf("?")) + ".png");
						//workingDir + sep + "temp" + sep + sessionString + sep
						String fileName = "_" + ref.toString().substring(ref.toString().indexOf("?")+1) + ".gif";
						//File f = new File(dir + sep + time + fileName);
						File f = new File(dir, time + fileName);
//						this.path = f.getAbsolutePath();
						this.path =  sep + "temp" + sep + sessionString + sep + time + fileName;

						// replaces all file separators by a slash (/) in order to properly display images stored on the server
						this.path = "/" + "temp" + "/" + sessionString + "/" + time + fileName;
						System.out.println("path -> " + this.path);
						System.out.println("ref local -> " + ref.getLocalPart() + "  namespace -> " + ref.getNamespacePart());
						FileOutputStream fos;
						try {
							fos = new FileOutputStream(f);
							fos.write(arr);
							fos.flush();
							fos.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						int biH = 0;
						int biW = 0;
						try {
							//Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
							//ImageReader reader = (ImageReader) readers.next();
							BufferedImage bi = ImageIO.read(f);
							biH = bi.getHeight();
							biW = bi.getWidth();
							
							//ImageInputStream iis = ImageIO.createImageInputStream(f);
							//reader.setInput(iis, true);
							//ImageReadParam param = reader.getDefaultReadParam();
							//int imageIndex = 0;
							////int half_width = reader.getImageWidth(imageIndex)/2;
							//int half_width = reader.getWidth(imageIndex)/2;
							////int half_height = reader.getImageHeight(imageIndex)/2;
							//int half_height = reader.getHeight(imageIndex)/2;							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						if(biH > 1000 | biW > 1200) {
							biH = biH / 2;
							biW = biW / 2;
						}
						
						this.elements.add(new WorkflowOutput(ref.toString(), ref.getDepth(), f.getName(), this.path, true, biW, biH));
					}
				} 
				else {
					System.out.println("ELSE...");
				}
			}
		} 
		// mehrere ergebnisse
		else if (type.equals(T2ReferenceType.IdentifiedList)) {
			this.value = ref.toString();
			ListService ls = rs.getListService();
			// Use the list service to get the IdentifiedList of T2Reference
			IdentifiedList<T2Reference> il = ls.getList(ref);

			// Iterate over all members of the list, recursively calling this
			// method with an increased indentation level
			for (T2Reference child : il) {
				convert(child);
			}
		}
	}
	
	/**
	 * converts T2References into readable presentations inside a tree representation while keeping the list structure
	 * 
	 * @param ref the T2reference which needs to be converted
	 * @param parent the parent tree node to which the reference output should be connected
	 */
	public void convert(T2Reference ref, DefaultMutableTreeNode parent) {
		T2ReferenceType type = ref.getReferenceType();
		StringBuffer sb = new StringBuffer();
		String indentString = getIndent(2);
		this.treeDepth = this.treeDepth++;
		
		// nur ein ergebnis
		if (type.equals(T2ReferenceType.ErrorDocument)) {
			ErrorDocumentService eds = rs.getErrorDocumentService();
			ErrorDocument ed = eds.getError(ref);

			sb.append(indentString + " message = \"" + ed.getMessage()
							+ "\"\n");
			sb.append(indentString + " exception message = \""
					+ ed.getExceptionMessage() + "\"\n");

			List<StackTraceElementBean> stackFrames = ed.getStackTraceStrings();
			if (!stackFrames.isEmpty()) {
				boolean first = true;
				for (StackTraceElementBean frame : stackFrames) {
					sb.append(indentString
							+ (first ? " stack trace : " : "               ")
							+ frame.getClassName() + ":"
							+ frame.getMethodName() + ":"
							+ frame.getLineNumber() + "\n");
					first = false;
				}
			}
			
			this.elements.add(new WorkflowOutput(ref.toString(), ref.getDepth(), sb.toString(), "", false));
			
			// added newly created output to tree representation
			DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode();
	        leafObject = new NodeOutputObject(leafNode);
	        leafObject.setText("node-");
	        leafObject.setOutput(new WorkflowOutput(ref.toString(), ref.getDepth(), sb.toString(), "", false));
	        leafObject.setBranchContractedIcon(RIME_BRANCH_CONTRACTED_ICON);
	        leafObject.setBranchExpandedIcon(RIME_BRANCH_EXPANDED_ICON);
	        leafObject.setLeafIcon(RIME_BRANCH_LEAF_ICON);
	        leafObject.setLeaf(true);
	        leafNode.setUserObject(leafObject);
	        //rootTreeNode.add(parent);
	        parent.add(leafNode);
	        
		} 
		// nur ein ergebnis
		else if (type.equals(T2ReferenceType.ReferenceSet)) {
			ReferenceSetService rss = rs.getReferenceSetService();
			ReferenceSet refSet = rss.getReferenceSet(ref);
			for (ExternalReferenceSPI extRef : refSet.getExternalReferences()) {
				if (extRef.getClass().getSimpleName().equals("InlineStringReference")) {
					String output = (String) rs.renderIdentifier(ref, String.class, null);
					
					// xml tags escapen wenn vorhanden
					if(output.contains("</") | output.contains("/>")) {
						escape = false;
						//output = output.replaceAll("> <", "><br/><");
						output = output.replaceAll("><", "><br/><");
						output = "<pre>" + output + "</pre>";						
					}
					else escape = false;
					
					// zeilenumbrueche durch <br> tags ersetzen
					output = replaceCRLF(output);
					
					this.elements.add(new WorkflowOutput(ref.toString(), ref.getDepth(), output, "", false, escape));
					
					// added newly created output to tree representation
					DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode();
			        leafObject = new NodeOutputObject(leafNode);
			        leafObject.setText("node-");
			        leafObject.setOutput(new WorkflowOutput(ref.toString(), ref.getDepth(), output, "", false, escape));
			        leafObject.setBranchContractedIcon(RIME_BRANCH_CONTRACTED_ICON);
			        leafObject.setBranchExpandedIcon(RIME_BRANCH_EXPANDED_ICON);
			        leafObject.setLeafIcon(RIME_BRANCH_LEAF_ICON);
			        leafObject.setLeaf(true);
			        leafNode.setUserObject(leafObject);
			        //rootTreeNode.add(parent);
			        parent.add(leafNode);
			        
				} 
				else if (extRef.getClass().getSimpleName().equals("InlineByteArrayReference")) {
					Object output = rs.renderIdentifier(ref, Object.class, null);
					//byte[] out = Base64.decode(output);
					if(output instanceof byte[]) {
						//System.out.println("byte[]");
						byte[] arr = (byte[]) output;
												
						Date d = new Date();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd-HH-mm-ss");
						String time = sdf.format(d);
						
						File dir = new File(workingDir + sep + "temp" + sep + sessionString + sep);
//						try {
//							System.out.println("workingDir -> " + dir.getCanonicalPath());
//						} catch (IOException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
						dir.mkdirs();
						//File f = new File(name + "_" + ref.toString().substring(ref.toString().indexOf("?")) + ".png");
						//workingDir + sep + "temp" + sep + sessionString + sep
						String fileName = "_" + ref.toString().substring(ref.toString().indexOf("?")+1) + ".png";
						//File f = new File(dir + sep + time + fileName);
						File f = new File(dir, time + fileName);
//						this.path = f.getAbsolutePath();
						this.path =  sep + "temp" + sep + sessionString + sep + time + fileName;

						// replaces all file separators by a slash (/) in order to properly display images stored on the server
						this.path = "/" + "temp" + "/" + sessionString + "/" + time + fileName;

						FileOutputStream fos;
						try {
							fos = new FileOutputStream(f);
							fos.write(arr);
							fos.flush();
							fos.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						int biH = 0;
						int biW = 0;
						try {
							//Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
							//ImageReader reader = (ImageReader) readers.next();
							BufferedImage bi = ImageIO.read(f);
							biH = bi.getHeight();
							biW = bi.getWidth();
							
							//ImageInputStream iis = ImageIO.createImageInputStream(f);
							//reader.setInput(iis, true);
							//ImageReadParam param = reader.getDefaultReadParam();
							//int imageIndex = 0;
							////int half_width = reader.getImageWidth(imageIndex)/2;
							//int half_width = reader.getWidth(imageIndex)/2;
							////int half_height = reader.getImageHeight(imageIndex)/2;
							//int half_height = reader.getHeight(imageIndex)/2;							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							biH = 100;
							biW = 100;
						}
						
						if(biH > 1000 | biW > 1200) {
							biH = biH / 2;
							biW = biW / 2;
						}
						
						this.elements.add(new WorkflowOutput(ref.toString(), ref.getDepth(), f.getName(), this.path, true, biW, biH));
						
						// added newly created output to tree representation
						DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode();
				        leafObject = new NodeOutputObject(leafNode);
				        leafObject.setText("node-");
				        leafObject.setOutput(new WorkflowOutput(ref.toString(), ref.getDepth(), f.getName(), this.path, true, biW, biH));
				        leafObject.setBranchContractedIcon(RIME_BRANCH_CONTRACTED_ICON);
				        leafObject.setBranchExpandedIcon(RIME_BRANCH_EXPANDED_ICON);
				        leafObject.setLeafIcon(RIME_BRANCH_LEAF_ICON);
				        leafObject.setLeaf(true);
				        leafNode.setUserObject(leafObject);
				        //rootTreeNode.add(parent);
				        parent.add(leafNode);
					}
				} 
				else {
					System.out.println("ELSE...");
				}
			}
		} 
		// mehrere ergebnisse
		else if (type.equals(T2ReferenceType.IdentifiedList)) {
			this.value = ref.toString();
			ListService ls = rs.getListService();
			// Use the list service to get the IdentifiedList of T2Reference
			IdentifiedList<T2Reference> il = ls.getList(ref);
			
			DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode();
			this.branchObject = new NodeOutputObject(branchNode);
			branchObject.setText("node-" + ref.toString());
	        branchNode.setUserObject(branchObject);
	        branchObject.setBranchContractedIcon(RIME_BRANCH_CONTRACTED_ICON);
	        branchObject.setBranchExpandedIcon(RIME_BRANCH_EXPANDED_ICON);
	        branchObject.setLeafIcon(RIME_BRANCH_LEAF_ICON);
	        branchObject.setExpanded(false);
	        branchObject.setOutput(null);
	        branchObject.setText(value);
	        if(this.treeDepth > 1)
	        	branchObject.setExpanded(false);
	        else branchObject.setExpanded(true);
	        
	        // only add branchnode if there are children for that node
	        if(il.size() > 0)
	        	parent.add(branchNode);

	        // Iterate over all members of the list, recursively calling this
			// method with an increased indentation level
			for (T2Reference child : il) {
				convert(child, branchNode);
			}
		}
	}
	
	// Simple method to generate a string containing a number of space
	// characters proportional to the indentation level
	private static String getIndent(int indent) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < indent; i++) {
			for (int j = 0; j < spacesPerIndent; j++) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	/**
	 * method which replaces all line feed chars into <br/> for convenient display
	 * in a JSF web page
	 * 
	 * @param input - the String which should be converted
	 * @return cleaned string representation
	 */
	public String replaceCRLF(String input) {
		String temp = "";
		if(input.contains("\n")) {	// & !(input.contains("/>"))
			temp = input.replaceAll("\n", "<br/>");
		}
		else temp = input;
		
		return temp;
	}
	
	public String getName() {
		return name;
	}


	public int getDepth() {
		return depth;
	}


	public String getValue() {
		return value;
	}


	public String getMessage() {
		return message;
	}


	public ArrayList<WorkflowOutput> getElements() {
		return elements;
	}


	public ReferenceService getRs() {
		return rs;
	}


	public void setRs(ReferenceService rs) {
		this.rs = rs;
	}


	public void setName(String name) {
		this.name = name;
	}


	public void setDepth(int depth) {
		this.depth = depth;
	}


	public void setValue(String value) {
		this.value = value;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public void setElements(ArrayList<WorkflowOutput> elements) {
		this.elements = elements;
	}


	public void setRef(T2Reference ref) {
		this.ref = ref;
	}


	public T2Reference getRef() {
		return ref;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	public String getWorkingDir() {
		return workingDir;
	}

	public void setImage(boolean image) {
		this.image = image;
	}

	public boolean isImage() {
		return image;
	}

	public void setEscape(boolean escape) {
		this.escape = escape;
	}

	public boolean isEscape() {
		return escape;
	}

	public void setImageW(String imageW) {
		this.imageW = imageW;
	}

	public String getImageW() {
		return imageW;
	}

	public void setImageH(String imageH) {
		this.imageH = imageH;
	}

	public String getImageH() {
		return imageH;
	}

	public String getSep() {
		return sep;
	}

	public String getSessionString() {
		return sessionString;
	}

	public void setSessionString(String sessionString) {
		this.sessionString = sessionString;
	}

	public void setAligns(List<WorkflowOutputAlignment> aligns) {
		this.aligns = aligns;
	}

	public List<WorkflowOutputAlignment> getAligns() {
		return aligns;
	}

	public void setTreeModel(DefaultTreeModel treeModel) {
		this.treeModel = treeModel;
	}

	public DefaultTreeModel getTreeModel() {
		return treeModel;
	}

	public void setRootTreeNode(DefaultMutableTreeNode rootTreeNode) {
		this.rootTreeNode = rootTreeNode;
	}

	public DefaultMutableTreeNode getRootTreeNode() {
		return rootTreeNode;
	}

	public void setRootObject(NodeOutputObject rootObject) {
		this.rootObject = rootObject;
	}

	public NodeOutputObject getRootObject() {
		return rootObject;
	}

	public void setBranchObject(NodeOutputObject branchObject) {
		this.branchObject = branchObject;
	}

	public NodeOutputObject getBranchObject() {
		return branchObject;
	}

	public void setLeafObject(NodeOutputObject leafObject) {
		this.leafObject = leafObject;
	}

	public NodeOutputObject getLeafObject() {
		return leafObject;
	}

	public void setTreeDepth(int treeDepth) {
		this.treeDepth = treeDepth;
	}

	public int getTreeDepth() {
		return treeDepth;
	}

	
	/** @see java.lang.Comparable#compareTo(java.lang.Object)
	 * 
	 * Implementation of the Comparable interface, comparison is based on the
	 * lexicographically representation of the workflow's output ports.
	 * 
	 * @param o the WorkflowOutput object that is to be compared to the current one
	 * 
	 * @return integer, maybe negative, zero or positive regarding the outcome of the comparison
	 */
	public int compareTo(WorkflowOutput o) {
		// TODO Auto-generated method stub
		// implement comparision via field name, as it represents the output port name
		return this.name.compareTo(o.getName());
		//return 0;
	}

}
