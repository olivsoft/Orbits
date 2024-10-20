package orbits.applications;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import ch.oliverfritz.util.MoreUtil;
import orbits.components.OrbitsAnimation;
import orbits.components.OrbitsControllerSWT;
import orbits.components.OrbitsAnimation.LayoutType;
import orbits.resources.R;

public class OrbitsAppSWT {

	private Shell shell;
	private Composite animationComposite;
	private OrbitsAnimation orbitsAnimation;
	private GridData gdAnimation, gdController;
	private MenuItem miPauseCont, miRestart, miSave, miExit, miAbout;
	private Rectangle lastArea;

	public OrbitsAppSWT() {
		Display display = new Display();
		shell = new Shell(display);

		// Title and icon. This also works for jar deployment.
		shell.setText(R.APP_TITLE);
		shell.setImage(new Image(display, getClass().getClassLoader().getResourceAsStream(R.PATH_ICON)));

		// Menu (fully integrated here unlike in AWT version)
		Menu mMain = new Menu(shell, SWT.BAR);
		shell.setMenuBar(mMain);

		Menu mControl = new Menu(shell, SWT.DROP_DOWN);
		MenuItem miControlHeader = new MenuItem(mMain, SWT.CASCADE);
		miControlHeader.setText(R.M_CONTROL);
		miControlHeader.setMenu(mControl);
		miPauseCont = new MenuItem(mControl, SWT.PUSH);
		miPauseCont.setText(R.TXT_PAUSE);
		miRestart = new MenuItem(mControl, SWT.PUSH);
		miRestart.setText(R.MI_RESTART);
		new MenuItem(mControl, SWT.SEPARATOR);
		miSave = new MenuItem(mControl, SWT.PUSH);
		miSave.setText(R.MI_SAVE);
		new MenuItem(mControl, SWT.SEPARATOR);
		miExit = new MenuItem(mControl, SWT.PUSH);
		miExit.setText(R.MI_EXIT);

		Menu mInfo = new Menu(shell, SWT.DROP_DOWN);
		MenuItem miInfoHeader = new MenuItem(mMain, SWT.CASCADE);
		miInfoHeader.setText(R.M_INFO);
		miInfoHeader.setMenu(mInfo);
		miAbout = new MenuItem(mInfo, SWT.PUSH);
		miAbout.setText(R.MI_ABOUT);

		Arrays.asList(miPauseCont, miRestart, miSave, miExit, miAbout)
				.forEach(mi -> mi.addSelectionListener(selectionListener));

		// Resize listener to avoid repetitive repainting
		shell.addControlListener(resizeListener);

		// Layout
		shell.setLayout(new GridLayout());
		gdAnimation = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdAnimation.heightHint = 400;
		gdAnimation.widthHint = 600;
		gdController = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gdController.minimumHeight = 50;

		// Restart separated for access from menu
		restartAnimation();

		// Run
		shell.open();
		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
		display.dispose();
	}

	public void restartAnimation() {
		// Cleanup
		if (orbitsAnimation != null)
			orbitsAnimation.terminate();
		for (Control c : shell.getChildren())
			c.dispose();

		// Animation
		animationComposite = new Composite(shell, SWT.EMBEDDED);
		orbitsAnimation = new OrbitsAnimation(SWT_AWT.new_Frame(animationComposite), LayoutType.RICH_LAYOUT, false);
		animationComposite.setLayoutData(gdAnimation);

		// Controller
		OrbitsControllerSWT oc = new OrbitsControllerSWT(shell, orbitsAnimation);
		oc.setLayoutData(gdController);

		// Register for state changes
		orbitsAnimation.addStateListener(running -> miPauseCont.setText(running ? R.TXT_PAUSE : R.TXT_CONT));

		// Make things visible and running
		shell.requestLayout();
		orbitsAnimation.setRunning(true);
	}

	// We use this friendly static helper method for the menu
	SelectionListener selectionListener = SelectionListener.widgetSelectedAdapter(e -> {
		if (e.widget.equals(miPauseCont)) {
			orbitsAnimation.setRunning(!orbitsAnimation.isRunning());
		} else if (e.widget.equals(miRestart)) {
			orbitsAnimation.terminate();
			restartAnimation();
		} else if (e.widget.equals(miSave)) {
			boolean wasRunning = orbitsAnimation.isRunning();
			orbitsAnimation.setRunning(false);
			FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
			fileDialog.setText(R.TXT_SAVEAS);
			fileDialog.setFilterNames(new String[] { R.TXT_PNGFILES, R.TXT_ALLFILES });
			fileDialog.setFilterExtensions(new String[] { MoreUtil.PNG_FILTER, "*.*" });
			fileDialog.setFileName(R.TIT_ORBIT + MoreUtil.PNG_EXT);
			fileDialog.setOverwrite(true);
			File dtDir = new File(System.getProperty("user.home"), "Desktop");
			if (dtDir.exists())
				fileDialog.setFilterPath(dtDir.getPath());
			String fileName = fileDialog.open();
			if (fileName != null)
				orbitsAnimation.saveOrbitPlot(new File(fileName));
			orbitsAnimation.setRunning(wasRunning);
		} else if (e.widget.equals(miExit)) {
			orbitsAnimation.terminate();
			shell.close();
		} else if (e.widget.equals(miAbout)) {
			InputStream is = getClass().getClassLoader().getResourceAsStream(R.PATH_ABOUT);
			String msg = is == null ? R.TXT_ERROR : MoreUtil.readAllText(is, StandardCharsets.UTF_8, true);
			MessageBox mb = new MessageBox(shell, SWT.OK);
			mb.setText(R.MI_ABOUT);
			mb.setMessage(msg);
			mb.open();
		}
	});

	// Again, we have a suitable adapter
	ControlListener resizeListener = ControlListener.controlResizedAdapter(e -> {
		animationComposite.layout(true, true);
		Rectangle clientArea = shell.getClientArea();
		// Initialize
		if (lastArea == null || lastArea.isEmpty())
			lastArea = clientArea;
		// No resize, so no need to do anything
		if (lastArea.equals(clientArea))
			return;
		boolean wasRunning = orbitsAnimation.isRunning();
		// Suspend graphical activity
		orbitsAnimation.setRunning(false);
		animationComposite.setRedraw(false);
		e.display.timerExec(100, () -> {
			// Restart graphics again after 100 ms of no motion
			animationComposite.setRedraw(true);
			if (wasRunning)
				orbitsAnimation.setRunning(true);
			lastArea = clientArea;
		});
	});

	public static void main(String[] args) {
		new OrbitsAppSWT();
		System.exit(0);
	}
}
