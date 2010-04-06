package de.ipbhalle.MetFlow.utilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

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
import net.sf.taverna.t2.reference.impl.external.object.InlineByteArrayReference;

/**
 * Utility class, prints a summary of the internal reference structure of a
 * T2Reference
 */
public class ReferencePrinter {

	private static int spacesPerIndent = 3;

	// Generate the string form of the supplied reference and print it to the
	// system console
	public static void printReference(ReferenceService rs, T2Reference ref) {
		System.out.println(printReference(rs, ref, 0));
	}

	private static String printReference(ReferenceService rs, T2Reference ref,
			int indent) {
		
		System.out.println("\nReferencePrinter...");
		
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
			sb.append(indentString + " message = \"" + ed.getMessage() + "\"\n");
			sb.append(indentString + " exception message = \""
					+ ed.getExceptionMessage() + "\"\n");
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
				System.out.println("extref class -> " + extRef.getClass());
				System.out.println("canonical name = " + extRef.getClass().getCanonicalName());
				System.out.println("simple name = " + extRef.getClass().getSimpleName());
				
				if(extRef.getClass().getSimpleName().equals("InlineStringReference")) {
					String output = (String) rs.renderIdentifier(ref, String.class, null);
					System.out.println("rendered output -> \n" + output);
					String sub = output.substring(output.indexOf("<GetCompoundThumbnailResult>") + "<GetCompoundThumbnailResult>".length(),
										output.indexOf("</GetCompoundThumbnailResult></GetCompoundThumbnailResponse>"));
					byte[] out = Base64.decode(sub);
					System.out.println("Datanature = " + extRef.getDataNature().toString());
					
					try {
						FileOutputStream fos = new FileOutputStream("/home/mgerlich/Desktop/temp_stringref.png");
						//byte[] b = output.getBytes();
						fos.write(out);
						fos.flush();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(extRef.getClass().getSimpleName().equals("InlineByteArrayReference")) {			
					//InlineByteArrayReference ib = (InlineByteArrayReference) rs.renderIdentifier(ref, InlineByteArrayReference.class, null);
					//Byte[] b = (Byte[]) rs.renderIdentifier(ref, Byte.class, null);
					System.out.println("renderer -> \n" +rs.renderIdentifier(ref, String.class, null));
					String render = (String) rs.renderIdentifier(ref, String.class, null);
					System.out.println("length = " + render.length());
					System.out.println(extRef);
					System.out.println("Datanature = " + extRef.getDataNature().toString());
					
					InputStream is = extRef.openStream(null);
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					StringBuffer sb1 = new StringBuffer();
					String temp = null;
					try {
						temp = br.readLine();
						while (temp != null) {
							sb1.append(temp);
							//sb1.append("\n");
							temp = br.readLine();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						FileOutputStream fos = new FileOutputStream("/home/mgerlich/Desktop/temp_arrayref.png");
						byte[] b = render.getBytes();
						fos.write(b);
						fos.flush();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					System.out.println("temp = " + temp);
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
	private static String getIndent(int indent) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < indent; i++) {
			for (int j = 0; j < spacesPerIndent; j++) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}
}
