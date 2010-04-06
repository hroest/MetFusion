package de.ipbhalle.MetFlow.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

public class ReferenceConverter {
	
	private int spacesPerIndent = 3;
	private String path = "";
	
	// Generate the string form of the supplied reference and print it to the
	// system console
	public void printReference(ReferenceService rs, T2Reference ref) {
		System.out.println(printReference(rs, ref, 0));
	}
	
	private String printReference(ReferenceService rs, T2Reference ref,
			int indent) {
		StringBuffer sb = new StringBuffer();
		String indentString = getIndent(indent);
		T2ReferenceType type = ref.getReferenceType();
		// Print the reference itself
		sb.append(indentString + ref + "\n");
		// Traverse into reference components
		if (type.equals(T2ReferenceType.ErrorDocument)) {
			// Error documents are leaf nodes, so print the error document
			// specific properties here. Firstly we need an error document
			// service obtained from the reference service
			ErrorDocumentService eds = rs.getErrorDocumentService();
			// Then use this service to get the ErrorDocument from the reference
			ErrorDocument ed = eds.getError(ref);
			// Print the 'message' and 'exception message' properties of the
			// error document
			sb.append(indentString + " message = \"" + ed.getMessage()	+ "\"\n");
			sb.append(indentString + " exception message = \""	+ ed.getExceptionMessage() + "\"\n");
			// If the error document contains a non-empty stack trace (it may
			// not, not all error documents are created from exceptions) then
			// iterate over the stack frames and print them in a format similar
			// to a Java exception trace
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
		} else if (type.equals(T2ReferenceType.ReferenceSet)) {
			// A reference set is a leaf node, it contains a set of
			// ExternalReferenceSPI implementations which all point to the same
			// data. In our examples each reference set will only contain a
			// single one of these, the type of which will depend on the object
			// it was generated from. To inspect this we need a reference set
			// service from the reference service.
			ReferenceSetService rss = rs.getReferenceSetService();
			// Use the reference set service to get a ReferenceSet
			ReferenceSet refSet = rss.getReferenceSet(ref);
			// Iterate over all the reference set's ExternalReferenceSPI members
			// and print them to the stringbuffer
			for (ExternalReferenceSPI extRef : refSet.getExternalReferences()) {
				// It's up to the ExternalReferenceSPI implementation to
				// implement toString to return something sensible
				sb.append(indentString + " external : " + extRef + "\n");
				
				if (extRef.getClass().getSimpleName().equals("InlineStringReference")) {
					String output = (String) rs.renderIdentifier(ref, String.class, null);
					System.out.println("output = " + output);
				} 
				else if (extRef.getClass().getSimpleName().equals("InlineByteArrayReference")) {
					Object output = rs.renderIdentifier(ref, Object.class, null);
					//byte[] out = Base64.decode(output);
					if(output instanceof byte[]) {
						System.out.println("byte[]");
						byte[] arr = (byte[]) output;
//						for (int i = 0; i < arr.length; i++) {
//							System.out.print(arr[i] + " ");
//						}
						Date d = new Date();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd-HH:mm:ss");
						String name = sdf.format(d);
						File f = new File(name + ".png");
						this.path = f.getAbsolutePath();
						System.out.println("path -> " + this.path);

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
						
					}
				} 
				else {
					System.out.println("ELSE...");
				}
			}
		} else if (type.equals(T2ReferenceType.IdentifiedList)) {
			// IdentifiedList is a subclass of the Java List interface with an
			// associated T2Reference. If we have one of these we need to
			// recurse, but first we need to obtain the actual IdentifierList
			// object through the list service. As before, this is obtained from
			// the reference service.
			ListService ls = rs.getListService();
			// Use the list service to get the IdentifiedList of T2Reference
			IdentifiedList<T2Reference> il = ls.getList(ref);
			// Iterate over all members of the list, recursively calling this
			// method with an increased indentation level
			for (T2Reference child : il) {
				sb.append(printReference(rs, child, indent + 1));
			}
		}
		return sb.toString();
	}

	// Simple method to generate a string containing a number of space
	// characters proportional to the indentation level
	private String getIndent(int indent) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < indent; i++) {
			for (int j = 0; j < spacesPerIndent; j++) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
}
