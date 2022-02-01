package orbits.components;

import java.awt.Button;
import java.awt.Choice;
import java.awt.HeadlessException;
import java.awt.Panel;
import ch.oliverfritz.util.DoubleField;
import ch.oliverfritz.util.LabeledComponentPanel;
import ch.oliverfritz.util.MoreUtil;
import orbits.resources.R;

public class OrbitsController extends Panel {

	// Standard format for double values
	private static final String twoDigits = "%.2f";
	private Button btnPauseCont = new Button(MoreUtil.centerString(11, R.TXT_PAUSE));

	public OrbitsController(OrbitsAnimation orbitsAnimation) throws HeadlessException {
		btnPauseCont.addActionListener(e -> orbitsAnimation.setRunning(!orbitsAnimation.isRunning()));
		orbitsAnimation.addStateListener(
				running -> btnPauseCont.setLabel(MoreUtil.centerString(11, running ? R.TXT_PAUSE : R.TXT_CONT)));

		// Input fields
		DoubleField angMomentum = new DoubleField(twoDigits, orbitsAnimation.getAngMomentum());
		angMomentum.addActionListener(e -> orbitsAnimation.setAngMomentum(angMomentum.getValue()));

		DoubleField mass = new DoubleField(twoDigits, orbitsAnimation.getMass());
		mass.addActionListener(e -> orbitsAnimation.setMass(mass.getValue()));

		// Animation speed
		Choice speed = new Choice();
		int fpsStep = 25;
		int fpsMax = 200;
		int initSelection = 2;
		for (int fps = fpsStep; fps <= fpsMax; fps += fpsStep)
			speed.add(String.valueOf(fps));
		speed.select(initSelection);
		orbitsAnimation.setFrameRate((initSelection + 1) * fpsStep);
		speed.addItemListener(e -> orbitsAnimation.setFrameRate((speed.getSelectedIndex() + 1) * fpsStep));

		// Layout
		add(new LabeledComponentPanel(R.TXT_ANG, angMomentum));
		add(new LabeledComponentPanel(R.TXT_MASS, mass));
		add(new LabeledComponentPanel(R.TXT_SPEED, speed));
		add(btnPauseCont);
	}

	@Override
	protected void validateTree() {
		super.validateTree();
		btnPauseCont.requestFocus();
	}
}
