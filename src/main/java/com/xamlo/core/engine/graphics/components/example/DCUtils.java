package com.xamlo.core.engine.graphics.components.example;

import org.joml.Vector3d;
import org.joml.Vector4d;

public class DCUtils {

	
    public static double[][][] sample(Function f, Vector3d min, Vector3d max, int gX, int gY, int gZ) {
		double stepX, stepY, stepZ;
		double x, y, z;
	     
		stepX = (max.x - min.x)/(gX-1);
		stepY = (max.y - min.y)/(gY-1);
		stepZ = (max.z - min.z)/(gZ-1);
	     
		double value[][][] = new double[gZ][gY][gX];
	    
		int i,j,k;
		for(i=0; i<gZ; i++){
		    z = min.z + i*stepZ;
		    for(j=0; j<gY; j++){
				y = min.y + j*stepY;
				for(k=0; k<gX; k++){
				    x = min.x + k*stepX;
				    value[i][j][k] = f.evaluate(x, y, z);
				}
		    }
		}
		return value;
    }
    
    
    public static FilledCube fillCube(double[][][] value, int gX, int gY, int gZ) {
		

		FilledCube cube = new FilledCube(gZ-1, gY-1, gX-1);
		
		for(int i=0; i<gZ-1; i++){
			
		    for(int j=0; j<gY-1; j++){
		    	
				for(int k=0; k<gX-1; k++){
					
				    boolean flag = (value[i][j][k] > 0);
				    
				    if(flag != (value[i][j][k+1] > 0) ||
				       flag != (value[i][j+1][k] > 0) ||
				       flag != (value[i][j+1][k+1] > 0) ||
				       flag != (value[i+1][j][k] > 0) ||
				       flag != (value[i+1][j][k+1] > 0) ||
				       flag != (value[i+1][j+1][k] > 0) ||
				       flag != (value[i+1][j+1][k+1] > 0)) {
				    	cube.arrayData[i][j][k] = cube.vertexNumber++;
				    } else{
				    	cube.arrayData[i][j][k] = -1;
				    }
				    
				}
				
		    }
		    
		}
		
		return cube;
	}


    public static Vector3d grad(Function function,  Vector3d redPoint, Vector3d min, Vector3d max) {
    	
		double value = function.evaluate(redPoint.x, redPoint.y, redPoint.z);
		Vector3d point = new Vector3d();
		
		double diagonal = max.distance(min);
		double h = 0.1f * diagonal; //��� ��� �����?
		
		point.x = (function.evaluate(redPoint.x+h, redPoint.y,   redPoint.z)   - value)/h;
		point.y = (function.evaluate(redPoint.x,   redPoint.y+h, redPoint.z)   - value)/h;
		point.z = (function.evaluate(redPoint.x,   redPoint.y,   redPoint.z+h) - value)/h;
		
		return point;
	}
    
	public static Vector3d root(Function function, Vector4d pos1, Vector4d pos2, Vector3d min, Vector3d max) {
		
		Vector3d point = new Vector3d();
		double value;
		
		int i;
	
		int IMAX = 10;
		
		double eps;
		
		//��������� ����� (��������� ����)
		double diagonal = min.distance(max);
		
		//����������� ����������
		eps = 0.0001f * diagonal;
	
		for(i = 0; i < IMAX; i++) {
			
		    double w1 = Math.abs(pos2.w);
		    double w2 = Math.abs(pos1.w);
		    double w = w1 + w2;
		    
		    if(w == 0) {
		    	point.set(pos1.x, pos1.y, pos1.z);
		    	value = 0;
				break;
		    }
		    
		    w1 /= w;
		    w2 /= w;
	      
	    	point.set(w1*pos1.x + w2*pos2.x, w1*pos1.y + w2*pos2.y, w1*pos1.z + w2*pos2.z);
	    	value = function.evaluate(point.x, point.y, point.z);
	      
		    //�������� �����������
		    if(Math.abs(value) < eps) {
		    	break;
		    }
		    
		    if(pos1.w * value > 0) {
		    	pos1.set(point.x, point.y, point.z, value);
		    }  else{
		    	pos2.set(point.x, point.y, point.z, value);
		    }
		}
		
		//Change into Bisection
		if(i == IMAX) {
			
		    for(int j = 0; j < 2*IMAX; j++) {
		    	//���������� ����� �������� ���������
		    	point.set(0.5f*(pos1.x + pos2.x), 0.5f*(pos1.y + pos2.y), 0.5f*(pos1.z + pos2.z));
		        
		    	value = function.evaluate(point.x, point.y, point.z);
		        
			    if(Math.abs(value) < eps) {
			    	break;
			    }
			    
				if(pos1.w * value > 0){
			    	pos1.set(point.x, point.y, point.z, value);
				} else {
			    	pos2.set(point.x, point.y, point.z, value);
				}
		    }
		}
		
		return point;
    }

	public static double angle(Vector3d p0, Vector3d p1, Vector3d p2) {
		
		//����� �������
		double v1[] = new double[] {p1.x-p0.x, p1.y-p0.y, p1.z-p0.z};   //Vec p0 -> p1
		double v2[] = new double[] {p2.x-p0.x, p2.y-p0.y, p2.z-p0.z};   //Vec p0 -> p2
		double v3[] = new double[] {p2.x-p1.x, p2.y-p1.y, p2.z-p1.z};	  //Vec p1 ->p2
		
		//����� �����
		double dot1 =   v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2];
		double dot2 = -(v1[0]*v3[0] + v1[1]*v3[1] + v1[2]*v3[2]);
		double dot3 =   v3[0]*v2[0] + v3[1]*v2[1] + v3[2]*v2[2];
		
		double dot = dot1;
		
		if(dot < dot2) {
		    dot = dot2;
		}
		
		if(dot < dot3) {
		    dot = dot3;
		}
		
		return (double)Math.acos((double)dot);
}
}

