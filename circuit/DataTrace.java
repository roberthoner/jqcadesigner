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

package jqcadesigner.circuit;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 *
 * @author Robert Honer <rhoner@cs.ucla.edu>
 */
public class DataTrace
{
	public String		name;
	private double[]	_data;
	private int			_index;
	
	public DataTrace()
	{
		this( "", 0 );
	}

	public DataTrace( final String name )
	{
		this( name, 0 );
	}

	public DataTrace( final String n, final int size )
	{
		if( n == null )
		{
			throw new IllegalArgumentException( "DataTrace can't have a null name." );
		}

		if( size < 0 )
		{
			throw new IllegalArgumentException( "DataTrace can't have a negative size." );
		}

		name = n;
		_data = new double[ size ];
		_index = 0;
	}

	/**
	 * Sets the size for the DataTrace, clearing all of the previous values.
	 * @param size
	 */
	public void setSize( final int size )
	{
		if( size < 0 )
		{
			String msg = "DataTrace can't have a negative size.";
			throw new IllegalArgumentException( msg );
		}

		_data = new double[ size ];
		_index = 0;
	}

	public int getSize()
	{
		return _data.length;
	}

	public void resetIndex()
	{
		_index = 0;
	}

	public int getIndex()
	{
		return _index;
	}

	public void setIndex( final int index )
	{
		if( index < 0 || index >= _data.length )
		{
			String msg = "Invalid DataTrace index: " + index;
			throw new IndexOutOfBoundsException( msg );
		}

		_index = index;
	}

	public boolean hasNext()
	{
		return _index < _data.length;
	}

	public void addNext( final double value )
	{
		if( _index >= _data.length )
		{
			String msg = "No more room. Can't add another datum.";
			throw new RuntimeException( msg );
		}

		if( value < -1.0 || value > 1.0 )
		{
			String msg = "DataTrace values must be between -1.0 and 1.0.";
			throw new RuntimeException( msg );
		}

		_data[ _index++ ] = value;
	}

	public double getNext()
	{
		if( _index >= _data.length )
		{
			String msg = "No more datums. Can't get another datum.";
			throw new RuntimeException( msg );
		}

		return _data[ _index++ ];
	}

	public double get( final int index )
	{
		if( index < 0 || index >= _data.length )
		{
			String msg = "Invalid DataTrace index: " + index;
			throw new IndexOutOfBoundsException( msg );
		}

		return _data[ index ];
	}

	public void set( final int index, final double value )
	{
		if( index < 0 || index >= _data.length )
		{
			String msg = "Invalid DataTrace index: " + index;
			throw new IndexOutOfBoundsException( msg );
		}

		if( value < -1.0 || value > 1.0 )
		{
			String msg = "DataTrace values must be between -1.0 and 1.0.";
			throw new RuntimeException( msg );
		}

		_data[ index ] = value;
	}

	public void outputCSV( final String fileName ) throws FileNotFoundException
	{
		PrintStream ps = new PrintStream( fileName );

		for( int i = 0; i < _data.length; ++i )
		{
			ps.printf( "%d,%s\n", i+1, _data[i] );
		}
	}
}
