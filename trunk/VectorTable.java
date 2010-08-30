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
	public final boolean[] active;
	public final boolean[][] inputs;

	public VectorTable( String vectorTableFile )
		throws FileNotFoundException, IOException, ParseException
	{
		ArrayList<boolean[]> vectors = _load( vectorTableFile );

		// This first vector should be the active vector.
		active = vectors.remove( 0 );

		// The rest of the vectors contain the input vectors.
		// These vectors need to be split up and grouped by input. Currently,
		// they are grouped by time. The first vector contains the values of all
		// the inputs at time 0, the second contains the values of all the inputs
		// at time 1, and so on.
		final int inputCount = active.length;	// The number of input vectors.
		final int valueCount = vectors.size();	// The number of values per input vector.

		inputs = new boolean[ inputCount ][ valueCount ];
		for( int i = 0; i < valueCount; ++i )
		{
			// Contains the values for all the inputs at time i.
			boolean[] crtVector = vectors.get( i );

			for( int j = 0; j < inputCount; ++j )
			{
				inputs[j][i] = crtVector[j];
			}
		}
	}

	private ArrayList<boolean[]> _load( String vectorTableFile )
		throws FileNotFoundException, IOException, ParseException
	{
		assert vectorTableFile != null;

		FileReader fileReader = new FileReader( vectorTableFile );
		BufferedReader in = new BufferedReader( fileReader );

		
		ArrayList<boolean[]> vectors = _parseFile( in );

		in.close();

		return vectors;
	}

	private ArrayList<boolean[]> _parseFile( BufferedReader in )
		throws IOException, ParseException
	{
		assert in != null;

		_parseMagicString( in );

		// This will hold the vectors as we find them.
		// The first of which will be the active vector.
		ArrayList<boolean[]> vectors = new ArrayList<boolean[]>();

		// Get the active vector
		// This tells us which inputs are activated.
		boolean[] activeVector = _parseActiveVector( in );
		vectors.add( activeVector );

		boolean[] currentInputVector;
		while( (currentInputVector = _parseVector( in )) != null )
		{
			if( currentInputVector.length != activeVector.length )
			{
				throw new ParseException( "There must be exactly one vector for every input." );
			}

			vectors.add( currentInputVector );
		}
		
		return vectors;
	}

	private void _parseMagicString( BufferedReader in )
		throws IOException, ParseException
	{
		assert in != null;

		String line = in.readLine().trim();

		if( !line.equals( "%%VECTOR TABLE%%" ) )
		{
			throw new ParseException( "File must start with '%%VECTOR TABLE%%'." );
		}
	}

	private boolean[] _parseActiveVector( BufferedReader in )
		throws IOException, ParseException
	{
		assert in != null;

		boolean[] activeVector = _parseVector( in );

		if( activeVector == null )
		{
			throw new ParseException( "No active vector found." );
		}

		return activeVector;
	}

	private boolean[] _parseVector( BufferedReader in ) throws IOException
	{
		assert in != null;

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
		assert in != null;

		String line = null;

		while( (line = in.readLine()) != null && _isComment( line ) );
		
		return line;
	}

	private boolean _isComment( String line )
	{
		assert line != null;
		return line.trim().charAt( 0 ) == '#';
	}

	public static class ParseException extends Exception
	{
		public ParseException( String msg )
		{
			super( msg );
		}
	}
}
