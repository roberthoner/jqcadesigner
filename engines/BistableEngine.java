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
import java.text.ParseException;

import jqcadesigner.circuit.Circuit;
import jqcadesigner.VectorTable;
import jqcadesigner.config.ConfigFile;
import jqcadesigner.config.syntaxtree.Section;
import jqcadesigner.config.syntaxtree.SettingsSection;

public final class BistableEngine extends Engine
{
	public static final class DefaultConfig
	{
		public static final int		NUMBER_OF_SAMPLES			= 12500;
		public static final double	CONVERGENCE_TOLERANCE		= 0.001;
		public static final double	RADIUS_OF_EFFECT			= 65.00;
		public static final double	EPSILON_R					= 12.9;
		public static final double	CLOCK_HIGH					= 9.8e-22;
		public static final double	CLOCK_LOW					= 3.8e-23;
		public static final double	CLOCK_SHIFT					= 0;
		public static final double	CLOCK_AMPLITUDE_FACTOR		= 2;
		public static final int		MAX_ITERATIONS_PER_SAMPLE	= 15;
		public static final double	LAYER_SEPARATION			= 10;
		public static final boolean	RANDOMIZE_CELLS				= true;
	}

	private final int		_numberOfSamples;
	private final double	_convergenceTolerance;
	private final double	_radiusOfEffect;
	private final double	_epsilonR;
	private final double	_clockHigh;
	private final double	_clockLow;
	private final double	_clockShift;
	private final double	_clockAmplitudeFactor;
	private final int		_maxIterationsPerSample;
	private final double	_layerSeparation;
	private final boolean	_randomizeCells;

	private final SettingsSection _configSect;

	public BistableEngine( Circuit circuit )
		throws	FileNotFoundException, IOException, ConfigFile.ParseException,
				EngineException
	{
		this( circuit, null );
	}
	
	public BistableEngine( Circuit circuit, String configFileName )
		throws	FileNotFoundException, IOException, ConfigFile.ParseException,
				EngineException
	{
		super( circuit, configFileName );

		if( _configFile == null )
		{
			// This tells _loadConfigValue to use the default value.
			_configSect = null;
		}
		else
		{
			Section section = _configFile.get( "BISTABLE_OPTIONS" ).get( 0 );

			if( section == null || !section.hasSettings() )
			{
				String msg = "Bistable engine config file needs settings.";
				throw new EngineException( msg );
			}

			// This allows _loadConfigValue to see the values specified in the
			// config file.
			_configSect = (SettingsSection)section;
		}
		
		// Load the config settings.
		_numberOfSamples =
			(Integer)_loadConfigValue(	"number_of_samples",
										DefaultConfig.NUMBER_OF_SAMPLES );
		_convergenceTolerance =
			(Double)_loadConfigValue(	"convergence_tolerance",
										DefaultConfig.CONVERGENCE_TOLERANCE );
		_radiusOfEffect =
			(Double)_loadConfigValue(	"radius_of_effect",
										DefaultConfig.RADIUS_OF_EFFECT );
		_epsilonR =
			(Double)_loadConfigValue(	"epsilonR",
										DefaultConfig.EPSILON_R );
		_clockHigh =
			(Double)_loadConfigValue(	"clock_high",
										DefaultConfig.CLOCK_HIGH );
		_clockLow =
			(Double)_loadConfigValue(	"clock_low",
										DefaultConfig.CLOCK_LOW );
		_clockShift =
			(Double)_loadConfigValue(	"clock_shift",
										DefaultConfig.CLOCK_SHIFT );
		_clockAmplitudeFactor =
			(Double)_loadConfigValue(	"clock_amplitude_factor",
										DefaultConfig.CLOCK_AMPLITUDE_FACTOR );
		_maxIterationsPerSample =
			(Integer)_loadConfigValue(	"max_iterations_per_sample",
										DefaultConfig.MAX_ITERATIONS_PER_SAMPLE );
		_layerSeparation =
			(Double)_loadConfigValue(	"layer_separation",
										DefaultConfig.LAYER_SEPARATION );
		_randomizeCells =
			(Boolean)_loadConfigValue(	"randomize_cells",
										DefaultConfig.RANDOMIZE_CELLS );
	}

	/**
	 * Attempts to load a configuration value, defaulting to defaultValue if it can't.
	 *
	 * This is a helper method that is called by the constructor.  It is not
	 * intended to called elsewhere.  If you need access to the loaded settings
	 * you should use the private final fields which are initialized in the
	 * constructor.
	 *
	 * @param key The config name.
	 * @param defaultValue The default config value.
	 * @return An object containing the value of the config name.
	 */
	private Object _loadConfigValue( String key, Object defaultValue )
	{
		assert key != null;

		Object retval;
		String value;

		if( _configSect != null && (value = _configSect.settings.get( key )) != null )
		{
			if( defaultValue instanceof Integer )
			{
				retval = Integer.parseInt( value );
			}
			else if( defaultValue instanceof Double )
			{
				retval = Double.parseDouble( value );
			}
			else if( defaultValue instanceof Boolean )
			{
				retval = Boolean.parseBoolean( value );
			}
			else
			{
				retval = value;
			}
		}
		else
		{
			retval = defaultValue;
		}

		return retval;
	}

	@Override
	protected RunResults _run( VectorTable vectorTable )
	{
		assert vectorTable != null;

		RunResults retval = new RunResults();
		
		
		return retval;
	}
	
	public class RunResults extends Engine.RunResults
	{
		@Override
		public void printStats()
		{
			
		}
	}
}
