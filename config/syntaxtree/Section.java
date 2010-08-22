/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jqcadesigner.config.syntaxtree;

import java.util.HashMap;

/**
 * Represents a section within the config file.
 *
 * A representation of a section within the configuration file.  A section
 * is defined by blocks like so:
 *
 *		[SECTIONNAME]
 *		mysetting=Some Value
 *		another.setting=1.0
 *		[#SECTIONNAME]
 *
 * This class stores the mapping between setting names and setting values.
 */
public abstract class Section
{
	public final SectionMap subSections;

	/**
	 * Constructs the Section.
	 *
	 * @param startingLineNum The line number in the file that the section started.
	 */
	public Section()
	{
		subSections = new SectionMap();
	}

	public Section( SectionMap ss )
	{
		subSections = ss;
	}

	/**
	 * @see SectionMap.put
	 *
	 * @param sectionName
	 * @param section
	 */
	public void addSubSection( SectionTriple sectionPair )
	{
		subSections.put( sectionPair );
	}

	/**
	 * Take in config line data, this should be overridden by a subclass.
	 *
	 * @param configLine
	 */
	public abstract void put( ConfigLine configLine );

	public boolean containsSettings()
	{
		return false;
	}

	public boolean containsData()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return getClass().getName();
	}
}
