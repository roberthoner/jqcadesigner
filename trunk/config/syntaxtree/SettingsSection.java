/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
		settings.put( settingConfigLine.name, settingConfigLine.value );
	}

	@Override
	public boolean containsSettings()
	{
		return true;
	}
}
