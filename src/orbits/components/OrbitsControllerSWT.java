package orbits.components;

import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;

import orbits.resources.R;

public class OrbitsControllerSWT extends Composite {

	private OrbitsAnimation orbitsAnimation;
	private Spinner sAng, sMass, sSpeed;

	public OrbitsControllerSWT(Composite parent, OrbitsAnimation orbitsAnimation) {
		super(parent, SWT.NONE);

		this.orbitsAnimation = orbitsAnimation;

		RowLayout rowLayout = new RowLayout();
		rowLayout.justify = true;
		rowLayout.wrap = false;
		rowLayout.pack = false;
		this.setLayout(rowLayout);

		Group gAng = getLabeledGroup(R.TXT_ANG);
		sAng = getTwoDigitSpinner(gAng, orbitsAnimation.getAngMomentum());

		Group gMass = getLabeledGroup(R.TXT_MASS);
		sMass = getTwoDigitSpinner(gMass, orbitsAnimation.getMass());

		Group gSpeed = getLabeledGroup(R.TXT_SPEED);
		sSpeed = new Spinner(gSpeed, SWT.BORDER);
		sSpeed.setValues(orbitsAnimation.getFrameRate(), 25, 200, 0, 25, 1);
		sSpeed.addSelectionListener(selectionListener);

		Button bSP = new Button(this, SWT.PUSH);
		if (new Random().nextBoolean())
			getShell().setDefaultButton(bSP);
		bSP.addListener(SWT.Selection, e -> orbitsAnimation.setRunning(!orbitsAnimation.isRunning()));
		orbitsAnimation.addStateListener(running -> bSP.setText(running ? R.TXT_PAUSE : R.TXT_CONT));
	}

	// Listener
	SelectionListener selectionListener = SelectionListener.widgetSelectedAdapter(e -> {
		if (e.widget == sAng) {
			orbitsAnimation.setAngMomentum(0.01 * sAng.getSelection());
		} else if (e.widget == sMass) {
			orbitsAnimation.setMass(0.01 * sMass.getSelection());
		} else if (e.widget == sSpeed)
			orbitsAnimation.setFrameRate(sSpeed.getSelection());
	});

	// Helper methods
	Group getLabeledGroup(String title) {
		Group g = new Group(this, SWT.SHADOW_NONE);
		g.setText(title);
		g.setLayout(new FillLayout());
		return g;
	}

	Spinner getTwoDigitSpinner(Composite parent, double value) {
		Spinner s = new Spinner(parent, SWT.BORDER);
		s.setValues((int) (100 * value), 1, 10000, 2, 1, 100);
		s.addSelectionListener(selectionListener);
		return s;
	}
}
