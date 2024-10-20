package orbits.applications;

import java.applet.Applet;
import java.awt.Dimension;
import java.util.logging.Logger;

import ch.oliverfritz.util.MoreUtil;
import orbits.components.OrbitsAnimation;
import orbits.components.OrbitsAnimation.LayoutType;
import orbits.resources.R;

@SuppressWarnings("removal")
public class OrbitsApplet extends Applet {
	private static final Logger LOGGER = MoreUtil.getLogger(OrbitsApplet.class);

	private OrbitsAnimation orbitsAnimation;

	@Override
	public String getAppletInfo() {
		return R.APP_TITLE;
	}

	@Override
	public void init() {
		// Short logging format
		LOGGER.info("init");
		this.setMinimumSize(new Dimension(500, 500));
		orbitsAnimation = new OrbitsAnimation(this, LayoutType.SIMPLE_LAYOUT, true);
	}

	@Override
	public void start() {
		LOGGER.info("start");
		orbitsAnimation.setRunning(true);
	}

	@Override
	public void stop() {
		LOGGER.info("stop");
		orbitsAnimation.setRunning(false);
	}

	@Override
	public void destroy() {
		LOGGER.info("destroy");
		orbitsAnimation.terminate();
	}
}
