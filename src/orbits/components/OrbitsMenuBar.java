package orbits.components;

import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import ch.oliverfritz.util.AboutDialog;
import ch.oliverfritz.util.MoreUtil;
import ch.oliverfritz.util.RunnableContainer;
import orbits.interfaces.AnimationMenuListener;
import orbits.resources.R;

public class OrbitsMenuBar extends MenuBar implements ActionListener, RunnableContainer.Receiver {

	private AnimationMenuListener animationMenuListener;
	private Frame frame;
	private MenuItem miPauseCont, miRestart, miSave, miExit, miAbout;

	public OrbitsMenuBar(AnimationMenuListener animationMenuListener, Frame frame) {
		this.animationMenuListener = animationMenuListener;
		this.frame = frame;

		miPauseCont = new MenuItem(R.TXT_PAUSE);
		miRestart = new MenuItem(R.MI_RESTART);
		miSave = new MenuItem(R.MI_SAVE);
		miExit = new MenuItem(R.MI_EXIT);
		miAbout = new MenuItem(R.MI_ABOUT);

		miPauseCont.addActionListener(this);
		miRestart.addActionListener(this);
		miSave.addActionListener(this);
		miExit.addActionListener(this);
		miAbout.addActionListener(this);

		Menu mControl = new Menu(R.M_CONTROL);
		mControl.add(miPauseCont);
		mControl.add(miRestart);
		mControl.addSeparator();
		mControl.add(miSave);
		mControl.addSeparator();
		mControl.add(miExit);
		add(mControl);

		Menu mInfo = new Menu(R.M_INFO);
		mInfo.add(miAbout);
		add(mInfo);
		setHelpMenu(mInfo);
	}

	@Override
	public void setRunnableContainer(RunnableContainer runnableContainer) {
		runnableContainer.addStateListener(running -> miPauseCont.setLabel(running ? R.TXT_PAUSE : R.TXT_CONT));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == miPauseCont)
			animationMenuListener.setRunning(!animationMenuListener.isRunning());
		else if (e.getSource() == miRestart)
			animationMenuListener.restartAnimation();
		else if (e.getSource() == miSave)
			animationMenuListener.savePlot();
		else if (e.getSource() == miExit)
			animationMenuListener.exitApplication();
		else if (e.getSource() == miAbout) {
			InputStream is = getClass().getClassLoader().getResourceAsStream(R.PATH_ABOUT);
			if (is == null)
				new AboutDialog(frame, R.MI_ABOUT, R.TXT_ERROR, R.BTN_OK).setVisible(true);
			else {
				// Trimming is taken care of in the AboutDialog
				List<String> sl = MoreUtil.readAllLines(is, StandardCharsets.UTF_8, false);
				new AboutDialog(frame, R.MI_ABOUT, sl, R.BTN_OK).setVisible(true);
			}
		}
	}
}