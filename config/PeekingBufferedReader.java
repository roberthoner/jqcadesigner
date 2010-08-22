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

package jqcadesigner.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * A BufferedReader that allows peeking at the next line without consuming it.
 *
 * @author Robert
 */
public class PeekingBufferedReader extends BufferedReader
{
	private String _peekedValue;

	public PeekingBufferedReader( Reader reader )
	{
		super( reader );
	}

	/**
	 * Reads a line and consumes it.
	 *
	 * @return The newly read line.
	 * @throws IOException
	 */
	@Override
	public String readLine() throws IOException
	{
		String retval;

		if( _peekedValue == null )
		{
			retval = super.readLine();
		}
		else
		{
			retval = _peekedValue;
			_peekedValue = null;
		}

		return retval;
	}

	/**
	 * A wrapper method for readability.
	 * @throws IOException
	 */
	public void consumeLine() throws IOException
	{
		readLine();
	}

	public String peekLine() throws IOException
	{
		String retval;

		if( _peekedValue == null )
		{
			retval = super.readLine();
			_peekedValue = retval;
		}
		else
		{
			retval = _peekedValue;
		}

		return retval;
	}
}
