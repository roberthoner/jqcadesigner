/*
 *  Copyright (c) 2010 Robert Honer <rhoner@cs.ucla.edu>
 *  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the <organization> nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package jqcadesigner.circuit.units;

import jqcadesigner.JQCADConstants;

/**
 *
 * @author Robert Honer <rhoner@cs.ucla.edu>
 */
public abstract class Cell
{
	public static enum Mode { VERTICAL, CROSSOVER, NORMAL }
	public static enum Function { NORMAL, OUTPUT, INPUT, FIXED }
	
	/**
	 * Allows additional information to be added to the cell.
	 * 
	 * If a simulation engine wants to be able to associate information specific
	 * to itself in a cell, it can extend CellInfo and place whatever information
	 * it wants into this cell's info field.
	 */
	public static abstract class CellInfo {}

	public final Mode mode;
	public final Function function;
	public final byte clock;
	public final double xCoord;
	public final double yCoord;
	public final double dotDiameter;
	public final int layerNum;

	/**
	 * Additional information can be added here by simulation engines.
	 */
	public CellInfo info;

	public final QuantumDot[] dots;

	public Cell( Mode m, Function f, byte c, double x, double y, double dd, int ln, QuantumDot[] d )
	{
		assert m != null && f != null && dd > 0 && d != null && d.length == 4;
		assert d[0] != null && d[1] != null && d[2] != null && d[3] != null;
		
		mode = m;
		function = f;
		clock = c;
		xCoord = x;
		yCoord = y;
		dotDiameter = dd;
		layerNum = ln;

		dots = d;
	}

	public double calcPolarization()
	{
		double p	= ((dots[0].charge + dots[2].charge)
					- (dots[1].charge + dots[3].charge))
					* JQCADConstants.ONE_OVER_FOUR_HALF_QCHARGE;

		return p;
	}
}
