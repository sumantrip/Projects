public class TestPlanet {
	public static void main (String[] args) {
		Planet p1 = new Planet(0,0, 5, 5, 100, "ball.gif");
		Planet p2 = new Planet(10, 10, 5, 5, 100, "asteroid.gif");
		//assertTrue(4.7 * Math.pow(10,-8) <= p1.calcForceExertedBy(p2) && p1.calcForceExertedBy(p2) <= 4.72 * Math.pow(10,-8), "not in range");
	}
}