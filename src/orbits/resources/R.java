package orbits.resources;

import java.util.ResourceBundle;

public class R {
	private static final String RESOURCE_NAME = "resources.Orbits";
	private static final ResourceBundle r = ResourceBundle.getBundle(RESOURCE_NAME);

	// Property strings
	public static final String APP_TITLE = r.getString("APP_TITLE");

	public static final String BTN_OK = r.getString("BTN_OK");
	public static final String TXT_PAUSE = r.getString("TXT_PAUSE");
	public static final String TXT_CONT = r.getString("TXT_CONT");

	public static final String MI_RESTART = r.getString("MI_RESTART");
	public static final String MI_SAVE = r.getString("MI_SAVE");
	public static final String MI_EXIT = r.getString("MI_EXIT");
	public static final String MI_ABOUT = r.getString("MI_ABOUT");
	public static final String M_CONTROL = r.getString("M_CONTROL");
	public static final String M_INFO = r.getString("M_INFO");

	public static final String TIT_ENERGY = r.getString("TIT_ENERGY");
	public static final String TIT_SCHWARZSCHILD = r.getString("TIT_SCHWARZSCHILD");
	public static final String TIT_ORBIT = r.getString("TIT_ORBIT");

	public static final String TXT_ANG = r.getString("TXT_ANG");
	public static final String TXT_MASS = r.getString("TXT_MASS");
	public static final String TXT_MAXRAD = r.getString("TXT_MAXRAD");
	public static final String TXT_SPEED = r.getString("TXT_SPEED");
	public static final String TXT_SAVEAS = r.getString("TXT_SAVEAS");
	public static final String TXT_PNGFILES = r.getString("TXT_PNGFILES");
	public static final String TXT_ALLFILES = r.getString("TXT_ALLFILES");

	public static final String PATH_ICON = r.getString("PATH_ICON");
	public static final String PATH_ABOUT = r.getString("PATH_ABOUT");

	public static final String TXT_ERROR = r.getString("TXT_ERROR");
}
