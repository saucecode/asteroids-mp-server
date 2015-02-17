
public class GameModeTestBed extends GameMode {

	public boolean mayPlayerJoin() {
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
		System.out.println("Asking " + agent.id + " to send keys.");
		agent.metaData[0] = 0;
	}

	public void onServerStart(){
		System.out.println("Running GameModeTestBed");
	}

	public void onPlayerRespawn(Agent agent) {
		
	}

	public void onPlayerKeyPress(Agent agent, int key) {
		if(key == 28){
			if(agent.metaData[0] == 0){
				PacketDrawText packet = new PacketDrawText();
				packet.id = 20;
				packet.x = 20;
				packet.y = 120;
				packet.size = 24;
				packet.color = 1;
				packet.text = "Score: 0";
				agent.conn.sendTCP(packet);
			}else{
				PacketUpdateText packet = new PacketUpdateText();
				packet.id = 20;
				packet.text = "Score: " + agent.metaData[0];
				agent.conn.sendUDP(packet);
			}
			AsteroidFieldServer.asteroids.addAll(AsteroidFieldServer.generateAsteroids(1));
			agent.metaData[0]++;
		}
		
	}

	public void onPlayerDisconnect(Agent agent) {
		
	}
	
}
