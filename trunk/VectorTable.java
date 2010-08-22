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

package jqcadesigner;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Robert
 */
public final class VectorTable
{
	private boolean[] active;
	private boolean[][] inputs;

	public VectorTable( String vectorTableFile )
		throws FileNotFoundException, IOException, ParseException
	{
		_load( vectorTableFile );
	}

	public void setInputs( boolean[][] inputs ) throws Exception
	{
		if( inputs == null )
		{
			throw new Exception( "Can't set values to null." );
		}
		else if( inputs[0].length < active.length )
		{
			throw new Exception( "Not enough input vectors." );
		}

		this.inputs = inputs;
	}

	private void _load( String vectorTableFile )
		throws FileNotFoundException, IOException, ParseException
	{
		FileReader fileReader = new FileReader( vectorTableFile );
		BufferedReader in = new BufferedReader( fileReader );

		if( !in.ready() )
		{
			throw new IOException();
		}

		_parseFile( in );

		in.close();
	}

	private void _parseFile( BufferedReader in )
		throws IOException, ParseException
	{
		_parseMagicString( in );

		// Get the active vector
		// This tells us which inputs are activated.
		boolean[] activeVector = _parseActiveVector( in );
		active = activeVector;

		// This will hold the input vectors as we find them.
		ArrayList<boolean[]> vectors = new ArrayList<boolean[]>();

		boolean[] currentInputVector;
		while( (currentInputVector = _parseVector( in )) != null )
		{
			if( currentInputVector.length != activeVector.length )
			{
				throw new ParseException( "There must be exactly one vector for every input." );
			}

			vectors.add(  currentInputVector );
		}
		
		vectors.toArray( inputs );
	}

	private void _parseMagicString( BufferedReader in )
		throws IOException, ParseException
	{
		String line = in.readLine().trim();

		if( !line.equals( "%%VECTOR TABLE%%" ) )
		{
			throw new ParseException( "File must start with '%%VECTOR TABLE%%'." );
		}
	}

	private boolean[] _parseActiveVector( BufferedReader in )
		throws IOException, ParseException
	{
		boolean[] activeVector = _parseVector( in );

		if( activeVector == null )
		{
			throw new ParseException( "No active vector found." );
		}

		return activeVector;
	}

	private boolean[] _parseVector( BufferedReader in ) throws IOException
	{
		String line = _getNextNonComment( in );

		if( line == null )
		{
			return null;
		}

		int vectorLength = line.length();

		boolean[] vector = new boolean[vectorLength];

		for( int i = 0; i < vectorLength; ++i )
		{
			vector[i] = (line.charAt( i ) == '1' ? true : false);
		}

		return vector;
	}

	private String _getNextNonComment( BufferedReader in ) throws IOException
	{
		String line = null;

		while( (line = in.readLine().trim()) != null && _isComment( line ) );

		return line;
	}

	private boolean _isComment( String line )
	{
		return line.charAt( 0 ) == '#';
	}

	public static class ParseException extends Exception
	{
		public ParseException( String msg )
		{
			super( msg );
		}
	}
}
