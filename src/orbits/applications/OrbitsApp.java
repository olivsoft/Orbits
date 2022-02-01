package orbits.applications;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.File;
import ch.oliverfritz.util.MoreUtil;
import ch.oliverfritz.util.WindowDisposer;
import orbits.components.OrbitsAnimation;
import orbits.components.OrbitsMenuBar;
import orbits.components.OrbitsAnimation.LayoutType;
import orbits.interfaces.AnimationMenuListener;
import orbits.resources.R;

public class OrbitsApp extends Frame implements AnimationMenuListener {

	private OrbitsAnimation orbitsAnimation;
	private OrbitsMenuBar orbitsMenuBar;

	public OrbitsApp() {
		// Title and icon
		setTitle(R.APP_TITLE);
		setIconImage(MoreUtil.getImageResource(this, R.PATH_ICON));

		// Close this Window in a good way
		addWindowListener(new WindowDisposer() {
			@Override
			public void windowClosing(WindowEvent e) {
				exitApplication();
			}
		});

		// Menu
		orbitsMenuBar = new OrbitsMenuBar(this, this);
		setMenuBar(orbitsMenuBar);

		// Main content, layout and start. 16:9 is a good format.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width * 2 / 3, screenSize.width * 3 / 8);
		setLocation(screenSize.width / 10, screenSize.height / 10);
		restartAnimation();
	}

	// Separated for easy access from MenuItem
	@Override
	public void restartAnimation() {
		if (orbitsAnimation != null)
			orbitsAnimation.terminate();
		removeAll();
		// Simple layout option here
		orbitsAnimation = new OrbitsAnimation(this, LayoutType.SIMPLE_LAYOUT, true);
		// The MenuBar can also listen to state changes this way
		orbitsMenuBar.setRunnableContainer(orbitsAnimation);
		setVisible(true);
		setRunning(true);
	}

	@Override
	public boolean isRunning() {
		return orbitsAnimation.isRunning();
	}

	@Override
	public void setRunning(boolean isRunning) {
		orbitsAnimation.setRunning(isRunning);
	}

	@Override
	public void savePlot() {
		boolean wasRunning = isRunning();
		if (wasRunning)
			setRunning(false);
		FileDialog fileDialog = new FileDialog(this, R.TXT_SAVEAS, FileDialog.SAVE);
		File dtDir = new File(System.getProperty("user.home"), "Desktop");
		if (dtDir.exists())
			fileDialog.setDirectory(dtDir.getName());
		fileDialog.setFile(R.TIT_ORBIT + MoreUtil.PNG_EXT);
		fileDialog.setVisible(true);
		String fileName = fileDialog.getFile();
		if (fileName == null)
			return;
		if (!fileName.endsWith(MoreUtil.PNG_EXT))
			fileName += MoreUtil.PNG_EXT;
		orbitsAnimation.saveOrbitPlot(new File(fileDialog.getDirectory(), fileName));
		if (wasRunning)
			setRunning(true);
	}

	@Override
	public void exitApplication() {
		orbitsAnimation.terminate();
		dispose();
	}

	public static void main(String[] args) {
		new OrbitsApp();
	}
}
