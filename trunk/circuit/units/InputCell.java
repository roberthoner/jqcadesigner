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

import java.io.FileNotFoundException;
import jqcadesigner.circuit.DataTrace;

/**
 *
 * @author Robert Honer <rhoner@cs.ucla.edu>
 */
public class InputCell extends Cell
{
	private final DataTrace _inputValues;

	/**
	 * Whether or not this input cell is active. If not, it should function as a normal cell.
	 */
	public boolean active;

	public InputCell( Mode m, byte c, double x, double y, double dd, int ln, QuantumDot[] d )
	{
		super( m, Function.INPUT, c, x, y, dd, ln, d );

		_inputValues = new DataTrace( "Input" );
		active = true;
	}

	public void setValues( boolean[] values, int granularity )
	{
		if( values == null )
		{
			String msg = "InputCell values can't be null.";
			throw new IllegalArgumentException( msg );
		}

		final int valueCount = values.length;

		if( granularity < valueCount )
		{
			String msg = "Granularity must be at least equal to the number of values.";
			throw new IllegalArgumentException( msg );
		}

		final int ticksPerValue = granularity / valueCount;

		// The remainder of the above integer division.
		int excessTicks = granularity - (ticksPerValue * valueCount);
		
		// Says how often an extra tick should be inserted, so as to make up for
		// the excess ticks that don't fit evenly into the granularity. This
		// distributes the excess ticks more evenly.
		final int extraInsertFreq = excessTicks > 0 ? valueCount / excessTicks
									: 0;

		_inputValues.setSize( granularity );

		for( int i = 0; i < valueCount; ++i )
		{
			double crtValue = values[i] ? 1.0 : -0.1;

			for( int j = ticksPerValue; j > 0; --j )
			{
				_inputValues.addNext( crtValue );
			}

			if( extraInsertFreq != 0 && excessTicks-- > 0 && i % extraInsertFreq == 0 )
			{
				// Add an extra here to make sure that we completely fill up the
				// DataTrace.
				_inputValues.addNext( crtValue );
			}
		}
	}

	public void outputCSV( String fileName ) throws FileNotFoundException
	{
		_inputValues.outputCSV( fileName );
	}

	@Override
	public void reset()
	{
		_inputValues.resetIndex();
	}

	/**
	 * Advance the cell's, returning it's new polarization.
	 *
	 * @return The cell's new polarization.
	 */
	@Override
	public double tick()
	{
		double retval;

		if( active )
		{
			if( !_inputValues.hasNext() )
			{
				_inputValues.resetIndex();
			}

			retval = _inputValues.getNext();

			setPolarization( retval );
		}
		else
		{
			retval = super.tick();
		}

		return retval;
	}
}
