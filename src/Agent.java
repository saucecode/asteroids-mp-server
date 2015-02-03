import com.esotericsoftware.kryonet.Connection;


public class Agent {
	
	public static long RESPAWN_TIME = 5000L;

	int id;
	String username;
	Connection conn;
	float x, y, dx = 0, dy = 0, ddx, ddy;
	short angle = 0;
	boolean dead = false;
	long respawnTime;
	byte[] avatarData;
	
	public Agent(int id, String username, Connection c){
		this.id = id;
		this.username = username;
		conn = c;
		x = 720/2;
		y = 720/2;
	}
	
	public void kill(){
		x = -10;
		y = -10;
		dx = 0;
		dy = 0;
		dead = true;
		respawnTime = System.currentTimeMillis() + RESPAWN_TIME;
		
		PacketPlayerState packet = new PacketPlayerState();
		packet.id = -1;
		packet.respawnTime = (int) RESPAWN_TIME;
		packet.dead = true;
		conn.sendTCP(packet);
		packet.id = id;
		AsteroidFieldServer.server.sendToAllExceptTCP(id, packet);
	}

	public void respawn() {
		x = 720/2;
		y = 720/4;
		dead = false;
		
		PacketPlayerState packet = new PacketPlayerState();
		packet.id = -1;
		packet.dead = false;
		conn.sendTCP(packet);
		packet.id = id;
		AsteroidFieldServer.server.sendToAllExceptTCP(id, packet);
		
		PacketUpdatePlayer playerUpdate = new PacketUpdatePlayer();
		//playerUpdate.id = (short) id;
		playerUpdate.id = -1;
		playerUpdate.x = x;
		playerUpdate.y = y;
		playerUpdate.dx = dx;
		playerUpdate.dy = dy;
		playerUpdate.angle = angle;
		conn.sendUDP(playerUpdate);
		playerUpdate.id = (short) id;
		AsteroidFieldServer.server.sendToAllExceptUDP(id, playerUpdate);
	}
}
