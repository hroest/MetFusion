/**
 * created by Michael Gerlich on Jun 2, 2010
 * last modified Jun 2, 2010 - 10:39:52 AM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.CDK;

import java.io.*;
import java.util.regex.Pattern;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.io.*;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.ChemFile;


public class MyErrorHandler implements IChemObjectReaderErrorHandler {
	
	public static void main(String[] args) {
		//File f = new File("/vol/massbank/Cache/JP_Funatsu/mol/JP011837.mol");
		File f = new File("/home/mgerlich/Desktop/test.mol");
		
		try {
			FileInputStream fis = new FileInputStream(f);
			StringBuilder sb = new StringBuilder();
			String line, mol = "";
			
			try {
				int counter = 0;
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(fis, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					if (line.equals("") || line.equals("\n"))
						sb.append(line);
					else
						sb.append(line).append("\n"); // .append("\n");
					
					counter++;
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			mol = sb.toString();
			System.out.println(mol);
			
			sb = new StringBuilder();
			Pattern p = Pattern.compile("[0-9]+\n[0-9]+\n.*\n.*\n [0-9]+.*V2000");
			if(p.matcher(mol).lookingAt()) {
				Pattern p1 = Pattern.compile("[0-9]+\n[0-9]+\n");
				mol = p1.matcher(mol).replaceFirst("\n");
			}
			
//			if(!mol.equals("0\n")) {	// (do not) change molfile properties block (first 3 rows)
//				Pattern p1 = Pattern.compile("[0-9]+\n[0-9]+\n");
//				Pattern p2 = Pattern.compile("[0-9]+\n[0-9]+\n\n");
//				if(p2.matcher(mol).matches())
//					mol = p2.matcher(mol).replaceFirst("");
//				else mol = p1.matcher(mol).replaceFirst("");
//			}
			System.out.println();
			System.out.println(mol);
			
			FileWriter fw = new FileWriter(f);
			fw.write(mol);
			fw.flush();
			fw.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MDLReader reader = null;
		try {
			reader = new MDLReader(new FileReader(f));
			reader.setErrorHandler(new MyErrorHandler());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			IChemFile chemFile = new ChemFile();
			IAtomContainer container = null;
			chemFile = (IChemFile) reader.read(chemFile);
			container = ChemFileManipulator.getAllAtomContainers(chemFile).get(0);
			System.out.println(container.getAtomCount());
			//IAtomContainer mol = reader.read(chemFile);
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public void handleError(String message) {
		System.err.println(message);
	}

	public void handleError(String message, Exception exception) {
		System.err.println(message + "\n -> " + exception.getMessage());
	}

	public void handleError(String message, int row, int colStart, int colEnd) {
		System.err.print("location: " + row + ", " + colStart + "-" + colEnd
				+ ": ");
		System.err.println(message);
	}

	public void handleError(String message, int row, int colStart, int colEnd,
			Exception exception) {
		System.err.print("location: " + row + ", " + colStart + "-" + colEnd
				+ ": ");
		System.err.println(message + "\n -> " + exception.getMessage());
	}
}