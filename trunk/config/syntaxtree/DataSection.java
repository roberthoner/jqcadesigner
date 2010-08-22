/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jqcadesigner.config.syntaxtree;

import java.util.ArrayList;

/**
 *
 * @author Robert
 */
public class DataSection extends Section
{
	public final ArrayList<int[]> data;

	public DataSection()
	{
		data = new ArrayList<int[]>();
	}

	public DataSection( SectionMap subSections )
	{
		super( subSections );

		data = new ArrayList<int[]>();
	}

	@Override
	public void put( ConfigLine configLine )
	{
		if( !configLine.isData() )
		{
			String msg =	"You can only put DataConfigLines into "
							+ "DataSections.";

			throw new RuntimeException( msg );
		}

		put( (DataConfigLine)configLine );
	}

	public void put( DataConfigLine dataConfigLine )
	{
		data.add( dataConfigLine.data );
	}

	@Override
	public boolean containsData()
	{
		return true;
	}
}
