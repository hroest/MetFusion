/**
 * created by Michael Gerlich on Apr 6, 2010
 * last modified Apr 6, 2010 - 12:40:40 PM
 * email: mgerlich@ipb-halle.de
 */
package de.ipbhalle.MetFlow.utilities.MetFlow;

import java.util.ArrayList;
import java.util.List;

import java.awt.*;
import java.awt.image.*;
import java.io.File;

import javax.imageio.*;

import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.layout.*;
import org.openscience.cdk.renderer.*;
import org.openscience.cdk.renderer.font.*;
import org.openscience.cdk.renderer.generators.*;
import org.openscience.cdk.renderer.visitor.*;
import org.openscience.cdk.templates.*;

public class MoleculeRenderer {

	static int WIDTH = 600;
	static int HEIGHT = 600;

	public static void drawMolecule() throws Exception {
		// the draw area and the image should be the same size
		Rectangle drawArea = new Rectangle(WIDTH, HEIGHT);
		Image image = new BufferedImage(
		  WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB
		);

		IMolecule triazole = MoleculeFactory.make123Triazole();
		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		sdg.setMolecule(triazole);
		sdg.generateCoordinates();
		triazole = sdg.getMolecule();

		// generators make the image elements
		List generators = new ArrayList();
		generators.add(new BasicSceneGenerator());
		generators.add(new BasicBondGenerator());
		generators.add(new BasicAtomGenerator());

		// the renderer needs to have a toolkit-specific font manager
		AtomContainerRenderer renderer =
		  new AtomContainerRenderer(generators, new AWTFontManager());

		// the call to 'setup' only needs to be done on the first paint
		renderer.setup(triazole, drawArea);

		// paint the background
		Graphics2D g2 = (Graphics2D)image.getGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, WIDTH, HEIGHT);

		// the paint method also needs a toolkit-specific renderer
		renderer.paint(triazole, new AWTDrawVisitor(g2));

		ImageIO.write((RenderedImage)image, "PNG", new File("triazole.png"));
	}
}
