/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jqcadesigner.config.syntaxtree;

import java.util.ArrayList;

/**
 * Groups Sections together that have the section name in the config file.
 *
 * Since there can potentially be many sections with the same section name,
 * e.g., "QCADLayer", sections of the same name need to be grouped together.
 *
 * @author Robert
 */
public class SectionGroup extends ArrayList<Section>
{
	
}