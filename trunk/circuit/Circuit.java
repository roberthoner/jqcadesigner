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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import jqcadesigner.JQCADesigner;
import jqcadesigner.config.ConfigFile;
import jqcadesigner.config.ConfigFile.ParseException;
import jqcadesigner.config.syntaxtree.Section;
import jqcadesigner.config.syntaxtree.SettingsSection;

public final class Circuit
{
	public static final double FILE_VERSION = 2.0;
	
	private final String _file;
	private final Logger _log;

	public Circuit( String circuitFile )
		throws	FileNotFoundException, IOException, ParseException,
				CircuitException
	{
		_file = circuitFile;

		_log = JQCADesigner.log;

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

		if( !versionSect.containsSettings() )
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

	private void _loadDesign( Section designSect )
	{
		
	}

	public static class CircuitException extends Exception
	{
		public CircuitException( String msg )
		{
			super( msg );
		}
	}
}
