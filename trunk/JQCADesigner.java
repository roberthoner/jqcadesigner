package jqcadesigner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import jqcadesigner.Options.DuplicateOptionKeyException;
import jqcadesigner.Options.InvalidTypeException;
import jqcadesigner.Options.ParseException;

import jqcadesigner.engines.BistableEngine;
import jqcadesigner.engines.Engine;

// TODO: create configuration class that allows you to load a config file.
// TODO: make it so that Engine will accept a config file name as an option and will load the file as part of its constructor.
// TODO: write out the Circuit class so that circuits can be loaded and create all the objects needed for it
// TODO: add logger
public class JQCADesigner
{
	public static String[] VALID_ENGINES = { "bistable" };

	public static final Options options = new Options();

	static
	{
		try
		{
			// The circuit file.
			options.addOption( "-f", "" );

			// The simulation engine.
			options.addOption( "-e", "bistable" );

			// The simulation engine configuration file.
			options.addOption( "-c", "" );

			// The number of simulations to run.
			options.addOption( "-n", 1 );

			// The radial tolerance.
			options.addOption( "-t", 0 );

			// The vector table file.
			options.addOption( "--vt", "" );
			
			// Whether or not to output verbosely.
			options.addOption( "--verbose", true );

			// Whether or not to run in command line mode.
			options.addOption( "--clm", true );
		}
		catch( DuplicateOptionKeyException ex )
		{
			Logger.getLogger( JQCADesigner.class.getName() ).log( Level.SEVERE, null, ex );
		}
		catch( InvalidTypeException ex )
		{
			Logger.getLogger( JQCADesigner.class.getName() ).log( Level.SEVERE, null, ex );
		}
	}

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		try
		{
			handleArgs( args );
		}
		catch( Exception ex )
		{
			System.err.println( ex.getMessage() );
			System.exit( 1 );
		}

		Circuit circuit = null;
		VectorTable vectorTable = null;

		try
		{
			circuit = new Circuit( (String)options.get( "-f" ) );

			if( !options.get( "--vt" ).equals( "" ) )
			{
				vectorTable = new VectorTable( (String)options.get( "--vt" ) );
			}
		}
		catch( Exception ex )
		{
			System.err.println( ex.getMessage() );
			System.exit( 2 );
		}

		boolean commandLineMode = (Boolean)options.get( "--clm" );
		if( commandLineMode )
		{
			Engine engine = null;
			
			String engineName = (String)options.get( "-e" );
			String engineConfigFileName = (String)options.get( "-c" );
			
			if( engineName.equals(  "bistable" ) )
			{
				engine = new BistableEngine(	circuit,
												System.out,
												engineConfigFileName );
			}
			else
			{
				System.err.println( "Invalid engine name: " + engineName );
				System.exit( 3 );
			}

			Engine.RunResults results;
			results = engine.run( vectorTable, (Boolean)options.get( "--verbose" ) );
			
			results.printStats();
		}
		else
		{
			System.err.println( "The GUI version of JQCADesigner has not been implemented." );
		}
	}
	
	public static void handleArgs( String[] args ) throws Exception
	{
		options.parseArgs( args );

		String circuitFile = (String)options.get( "-f" );
		String engineName = (String)options.get( "-e" );

		if( circuitFile.equals( "" ) )
		{
			throw new Exception( "A circuit file must be specified." );
		}

		if( !(new File( circuitFile )).isFile() )
		{
			String msg = "The specified circuit must exist and be a file";
			throw new Exception( msg );
		}

		if( !isValidEngineName( engineName ) )
		{
			throw new Exception( "Invalid engine name: '" + engineName );
		}
	}

	public static boolean isValidEngineName( String engineName )
	{
		for( String validName : JQCADesigner.VALID_ENGINES )
		{
			if( validName.equals( engineName ) )
			{
				return true;
			}
		}
		
		return false;
	}
	

	public static void usage( String programName )
	{
		System.out.println(
				"Usage: "+programName+" -f circuit_file -e engine_name " +
				"[-c engine_config_file] [-n number_of_simulations] [-t radial_tolerance] [-vt vector_table_file]"
		);
	}
}
