import java.awt.Polygon;


public class Asteroid {

	static int ASTEROID_ID = 0;
	Polygon shape;
	int id;
	float x, y, vx, vy;
	
	public Asteroid(float x, float y){
		this.x = x;
		this.y = y;
		shape = new Polygon();
		id = ASTEROID_ID;
		ASTEROID_ID++;
	}

	public void update() {
		x += vx;
		y += vy;
		
		if(x > AsteroidFieldServer.WORLD_WIDTH) vx = -vx;
		if(x < 0) vx = -vx;
		if(y > AsteroidFieldServer.WORLD_HEIGHT) vy = -vy;
		if(y < 0) vy = -vy;		
	}
}
