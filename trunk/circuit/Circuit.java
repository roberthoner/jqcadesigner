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
import jqcadesigner.circuit.units.Cell;
import jqcadesigner.circuit.units.CellLayer;
import jqcadesigner.circuit.units.Layer;
import jqcadesigner.config.ConfigFile;
import jqcadesigner.config.ConfigFile.ParseException;
import jqcadesigner.config.syntaxtree.Section;
import jqcadesigner.config.syntaxtree.SectionGroup;
import jqcadesigner.config.syntaxtree.SettingsSection;

public final class Circuit
{
	public static final double FILE_VERSION = 2.0;
	
	private final String _file;
	private final Logger _log;

	private final ArrayList<Layer> _layers;

	public Circuit( String circuitFile )
		throws	FileNotFoundException, IOException, ParseException,
				CircuitException
	{
		_file = circuitFile;
		_log = JQCADesigner.log;

		_layers = new ArrayList<Layer>();

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
		SectionGroup busLayout = designSect.subSections.get( "TYPE:BUS_LAYOUT" );

		int layerCount = layers.size();
		for( int i = 0; i < layerCount; ++i )
		{
			Section crtLayerSect = layers.get( i );
			System.out.println( "Layer " + i );

			if( !crtLayerSect.hasSettings() )
			{
				String msg = "Settings missing from layer.";
				throw new CircuitException( msg );
			}

			_loadLayer( (SettingsSection)crtLayerSect );
		}
	}

	private void _loadLayer( SettingsSection layerSect ) throws CircuitException
	{
		if( !layerSect.containsSettings( "pszDescription", "status", "type" ) )
		{
			String msg = "Missing settings in layer.";
			throw new CircuitException( msg );
		}

		Layer layer = null;

/*		String description	= layerSect.settings.get( "pszDescription" );
		byte status			= Byte.parseByte( layerSect.settings.get( "status" ) );*/
		byte type			= Byte.parseByte( layerSect.settings.get( "type" ) );
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

		if( layer != null )
		{
			_layers.add( layer );
		}
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
	public CellLayer _loadCellLayer( SettingsSection layerSect ) throws CircuitException
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

		if( modeString.endsWith( "VERTICAL" ) )
		{
			mode = Cell.Mode.VERTICAL;
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
		else
		{
			String msg = "Unknown cell function: " + funcString;
		}

		Cell cell = new Cell( mode, func, clock, xCoord, yCoord, dotDiameter );

		// TODO: load dots, then that's it!

		return cell;
	}

	public static class CircuitException extends Exception
	{
		public CircuitException( String msg )
		{
			super( msg );
		}
	}
}
