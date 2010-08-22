/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jqcadesigner.config.syntaxtree;

/**
 *
 * @author Robert
 */
public class SettingConfigLine extends ConfigLine
{
	public final String name;
	public final String value;

	public SettingConfigLine( String n, String v )
	{
		name = n;
		value = v;
	}

	@Override
	public boolean isSetting()
	{
		return true;
	}
}
