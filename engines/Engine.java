package jqcadesigner.engines;

import java.io.PrintStream;

import jqcadesigner.Circuit;
import jqcadesigner.VectorTable;

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
	
	public RunResults run( VectorTable vectorTable, boolean output )
	{
		RunResults retval;
		
		long startTime = System.currentTimeMillis();
		
		retval = _run( vectorTable, output );
		
		retval.runTime = System.currentTimeMillis() - startTime;
		
		return retval;
	}
	
	abstract protected RunResults _run( VectorTable vectorTable, boolean output );
	
	public abstract class RunResults
	{
		public long runTime;
		
		abstract public void printStats();
	}
}
