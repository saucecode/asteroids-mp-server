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
}
