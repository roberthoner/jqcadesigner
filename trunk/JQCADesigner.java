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

import java.util.logging.Level;
import jqcadesigner.circuit.Circuit;
import java.io.File;
import java.util.logging.Logger;

import jqcadesigner.engines.BistableEngine;
import jqcadesigner.engines.Engine;

// Todos for Alpha:
// TODO: implement bistable engine.
// TODO: implement a class that allows easily reading engine results and output values.
// TODO: make it so if no vector table file is specified, it automatically loads an exhaustive vector table.
public class JQCADesigner
{
	public static final String		PROGRAM_NAME = "JQCADesigner";
	public static final String		PROGRAM_VERSION = "0.1a";
	public static final String[][]	PROGRAM_AUTHORS = { {"Robert Honer", "rhoner@ucla.edu"} };
	public static final String		PROGRAM_LICENSE = "BSD";


	public static final String[]	VALID_ENGINES = { "bistable" };

	public static final Logger	log;
	public static final Options	options = new Options();

	static
	{
		log = Logger.getLogger( JQCADesigner.class.getName() );

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
		catch( Exception ex )
		{
			log.severe( ex.getMessage() );
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

		if( (Boolean)options.get( "--clm" ) )
		{
			enterCommandLineMode();
		}
		else
		{
			enterGUIMode();
		}
	}

	public static void enterCommandLineMode()
	{
		Circuit circuit				= null;
		VectorTable vectorTable		= null;
		Engine engine				= null;

		String circuitFile			= (String)options.get( "-f" );
		String vectorTableFile		= (String)options.get( "--vt" );
		String engineName			= (String)options.get( "-e" );
		String engineConfigFileName	= ((String)options.get( "-c" )).equals( "" )
									? null : (String)options.get( "-c" );



		try
		{
			log.log( Level.INFO, "Loading circuit from <{0}>.", circuitFile );

			circuit = new Circuit( circuitFile );

			if( !vectorTableFile.equals( "" ) )
			{
				log.log( Level.INFO, "Loading vector table from <{0}>.", vectorTableFile );
				vectorTable = new VectorTable( vectorTableFile );
			}
			else
			{
				vectorTable = null;
			}

			if( engineName.equals( "bistable" ) )
			{
				engine = new BistableEngine(	circuit,
												engineConfigFileName );
			}
			else
			{
				log.log( Level.SEVERE, "Invalid engine name: {0}", engineName );
				System.exit( 3 );
			}

			Engine.RunResults results;
			results = engine.run( vectorTable );
			results.printStats();
		}
		catch( Exception ex )
		{
			log.severe( ex.toString() );
			ex.printStackTrace();
		}
	}

	public static void enterGUIMode()
	{
		System.err.println( "GUI mode is not implemented." );
	}

	public static void handleArgs( String[] args ) throws Exception
	{
		assert args != null && options != null;
		
		options.parseArgs( args );

		String circuitFile = (String)options.get( "-f" );
		String engineName = (String)options.get( "-e" );

		if( circuitFile.equals( "" ) )
		{
			throw new Exception( "A circuit file must be specified." );
		}

		if( !(new File( circuitFile )).isFile() )
		{
			String msg = "The specified circuit must exist and be a file.";
			throw new Exception( msg );
		}

		if( !isValidEngineName( engineName ) )
		{
			throw new Exception( "Invalid engine name: " + engineName );
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
	
	public static void usage()
	{
		String programName = "JQCADesigner";

		System.out.printf( "%s v%s by\n", PROGRAM_NAME, PROGRAM_VERSION );

		for( String[] author : PROGRAM_AUTHORS )
		{
			System.out.printf( "  %s <%s>\n", author[0], author[1] );
		}

		System.out.println(
				"\nUsage: "+programName+" -f circuit_file -e engine_name "
				+ "[-c engine_config_file] [-n number_of_simulations] "
				+ "[-t radial_tolerance] [-vt vector_table_file]"
			);
	}
}
