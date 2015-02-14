
public abstract class GameMode {

	boolean gameStarted = false;
	
	public abstract void onPlayerJoin(Agent agent);
	public abstract boolean allowPlayerToJoin();
	public abstract void onGameTick();
	public abstract void onPlayerDeath(Agent agent);
	public abstract void onPlayerRespawn();
	public abstract void onServerStart();
}
