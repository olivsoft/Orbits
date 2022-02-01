package orbits.components;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.oliverfritz.util.MoreUtil;
import ch.oliverfritz.util.RunnableContainer;
import orbits.plots.BasePlot;
import orbits.plots.EnergyPlot;
import orbits.plots.OrbitPlot;
import orbits.plots.SchwarzschildPlot;

public class OrbitsAnimation extends RunnableContainer {

	// Suggested initial frame rate
	private static final int initialFrameRate = 75;

	BasePlot energyPlot, schwarzschildPlot, orbitPlot;

	// Get and set methods
	public double getAngMomentum() {
		return BasePlot.L;
	}

	public double getMass() {
		return BasePlot.M;
	}

	public void setAngMomentum(double value) {
		BasePlot.L = value;
		BasePlot.plots.values().forEach(p -> p.clear());
		if (!isRunning())
			repaintContainer();
	}

	public void setMass(double value) {
		BasePlot.M = value;
		BasePlot.plots.values().forEach(p -> p.clear());
		if (!isRunning())
			repaintContainer();
	}

	public void saveOrbitPlot(File file) {
		BufferedImage bImg = new BufferedImage(orbitPlot.getWidth(), orbitPlot.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		orbitPlot.paintAll(bImg.createGraphics());
		try {
			ImageIO.write(bImg, MoreUtil.PNG_TYPE, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void repaintContainer() {
		// Remark: There is little clarity on when repaint is executed. But the response
		// is clearly sooner or later an update() call in the plots. So, let us focus on
		// that.
		BasePlot.plots.values().forEach(p -> p.repaint());
	}

	// Constructors
	public static enum LayoutType {
		NO_LAYOUT, SIMPLE_LAYOUT, RICH_LAYOUT
	}

	public OrbitsAnimation() {
		this(null, LayoutType.NO_LAYOUT, false);
	}

	public OrbitsAnimation(LayoutType layoutType, boolean includeController) {
		this(null, layoutType, includeController);
	}

	public OrbitsAnimation(Container container, LayoutType layoutType, boolean includeController) {
		super(initialFrameRate);

		// Avoid more than one "refresh" when resizing
		Toolkit.getDefaultToolkit().setDynamicLayout(false);

		// Create plots
		BasePlot.resetToDefaultParameters();
		energyPlot = new EnergyPlot();
		schwarzschildPlot = new SchwarzschildPlot();
		orbitPlot = new OrbitPlot();

		// Layout
		if (container == null && layoutType != LayoutType.NO_LAYOUT)
			container = this;

		switch (layoutType) {
		case SIMPLE_LAYOUT:
			// Simple layout
			Panel pLeft = new Panel(new GridLayout(2, 1, 0, 2));
			pLeft.add(energyPlot);
			pLeft.add(schwarzschildPlot);
			Panel pCenter = new Panel(new GridLayout(1, 2, 2, 0));
			pCenter.add(pLeft);
			pCenter.add(orbitPlot);
			container.setLayout(new BorderLayout());
			container.add(pCenter, BorderLayout.CENTER);
			if (includeController)
				container.add(new OrbitsController(this), BorderLayout.PAGE_END);
			break;

		case RICH_LAYOUT:
			// More sophisticated layout based on original code
			GridBagLayout gribble = new GridBagLayout();
			container.setLayout(gribble);

			GridBagConstraints gc = new GridBagConstraints();
			gc.fill = GridBagConstraints.BOTH;
			gc.weightx = 1;
			gc.weighty = 1;

			gc.gridx = 1;
			gc.gridy = 1;
			gc.insets.bottom = 4;
			gribble.setConstraints(energyPlot, gc);
			container.add(energyPlot);

			gc.gridy = 2;
			gc.insets.bottom = 0;
			gribble.setConstraints(schwarzschildPlot, gc);
			container.add(schwarzschildPlot);

			gc.gridx = 2;
			gc.gridy = 1;
			gc.gridheight = 2;
			gc.insets.left = 4;
			gribble.setConstraints(orbitPlot, gc);
			container.add(orbitPlot);

			if (includeController) {
				gc = new GridBagConstraints();
				gc.gridx = 1;
				gc.gridy = 3;
				gc.gridwidth = 2;
				OrbitsController pController = new OrbitsController(this);
				gribble.setConstraints(pController, gc);
				container.add(pController);
			}
			break;

		case NO_LAYOUT:
		default:
			break;
		}
	}
}
