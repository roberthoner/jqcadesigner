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
import java.util.logging.Level;
import java.util.logging.Logger;
import jqcadesigner.JQCADConstants;
import java.lang.Math;
import java.util.HashMap;
import jqcadesigner.circuit.Circuit;
import jqcadesigner.VectorTable;
import jqcadesigner.circuit.units.Cell;
import jqcadesigner.circuit.units.Clock;
import jqcadesigner.circuit.units.InputCell;
import jqcadesigner.circuit.units.OutputCell;
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

	protected final int		_numberOfSamples;
	protected final double	_convergenceTolerance;
	protected final double	_radiusOfEffect;
	protected final double	_epsilonR;
	protected final double	_clockHigh;
	protected final double	_clockLow;
	protected final double	_clockShift;
	protected final double	_clockAmplitudeFactor;
	protected final int		_maxIterationsPerSample;
	protected final double	_layerSeparation;
	protected final boolean	_randomizeCells;
	
	protected Cell[][] _cellMatrix;
	
	protected final double _kinkConstant;

	protected boolean _stableFlag;
	protected boolean _stopSimulation;

	/**
	 * Used in _calcKinkEnergy.
	 */
	protected static final double[][] _kinkSamePolarization =
	{
		{ JQCADConstants.QCHARGE_SQRD_OVER_FOUR, -JQCADConstants.QCHARGE_SQRD_OVER_FOUR,
			  JQCADConstants.QCHARGE_SQRD_OVER_FOUR, -JQCADConstants.QCHARGE_SQRD_OVER_FOUR },
		{ -JQCADConstants.QCHARGE_SQRD_OVER_FOUR, JQCADConstants.QCHARGE_SQRD_OVER_FOUR,
			  -JQCADConstants.QCHARGE_SQRD_OVER_FOUR, JQCADConstants.QCHARGE_SQRD_OVER_FOUR },
		{ JQCADConstants.QCHARGE_SQRD_OVER_FOUR, -JQCADConstants.QCHARGE_SQRD_OVER_FOUR,
			  JQCADConstants.QCHARGE_SQRD_OVER_FOUR, -JQCADConstants.QCHARGE_SQRD_OVER_FOUR },
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

		// Either they both should be null or neither should be null.
		assert !(configFileName == null ^ _configFile == null);

		SettingsSection configSect;

		if( _configFile == null )
		{
			// No config file was loaded so we need to create an empty
			// SettingsSection to signify that we haven't loaded any extrenal
			// settings -- i.e., we should use the defaults.
			configSect = new SettingsSection();
		}
		else
		{
			Section section = _configFile.get( "BISTABLE_OPTIONS" ).get( 0 );

			if( section == null || !section.hasSettings() )
			{
				String msg = "Bistable engine config file needs settings.";
				throw new EngineException( msg );
			}

			configSect = (SettingsSection)section;
		}
		
		// Load the config settings.
		_numberOfSamples		= configSect.get(	"number_of_samples",
													DefaultConfig.NUMBER_OF_SAMPLES );

		_convergenceTolerance	= configSect.get(	"convergence_tolerance",
													DefaultConfig.CONVERGENCE_TOLERANCE );

		_radiusOfEffect			= configSect.get(	"radius_of_effect",
													DefaultConfig.RADIUS_OF_EFFECT );

		_epsilonR				= configSect.get(	"epsilonR",
													DefaultConfig.EPSILON_R );

		_clockHigh				= configSect.get(	"clock_high",
													DefaultConfig.CLOCK_HIGH );

		_clockLow				= configSect.get(	"clock_low",
													DefaultConfig.CLOCK_LOW );

		_clockShift				= configSect.get(	"clock_shift",
													DefaultConfig.CLOCK_SHIFT );

		_clockAmplitudeFactor	= configSect.get(	"clock_amplitude_factor",
													DefaultConfig.CLOCK_AMPLITUDE_FACTOR );

		_maxIterationsPerSample	= configSect.get(	"max_iterations_per_sample",
													DefaultConfig.MAX_ITERATIONS_PER_SAMPLE );

		_layerSeparation		= configSect.get(	"layer_separation",
													DefaultConfig.LAYER_SEPARATION );

		_randomizeCells			= configSect.get(	"randomize_cells",
													DefaultConfig.RANDOMIZE_CELLS );

		// Used by _calcKinkEnergy
		_kinkConstant = 1 / (JQCADConstants.FOUR_PI_EPSILON * _epsilonR);
	}

	// TODO: should this be a part of the engine super class?
	public void stop()
	{
		_stopSimulation = true;
	}

	@Override
	protected void _init( VectorTable vectorTable )
	{
		_log.info( "Bistable engine initializing..." );

		// Put the values in the vectorTable into their associated input cell.
		_circuit.updateInputs( vectorTable, _numberOfSamples );

		_circuit.updateOutputs( _numberOfSamples );

		// TODO: Am I initializing the outputs?

		// Prepare the clocks.
		_circuit.updateClocks(	vectorTable.inputs[0].length,
								_numberOfSamples,
								_clockLow,
								_clockHigh,
								_clockAmplitudeFactor,
								_clockShift );



		final Cell[][] cellMatrix = _circuit.getCellMatrix();

		// Tell the cells not to update their dots, we don't need this information
		// for this engine and it will just waste CPU.
		for( Cell[] layer : cellMatrix )
		{
			for( Cell cell : layer )
			{
				cell.setUpdateDots( false );
			}
		}

		if( _randomizeCells )
		{
			_randomizeCells( cellMatrix );
		}

		_initCells( cellMatrix, _circuit.getClocks() );

		_cellMatrix = cellMatrix;
		_log.info( "Bistable engine finished initializing." );
	}

	@Override
	protected RunResults _run( VectorTable vectorTable )
	{
		assert vectorTable != null;

		_log.info( "Bistable engine running..." );

		final Cell[][] cellMatrix = _cellMatrix;
		final int layerCount = cellMatrix.length;

		final InputCell[] inputCells = _circuit.getInputCells();
		final int inputCellsCount = inputCells.length;

		final OutputCell[] outputCells = _circuit.getOutputCells();
		final int outputCellsCount = outputCells.length;

		final int maxIterationsPerSample = _maxIterationsPerSample;

		final Clock clock0 = _circuit.getClock( 0 );
		final Clock clock1 = _circuit.getClock( 1 );
		final Clock clock2 = _circuit.getClock( 2 );
		final Clock clock3 = _circuit.getClock( 3 );

		for( int i = _numberOfSamples; i > 0 && !_stopSimulation; --i )
		{
			// Advance the clocks.
			clock0.tick();
			clock1.tick();
			clock2.tick();
			clock3.tick();

			// Update the input cells.
			for( int j = inputCellsCount - 1; j >= 0; --j )
			{
				inputCells[j].tick();
			}

			if( _randomizeCells )
			{
				_randomizeCells( cellMatrix );
			}

			int iterationCount = 0;

			do
			{
				_stableFlag = true;
				if( iterationCount > maxIterationsPerSample )
				{
					// TODO: make note that we couldn't get to a stable state.
					break;
				}

				// Update the cells.
				for( int layerNum = layerCount - 1; layerNum >= 0; --layerNum )
				{
					final Cell[] cellLayer = cellMatrix[ layerNum ];
					final int cellsInLayer = cellLayer.length;

					for( int cellNum = cellsInLayer - 1; cellNum >= 0; --cellNum )
					{
						final Cell crtCell = cellLayer[ cellNum ];

						final Cell.Function crtFunc = crtCell.function;
						if( crtFunc == Cell.Function.NORMAL
							|| crtFunc == Cell.Function.OUTPUT
							|| (crtFunc == Cell.Function.INPUT
								&& !((InputCell)crtCell).active) )
						{
							crtCell.tick();
						}
					}
				}
			}
			while( !_stableFlag );

			// Have the output cells plot their stable values.
			for( int j = outputCellsCount - 1; j >= 0; --j )
			{
				outputCells[j].plotPolarization();
			}
		}
		_log.info( "Bistable engine finished running." );

		return new RunResults( outputCells );
	}

	/**
	 * Randomly swaps the cells around. Makes as many swaps as there are cells.
	 *
	 * @param cellMatrix
	 */
	protected void _randomizeCells( final Cell[][] cellMatrix )
	{
		assert cellMatrix != null;

		MersenneTwisterFast rand = new MersenneTwisterFast();

		int layerCount = cellMatrix.length;
		for( int i = layerCount - 1; i >= 0; --i )
		{
			Cell[] crtLayer = cellMatrix[i];
			int crtCellCount = crtLayer.length;

			// Perform as many swaps as there are cells.
			for( int j = crtCellCount; j > 0; --j )
			{
				int index1 = rand.nextInt( crtCellCount );
				int index2 = rand.nextInt( crtCellCount );

				Cell swap = crtLayer[ index1 ];
				crtLayer[ index1 ] = crtLayer[ index2 ];
				crtLayer[ index2 ] = swap;
			}
		}
	}

	/**
	 * Initializes each of the cells.
	 *
	 * Calculates the _neighbors and kink energies for each cell in the
	 * cellMatrix. Sets the cells' TickHandlers.
	 *
	 * @param cellMatrix
	 */
	protected void _initCells( final Cell[][] cellMatrix, final Clock[] clocks )
	{
		final KinkEnergyCache kinkCache = new KinkEnergyCache();

		final int layerCount = cellMatrix.length;
		for( int i = 0; i < layerCount; ++i )
		{
			int crtCellCount = cellMatrix[i].length;

			// Counting backwards for performance
			for( int j = crtCellCount - 1; j >= 0; --j )
			{
				final Cell crtCell = cellMatrix[i][j];
				final Cell.Function crtFunc = crtCell.function;

				if( crtFunc == Cell.Function.NORMAL
					|| crtFunc == Cell.Function.OUTPUT
					|| (crtFunc == Cell.Function.INPUT
						&& !((InputCell)crtCell).active) )
				{
					Cell[] neighbors = _findCellNeighbors( cellMatrix, crtCell );
					double[] kinkEnergies = _calcKinkEnergies( kinkCache, crtCell, neighbors );

					TickHandler th = new TickHandler(	crtCell,
														neighbors,
														kinkEnergies,
														clocks[ crtCell.clockNum ] );
					crtCell.setTickHandler( th );
				}
			}
		}
	}

	/**
	 * Finds the _neighbors of a cell within a cellMatrix.
	 *
	 * @param cellMatrix
	 * @param cell The cell to find the _neighbors of.
	 * @return The cells determined to be within the radius of effect.
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

	/**
	 * Calculate the kink energies between the cell and its _neighbors.
	 *
	 * @param kinkCache A cache of kink energies to speed up the process.
	 * @param cell The main cell.
	 * @param _neighbors The main cell's _neighbors.
	 * @return The kink energies for all of the _neighbors.
	 */
	protected double[] _calcKinkEnergies(	KinkEnergyCache kinkCache,
											final Cell cell,
											final Cell[] neighbors )
	{
		assert kinkCache != null;

		int neighborsCount = neighbors.length;
		double[] kinkEnergies = new double[ neighborsCount ];

		HashMap<Cell, Double> cellCache = new HashMap<Cell, Double>();

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

	/**
	 * Calculate the kink energy between two cells.
	 *
	 * @param cell1
	 * @param cell2
	 * @return The kink energy.
	 */
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

				// TODO: make sure this is equivalent to what is in the original code.
				double newEnergySame = samePolarization[i][j] / distance;
				energySame += newEnergySame;
				energyDiff -= newEnergySame;
			}
		}

		return _kinkConstant * (energyDiff - energySame);
	}

	protected class TickHandler extends Cell.TickHandler
	{
		/**
		 * The cells determined to be within the radius of effect of the cell.
		 */
		protected final Cell[] _neighbors;

		/**
		 *  Kink energies between this cell and each of its _neighbors.
		 */
		protected final double[] _kinkEnergies;

		protected final Clock _clock;

		/**
		 * Construct the TickHandler.
		 *
		 * @param c The cell to which this handler belongs.
		 * @param n The _neighbors of the cell.
		 * @param ke The kink energies of the between this cell and its _neighbors.
		 */
		public TickHandler( Cell cell, Cell[] n, double[] ke, Clock cl )
		{
			super( cell );

			assert n.length == ke.length;

			_neighbors = n;
			_kinkEnergies = ke;
			_clock = cl;
		}

		@Override
		public double tick()
		{
			final Cell cell = _cell;
			final double oldPol = cell.getPolarization();

			final Cell[] neighbors = _neighbors;
			final int neighborCount = neighbors.length;

			final double[] ke = _kinkEnergies;

			double polarizationMath = 0;

			for( int i = neighborCount - 1; i >= 0; --i )
			{
				polarizationMath += ke[i] * neighbors[i].getPolarization();
			}

			polarizationMath /= 2.0 * _clock.check();

			double newPol =
				(polarizationMath > 1000)
				? 1 : (polarizationMath < -1000)
					? -1 : (Math.abs( polarizationMath ) < 0.001)
						? polarizationMath
						: polarizationMath / Math.sqrt( 1 + polarizationMath * polarizationMath );

			cell.setPolarization( newPol );

			// _stableFlag and _convergenceTolerance come from the containing
			// instance of BistableEngine.
			boolean stable = (Math.abs( newPol - oldPol ) <= _convergenceTolerance);
			
			// We don't want to set the flag to true just because this one cell
			// is stable.  They all have to be stable for the system to be
			// considered stable.
			if( !stable )
			{
				_stableFlag = false;
			}

			//_stableFlag = stable;

			return newPol;
		}
	}

	/**
	 * A helper class for readability. Used when kink energies are being calculated.
	 */
	protected static class KinkEnergyCache extends
		HashMap<Cell, HashMap<Cell, Double>>
	{
	}

	public class RunResults extends Engine.RunResults
	{
		protected final OutputCell[] _outputCells;

		public RunResults( OutputCell[] outputCells )
		{
			_outputCells = outputCells;

			for( OutputCell outputCell : outputCells )
			{
				String name = outputCell.getName();
				_outputValues.put( name, outputCell.getValues() );
				_outputTraces.put( name, outputCell.getTrace() );
			}
		}

		@Override
		public void printStats()
		{
			System.out.printf( "Initialization time: %dms\n", initTime );
			System.out.printf( "Run time: %dms\n", runTime );

			System.out.println( "Outputs:");
			for( int i = 0; i < _outputCells.length; ++i )
			{
				System.out.printf( "%10s", _outputCells[i].getName() );

				for( byte v : _outputCells[i].getValues() )
				{
					System.out.printf( " %d", v );
				}

				System.out.println();
			}
		}
	}
}
