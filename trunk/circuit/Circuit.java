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
import jqcadesigner.circuit.units.Bus;
import jqcadesigner.circuit.units.BusLayout;
import jqcadesigner.circuit.units.Cell;
import jqcadesigner.circuit.units.CellLayer;
import jqcadesigner.circuit.units.Layer;
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

	private final String _file;
	private final ArrayList<Layer> _layers;
	private final BusLayout _busLayout;

	public Circuit( String circuitFile )
		throws	FileNotFoundException, IOException, ParseException,
				CircuitException
	{
		_file = circuitFile;

		_layers = new ArrayList<Layer>();
		_busLayout = new BusLayout();

		_load();
	}
	
	private void _load()
		throws	FileNotFoundException, IOException, ParseException,
				CircuitException
	{
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
		if( !designSect.containsSubSections( "TYPE:QCADLayer", "TYPE:BUS_LAYOUT" ) )
		{
			String msg = "Missing sub-sections from design.";
			throw new CircuitException( msg );
		}
		
		SectionGroup layers = designSect.subSections.get( "TYPE:QCADLayer" );
		Section busLayout = designSect.subSections.get( "TYPE:BUS_LAYOUT" ).get( 0 );
		SectionGroup buses = busLayout.subSections.get( "TYPE:BUS" );

		int layerCount = layers.size();
		for( int i = 0; i < layerCount; ++i )
		{
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

	private Layer _loadLayer( SettingsSection layerSect ) throws CircuitException
	{
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

		// Counting backwards to try to get a little extra performance boost.
		// There could be a lot of cells here!
		for( int i = cellCount - 1; i >= 0; --i )
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

		double xCoord =
			Double.parseDouble( cellSect.settings.get( "cell_options.cxCell" ) );

		double yCoord =
			Double.parseDouble( cellSect.settings.get( "cell_options.cyCell" ) );

		double dotDiameter =
			Double.parseDouble( cellSect.settings.get( "cell_options.dot_diameter" ) );

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
			func = Cell.Function.NORMAL;
		}
		else if( funcString.endsWith( "OUTPUT" ) )
		{
			func = Cell.Function.OUTPUT;
		}
		else if( funcString.endsWith( "INPUT" ) )
		{
			func = Cell.Function.INPUT;
		}
		else if( funcString.endsWith( "FIXED" ) )
		{
			func = Cell.Function.FIXED;
		}
		else
		{
			String msg = "Unknown cell function: " + funcString;
			throw new CircuitException( msg );
		}

		Cell cell = new Cell( mode, func, clock, xCoord, yCoord, dotDiameter );

		cellSect.containsSubSections( "TYPE:CELL_DOT" );

		SectionGroup dots = cellSect.subSections.get( "TYPE:CELL_DOT" );

		for( int i = 3; i >= 0; --i )
		{
			Section dotSect = dots.get( i );

			if( !dotSect.hasSettings() )
			{
				throw new CircuitException( "Dots must have settings." );
			}

			cell.dots[i] = _loadQuantumDot( (SettingsSection)dots.get( i ) );
		}


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
		if( !dotSect.containsSettings( "x", "y", "diameter", "charge", "spin", "potential" ) )
		{
			throw new CircuitException( "Quantum dot does not have enough settings." );
		}

		double xCoord	= Double.parseDouble( dotSect.settings.get( "x" ) );
		double yCoord	= Double.parseDouble( dotSect.settings.get( "y" ) );
		double diameter = Double.parseDouble( dotSect.settings.get( "diameter" ) );
		double charge	= Double.parseDouble( dotSect.settings.get( "charge" ) );
		double spin		= Double.parseDouble( dotSect.settings.get( "spin" ) );
		double potential= Double.parseDouble( dotSect.settings.get( "potential" ) );

		return new QuantumDot( xCoord, yCoord, diameter, charge, spin, potential );
	}

	private Bus _loadBus( SettingsSection busSect ) throws CircuitException
	{
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

	public static class CircuitException extends Exception
	{
		public CircuitException( String msg )
		{
			super( msg );
		}
	}
}
