package picocli.issue803;

import picocli.CommandLine.*;
import picocli.CommandLine.Model.*;

import java.util.Stack;
import java.util.concurrent.Callable;

import picocli.CommandLine;

@Command
public class Issue803 implements Callable<Integer> {
	@Parameters(index = "0", description = "Ignored", parameterConsumer = LocalPC.class)
	private String file;
	@Override
	public Integer call() throws Exception {return 0;}

	public static class LocalPC implements IParameterConsumer {
		@Override public void consumeParameters(Stack<String> args, ArgSpec argSpec, CommandSpec commandSpec) {return;}
	}
}
