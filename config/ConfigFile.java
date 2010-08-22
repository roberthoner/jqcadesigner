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

package jqcadesigner.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import jqcadesigner.config.syntaxtree.*;

/**
 *
 * @author Robert
 */
public class ConfigFile extends SectionMap
{
	public ConfigFile( String fileName )
		throws FileNotFoundException, IOException, ParseException
	{
		super();
		_load( fileName );
	}

	private void _load( String fileName )
		throws FileNotFoundException, IOException, ParseException
	{
		FileReader fileReader = new FileReader( fileName );
		PeekingBufferedReader in = new PeekingBufferedReader( fileReader );

		_parseFile( in );

		in.close();
	}

	private void _parseFile( PeekingBufferedReader in )
		throws IOException, ParseException
	{
		int currentLineNum = 1;
		
		while( in.peekLine() != null )
		{
			SectionTriple sectionTriple = _parseSection( in, currentLineNum );
			
			put( sectionTriple );
			
			currentLineNum = sectionTriple.endingLineNum + 1;
		}
	}

	private SectionTriple _parseSection(	PeekingBufferedReader in,
											int currentLineNum )
		throws IOException, ParseException
	{
		String sectionName = _parseSectionOpenTag( in, currentLineNum );

		// Represents all the section that we are parsing.
		Section section = null;

		// Stores the subsections found.
		SectionMap subSections = new SectionMap();

		String line;

		while( (line = in.peekLine()) != null )
		{
			line = line.trim();
			++currentLineNum;

			if( line.charAt( 0 ) == '[' )
			{
				if( line.charAt( 1 ) == '#' )
				{
					_parseSectionCloseTag( in, sectionName, currentLineNum );
					
					// We're done parsing this section.
					break;
				}
				else
				{
					// It must be an opening tag. We found a sub section.
					SectionTriple subSectionTriple =
						_parseSection( in, currentLineNum );

					subSections.put( subSectionTriple );

					currentLineNum = subSectionTriple.endingLineNum;
				}
			}
			else
			{
				// We found a config line.
				ConfigLine configLine = _parseConfigLine( in, currentLineNum );

				if( section == null )
				{
					if( configLine.isSetting() )
					{
						section = new SettingsSection( subSections );
					}
					else // Assume it's a DataConfigLine
					{
						section = new DataSection( subSections );
					}
				}

				try
				{
					section.put( configLine );
				}
				catch( Exception ex )
				{
					String msg =	"Exception on line " + currentLineNum + ": "
									+ ex.getMessage();
					
					throw new ParseException( msg );
				}
			}
		}

		// Check to see if there were no config lines in the section.
		if( section == null )
		{
			// Wrap the subsections in a generic section.
			section = new Section( subSections );
		}

		return new SectionTriple( sectionName, section, currentLineNum );
	}

	/**
	 * Parses a section open tag, i.e., "[SECTIONNAME]"
	 * @param in
	 * @return The name of the section.
	 * @throws IOException
	 * @throws jqcadesigner.ConfigFile.ParseException
	 */
	private String _parseSectionOpenTag( PeekingBufferedReader in, int lineNum )
		throws IOException, ParseException
	{
		String line = in.readLine();

		if( line == null )
		{
			String msg =	"Was expecting an opening section tag but the end "
							+ "of the file was reached.";

			throw new ParseException( msg );
		}

		line = line.trim();

		if( line.charAt( 0 ) != '[' || line.charAt( 1 ) == '#' )
		{
			String msg =	"Was expecting an opening section tag on line "
							+ String.valueOf( lineNum ) + ", but found: "
							+ line;

			throw new ParseException( msg );
		}
		else if( !line.endsWith( "]" ) )
		{
			String msg =	"Was expecting ending ']' on line "
							+ String.valueOf( lineNum ) + ", but none found.";

			throw new ParseException( msg );
		}

		return line.substring( 1, line.length() - 1 );
	}

	private void _parseSectionCloseTag(	PeekingBufferedReader in,
										String sectionName,
										int lineNum )
		throws IOException, ParseException
	{
		String line = in.readLine();

		if( line == null )
		{
			String msg =	"Was expecting a closing tag for '" + sectionName
							+ "', but the end of file was reached.";

			throw new ParseException( msg );
		}

		line = line.trim();

		if( !line.equals( "[#" + sectionName + "]" ) )
		{
			String msg =	"Was expecting a closing tag for '" + sectionName
							+ "' on line " + String.valueOf( lineNum ) + ", but "
							+ " found: " + line;

			throw new ParseException( msg );
		}
	}

 	private ConfigLine _parseConfigLine( PeekingBufferedReader in, int lineNum )
		throws IOException, ParseException
	{
		String line = in.peekLine();

		if( line == null )
		{
			String msg =	"Was expecting a config line, but the end of the "
							+ "file was reached.";

			throw new ParseException( msg );
		}

		line = line.trim();

		ConfigLine configLine;

		if( Character.isDigit( line.charAt( 0 ) ) )
		{
			// It must be a data config line.
			configLine = _parseDataConfigLine( in, lineNum );
		}
		else
		{
			// It must be a setting config line.
			configLine = _parseSettingConfigLine( in, lineNum );
		}

		return configLine;
	}

	private DataConfigLine _parseDataConfigLine(	PeekingBufferedReader in,
													int lineNum )
		throws IOException, ParseException
	{
		String line = in.readLine();

		if( line == null )
		{
			String msg =	"Was expecting a data config line, but the end of "
							+ "the file was reached.";

			throw new ParseException( msg );
		}

		String[] pieces = line.split( " " );

		int[] data = new int[ pieces.length ];

		for( int i = 0; i < pieces.length; ++i )
		{
			try
			{
				data[i] = Integer.parseInt( pieces[i] );
			}
			catch( NumberFormatException ex )
			{
				String msg =	"Was expecting an integer on line " + lineNum
								+ " at offset " + line.indexOf( pieces[i] )
								+ ", but found '" + pieces[i] + "'.";
			}
		}

		return new DataConfigLine( data );
	}

	private SettingConfigLine _parseSettingConfigLine(	PeekingBufferedReader in,
														int lineNum )
		throws IOException, ParseException
	{
		String line = in.readLine();

		if( line == null )
		{
			String msg =	"Was expecting a setting config line, but the end "
							+ "of the file was reached.";

			throw new ParseException( msg );
		}

		int equalsIndex = line.indexOf( '=' );

		if( equalsIndex == -1 )
		{
			String msg =	"Was expecting an '=' on line "
							+ String.valueOf( lineNum ) + ", but none was "
							+ "found. " + line.length();

			throw new ParseException( msg );
		}

		String configName;
		String configValue;

		configName = line.substring( 0, equalsIndex ).trim();
		configValue = line.substring( equalsIndex+1 ).trim();

		return new SettingConfigLine( configName, configValue );
	}

	/**
	 * Indicates that something went wrong when parsing the config file.
	 */
	public static class ParseException extends Exception
	{
		public ParseException( String message )
		{
			super( message );
		}
	}
}