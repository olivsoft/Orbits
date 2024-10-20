package orbits.plots;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.oliverfritz.util.BufferedCanvas;
import ch.oliverfritz.util.MoreUtil;

public abstract class BasePlot extends BufferedCanvas {
	private static final Logger LOGGER = MoreUtil.getLogger(BufferedCanvas.class);

	// These default values produce a nice potential. The actual values can be
	// changed from outside.
	private static final double L_DEF = 3.57d;
	private static final double M_DEF = 1d;
	public static double L = L_DEF;
	public static double M = M_DEF;

	// Radial coordinate and plot parameter.
	protected static double r, rMax;
	protected static double speedFactor = 1d;

	// Drawing elements
	private static final Font titleFont = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
	private static final Ellipse2D particle = new Ellipse2D.Double(-5, -5, 10, 10);
	protected final Path2D periastronPath = new Path2D.Double();
	protected final Path2D apastronPath = new Path2D.Double();
	protected AffineTransform t0;

	// Order-preserving map of implementing plots. The plot title must be unique
	// because it is used as the key. Each plot is added in the constructor.
	// Careful: Static lists may not die in the right moment. There may be strange
	// effects in certain situations (restart).
	public static final Map<String, BasePlot> plots = new LinkedHashMap<String, BasePlot>();

	// Reset L and M to their default values. Can be called from outside.
	public static void resetToDefaultParameters() {
		L = L_DEF;
		M = M_DEF;
	}

	// Constructor
	public BasePlot() {
		super();
		plots.put(this.getPlotTitle(), this);
		addMouseListener(MoreUtil.mousePressedAdapter(e -> this.mousePressed(e)));
	}

	// Transforms
	protected double toX(double r) {
		return r * getWidth() / rMax;
	}

	protected double toR(double x) {
		return x * rMax / getWidth();
	}

	// Plot methods
	protected static final void drawParticle(Graphics2D g, double x, double y, boolean resetTransform) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.red);
		g.translate(x, y);
		g.fill(particle);
		// Be nice and clean up
		if (resetTransform)
			g.translate(-x, -y);
	}

	protected static final void addLineMark(Path2D path, double x, double y) {
		// Just a little line
		if (path.getCurrentPoint() != null)
			return;
		path.append(new Line2D.Double(x, y - 1, x, y - 5), false);
	}

	protected void resetMarks() {
		periastronPath.reset();
		apastronPath.reset();
	}

	// Abstract methods
	protected abstract String getPlotTitle();

	protected abstract void mousePressed(MouseEvent e);

	protected abstract void addPeriastronMark();

	protected abstract void addApastronMark();

	protected abstract void drawPotential(Graphics2D g);

	@Override
	protected final void paintSteadyPart(Graphics2D g) {
		// Quality
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// Here, we speculate...
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		//g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		//g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		// Background
		g.setColor(Color.gray);
		g.fillRect(0, 0, getWidth(), getHeight());

		// Potential in transformed coordinates
		g.transform(t0);
		drawPotential(g);
		try {
			g.transform(t0.createInverse());
		} catch (NoninvertibleTransformException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
			// Reset to identity
			g.setTransform(new AffineTransform());
		}

		// Title, well measured and aligned
		g.setColor(Color.white);
		g.setFont(titleFont);
		FontMetrics fm = g.getFontMetrics(titleFont);
		int x = fm.charWidth('H');
		int y = fm.getDescent();
		for (String s : getPlotTitle().split(" ")) {
			y += fm.getHeight();
			g.drawString(s, x, y);
		}
	}

	@Override
	protected void paintDynamicPart(Graphics2D g) {
		// Choose anti-aliasing in each implementation individually
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.transform(t0);
	}

	@Override
	public void validate() {
		super.validate();
		// Simple transform to lower left corner and an upward y axis
		t0 = new AffineTransform(1, 0, 0, -1, 0, getHeight());
	}
}
