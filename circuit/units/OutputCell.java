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

	/**
	 * Sets the cell's polarization and adds the value to its cache.
	 * @param polarization
	 */
	@Override
	public void setPolarization( double polarization )
	{
		super.setPolarization( polarization );

		if( !_valueCache.hasNext() )
		{
			String msg	= "Output cell " + _valueCache.getName()
						+ " is out of cache space.";

			throw new RuntimeException( msg );
		}

		_valueCache.addNext( polarization );
	}

	@Override
	public void reset()
	{
		_valueCache.resetIndex();
	}
}