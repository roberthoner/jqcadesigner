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

import jqcadesigner.circuit.DataTrace;

/**
 *
 * @author Robert Honer <rhoner@cs.ucla.edu>
 */
public class Clock
{
	public final int	number;
	public final int	cycles;
	public final int	granularity;
	public final double	clockLow;
	public final double	clockHigh;
	public final double	amplitudeFactor;
	public final double	clockShift;
	
	private final DataTrace _trace;

	private double _crtValue;

	public Clock( int n, int c, int g, double cLow, double cHigh, double af, double cs )
	{
		if( n < 0 || n > 3 )
		{
			throw new IllegalArgumentException( "Clock number must be between 0 and 3." );
		}
		
		if( c <= 0 )
		{
			throw new IllegalArgumentException( "Clock must be at least one cycle." );
		}

		if( g <= 0 )
		{
			throw new IllegalArgumentException( "Granularity must be greater than 0. " );
		}

		number			= n;
		cycles			= c;
		granularity		= g;
		clockLow		= cLow;
		clockHigh		= cHigh;
		amplitudeFactor	= af;
		clockShift		= (cHigh + cLow) / 2 + cs;

		_trace = new DataTrace( "Clock " + n, g );

		_fillTrace();
	}

	private void _fillTrace()
	{
		assert number >=0 && number <= 3;
		assert cycles > 0;
		assert granularity > 0;
		assert _trace != null;

		final DataTrace trace = _trace;
		final double twoPiCyclesOverGranularity = (2 * Math.PI * cycles) / granularity;

		final double clockPrefactor = (clockHigh - clockLow) * amplitudeFactor;
		final double clockShiftLocal = clockShift;

		final double piNumberOverTwo = (Math.PI * number) / 2;

		for( int i = 0; i < granularity; ++i )
		{
			double crtValue;
			crtValue = clockPrefactor * Math.cos( i * twoPiCyclesOverGranularity - piNumberOverTwo ) + clockShiftLocal;

			if( crtValue > clockHigh )
			{
				crtValue = clockHigh;
			}
			else if( crtValue < clockLow )
			{
				crtValue = clockLow;
			}

			trace.set( i, crtValue );
		}
	}

	public void reset()
	{
		_trace.resetIndex();
	}

	/**
	 * Advance the clock one tick, returning its new value.
	 *
	 * @return The clock's new value.
	 */
	public double tick()
	{
		if( !_trace.hasNext() )
		{
			_trace.resetIndex();
		}

		return _crtValue = _trace.getNext();
	}

	/**
	 * Check the clocks value without advancing it.
	 *
	 * @return The clock's current value.
	 */
	public double check()
	{
		return _crtValue;
	}
}
