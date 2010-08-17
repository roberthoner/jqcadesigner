package jqcadesigner;

import java.io.File;
import java.util.Scanner;

import jqcadesigner.engines.BistableEngine;
import jqcadesigner.engines.Engine;

// TODO: create configuration class that allows you to load a config file.
// TODO: make it so that Engine will accept a config file name as an option and will load the file as part of its constructor.
// TODO: write out the Circuit class so that circuits can be loaded and create all the objects needed for it
// TODO: 
public class JQCADesigner
{
	public static String[] VALID_ENGINES = { "bistable" };
	
	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		Arguments userArgs = handleArgs( args );
		
		Circuit circuit = new Circuit( userArgs.circuitFileName );
		
		if( userArgs.commandLineMode )
		{	
			Engine engine = null;
			
			if( userArgs.engineName == "bistable" )
			{
				engine = new BistableEngine( circuit, System.out, userArgs.engineConfigFileName );
			}
			else
			{
				System.err.println( "Invalid engine name: " + userArgs.engineName );
				System.exit( 1 );
			}
			
			Engine.RunResults results = engine.run( userArgs.verbose );
			
			results.printStats();
		}
		else
		{
			System.err.println( "The GUI version of JQCADesigner has not be implemented." );
		}
	}
	
	public static Arguments handleArgs( String[] args )
	{
		Arguments retval = new Arguments();
		
		// Define default arguments.
		retval.engineName = "bistable";
		retval.numberOfSimulations = 1;
		retval.radialTolerance = 0.0f;
		
		// Parse out the arguments.
		for( int i = 1; i < args.length; ++i )
		{
			if( args[i] == "-f" && ++i < args.length )
			{				
				if( !UtilityFunctions.doesFileExist( args[i] ) )
				{
					System.err.println( "The circuit file <"+args[i]+"> does not exist." );
					System.exit( 1 );
				}
				
				retval.circuitFileName = args[i];
			}
			else if( args[i] == "-e" && ++i < args.length )
			{
				args[i] = args[i].toLowerCase();
				if( !isValidEngineName( args[i] ) )
				{
					System.err.println( "The engine name specified by option '-e' is not valid." );
					System.exit( 1 );
				}
				retval.engineName = args[i];
			}
			else if( args[i] == "-c" && ++i < args.length )
			{
				if( !UtilityFunctions.doesFileExist( args[i] ) )
				{
					System.err.println( "The engine config file <"+args[i]+"> does not exist." );
					System.exit( 1 );
				}
				
				retval.engineConfigFileName = args[i];
			}
			else if( args[i] == "-n" && ++i < args.length )
			{
				try
				{
					retval.numberOfSimulations = Integer.parseInt( args[i] );
					if( retval.numberOfSimulations < 1 )
					{
						System.err.println( "The number of simulations specified by option '-n' must be greater than 0.");
					}
				}
				catch( NumberFormatException ex )
				{
					System.err.println( "The number of simulations specified by option '-n' must be an integer" );
					System.exit( 1 );
				}
			}
			else if( args[i] == "-t" && ++i < args.length )
			{
				try
				{
					retval.radialTolerance = Float.parseFloat( args[i] );
					
					if( retval.radialTolerance < 0 )
					{
						System.err.println( "The radial tolerance specified by '-t' must be greater than or equal to 0." );
						System.exit( 1 );
					}
				}
				catch( NumberFormatException ex )
				{
					System.err.println( "The radial tolerance specified by '-t' must be a floating-point number." );
					System.exit( 1 );
				}
			}
			else if( args[i] == "-vt" && ++i < args.length )
			{
				if( !UtilityFunctions.doesFileExist( args[i] ) )
				{
					System.err.println( "The vector table file <"+args[i]+"> does not exist." );
					System.exit( 1 );
				}
				
				retval.vectorTableFileName = args[i];
			}
		}

		if( !retval.isSufficient() )
		{
			System.err.println( "Insufficient arguments passed." );
			usage( args[0] );
			System.exit( 1 );
		}
		
		return retval;
	}

	public static boolean isValidEngineName( String engineName )
	{
		for( String validName : JQCADesigner.VALID_ENGINES )
		{
			if( validName == engineName )
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
	
	/**
	 * Stores the arguments passed from the command line.
	 * @author Robert
	 */
	public static class Arguments
	{
		public boolean commandLineMode = true;
		public boolean verbose = true;
		public String circuitFileName;
		public String engineName;
		public String engineConfigFileName;
		public int numberOfSimulations;
		public float radialTolerance;
		public String vectorTableFileName;
		
		/**
		 * Determines whether or not the current set of arguments is sufficient to execute a run.
		 */
		public boolean isSufficient()
		{
			if(	circuitFileName != null		&&
				engineName != null			&&
				numberOfSimulations > 0		&&
				radialTolerance >= 0
				)
			{
				return true;
			}
			
			return false;
		}
	}
}
