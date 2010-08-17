package jqcadesigner;

import java.io.File;

/**
 * Contains some simple and handy functions
 * @author Robert
 */
public final class UtilityFunctions
{
	public static boolean doesFileExist( String fileName )
	{
		return (new File( fileName )).exists();
	}
}
