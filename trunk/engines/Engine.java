package jqcadesigner.engines;

import java.io.PrintStream;

import jqcadesigner.Circuit;

public abstract class Engine
{
	private Circuit _circuit;
	private PrintStream _out;
	
	public Engine( Circuit circuit, PrintStream out )
	{
		_circuit = circuit;
		_out = System.out;
	}
	
	public Engine( Circuit circuit )
	{
		this( circuit, System.out );
	}
	
	public RunResults run( boolean output )
	{
		RunResults retval;
		
		long startTime = System.currentTimeMillis();
		
		retval = _run( output );
		
		retval.runTime = System.currentTimeMillis() - startTime;
		
		return retval;
	}
	
	abstract protected RunResults _run( boolean output );
	
	public abstract class RunResults
	{
		public long runTime;
		
		abstract public void printStats();
	}
}
