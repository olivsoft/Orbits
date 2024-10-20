package orbits.plots;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import ch.oliverfritz.util.MoreMath;
import orbits.resources.R;

public final class OrbitPlot extends BasePlot {

	// Path objects
	private Path2D orbitPathL = new Path2D.Double(Path2D.WIND_NON_ZERO, 5000);
	private Path2D orbitPathP = new Path2D.Double(Path2D.WIND_NON_ZERO, 500);
	private static final float rP = 1.5f;
	private static Stroke pointStroke = new BasicStroke(2 * rP, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);

	// Plot size, particle coordinates, transform to center of screen
	private double s0, phi, x, y;
	private boolean initMotion;

	// Some variety in appearance
	private enum PlotType {
		LINE, NO_PARTICLE, POINTS;

		// Perfect for cycling through the types
		PlotType getNext() {
			return values()[(ordinal() + 1) % values().length];
		}
	}

	private PlotType plotType = PlotType.LINE;

	// Plot title
	@Override
	protected String getPlotTitle() {
		return R.TIT_ORBIT;
	}

	// Coordinate transform radial -> Cartesian
	private double x() {
		return r * Math.cos(phi) * s0 / rMax;
	}

	private double y() {
		return r * Math.sin(phi) * s0 / rMax;
	}

	// Clear trails, hide particle
	@Override
	protected void mousePressed(MouseEvent e) {
		// Checking for left and right mouse button. No default therefore.
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			if (r >= 2 * M) {
				// Clear, but keep the current coordinate
				double currentPhi = phi;
				clear();
				phi = currentPhi;
			}
			break;

		case MouseEvent.BUTTON3:
			plotType = plotType.getNext();
			initMotion = true;
			repaint();
			break;
		}
	}

	@Override
	public void clear() {
		super.clear();
		// Reset phi and all paths
		phi = 0;
		initMotion = true;
		resetMarks();
		orbitPathL.reset();
		orbitPathP.reset();
	}

	@Override
	protected void addPeriastronMark() {
		if (r > 2 * M) {
			double l = (r - 4 * M) * s0 / rMax / 2;
			double xl = x() - l * Math.cos(phi);
			double yl = y() - l * Math.sin(phi);
			periastronPath.append(new Line2D.Double(x(), y(), xl, yl), false);
		}
	}

	@Override
	protected void addApastronMark() {
		// Draw a line from the center
		apastronPath.append(new Line2D.Double(0, 0, x(), y()), false);
		orbitPathP.reset();
	}

	@Override
	protected void drawPotential(Graphics2D g) {
		// Draw black hole
		double r0 = 4 * M * s0 / rMax;
		g.setColor(Color.black);
		g.fill(new Ellipse2D.Double(-r0, -r0, 2 * r0, 2 * r0));

		// Also initialize x and y as r should be ready by now
		x = x();
		y = y();
	}

	@Override
	protected void updateDynamicPart() {
		// No more dynamics below the critical radius
		if (r <= 2 * M)
			return;

		if (initMotion || orbitPathL.getCurrentPoint() == null) {
			orbitPathL.moveTo(x(), y());
			initMotion = false;
		} else {
			// Update phi. Include basic range reduction.
			// Slow down in the black hole.
			phi += speedFactor * L / MoreMath.sqr(r);
			// Alternative: phi %= 2 * Math.PI;
			if (phi > Math.PI)
				phi -= 2 * Math.PI;
		}

		// Various options of complexity (quad, Bezier...). Here is the simple one!
		x = x();
		y = y();
		orbitPathL.lineTo(x, y);
		orbitPathP.append(new Ellipse2D.Double(x - rP, y - rP, 2 * rP, 2 * rP), false);
	}

	@Override
	protected void paintDynamicPart(Graphics2D g) {
		super.paintDynamicPart(g);
		// Draw orbit, apastron, periastron, and particle.
		if (plotType == PlotType.POINTS) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setStroke(pointStroke);
			g.setColor(Color.yellow);
			g.draw(apastronPath);
			g.setColor(Color.cyan);
			g.draw(periastronPath);
			g.fill(orbitPathP);
		} else {
			//g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(Color.cyan);
			g.draw(orbitPathL);
			g.setColor(Color.yellow);
			g.draw(apastronPath);
			if (plotType == PlotType.LINE)
				drawParticle(g, x, y, false);
		}
	}

	@Override
	public void validate() {
		super.validate();
		// Refresh size and transform
		double x0 = 0.5 * getWidth();
		double y0 = 0.5 * getHeight();
		// Leave a bit of space at the edge
		s0 = 0.99 * Math.min(x0, y0);
		t0.translate(x0, y0);
	}
}
