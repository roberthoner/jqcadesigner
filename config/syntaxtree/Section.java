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
public class Section
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

	public boolean containsSubSections( String ... subSectionNames )
	{
		for( String subSectionName : subSectionNames )
		{
			// Using get here instead of containsKey, so that I can explicitly
			// check for null values.  Just because the key may exist doesn't
			// mean that the key doesn't point to a null value. Checking
			// explicitly for null values helps avoid NullPointerExceptions.
			if( subSections.get( subSectionName ) == null )
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Take in config line data, this should be overridden by a subclass.
	 *
	 * @param configLine
	 */
	public void put( ConfigLine configLine )
	{
		throw new RuntimeException( "Cannot put into this section." );
	}

	public boolean hasSettings()
	{
		return false;
	}

	public boolean hasData()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}
}
