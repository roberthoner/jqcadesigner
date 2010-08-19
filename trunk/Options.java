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
