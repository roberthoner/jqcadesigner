/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
