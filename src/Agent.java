import com.esotericsoftware.kryonet.Connection;


public class Agent {
	
	public static final long RESPAWN_TIME = 5000L, INVULNERABILITY_TIME = 2000L;

	int id;
	String username;
	Connection conn;
	float x, y, dx = 0, dy = 0, ddx, ddy;
	short angle = 0;
	boolean dead = false;
	long respawnTime, invulnerableTime = 0;
	byte[] avatarData;
	public short[] metaData = new short[8];
	
	public Agent(int id, String username, Connection c){
		this.id = id;
		this.username = username;
		conn = c;
		x = 720/2;
		y = 720/2;
		invulnerableTime = System.currentTimeMillis() + INVULNERABILITY_TIME;
	}

	public void update() {
		if(dead){
			if(respawnTime < System.currentTimeMillis()){
				respawn();
			}
		}
		x += dx;
		y += dy;
		
		if(x > AsteroidFieldServer.WORLD_WIDTH) dx = -dx;
		if(x < 0) dx = -dx;
		if(y > AsteroidFieldServer.WORLD_HEIGHT) dy = -dy;
		if(y < 0) dy = -dy;
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
		
		PacketDrawText drawText = new PacketDrawText();
		drawText.id = 69;
		drawText.size = 24;
		drawText.text = "You are dead!";
		drawText.x = 40;
		drawText.y = 40;
		drawText.color = (byte) 2; // red
		conn.sendTCP(drawText);
	}

	public void respawn() {
		x = AsteroidFieldServer.WORLD_WIDTH/2;
		y = AsteroidFieldServer.WORLD_HEIGHT/4;
		dead = false;
		
		PacketPlayerState packet = new PacketPlayerState();
		packet.id = -1;
		packet.dead = false;
		conn.sendTCP(packet);
		packet.id = id;
		AsteroidFieldServer.server.sendToAllExceptTCP(id, packet);
		
		PacketUpdatePlayer playerUpdate = new PacketUpdatePlayer();
		playerUpdate.id = -1;
		playerUpdate.x = x;
		playerUpdate.y = y;
		playerUpdate.dx = dx;
		playerUpdate.dy = dy;
		playerUpdate.angle = angle;
		conn.sendUDP(playerUpdate);
		playerUpdate.id = (short) id;
		AsteroidFieldServer.server.sendToAllExceptUDP(id, playerUpdate);
		
		invulnerableTime = System.currentTimeMillis() + INVULNERABILITY_TIME;
		
		PacketClearText clearText = new PacketClearText();
		clearText.id = 69;
		conn.sendTCP(clearText);
	}
	
	public boolean isInvulnerable(){
		if(invulnerableTime == 0) return false;
		if(System.currentTimeMillis() > invulnerableTime){
			invulnerableTime = 0;
			return false;
		}else{
			return true;
		}
	}
}
