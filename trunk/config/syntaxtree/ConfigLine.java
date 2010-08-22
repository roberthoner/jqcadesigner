/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jqcadesigner.config.syntaxtree;

/**
 *
 * @author Robert
 */
public abstract class ConfigLine
{
	public boolean isSetting()
	{
		return false;
	}

	public boolean isData()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return getClass().getName();
	}
}
