/**
 * created by Michael Gerlich, Jan 7, 2014 - 1:53:37 PM

 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */ 

package sandbox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

import de.ipbhalle.metfrag.tools.PPMTool;

public class CMLTest {

	/**
	 * @param args
	 * @throws CDKException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws CDKException, IOException {
		String smiles = "O";
		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		IAtomContainer ac = sp.parseSmiles(smiles);
		
		StringWriter sw = new StringWriter();
//		FileWriter fw = new FileWriter("/tmp/test.cml");
		CMLWriter cw = new CMLWriter(sw);
		cw.write(ac);
		cw.close();
		
		System.out.println(sw.toString());
		
		System.out.println(PPMTool.getPPMDeviation(273.014, 30));
		
		smiles = "OC(=O)Cc1cc2nnnc2cc1";
		ac = sp.parseSmiles(smiles);
		SmilesGenerator sg = new SmilesGenerator();
		System.out.println(sg.createSMILES(ac));
		sg.setUseAromaticityFlag(true);
		System.out.println(sg.createSMILES(ac));
	}

}
