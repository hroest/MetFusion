/**
 * created by Michael Gerlich, Oct 24, 2011 - 12:51:47 PM
 */ 

package de.ipbhalle.CDK;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

public class AtomContainerHandler {

	public static IAtomContainer addExplicitHydrogens(IAtomContainer container) throws CDKException {
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);
        CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(container.getBuilder());
        hAdder.addImplicitHydrogens(container);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        
        return container;
	}
}
