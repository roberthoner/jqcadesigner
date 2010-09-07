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
import jqcadesigner.circuit.Circuit;

/**
 *
 * @author Robert Honer <rhoner@cs.ucla.edu>
 */
public abstract class Cell
{
	public static enum Mode { VERTICAL, CROSSOVER, NORMAL }
	public static enum Function { NORMAL, OUTPUT, INPUT, FIXED }

	public final Mode mode;
	public final Function function;
	public final byte clockNum;
	public final double xCoord;
	public final double yCoord;
	public final double dotDiameter;
	public final int layerNum;
	public final QuantumDot[] dots;

	protected final Circuit _circuit;

	private double _polarization;
	
	/**
	 * Whether or not the dots should be updated when setPolarization is called.
	 * 
	 * If false, the QuantumDots will not be updated when setPolarization is
	 * called. This can be used to improve performance when no QuantumDot
	 * specific information is needed.
	 */
	private boolean _updateDots;

	private TickHandler _tickHandler;

	public Cell( Circuit cir, Mode m, Function f, byte c, double x, double y, double dd, int ln, QuantumDot[] d )
	{
		assert m != null && f != null && dd > 0 && d != null && d.length == 4;
		assert d[0] != null && d[1] != null && d[2] != null && d[3] != null;

		_circuit = cir;
		mode = m;
		function = f;
		clockNum = c;
		xCoord = x;
		yCoord = y;
		dotDiameter = dd;
		layerNum = ln;

		dots = d;
		
		_polarization = _calcPolarization();

		_updateDots = false;
	}

	public void setUpdateDots( boolean value )
	{
		_updateDots = value;
	}

	public final void setTickHandler( TickHandler tickHandler )
	{
		_tickHandler = tickHandler;
	}

	public void setPolarization( double polarization )
	{
		if( polarization < -1.0 || polarization > 1.0 )
		{
			String msg = "A cells polarization must be between -1.0 and 1.0.";
			throw new IllegalArgumentException( msg );
		}

		if( _updateDots )
		{
			// TODO: Make it modify the actual dots.
		}

		_polarization = polarization;
	}

	public double getPolarization()
	{
		return _polarization;
	}

	private double _calcPolarization()
	{
		double p	= ((dots[0].charge + dots[2].charge)
					- (dots[1].charge + dots[3].charge))
					* JQCADConstants.ONE_OVER_FOUR_HALF_QCHARGE;

		return p;
	}

	public double tick()
	{
		if( _tickHandler == null )
		{
			String msg = "This cell doesn't have a tick handler set.";
			throw new RuntimeException( msg );
		}

		double newPol = _tickHandler.tick();

		setPolarization( newPol );

		return newPol;
	}

	/**
	 * Reset the cell.
	 */
	public void reset()
	{
		// We don't need to do anything for a normal cell... at least I don't think :)
	}

	/**
	 *
	 */
	public static abstract class TickHandler
	{
		protected final Cell _cell;

		public TickHandler( Cell cell )
		{
			_cell = cell;
		}

		/**
		 * Advance the cells polarization.
		 *
		 * @return
		 */
		public abstract double tick();
	}
}
