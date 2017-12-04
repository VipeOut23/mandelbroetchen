package com.github.vipeout23.mandelbroetchen.main;

public class Mandelfunctions {
	public static class ComplexNumber {
		double realPart;
		double imaginaryPart;
		public ComplexNumber() {}
		public ComplexNumber(double realPart, double imaginaryPart) {
			this.realPart = realPart;
			this.imaginaryPart = imaginaryPart;
		}
		public ComplexNumber add(ComplexNumber c) {
			return new ComplexNumber(realPart+c.realPart, imaginaryPart+c.imaginaryPart);
		}
		public ComplexNumber sub(ComplexNumber c) {
			return new ComplexNumber(realPart-c.realPart, imaginaryPart-c.imaginaryPart);
		}
		public ComplexNumber times(ComplexNumber c) {
			return new ComplexNumber(realPart*c.realPart - imaginaryPart*c.imaginaryPart,
									 realPart*c.imaginaryPart + imaginaryPart*c.realPart);
		}
		public String toString() {
			return String.format("%.16f + %.16fi", realPart, imaginaryPart);
		}
	}
	
	
	public static int getColorFromDivergeValue(double dv) {
		int r = (int) ((double)0x22*dv);
		int g = (int) ((double)0x33*dv);
		int b =	(int) ((double)0xFF*dv);
		return (r<<16) | (g<<8) | b;
	}
	
	public static int calculateIterationsAt(int x, int y, int xBound, int yBound, int iterations,
			ComplexNumber viewBoxEdge0, ComplexNumber viewBoxEdge2) {
		ComplexNumber c = translateCoordinatesToComplexNumber(x, y, xBound, yBound, viewBoxEdge0, viewBoxEdge2);
		ComplexNumber c0 = c;			//Save start number
		
		int i = 0;						//Our Iteration index
		
		for(; i < iterations; i++) {
			if(!isInComplexPane(c)) break;	//Stop Iterating if the number is already out of our pane
			c = c.times(c).add(c0);			//Mandelbrot calculus
		}
		
		return i;	//Return the diverge value
	}
	
	public static boolean isInComplexPane(ComplexNumber c) {
		double dist = c.realPart*c.realPart + c.imaginaryPart*c.imaginaryPart; //Calculate the squared length
		return dist < 4; 		// 2 to the power of 2 is 4
	}
	
	private static ComplexNumber translateCoordinatesToComplexNumber(int x, int y, int xBound, int yBound,
			ComplexNumber viewBoxEdge0, ComplexNumber viewBoxEdge2) {
		ComplexNumber c = new ComplexNumber();
		
		double xRatio = (double)x / xBound;
		double yRatio = (double)y / yBound;
		
		c.realPart		= viewBoxEdge0.realPart + (Math.abs(viewBoxEdge2.realPart-viewBoxEdge0.realPart)*xRatio);
		c.imaginaryPart	= viewBoxEdge0.imaginaryPart - (Math.abs(viewBoxEdge2.imaginaryPart-viewBoxEdge0.imaginaryPart)*yRatio);
		
		return c;
	}
}
