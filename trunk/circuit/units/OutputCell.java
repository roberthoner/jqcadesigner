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
import java.util.ArrayList;
import jqcadesigner.circuit.DataTrace;

/**
 *
 * @author Robert Honer <rhoner@cs.ucla.edu>
 */
public class OutputCell extends Cell
{
	/**
	 * Stores the current and previous polarizations for this cell.
	 */
	private final DataTrace _valueCache;

	public OutputCell( Mode m, byte c, double x, double y, double dd, int ln, QuantumDot[] d )
	{
		super( m, Function.OUTPUT, c, x, y, dd, ln, d );

		_valueCache = new DataTrace( "Output" );
	}
	
	public void setValueCacheSize( int size )
	{
		_valueCache.setSize( size );
	}

	public void setName( String name )
	{
		_valueCache.name = name;
	}
	
	public String getName()
	{
		return _valueCache.name;
	}

	public void plotPolarization()
	{
		if( !_valueCache.hasNext() )
		{
			String msg	= "Output cell " + _valueCache.name
						+ " is out of cache space.";

			throw new RuntimeException( msg );
		}

		_valueCache.addNext( getPolarization() );
	}

	public void outputCSV( String fileName ) throws FileNotFoundException
	{
		_valueCache.outputCSV( fileName );
	}

	public byte[] getValues( Clock clock )
	{
		final double clockHigh = clock.clockHigh;
		final double clockLow = clock.clockLow;
		ArrayList<Byte> values = new ArrayList<Byte>();

		final int granularity = _valueCache.getSize();

		boolean valueSampled = false;
		clock.reset();
		for( int i = 0; i < granularity; ++i )
		{
			double crtClockValue = clock.tick();

			if( !valueSampled && crtClockValue < clockLow*1.001 )
			{
				valueSampled = true;
				byte value = -1;

				if( _valueCache.get( i ) > 0.9 )
				{
					value = 1;
				}
				else if( _valueCache.get( i ) < -0.9 )
				{
					value = 0;
				}

				values.add( value );
			}
			else if( valueSampled && crtClockValue > clockHigh*0.999 )
			{
				valueSampled = false;
			}
		}

		Byte[] b = values.toArray( new Byte[ values.size() ] );
		byte[] byteArray = new byte[ b.length ];
		for( int i = 0; i < byteArray.length; ++i )
		{
			byteArray[i] = b[i];
		}

		return byteArray;
	}

	@Override
	public void reset()
	{
		_valueCache.resetIndex();
	}
}