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

	@Option(names = "-x", converter = {IntConverter.class, DoubleConverter.class}, parameterConsumer = LocalPC2.class)
	private Integer x;

	@Override
	public Integer call() throws Exception {return 0;}

	public static class IntConverter implements ITypeConverter<Integer> {
		public Integer convert(String value) {
			return 123;
		}
	}
	public static class DoubleConverter implements ITypeConverter<Double> {
		public Double convert(String value) {
			return 456.0;
		}
	}

	public static class LocalPC implements IParameterConsumer {
		@Override public void consumeParameters(Stack<String> args, ArgSpec argSpec, CommandSpec commandSpec) {return;}
	}

	public static class LocalPC2 implements IParameterConsumer {
		@Override public void consumeParameters(Stack<String> args, ArgSpec argSpec, CommandSpec commandSpec) {return;}
	}
}
