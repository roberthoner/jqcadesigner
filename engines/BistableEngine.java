package jqcadesigner.engines;

import java.io.PrintStream;

import jqcadesigner.Circuit;
import jqcadesigner.VectorTable;

public final class BistableEngine extends Engine
{
	public BistableEngine( Circuit circuit )
	{
		this( circuit, System.out, null );
	}
	
	public BistableEngine( Circuit circuit, PrintStream out )
	{
		this( circuit, out, null );
	}
	
	public BistableEngine( Circuit circuit, PrintStream out, String configFileName )
	{
		super( circuit, out );
		
		if( configFileName != null )
		{
			_loadConfig( configFileName );
		}
	}

	private void _loadConfig( String configFileName )
	{
		
	}
	
	@Override
	protected RunResults _run( VectorTable vectorTable, boolean output )
	{
		RunResults retval = new RunResults();
		

		
		return retval;
	}
	
	public class RunResults extends Engine.RunResults
	{
		@Override
		public void printStats()
		{
			
		}
	}
}
