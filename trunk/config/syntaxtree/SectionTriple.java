/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jqcadesigner.config.syntaxtree;

/**
 * Pairs a section name with a section.
 *
 * Used as a way of returning a section name and a section together.
 * The Section class does not have a "name" field in order to minimize
 * data redundancy and memory usage.
 */
public class SectionTriple
{
	public final String name;
	public final Section section;
	public final int endingLineNum;

	public SectionTriple( String n, Section s, int e )
	{
		name = n;
		endingLineNum = e;
		section = s;
	}
}