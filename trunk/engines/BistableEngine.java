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

import ec.util.MersenneTwisterFast;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import jqcadesigner.JQCADConstants;
import java.lang.Math;
import java.util.HashMap;
import jqcadesigner.circuit.Circuit;
import jqcadesigner.VectorTable;
import jqcadesigner.circuit.units.Cell;
import jqcadesigner.circuit.units.QuantumDot;
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

	private Cell[][] _cellMatrix;
	
	private final double _kinkConstant;
	private static final double[][] _kinkSamePolarization =
	{
		{ JQCADConstants.QCHARGE_SQRD_OVER_FOUR, -JQCADConstants.QCHARGE_SQRD_OVER_FOUR,
			  JQCADConstants.QCHARGE_SQRD_OVER_FOUR, -JQCADConstants.QCHARGE_SQRD_OVER_FOUR },
		{ -JQCADConstants.QCHARGE_SQRD_OVER_FOUR, JQCADConstants.QCHARGE_SQRD_OVER_FOUR,
			  -JQCADConstants.QCHARGE_SQRD_OVER_FOUR, JQCADConstants.QCHARGE_SQRD_OVER_FOUR },
		{ JQCADConstants.QCHARGE_SQRD_OVER_FOUR, -JQCADConstants.QCHARGE_SQRD_OVER_FOUR, JQCADConstants.QCHARGE_SQRD_OVER_FOUR,
			  -JQCADConstants.QCHARGE_SQRD_OVER_FOUR },
		{ -JQCADConstants.QCHARGE_SQRD_OVER_FOUR, JQCADConstants.QCHARGE_SQRD_OVER_FOUR,
			  -JQCADConstants.QCHARGE_SQRD_OVER_FOUR, JQCADConstants.QCHARGE_SQRD_OVER_FOUR }
	};


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


		_kinkConstant = 1 / (JQCADConstants.FOUR_PI_EPSILON * _epsilonR);
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
	protected void _init()
	{
		_log.info( "Bistable engine initializing..." );
		final Cell[][] cellMatrix = _circuit.getCellMatrix();

		if( _randomizeCells )
		{
			_randomizeCells( cellMatrix );
		}

		_initCellInfo( cellMatrix );

		// TODO: Should I set up trace stuff?

		_cellMatrix = cellMatrix;
		_log.info( "Bistable engine finished initializing." );
	}

	@Override
	protected RunResults _run( VectorTable vectorTable )
	{
		assert vectorTable != null;

		_log.info( "Bistable engine running..." );

		RunResults retval = new RunResults();

		final Cell[][] cellMatrix = _cellMatrix;
		final int cellCount = _circuit.getCellCount();



		_log.info( "Bistable engine finished running." );
		
		return retval;
	}

	protected void _randomizeCells( final Cell[][] cellMatrix )
	{
		assert cellMatrix != null;

		MersenneTwisterFast rand = new MersenneTwisterFast();

		int layerCount = cellMatrix.length;
		for( int i = layerCount - 1; i >= 0; --i )
		{
			Cell[] crtLayer = cellMatrix[i];
			int crtCellCount = crtLayer.length;

			// Perform twice as many swaps as there are cells.
			for( int j = 2*crtCellCount; j > 0; --j )
			{
				int index1 = rand.nextInt( crtCellCount );
				int index2 = rand.nextInt( crtCellCount );

				Cell swap = crtLayer[ index1 ];
				crtLayer[ index1 ] = crtLayer[ index2 ];
				crtLayer[ index2 ] = swap;
			}
		}
	}

	protected void _initCellInfo( final Cell[][] cellMatrix )
	{
		final KinkEnergyCache kinkCache = new KinkEnergyCache();

		final int layerCount = cellMatrix.length;
		for( int i = 0; i < layerCount; ++i )
		{
			int crtCellCount = cellMatrix[i].length;

			// Counting backwards for performance
			for( int j = crtCellCount - 1; j >= 0; --j )
			{
				Cell crtCell = cellMatrix[i][j];

				Cell[] neighbors = _findCellNeighbors( cellMatrix, crtCell );
				double[] kinkEnergies = _calcKinkEnergies( kinkCache, crtCell, neighbors );

				crtCell.info = new CellInfo( neighbors, kinkEnergies );
			}
		}
	}

	/**
	 * Finds the neighbors of a cell within a cellMatrix.
	 * @param cellMatrix
	 * @param cell The cell to find the neighbors of.
	 * @return
	 */
	protected Cell[] _findCellNeighbors(	final Cell[][] cellMatrix,
											final Cell cell )
	{
		assert cellMatrix != null && cell != null;

		final int cellLayerNum = cell.layerNum;

		final double radiusOfEffectSqrd = _radiusOfEffect*_radiusOfEffect;
		final double layerSeparation = _layerSeparation;

		final ArrayList<Cell> neighbors = new ArrayList<Cell>();

		int layerCount = cellMatrix.length;
		for( int i = layerCount - 1; i >= 0; --i )
		{
			int crtCellCount = cellMatrix[i].length;

			for( int j = crtCellCount - 1; j >= 0; --j )
			{
				Cell crtCell = cellMatrix[i][j];

				if( crtCell != cell )
				{
					double xDiff = crtCell.xCoord - cell.xCoord;
					double yDiff = crtCell.yCoord - cell.yCoord;

					double zDiff	= Math.abs( crtCell.layerNum - cellLayerNum )
									* layerSeparation;

					double distanceSqrd	=	(xDiff * xDiff)
										+	(yDiff * yDiff)
										+	(zDiff * zDiff);

					if( distanceSqrd < radiusOfEffectSqrd )
					{
						neighbors.add( crtCell );
					}
				}
			}
		}

		return neighbors.toArray( new Cell[ neighbors.size() ] );
	}

	protected double[] _calcKinkEnergies(	KinkEnergyCache kinkCache,
											final Cell cell,
											final Cell[] neighbors )
	{
		assert kinkCache != null;

		int neighborsCount = neighbors.length;
		double[] kinkEnergies = new double[ neighborsCount ];

		HashMap<Cell, Double> cellCache = new HashMap<Cell, Double>();

		// TODO: currently this calculates redundantly. We should keep track of
		// the kink energies that have already been computed in a hash table
		// or something, and use those values instead of recalculating them
		// for each neighbor pair.
		for( int i = neighborsCount - 1; i >= 0; --i )
		{
			final Cell crtNeighbor = neighbors[i];
			final HashMap<Cell, Double> neighborCache = kinkCache.get( crtNeighbor );

			Double cachedValue;
			if( neighborCache != null
				&& (cachedValue = neighborCache.get( cell )) != null )
			{
				kinkEnergies[i] = cachedValue;
			}
			else
			{
				kinkEnergies[i] = _calcKinkEnergy( cell, neighbors[i] );
				cellCache.put( crtNeighbor, kinkEnergies[i] );
			}
		}

		if( cellCache.size() > 0 )
		{
			kinkCache.put( cell, cellCache );
		}

		return kinkEnergies;
	}

	protected double _calcKinkEnergy( final Cell cell1, final Cell cell2 )
	{
		final double zDiff	= Math.abs( cell1.layerNum - cell2.layerNum )
							* _layerSeparation;
		final double zDiffSqrd = zDiff * zDiff;
		final double qcharge_sqrd_over_four = JQCADConstants.QCHARGE_SQRD_OVER_FOUR;
		final double[][] samePolarization = _kinkSamePolarization;

		double energySame = 0;
		double energyDiff = 0;

		for( int i = 0; i < 4; ++i )
		{
			QuantumDot cell1dot = cell1.dots[i];

			for( int j = 0; j < 4; ++j )
			{
				QuantumDot cell2dot = cell2.dots[j];

				double xDiff = cell1dot.xCoord - cell2dot.xCoord;
				double yDiff = cell1dot.yCoord - cell2dot.yCoord;

				double distanceSqrd	= (xDiff * xDiff)
									+ (yDiff * yDiff)
									+ zDiffSqrd;

				// TODO: Is there a way to get out of having to do this sqrt?
				// TODO: Figure out what's the deal with this constant here.
				double distance = 1e-9 * Math.sqrt( distanceSqrd );

				assert distance != 0;

				// TODO: Make this more clear.
				double newEnergySame = samePolarization[i][j] / distance;
				energySame += newEnergySame;
				energyDiff -= newEnergySame;
			}
		}

		return _kinkConstant * (energyDiff - energySame);
	}

	/**
	 * Holds information specific to the BistableEngine on a single cell.
	 */
	protected static class CellInfo extends Cell.CellInfo
	{
		public final Cell[] neighbors;
		public final double[] kinkEnergies;

		public CellInfo( Cell[] n, double[] ke )
		{
			neighbors = n;
			kinkEnergies = ke;
		}
	}

	protected static class KinkEnergyCache extends
		HashMap<Cell, HashMap<Cell, Double>>
	{

	}

	public class RunResults extends Engine.RunResults
	{
		@Override
		public void printStats()
		{
			
		}
	}
}
