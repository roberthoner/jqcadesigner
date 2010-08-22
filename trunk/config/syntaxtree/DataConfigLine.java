/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jqcadesigner.config.syntaxtree;

/**
 *
 * @author Robert
 */
public class DataConfigLine extends ConfigLine
{
	public final int[] data;

	public DataConfigLine( int[] d )
	{
		data = d;
	}

	@Override
	public boolean isData()
	{
		return true;
	}
}
