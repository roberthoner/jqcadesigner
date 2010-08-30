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

/**
 *
 * @author Robert Honer <rhoner@cs.ucla.edu>
 */
public class DataTrace
{
	private String		_name;
	private double[]	_data;
	private int			_index;

	public DataTrace( String name )
	{
		this( name, 0 );
	}

	public DataTrace( String name, int size )
	{
		if( name == null )
		{
			throw new IllegalArgumentException( "DataTrace can't have a null name." );
		}

		if( size < 0 )
		{
			throw new IllegalArgumentException( "DataTrace can't have a negative size." );
		}

		_name = name;
		_data = new double[ size ];
		_index = 0;
	}

	public String getName()
	{
		return _name;
	}

	/**
	 * Sets the size for the DataTrace, clearing all of the previous values.
	 * @param size
	 */
	public void setSize( int size )
	{
		if( size < 0 )
		{
			String msg = "DataTrace can't have a negative size.";
			throw new IllegalArgumentException( msg );
		}

		_data = new double[ size ];
		_index = 0;
	}

	public void resetIndex()
	{
		_index = 0;
	}

	public int getIndex()
	{
		return _index;
	}

	public void setIndex( int index )
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

	public void addNext( double value )
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

	public double get( int index )
	{
		if( index < 0 || index >= _data.length )
		{
			String msg = "Invalid DataTrace index: " + index;
			throw new IndexOutOfBoundsException( msg );
		}

		return _data[ index ];
	}

	public void set( int index, double value )
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
}
