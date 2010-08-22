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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public final class Options extends HashMap<String, Object>
{	
	Options()
	{
		super();
	}

	/**
	 *
	 * @param optionKey
	 * @param defaultValue
	 * @throws jqcadesigner.Options.DuplicateOptionKeyException
	 */
	public void addOption( String optionKey, Object defaultValue )
		throws DuplicateOptionKeyException, InvalidTypeException
	{
		if( containsKey( optionKey ) )
		{
			throw new DuplicateOptionKeyException( optionKey );
		}

		if( !_validType( defaultValue ) )
		{
			throw new InvalidTypeException( defaultValue.getClass() );
		}

		put( optionKey, defaultValue );
	}

	private boolean _validType( Object value )
	{
		Class valueType = value.getClass();

		if( valueType == String.class	||
			valueType == Integer.class	||
			valueType == Double.class	||
			valueType == Boolean.class )
		{
			return true;
		}

		return false;
	}

	public void parseArgs( String[] args ) throws ParseException
	{
		Queue<String> argsQueue = new LinkedList<String>();
		argsQueue.addAll( Arrays.asList( args ) );
		
		String curArg;
		while( (curArg = argsQueue.poll()) != null )
		{
			if( curArg.charAt( 0 ) == '-' && containsKey( curArg ) )
			{
				String nextArg = argsQueue.poll();

				_parseArg( curArg, nextArg );
			}
		}
	}

	private void _parseArg( String optionKey, String optionValue )
		throws ParseException
	{
		if( optionKey == null )
		{
			throw new ParseException( "The option key can't be null." );
		}

		if( !containsKey( optionKey ) )
		{
			throw new ParseException( "Can't update a non-existent option." );
		}

		if( optionValue == null )
		{
			String msg = "The option '" + optionKey + "' requires an argument.";
			throw new ParseException( msg );
		}

		Class classType = get( optionKey ).getClass();

		if( classType == String.class )
		{
			put( optionKey, optionValue );
		}
		else if( classType == Integer.class )
		{
			put( optionKey, Integer.parseInt( optionValue ) );
		}
		else if( classType == Double.class )
		{
			put( optionKey, Double.parseDouble(  optionValue ) );
		}
		else if( classType == Boolean.class )
		{
			put( optionKey, Boolean.parseBoolean( optionValue ) );
		}
	}

	// Exceptions //
	public static class DuplicateOptionKeyException extends Exception
	{
		public DuplicateOptionKeyException( String optionKey )
		{
			super( "The tag '" + optionKey + "' is a duplicate." );
		}
	}

	public static class InvalidTypeException extends Exception
	{
		public InvalidTypeException( Class type )
		{
			super( "The '" + type.toString() + "' is an invalid option type." );
		}
	}

	public static class ParseException extends Exception
	{
		public ParseException( String msg )
		{
			super( msg );
		}
	}
}
