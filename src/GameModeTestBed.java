
public class GameModeTestBed extends GameMode {

	public boolean allowPlayerToJoin() {
		return true;
	}

	public void onGameTick() {
		
	}

	public void onPlayerDeath(Agent agent) {
		
	}

	public void onPlayerRespawn() {
		
	}

	public void onPlayerJoin(Agent agent) {
		// Tell clients to relay their key presses.
		PacketRelayKeys packetRelay = new PacketRelayKeys();
		packetRelay.relayKeys = true;
		agent.conn.sendTCP(packetRelay);
	}

	public void onServerStart(){
		
	}

	public void onPlayerRespawn(Agent agent) {
		
	}

	public void onPlayerKeyPress(Agent agent, int key) {
		
	}

	public void onPlayerDisconnect(Agent agent) {
		
	}
	
}
