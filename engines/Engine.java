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

package jqcadesigner.engines;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;
import jqcadesigner.JQCADesigner;

import jqcadesigner.circuit.Circuit;
import jqcadesigner.VectorTable;
import jqcadesigner.circuit.Circuit.CircuitException;
import jqcadesigner.circuit.DataTrace;
import jqcadesigner.config.ConfigFile;
import jqcadesigner.config.ConfigFile.ParseException;

public abstract class Engine
{
	protected final Circuit _circuit;
	protected final ConfigFile _configFile;
	protected static final Logger _log = JQCADesigner.log;

	public Engine( Circuit circuit )
		throws FileNotFoundException, IOException, ParseException
	{
		this( circuit, null );
	}
	
	public Engine( Circuit circuit, String configFileName )
		throws FileNotFoundException, IOException, ParseException
	{
		assert(circuit != null);

		_circuit = circuit;

		if( configFileName != null )
		{
			_configFile = new ConfigFile( configFileName );
		}
		else
		{
			_configFile = null;
		}
	}

	public RunResults run( VectorTable vectorTable ) throws CircuitException
	{
		RunResults retval;

		// Initialize the engine.
		long initTime = System.currentTimeMillis();
		_init( vectorTable );
		initTime = System.currentTimeMillis() - initTime;

		// Run the engine.
		long runTime = System.currentTimeMillis();
		retval = _run( vectorTable );
		runTime = System.currentTimeMillis() - runTime;

		// Update the results.
		retval.initTime = initTime;
		retval.runTime = runTime;

		return retval;
	}

	abstract protected void _init( VectorTable vectorTable );
	abstract protected RunResults _run( VectorTable vectorTable );
	
	public abstract class RunResults
	{
		public long initTime;
		public long runTime;
		protected final HashMap<String, byte[]> _outputValues;
		protected final HashMap<String, DataTrace> _outputTraces;

		public RunResults()
		{
			_outputValues = new HashMap<String, byte[]>();
			_outputTraces = new HashMap<String, DataTrace>();
		}

		public String[] getOutputNames()
		{
			Set<String> keys = _outputValues.keySet();

			return keys.toArray( new String[ keys.size() ] );
		}

		public byte[] getOutputValues( String outputName )
		{
			return _outputValues.get( outputName );
		}

		public DataTrace getOutputTrace( String outputName )
		{
			return _outputTraces.get( outputName );
		}

		abstract public void printStats();
	}

	public class EngineException extends Exception
	{
		public EngineException( String msg )
		{
			super( msg );
		}	
	}
}
