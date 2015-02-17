
public abstract class GameMode {

	boolean gameStarted = false;
	
	public abstract void onServerStart();
	public abstract void onGameTick();
	
	public abstract boolean mayPlayerJoin();
	
	public abstract void onPlayerJoin(Agent agent);
	public abstract void onPlayerDeath(Agent agent);
	public abstract void onPlayerRespawn(Agent agent);
	public abstract void onPlayerKeyPress(Agent agent, int key);
	public abstract void onPlayerDisconnect(Agent agent);
}
