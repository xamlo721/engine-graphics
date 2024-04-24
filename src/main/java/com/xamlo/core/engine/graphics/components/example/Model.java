package com.xamlo.core.engine.graphics.components.example;

import static java.lang.Math.sqrt;

public class Model implements Function {

    public Model() {
    	
    }
    
    @Override
    public double evaluate (double x, double y, double z){
		double p[]={x,y,z};
		return evaluate(p);
	}	
    
    public double evaluate(double p[]) {
		//p: point to evaluate -- x,y,z
	
		double v1[]={10,0,10};
		double r1 = 3;
		double sphere1 = sphere(p, v1, r1);
	
		double v2[]={5,5,0};
		double r2 = 1;
		double cylinder2 = cylinderZ(p,v2,r2);
	
		double v3[]={0,0,0};
		double d3[]={10,10,10};
		double box3 = box(p,v3,d3);
	
		return my_min(my_min(box3, -sphere1), -cylinder2);
    }
    
    private double my_min(double a, double b){
    	return a < b ? a : b;
    }

    private double square(double a){
    	return a*a;
    }

    private double box(double p[], double v[], double d[]) {
		// p: point to evaluate -- x,y,z
		// v: lower vertex of the box -- x,y,z
		// d: dimension of the box -- dx,dy,dz
	
		// 1st plane:
		double p1 = p[0] - (v[0]);
		
		// 2nd plane:
		double p2 = p[1] - (v[1]);
	
		// 3rd plane:
		double p3 = p[2] - (v[2]);
	
		// 4th plane:
		double p4 = (v[0] + d[0]) - p[0];
	
		// 5th plane:
		double p5 = (v[1] + d[1]) - p[1];
	
		// 6th plane:
		double p6 = (v[2] + d[2]) - p[2];
	
		return my_min(my_min(my_min(my_min(my_min(p1,p2),p3),p4),p5),p6);
    }

    private double cylinderZ(double p[], double v[], double r){
		// p: point to evaluate -- x,y,z
	        // v: one point of the main axis of the cylinder -- x,y
		// 	z is not needed, because axis is Z
	        // r: radius
	
		return r - sqrt(square(p[0]-v[0]) + square(p[1]-v[1]));
    }

    private double sphere(double p[], double v[], double r){
	// p: point to evaluate -- x,y,z
        // v: one point of the main axis of the cylinder -- x,y
        //      z is not needed, because axis is Z
        // r: radius

    	return r - sqrt(square(p[0]-v[0]) + square(p[1]-v[1]) + square(p[2]-v[2]));
    }




}
