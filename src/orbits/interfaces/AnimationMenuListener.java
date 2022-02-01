package orbits.interfaces;

public interface AnimationMenuListener {

	public void restartAnimation();

	public boolean isRunning();

	public void setRunning(boolean isRunning);
	
	public void savePlot();

	public void exitApplication();
}
