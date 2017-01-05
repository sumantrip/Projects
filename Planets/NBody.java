public class NBody{
	public static double readRadius(String fileName) {
		In scan = new In(fileName);
		int numPlanets = scan.readInt();
		return scan.readDouble();
	}

	public static Planet[] readPlanets(String fileName) {
		In in = new In(fileName);
		Planet[] pArray = new Planet[in.readInt()];
		double univRad = in.readDouble();
		for (int i = 0; i < pArray.length; i++) {
			double xPos = in.readDouble();
			double yPos = in.readDouble();
			double xVel = in.readDouble();
			double yVel = in.readDouble();
			double mass = in.readDouble();
			String imgName = in.readString();
			Planet p = new Planet(xPos, yPos, xVel, yVel, mass, imgName);
			pArray[i] = p;
		}
		return pArray;
	}

	public static void main(String[] args) {
		double T = Double.parseDouble(args[0]);
		double dt = Double.parseDouble(args[1]);
		String filename = args[2];
		double univRad = readRadius(filename);
		Planet[] planets = readPlanets(filename);

		StdDraw.setScale(-univRad, univRad);
		StdDraw.clear();
		StdDraw.picture(0,0, "images/starfield.jpg");
		StdDraw.show();

		for (int i = 0; i < planets.length; i++) {
			planets[i].draw();

		int time = 0;

		for (int n = time; n < T; n += dt) {
			double[] xForces = new double[planets.length]; 
			double[] yForces = new double[planets.length];

			for (int j = 0; j < xForces.length; j++) {
				xForces[j] = planets[j].calcNetForceExertedByX(planets); 
				yForces[j] = planets[j].calcNetForceExertedByY(planets); 
			}
			for (int p = 0; p < planets.length; p++) {
				planets[p].update(dt, xForces[p], yForces[p]);
			}

			StdDraw.clear();
			StdDraw.picture(0,0, "images/starfield.jpg");
			StdDraw.show();

			for (int k = 0; k < planets.length; k++) {
				planets[k].draw();
		    }
		    StdDraw.show(10);
		}

		StdOut.printf("%d\n", planets.length);
		StdOut.printf("%.2e\n", univRad);
		for (int f = 0; f < planets.length; f++) {
			StdOut.printf("%11.4e %11.4e %11.4e %11.4e %11.4e %12s\n", 
				planets[f].xxPos, planets[f].yyPos, planets[f].xxVel, planets[f].yyVel, planets[f].mass, planets[f].imgFileName);
		}	
	}
}
}