import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;


public class AsteroidFieldServer extends Listener {

	public static final int WORLD_WIDTH = 720, WORLD_HEIGHT = 720;
	
	static Server server;
	static Random random = new Random();
	static List<Asteroid> asteroids = new ArrayList<Asteroid>();
	static long updateAsteroidTicker = System.currentTimeMillis() + 1000;
	static long updatePlayersTicker = System.currentTimeMillis() + 1800;
	static GameMode activeGameMode = new GameModeTestBed();
	
	public static void main(String[] args){
		System.out.println("Starting...");
		try {
			server = new Server(16384, 12384);
			Kryo k = server.getKryo();
			k.register(int[].class);
			k.register(byte[].class);
			k.register(PacketJoin.class);
			k.register(PacketMakeAsteroid.class);
			k.register(PacketUpdateAsteroid.class);
			k.register(PacketDropPlayer.class);
			k.register(PacketUpdatePlayer.class);
			k.register(PacketPlayerState.class);
			k.register(PacketDrawText.class);
			k.register(PacketClearText.class);
			k.register(PacketUpdateText.class);
			k.register(PacketRelayKeys.class);
			k.register(PacketKeyPress.class);
			server.bind(25565, 25565);
			server.addListener(new AsteroidFieldServer());
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		asteroids.addAll(generateAsteroids(1));
		activeGameMode.onServerStart();
		
		while(true){
			try{
				activeGameMode.onGameTick();
				
				for(Asteroid a : asteroids){
					a.update();
				}
				
				for(Agent agent : agents){
					if(agent == null) continue;
					
					agent.update();
					
					if((agent.ddx != agent.dx || agent.ddy != agent.dy) && !agent.isInvulnerable()){
						double accelerationX = agent.ddx-agent.dx;
						double accelerationY = agent.ddy-agent.dy;
						boolean forward = Math.hypot(accelerationX, accelerationY) > 0.03f; // determine that the player is accelerating forwards
						if(forward){
							for(Asteroid asteroid : asteroids){
								// check if behind
								double astAngle = calcDeclinationAngle(agent.x, agent.y, asteroid.x, asteroid.y);
								if(Math.abs(astAngle - agent.angle) < 35){
									// calculate point distance
									double distance = Math.hypot(agent.x-asteroid.x, agent.y-asteroid.y);
									if(distance > 250) continue;
									double acceleration = 40.0/distance * 0.25;
									asteroid.vx += Math.sin(Math.toRadians(astAngle) + Math.PI) * acceleration;
									asteroid.vy += Math.cos(Math.toRadians(astAngle) + Math.PI) * acceleration;
									
									PacketUpdateAsteroid packet = new PacketUpdateAsteroid();
									packet.id = asteroid.id;
									packet.x = asteroid.x;
									packet.y = asteroid.y;
									packet.dx = asteroid.vx;
									packet.dy = asteroid.vy;
									server.sendToAllUDP(packet);
								}
							}
						}
						
						agent.ddx = agent.dx;
						agent.ddy = agent.dy;
					}
					
					// Check collisions
					if(!agent.isInvulnerable()){
						for(Asteroid asteroid : asteroids){
							if(asteroid.shape.contains(agent.x-asteroid.x, agent.y-asteroid.y)){
								if(!agent.dead){
									createExplosion(agent.x,agent.y, 250, 0.2f);
									agent.kill();
								}
							}
						}
					}
				}
				
				if(System.currentTimeMillis() > updateAsteroidTicker){
					resyncAsteroids();
				}
				
				if(System.currentTimeMillis() > updatePlayersTicker){
					resyncPlayers();
				}
				
				Thread.sleep(16,666667);
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
		}
	}
	
	private static void resyncPlayers() {
		updatePlayersTicker = System.currentTimeMillis() + 1800;
		for(Agent agent : agents){
			if(agent == null) continue;
			PacketUpdatePlayer packet = new PacketUpdatePlayer();
			packet.id = (short) agent.id;
			packet.x = agent.x;
			packet.y = agent.y;
			packet.dx = agent.dx;
			packet.dy = agent.dy;
			packet.angle = agent.angle;
			
			server.sendToAllExceptUDP(agent.id, packet);
			packet.id = -1;
			agent.conn.sendUDP(packet);
		}		
	}

	private static void resyncAsteroids() {
		updateAsteroidTicker = System.currentTimeMillis() + 3000;
		for(Asteroid a : asteroids){
			PacketUpdateAsteroid packet = new PacketUpdateAsteroid();
			packet.id = a.id;
			packet.x = a.x;
			packet.y = a.y;
			packet.dx = a.vx;
			packet.dy = a.vy;
			server.sendToAllUDP(packet);
		}		
	}

	private static void createExplosion(float x, float y, int size, float force) {
		for(Asteroid asteroid : asteroids){
			//if(Math.abs(x - asteroid.x) < size && Math.abs(y - asteroid.y) < size){
				double distance = Math.hypot(x-asteroid.x, y-asteroid.y);
				if(distance < size){
					double acceleration = size/distance * force;
					double angle = calcDeclinationAngle(x, y, asteroid.x, asteroid.y);
					asteroid.vx += (Math.sin(Math.toRadians(angle) + Math.PI) * acceleration);
					asteroid.vy += (Math.cos(Math.toRadians(angle) + Math.PI)) * acceleration;
					
					PacketUpdateAsteroid packet = new PacketUpdateAsteroid();
					packet.id = asteroid.id;
					packet.x = asteroid.x;
					packet.y = asteroid.y;
					packet.dx = asteroid.vx;
					packet.dy = asteroid.vy;
					server.sendToAllUDP(packet);
					
				}
			//}
		}
	}
	
	// same as createExplosion, but provide an angle (deg) instead of calculating it from x,y of explosion origin
	// TODO also broken
	private static void createExplosionWithAngle(float x, float y, int size, float force, double angle) {
		for(Asteroid asteroid : asteroids){
			//if(Math.abs(x - asteroid.x) < size && Math.abs(y - asteroid.y) < size){
				double distance = Math.hypot(x-asteroid.x, y-asteroid.y);
				if(distance < size){
					double acceleration = size/distance * force;
					//double angle = calcDeclinationAngle(x, y, asteroid.x, asteroid.y);
					asteroid.vx += Math.sin(Math.toRadians(angle) + Math.PI) * acceleration;
					asteroid.vy += -Math.cos(Math.toRadians(angle) + Math.PI) * acceleration;
					
					PacketUpdateAsteroid packet = new PacketUpdateAsteroid();
					packet.id = asteroid.id;
					packet.x = asteroid.x;
					packet.y = asteroid.y;
					packet.dx = asteroid.vx;
					packet.dy = asteroid.vy;
					server.sendToAllUDP(packet);
					
				}
			//}
		}
	}

	public static List<Asteroid> generateAsteroids(int count){
		List<Asteroid> _asteroids = new ArrayList<Asteroid>();
		for(int i=0; i<count; i++){
			
			Asteroid asteroid = new Asteroid(20 + i*20,200);
			for(int r=0; r<=12; r++){
				double theta = (r/12.0f) * Math.PI * 2.0 + (Math.toRadians(10) - random.nextFloat() * Math.toRadians(20));
				double distance = 28 + 38 * Math.random();
				asteroid.shape.addPoint((int) Math.round(Math.sin(theta) * distance), (int) Math.round(Math.cos(theta) * distance));
			}
			asteroid.vx = 5*(0.2f - 0.4f * random.nextFloat());
			asteroid.vy = 5*(0.2f - 0.4f * random.nextFloat());
			_asteroids.add(asteroid);
			System.out.println("Generated asteroid " + i);
			
			PacketMakeAsteroid packet = new PacketMakeAsteroid();
			packet.id = asteroid.id;
			packet.pointCount = asteroid.shape.npoints;
			packet.xpoints = asteroid.shape.xpoints;
			packet.ypoints = asteroid.shape.ypoints;
			packet.x = asteroid.x;
			packet.y = asteroid.y;
			packet.vx = asteroid.vx;
			packet.vy = asteroid.vy;
			server.sendToAllTCP(packet);
		}
		return _asteroids;
	}

	public static double calcDeclinationAngle(float p1x, float p1y, float p2x, float p2y){
		double x = 360-(Math.toDegrees(Math.atan2(p1y-p2y, p1x-p2x)) + 270);
		while(x < 0) x+=360;
		while(x > 360) x-=360;
		return x;
	}
	
	static Agent[] agents = new Agent[200];
	
	public void received(Connection c, Object o){
		if(o instanceof PacketJoin){
			PacketJoin packet = (PacketJoin) o;
			packet.id = c.getID();
			packet.accepted = activeGameMode.mayPlayerJoin();
			
			if(packet.accepted){
				server.sendToAllExceptTCP(c.getID(), packet);
				
				packet.id = -1;
				c.sendTCP(packet);
				
				for(int i=0; i<agents.length; i++){
					if(agents[i] == null) continue;
					PacketJoin addPlayer = new PacketJoin();
					addPlayer.id = agents[i].id;
					addPlayer.username = agents[i].username;
					if(agents[i].avatarData != null){
						addPlayer.hasAvatar = true;
						addPlayer.avatarData = agents[i].avatarData;
					}else{
						addPlayer.hasAvatar = false;
					}
					c.sendTCP(addPlayer);
					
				}
				
				agents[c.getID()] = new Agent(c.getID(), packet.username, c);
				agents[c.getID()].avatarData = packet.hasAvatar ? packet.avatarData : null;
				sendAllAsteroidsTo(agents[c.getID()]);
				
				System.out.println("Player " + packet.username + " joined!");
				activeGameMode.onPlayerJoin(agents[c.getID()]);
				System.out.println("Sending asteroids...");
			}else{
				System.out.println("Rejecting player join request.");
				c.sendTCP(packet);
				c.close();
				
			}
		}else if(o instanceof PacketUpdatePlayer){
			PacketUpdatePlayer packet = (PacketUpdatePlayer) o;
			agents[c.getID()].dx = packet.dx;
			agents[c.getID()].dy = packet.dy;
			agents[c.getID()].angle = packet.angle;
			
			packet.id = (short) c.getID();
			packet.x = agents[c.getID()].x;
			packet.y = agents[c.getID()].y;
			
			server.sendToAllExceptUDP(c.getID(), packet);
			
		}else if(o instanceof PacketKeyPress){
			PacketKeyPress packet = (PacketKeyPress) o;
			System.out.println("Key pressed " + (int) packet.key);
			activeGameMode.onPlayerKeyPress(agents[c.getID()], (int) packet.key);
		}
	}

	private void sendAllAsteroidsTo(Agent agent) {
		for(Asteroid asteroid : asteroids){
			PacketMakeAsteroid packet = new PacketMakeAsteroid();
			packet.id = asteroid.id;
			
			//packet.shape = asteroid.shape;
			packet.pointCount = asteroid.shape.npoints;
			packet.xpoints = asteroid.shape.xpoints;
			packet.ypoints = asteroid.shape.ypoints;
			
			packet.x = asteroid.x;
			packet.y = asteroid.y;
			packet.vx = asteroid.vx;
			packet.vy = asteroid.vy;
			agent.conn.sendTCP(packet);
		}
	}
	
	public void disconnected(Connection c){
		PacketDropPlayer packet = new PacketDropPlayer();
		packet.id = c.getID();
		server.sendToAllUDP(packet);
		System.out.println("Dropped player " + c.getID());
		activeGameMode.onPlayerDisconnect(agents[c.getID()]);
		agents[c.getID()] = null;
	}
	
	public static String md5(byte[] data){
		try {
			MessageDigest digestiveTract = MessageDigest.getInstance("MD5");
			digestiveTract.update(data);
			return byteArrayToHexString(digestiveTract.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String byteArrayToHexString(byte[] b) {
		String result = "";
		for (int i=0; i < b.length; i++) {
			result +=
					Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		}
		return result;
	}
}
