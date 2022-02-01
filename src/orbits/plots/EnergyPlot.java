package orbits.plots;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.lang.Math;
import java.util.Random;

import ch.oliverfritz.util.MoreMath;
import orbits.resources.R;

public final class EnergyPlot extends BasePlot {

	private int direction;
	private double rVmin, rVmax, eVmin, eVmax, eMin, eMax, eTot;
	private boolean skipInit;
	private static Random random = new Random();

	// Plot title
	@Override
	protected String getPlotTitle() {
		return R.TIT_ENERGY;
	}

	// Coordinate transform
	private double toY(double e) {
		return getHeight() * (e - eMin) / (eMax - eMin);
	}

	// Effective potential for a given radius
	private double eG(double r) {
		return Math.sqrt((1 - 2 * M / r) * (1 + MoreMath.sqr(L / r)));
	}

	// Initial calculations after resizing and other changes
	private void initSizes() {
		// Find min and max of energy (if any)
		double disc = MoreMath.sqr(L) - 12 * MoreMath.sqr(M);
		if (disc < 0) {
			rVmin = -1.0; // Flag
			rVmax = MoreMath.sqr(L) / 2 / M;
			eVmin = 0.75;
			rMax = 1.75 * rVmax;
		} else {
			rVmin = L * (L + Math.sqrt(disc)) / 2 / M;
			rVmax = L * (L - Math.sqrt(disc)) / 2 / M;
			eVmin = eG(rVmin);
			rMax = 1.75 * rVmin;
		}
		eVmax = eG(rVmax);
		while (eG(rMax) < eVmax)
			rMax *= 1.05;

		// Find good plot size
		eMax = eG(rMax);
		eMin = eVmin - (eMax - eVmin) / 10;
		eMax += (eMax - eMin) / 10;

		// Put particle at the start position. -1 is a flag.
		putParticle(-1);
	}

	// Put particle to initial radius (e.g. by mouse click in window)
	private void putParticle(int x) {
		// Without mouse click (x < 0), start near rVmax. With mouse click, for a
		// falling orbit, start at least outside the black hole, and for a periodic
		// orbit, start at least beyond the maximum effective potential.
		r = (1.01 + 0.03 * random.nextDouble()) * (rVmin < 0 && x >= 0 ? 4 * M : rVmax);
		r = Math.max(toR(x), r);
		eTot = eG(r);
		direction = r < rVmin ? 1 : -1;
		// Initial marks only for non-periodic orbits
		plots.values().forEach(p -> p.resetMarks());
		if (rVmin < 0 || eTot > eVmax)
			plots.values().forEach(p -> p.addApastronMark());
	}

	// Mouse handling: reposition particle
	@Override
	protected void mousePressed(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1)
			return;
		plots.get(R.TIT_ORBIT).clear();
		putParticle(e.getX());
		plots.values().forEach(p -> p.repaint());
	}

	@Override
	protected void addPeriastronMark() {
		// Do nothing at all
	}

	@Override
	protected void addApastronMark() {
		addLineMark(apastronPath, toX(r), toY(eG(r)));
	}

	@Override
	protected void drawPotential(Graphics2D g) {
		// Right after the call from validate() we can skip for once.
		if (skipInit)
			skipInit = false;
		else
			initSizes();

		// Build the plot (and don't care about points outside the visible area)
		Path2D p = new Path2D.Double(Path2D.WIND_NON_ZERO, getWidth());
		double xmin = toX(2 * M);
		p.moveTo(getWidth(), 0);
		for (int x = getWidth(); x > xmin; x--)
			p.lineTo(x, toY(eG(toR(x))));
		p.lineTo(xmin, 0);
		p.closePath();

		// Draw energy curve and min and max energy markers.
		g.setColor(Color.blue);
		g.fill(p);
		if (rVmin > 0) {
			double x = toX(rVmin);
			double y = toY(eVmin);
			g.setColor(Color.yellow);
			g.draw(new Line2D.Double(x, y - 1, x, y - 5));
			x = toX(rVmax);
			y = toY(eVmax);
			g.draw(new Line2D.Double(x, y - 1, x, y - 5));
		}
	}

	@Override
	protected void updateDynamicPart() {
		// No more dynamics below the critical radius
		if (r <= 2 * M)
			return;

		// Calculate velocity from kinetic energy, leave away factor M/2.
		// Delta t is one frame.
		double dr = Math.sqrt(MoreMath.sqr(eTot) - MoreMath.sqr(eG(r)));

		// Slow down inside the black hole. Avoid zero motion.
		speedFactor = (r < 4 * M ? 0.5 : 1);
		r += direction * speedFactor * Math.max(1e-6 * M, dr);

		// Check for extreme points (periastron, apastron)
		if (eG(r) >= eTot) {
			// An extreme point is reached. So, turn around and creep down below the total
			// energy pixel by pixel (but with some caution).
			direction *= -1;
			do
				r += toR(direction);
			while (eG(r) >= eTot && rVmax < r && r <= rMax);

			// Mark periastron and apastron
			if (direction < 0)
				plots.values().forEach(p -> p.addApastronMark());
			else if (rVmin > 0)
				plots.values().forEach(p -> p.addPeriastronMark());
		}
	}

	@Override
	protected void paintDynamicPart(Graphics2D g) {
		super.paintDynamicPart(g);
		// Draw the apastron markers and the particle
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.yellow);
		g.draw(apastronPath);
		drawParticle(g, toX(r), toY(eG(r)), false);
	}

	@Override
	public void validate() {
		super.validate();
		// Call parts of the potential plot routine once here in order to
		// make sure it is evaluated before any other plot activity.
		initSizes();
		skipInit = true;
	}
}
