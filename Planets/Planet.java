public class Planet {

	public double xxPos;
	public double yyPos;
	public double xxVel;
	public double yyVel;
	public double mass;
	public String imgFileName;

	public Planet(double xP, double yP, double xV,double yV, double m, String img) {
		xxPos = xP;
		yyPos = yP;
		xxVel = xV;
		yyVel = yV;
		mass = m;
		imgFileName = img;
	}

	public Planet(Planet p) {
		xxPos = p.xxPos;
		yyPos = p.yyPos;
		xxVel = p.xxVel;
		yyVel = p.yyVel;
		mass = p.mass;
		imgFileName = p.imgFileName;
	}

	public double calcDistance(Planet p) {
		return Math.sqrt((p.xxPos-xxPos)*(p.xxPos-xxPos) + 
				(p.yyPos-yyPos)*(p.yyPos-yyPos));
	}

	public double calcForceExertedBy(Planet p) {
		double G = 6.67*Math.pow(10, -11); 
		return G * p.mass * mass / Math.pow(calcDistance(p), 2);
	}

	public double calcForceExertedByX(Planet p) {
		return (p.xxPos-xxPos)*calcForceExertedBy(p)/calcDistance(p); 
	}

	public double calcForceExertedByY(Planet p) {
		return (p.yyPos-yyPos)*calcForceExertedBy(p)/calcDistance(p); 
	}

	public double calcNetForceExertedByX(Planet[] parray) {
		double netX = 0;
		for (int i = 0;  i < parray.length; i++) {
			if (parray[i].equals(this)) {
				netX += 0;
			}
			else {
				netX += calcForceExertedByX(parray[i]);
			}
		}
		return netX;
	}

	public double calcNetForceExertedByY(Planet[] parray) {
		double netY = 0;
		for (int i = 0;  i < parray.length; i++) {
			if (parray[i].equals(this)) {
				netY += 0;
			}
			else {
				netY += calcForceExertedByY(parray[i]);
			}
		}
		return netY;
	}

	public void update(double dt, double fX, double fY) {
		double aX = fX/mass; 
		double aY = fY/mass; 
		xxVel += dt*aX; 
		yyVel += dt*aY; 
		xxPos += dt*xxVel; 
		yyPos += dt*yyVel; 
	}

	public void draw() {
		StdDraw.picture(xxPos,yyPos, "images/"+imgFileName);
		StdDraw.show();
	}
}