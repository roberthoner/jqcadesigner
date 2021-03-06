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

package jqcadesigner.config.syntaxtree;

import java.util.HashMap;

/**
 *
 * @author Robert
 */
public class SettingsSection extends Section
{
	public final HashMap<String, String> settings;

	public SettingsSection()
	{
		settings = new HashMap<String, String>();
	}

	public SettingsSection( SectionMap subSections )
	{
		super( subSections );
		
		settings = new HashMap<String, String>();
	}

	@Override
	public void put( ConfigLine configLine )
	{
		assert configLine != null;
		
		if( !configLine.isSetting() )
		{
			String msg =	"You can only put SettingConfigLines into "
							+ "SettingsSections.";

			throw new RuntimeException( msg );
		}

		put( (SettingConfigLine)configLine );
	}

	public void put( SettingConfigLine settingConfigLine )
	{
		assert settingConfigLine != null;
		
		settings.put( settingConfigLine.name, settingConfigLine.value );
	}

	/**
	 * Determines whether the setting names specified are defined.
	 * @param settingNames
	 * @return
	 */
	public boolean containsSettings( String ... settingNames )
	{
		for( String settingName : settingNames )
		{
			// Using "get" here instead of "containsKey", so that I can explicitly
			// check for null values.  Just because the key may exist doesn't
			// mean that the key doesn't point to a null value. Checking
			// explicitly for null values helps avoid NullPointerExceptions.
			if( settings.get( settingName ) == null )
			{
				return false;
			}
		}

		return true;
	}

	public int get( String key, int defaultValue )
	{
		int retval = defaultValue;

		String value = settings.get( key );
		if( value != null )
		{
			retval = Integer.parseInt( value );
		}

		return retval;
	}

	public String get( String key, String defaultValue )
	{
		String retval = defaultValue;

		String value = settings.get( key );
		if( value != null )
		{
			retval = value;
		}

		return retval;
	}

	public double get( String key, double defaultValue )
	{
		double retval = defaultValue;

		String value = settings.get( key );
		if( value != null )
		{
			retval = Double.parseDouble( value );
		}

		return retval;
	}

	public boolean get( String key, boolean defaultValue )
	{
		boolean retval = defaultValue;

		String value = settings.get( key );
		if( value != null )
		{
			retval = Boolean.parseBoolean( value );
		}

		return retval;
	}

	@Override
	public boolean hasSettings()
	{
		return true;
	}
}
