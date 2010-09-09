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

package jqcadesigner.circuit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jqcadesigner.JQCADesigner;
import jqcadesigner.VectorTable;
import jqcadesigner.circuit.units.Bus;
import jqcadesigner.circuit.units.BusLayout;
import jqcadesigner.circuit.units.Cell;
import jqcadesigner.circuit.units.CellLayer;
import jqcadesigner.circuit.units.Clock;
import jqcadesigner.circuit.units.FixedCell;
import jqcadesigner.circuit.units.InputCell;
import jqcadesigner.circuit.units.Label;
import jqcadesigner.circuit.units.Layer;
import jqcadesigner.circuit.units.NormalCell;
import jqcadesigner.circuit.units.OutputCell;
import jqcadesigner.circuit.units.QuantumDot;
import jqcadesigner.config.ConfigFile;
import jqcadesigner.config.ConfigFile.ParseException;
import jqcadesigner.config.syntaxtree.DataSection;
import jqcadesigner.config.syntaxtree.Section;
import jqcadesigner.config.syntaxtree.SectionGroup;
import jqcadesigner.config.syntaxtree.SettingsSection;

public final class Circuit
{
	public static final double FILE_VERSION = 2.0;

	private static final Logger _log = JQCADesigner.log;

	private final String				_file;
	private final ArrayList<Layer>		_layers;
	private final ArrayList<InputCell>	_inputCells;
	private final ArrayList<OutputCell>	_outputCells;
	private final ArrayList<FixedCell>	_fixedCells;
	private final BusLayout				_busLayout;
	private final Clock[]				_clocks;

	/**
	 * Stores the current layer number when loading from the ConfigFile.
	 */
	private int _crtLayerNum;

	private int _cellCount;

	public Circuit( String circuitFile )
		throws	FileNotFoundException, IOException, ParseException,
				CircuitException
	{
		_file = circuitFile;

		_layers			= new ArrayList<Layer>();
		_inputCells		= new ArrayList<InputCell>();
		_outputCells	= new ArrayList<OutputCell>();
		_fixedCells		= new ArrayList<FixedCell>();
		_busLayout		= new BusLayout();
		_clocks			= new Clock[4];

		_load();
	}

	public int getCellCount()
	{
		return _cellCount;
	}

	public Cell[] getCellList()
	{
		Cell[][] cellMatrix = getCellMatrix();

		int listSize = 0;
		for( Cell[] cellLayer : cellMatrix )
		{
			listSize += cellLayer.length;
		}

		int listIndex = 0;
		Cell[] cellList = new Cell[ listSize ];
		for( Cell[] cellLayer : cellMatrix )
		{
			for( Cell cell : cellLayer )
			{
				cellList[ listIndex++ ] = cell;
			}
		}

		return cellList;
	}

	public Cell[][] getCellMatrix()
	{
		Cell[][] matrix = new Cell[ _layers.size() ][];

		int layerCount = _layers.size();
		for( int i = 0; i < layerCount; ++i )
		{
			Layer crtLayer = _layers.get( i );
			
			if( crtLayer.hasCells() )
			{
				ArrayList<Cell> cells = ((CellLayer)crtLayer).cells;
				matrix[i] = cells.toArray( new Cell[ cells.size() ] );
			}
		}
		
		return matrix;
	}

	public InputCell[] getInputCells()
	{
		return _inputCells.toArray( new InputCell[ _inputCells.size() ] );
	}

	/**
	 *
	 * @param vectorTable
	 * @param granularity
	 */
	public void updateInputs( VectorTable vectorTable, int granularity )
	{
		if( vectorTable == null || vectorTable.inputs.length == 0 )
		{
			String msg = "Can't use an empty vector table.";
			throw new IllegalArgumentException( msg );
		}

		if( vectorTable.inputs.length != _inputCells.size() )
		{
			String msg = "Invalid vector table. Incorrect dimensions.";
			throw new IllegalArgumentException( msg );
		}

		for( int i = 0; i < vectorTable.active.length; ++i )
		{
			_inputCells.get( i ).active = vectorTable.active[i];

			_inputCells.get( i ).setValues( vectorTable.inputs[i], granularity );
		}
	}

	public OutputCell[] getOutputCells()
	{
		return _outputCells.toArray( new OutputCell[ _outputCells.size() ] );
	}

	public void updateOutputs( final int granularity )
	{
		if( granularity <= 0 )
		{
			String msg = "The granularity of the outputs must be greater than 0.";
			throw new IllegalArgumentException( msg );
		}

		for( OutputCell outputCell : _outputCells )
		{
			outputCell.setValueCacheSize( granularity );
		}
	}

	public Clock getClock( int clockNum )
	{
		if( clockNum < 0 || clockNum > 3 )
		{
			throw new IllegalArgumentException( "Clock number must be between 0 and 3 " );
		}

		return _clocks[ clockNum ];
	}

	public Clock[] getClocks()
	{
		Clock[] retval = { _clocks[0], _clocks[1], _clocks[2], _clocks[3] };

		return retval;
	}

	public void updateClocks(	int cycles, int granularity, double cLow,
								double cHigh, double ampFactor, double clockShift )
	{
		for( int i = 0; i < 4; ++i )
		{
			_clocks[i] = new Clock( i, cycles, granularity, cLow, cHigh, ampFactor, clockShift );
		}
	}

	private void _load()
		throws	FileNotFoundException, IOException, ParseException,
				CircuitException
	{
		assert _file != null;

		ConfigFile config = new ConfigFile( _file );

		if(		!config.containsKey( "VERSION" )
			&&	!config.containsKey( "TYPE:DESIGN" ) )
		{
			String msg =	"Invalid circuit file. Does not include necessary "
							+ "sections.";

			throw new CircuitException( msg );
		}

		Section versionSect = config.get( "VERSION" ).get( 0 );
		Section designSect = config.get( "TYPE:DESIGN" ).get( 0 );

		if( !versionSect.hasSettings() )
		{
			String msg = "The VERSION section must contain settings.";
			throw new CircuitException( msg );
		}

		_checkVersion( (SettingsSection)versionSect );
		_loadDesign( designSect );
	}

	private void _checkVersion( SettingsSection versionSect )
		throws CircuitException
	{
		assert versionSect != null;

		String versionString = versionSect.settings.get( "qcadesigner_version" );
		
		double version = Double.parseDouble( versionString );

		if( version != FILE_VERSION )
		{
			_log.log(	Level.WARNING,
						"The circuit file <{0}> appears to be from version"
						+ "{1} of QCADesigner. JQCADesigner may perform "
						+ "unexpectedly or not support all features.",
						new Object[]{ _file, version });
		}

		_log.log( Level.INFO, "Circuit file version is {0}", versionString );
	}

	private void _loadDesign( Section designSect ) throws CircuitException
	{
		assert designSect != null && _layers != null;

		if( !designSect.containsSubSections( "TYPE:QCADLayer"/*, "TYPE:BUS_LAYOUT"*/ ) )
		{
			String msg = "Missing sub-sections from design.";
			throw new CircuitException( msg );
		}
		
		SectionGroup layers = designSect.subSections.get( "TYPE:QCADLayer" );

		int layerCount = layers.size();
		for( int i = 0; i < layerCount; ++i )
		{
			_crtLayerNum = i;
			
			Section crtLayerSect = layers.get( i );
			_log.log( Level.FINE, "Loading layer {0}", i );

			if( !crtLayerSect.hasSettings() )
			{
				String msg = "Settings missing from layer.";
				throw new CircuitException( msg );
			}

			Layer layer = _loadLayer( (SettingsSection)crtLayerSect );

			if( layer != null )
			{
				_layers.add( layer );
			}
		}

		if( designSect.containsSubSections( "TYPE:BUS_LAYOUT" ) )
		{
			Section busLayout = designSect.subSections.get( "TYPE:BUS_LAYOUT" ).get( 0 );
			SectionGroup buses = busLayout.subSections.get( "TYPE:BUS" );

			int busCount = buses.size();
			for( int i = 0; i < busCount; ++i )
			{
				Section crtBusSect = buses.get( i );

				if( !crtBusSect.hasSettings() )
				{
					 String msg = "Settings missing from bus.";
					 throw new CircuitException( msg );
				}

				Bus bus = _loadBus( (SettingsSection)crtBusSect );
				_busLayout.add( bus );
			}
		}
	}

	private Layer _loadLayer( SettingsSection layerSect ) throws CircuitException
	{
		assert layerSect != null;

		if( !layerSect.containsSettings( "pszDescription", "status", "type" ) )
		{
			String msg = "Missing settings in layer.";
			throw new CircuitException( msg );
		}

		Layer layer = null;
		byte type = Byte.parseByte( layerSect.settings.get( "type" ) );
		switch( type )
		{
			case 0:
				// Not sure what this is, Clock layer?
				break;
			case 1:
				layer = _loadCellLayer( layerSect );
				break;
			case 2:
				// Subtrate layer
				break;
			case 3:
				// Drawing Layer
				break;
			default:
				throw new CircuitException( "Invalid layer type: " + type );
		}

		return layer;
	}

	/**
	 * Loads a cell layer from the SettingsSection.
	 * 
	 * Note: assumes that it is being called from _loadLayer and that all
	 * necessary checks have been performed.
	 *
	 * @param layerSect
	 * @return
	 */
	private CellLayer _loadCellLayer( SettingsSection layerSect ) throws CircuitException
	{
		assert layerSect != null
			&& layerSect.containsSettings( "pszDescription", "status" );

		String description	= layerSect.settings.get( "pszDescription" );
		byte status			= Byte.parseByte( layerSect.settings.get( "status" ) );

		CellLayer cellLayer = new CellLayer( description, status );

		if( !layerSect.containsSubSections( "TYPE:QCADCell" ) )
		{
			// It's okay if it's just an empty layer.
			return cellLayer;
		}

		SectionGroup cells = layerSect.subSections.get( "TYPE:QCADCell" );

		int cellCount = cells.size();

		for( int i = 0; i < cellCount; ++i )
		{
			Section cellSect = cells.get( i );

			if( !cellSect.hasSettings() )
			{
				throw new CircuitException( "Cells must have settings." );
			}

			Cell cell = _loadCell( (SettingsSection)cellSect );
			cellLayer.cells.add( cell );
		}

		return cellLayer;
	}

	/**
	 * Loads a cell from the SettingsSection.
	 *
	 * Note: assumes it's being called from _loadCellLayer.
	 * @param cellSect
	 * @return
	 * @throws jqcadesigner.circuit.Circuit.CircuitException
	 */
	private Cell _loadCell( SettingsSection cellSect ) throws CircuitException
	{
		assert cellSect != null;
		
		Cell cell;

		boolean valid = cellSect.containsSettings(	"cell_options.cxCell",
													"cell_options.cyCell",
													"cell_options.dot_diameter",
													"cell_options.clock",
													"cell_options.mode",
													"cell_function" );
		if( !valid )
		{
			throw new CircuitException( "Cell does not contain enough settings." );
		}

		if( !cellSect.containsSubSections( "TYPE:CELL_DOT" ) )
		{
			throw new CircuitException( "Cell does not contain any quantum dots." );
		}

		// Load the QuantumDots.
		QuantumDot[] qDots = new QuantumDot[4];
		SectionGroup dotSects = cellSect.subSections.get( "TYPE:CELL_DOT" );
		for( int i = 0; i < 4; ++i )
		{
			Section dotSect = dotSects.get( i );

			if( !dotSect.hasSettings() )
			{
				throw new CircuitException( "Dots must have settings." );
			}

			qDots[i] = _loadQuantumDot( (SettingsSection)dotSects.get( i ) );
		}

		double xCoord =
			_parseDouble( cellSect.settings.get( "cell_options.cxCell" ) );

		double yCoord =
			_parseDouble( cellSect.settings.get( "cell_options.cyCell" ) );

		double dotDiameter =
			_parseDouble( cellSect.settings.get( "cell_options.dot_diameter" ) );

		byte clock =
			Byte.parseByte( cellSect.settings.get( "cell_options.clock" ) );

		String modeString = cellSect.settings.get( "cell_options.mode" );
		String funcString = cellSect.settings.get( "cell_function" );

		Cell.Mode mode = null;
		Cell.Function func = null;

		if( modeString.endsWith( "NORMAL" ) )
		{
			mode = Cell.Mode.NORMAL;
		}
		else if( modeString.endsWith( "VERTICAL" ) )
		{
			mode = Cell.Mode.VERTICAL;
		}
		else if( modeString.endsWith( "CROSSOVER" ) )
		{
			mode = Cell.Mode.CROSSOVER;
		}
		else
		{
			String msg = "Unknown cell mode: " + modeString;
			throw new CircuitException( msg );
		}

		if( funcString.endsWith( "NORMAL" ) )
		{
			cell = new NormalCell( this, mode, clock, xCoord, yCoord, dotDiameter, _crtLayerNum, qDots );
		}
		else if( funcString.endsWith( "OUTPUT" ) )
		{
			cell = new OutputCell( this, mode, clock, xCoord, yCoord, dotDiameter, _crtLayerNum, qDots );
			OutputCell ocell = (OutputCell)cell;
			Label label = _loadLabel( cellSect );
			ocell.setName( label.text );
			_outputCells.add( ocell );
		}
		else if( funcString.endsWith( "INPUT" ) )
		{
			cell = new InputCell( this, mode, clock, xCoord, yCoord, dotDiameter, _crtLayerNum, qDots );
			InputCell icell = (InputCell)cell;
			Label label = _loadLabel( cellSect );
			icell.setName( label.text );
			_inputCells.add( icell );
		}
		else if( funcString.endsWith( "FIXED" ) )
		{
			cell = new FixedCell( this, mode, clock, xCoord, yCoord, dotDiameter, _crtLayerNum, qDots );
			_fixedCells.add( (FixedCell)cell );
		}
		else
		{
			String msg = "Unknown cell function: " + funcString;
			throw new CircuitException( msg );
		}
		
		++_cellCount;
		return cell;
	}

	/**
	 * Loads a QuantumDot from a SettingsSection
	 *
	 * Note: assumes it's being called by _loadCell.
	 * @param dotSect
	 * @return
	 */
	private QuantumDot _loadQuantumDot( SettingsSection dotSect ) throws CircuitException
	{
		assert dotSect != null;
		
		if( !dotSect.containsSettings( "x", "y", "diameter", "charge", "spin", "potential" ) )
		{
			throw new CircuitException( "Quantum dot does not have enough settings." );
		}

		double xCoord	= _parseDouble( dotSect.settings.get( "x" ) );
		double yCoord	= _parseDouble( dotSect.settings.get( "y" ) );
		double diameter = _parseDouble( dotSect.settings.get( "diameter" ) );
		double charge	= _parseDouble( dotSect.settings.get( "charge" ) );
		double spin		= _parseDouble( dotSect.settings.get( "spin" ) );
		double potential= _parseDouble( dotSect.settings.get( "potential" ) );

		return new QuantumDot( xCoord, yCoord, diameter, charge, spin, potential );
	}

	private Label _loadLabel( Section sect ) throws CircuitException
	{
		if( !sect.containsSubSections( "TYPE:QCADLabel" ) )
		{
			throw new CircuitException( "Section does not have a QCADLabel." );
		}

		Section labelSect = sect.subSections.get( "TYPE:QCADLabel" ).get( 0 );

		if( !labelSect.hasSettings() )
		{
			throw new CircuitException( "QCADLabel section must contain settings." );
		}

		SettingsSection labelSettings = (SettingsSection)labelSect;

		if( !labelSettings.containsSettings( "psz" ) )
		{
			throw new CircuitException( "QCADLabel section must contain a 'psz' setting." );
		}


		String text = labelSettings.settings.get( "psz" );

		return new Label( text );
	}

	/**
	 * Loads a Bus from a SettingsSection
	 * @param busSect
	 * @return
	 * @throws jqcadesigner.circuit.Circuit.CircuitException
	 */
	private Bus _loadBus( SettingsSection busSect ) throws CircuitException
	{
		assert busSect != null;

		if( !busSect.containsSettings( "pszName", "bus_function" ) )
		{
			throw new CircuitException( "Bus does not have enough settings." );
		}
		else if( !busSect.containsSubSections( "BUS_DATA" ) )
		{
			throw new CircuitException( "Bus does not contain any data." );
		}

		String name = busSect.settings.get( "pszName" );
		byte func = Byte.parseByte( busSect.settings.get( "bus_function" ) );

		Section busDataSect = busSect.subSections.get( "BUS_DATA" ).get( 0 );

		if( !busDataSect.hasData() )
		{
			throw new CircuitException( "Bus data does not contain any data." );
		}

		DataSection dataSect = (DataSection)busDataSect;
		int[] inputCells = dataSect.data.get( 0 );

		return new Bus( name, func, inputCells );
	}

	private double _parseDouble( String str )
	{
		if( str.equals( "-1.#QNAN0" ) ) // To remedy a bug in QCADesigner
		{
			return 0;
		}
		else
		{
			return Double.parseDouble( str );
		}

	}

	public static class CircuitException extends Exception
	{
		public CircuitException( String msg )
		{
			super( msg );
		}
	}
}
