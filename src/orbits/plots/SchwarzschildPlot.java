package orbits.plots;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import orbits.resources.R;

public final class SchwarzschildPlot extends BasePlot {

	// Plot title
	@Override
	protected String getPlotTitle() {
		return R.TIT_SCHWARZSCHILD;
	}

	// Parabolic Schwarzschild geometry
	private double yS(double r) {
		return 0.9 * getHeight() * Math.sqrt(r - 2 * M) / Math.sqrt(rMax - 2 * M);
	}

	@Override
	protected void mousePressed(MouseEvent e) {
		plots.get(R.TIT_ENERGY).mousePressed(e);
	}

	@Override
	protected void addPeriastronMark() {
		addLineMark(periastronPath, toX(r), yS(r));
	}

	@Override
	protected void addApastronMark() {
		addLineMark(apastronPath, toX(r), yS(r));
	}

	@Override
	protected void drawPotential(Graphics2D g) {
		// Draw potential
		Path2D p = new Path2D.Double(Path2D.WIND_NON_ZERO, getWidth());
		p.moveTo(getWidth(), 0);
		double xmin = toX(2 * M);
		for (int x = getWidth(); x > xmin; x--)
			p.lineTo(x, yS(toR(x)));
		p.lineTo(xmin, 0);
		p.closePath();
		g.setColor(Color.yellow);
		g.fill(p);
	}

	@Override
	protected void updateDynamicPart() {
		// Nothing at all
	}

	@Override
	protected void paintDynamicPart(Graphics2D g) {
		super.paintDynamicPart(g);
		// Draw apastron and periastron
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.blue);
		g.draw(periastronPath);
		g.draw(apastronPath);
		// Draw particle
		if (r > 2 * M)
			drawParticle(g, toX(r), yS(r), false);
	}
}
