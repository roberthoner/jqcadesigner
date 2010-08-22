/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jqcadesigner.config.syntaxtree;

import java.util.HashMap;

/**
 * Maps section names to groups of sections that were founding with that name.
 *
 * @author Robert
 */
public class SectionMap extends HashMap<String, SectionGroup>
{
	/**
	 * Adds the section to the SectionGroup mapped to by sectionName.
	 *
	 * @param sectionName The name of the section.
	 * @param section The section to be added.
	 */
	public void put( String sectionName, Section section )
	{
		SectionGroup sectionGroup = get( sectionName );

		if( sectionGroup != null )
		{
			sectionGroup.add( section );
		}
		else
		{
			sectionGroup = new SectionGroup();
			sectionGroup.add( section );

			super.put( sectionName, sectionGroup );
		}
	}

	public void put( SectionTriple sectionTriple )
	{
		put( sectionTriple.name, sectionTriple.section );
	}

}