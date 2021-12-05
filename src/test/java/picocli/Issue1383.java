package picocli;
import junitparams.JUnitParamsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import picocli.ArgGroupTest.CommandMethodsWithGroupsAndMixins.InvokedSub;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.*;

import static picocli.ArgGroupTest.CommandMethodsWithGroupsAndMixins.InvokedSub.groupFirst;
import static picocli.ArgGroupTest.CommandMethodsWithGroupsAndMixins.InvokedSub.posAndMixin;
import static picocli.ArgGroupTest.CommandMethodsWithGroupsAndMixins.InvokedSub.posAndOptAndMixin;
import static picocli.ArgGroupTest.CommandMethodsWithGroupsAndMixins.InvokedSub.withMixin;


//Bad Error Message
public class Issue1383 implements Runnable{

    // Case 1

    @CommandLine.Option(names="-t",
            split="\\s"
            ,converter = TestLowerCaseConverter.class
    )

    //@CommandLine.ArgGroup(exclusive = false, heading = "%nGroup 1%n%n")
    //TestStringList lst = new TestStringList();
    List<String> args;

    //public List<MyType> args;

    static class TestStringList {
        @CommandLine.Option(names = "-t", split = "\\s", converter = MyType.MyConverter.class, description = "List of Strings")
        ArrayList<MyType> args;


    }

    public static class TestLowerCaseConverter implements CommandLine.ITypeConverter<String> {

        @Override
        public String convert(String arg) throws Exception {
            if (!arg.toLowerCase().equals(arg))
                throw new CommandLine.TypeConversionException("Text must be lower case");

            return new String(arg);
        }

    }
    // Case 2
    /*
	@CommandLine.Option(names="-t",
			converter = MyType.MyConverter.class
			)
	//public MyType args;
    public List<MyType> args;

     */

    static class MyType {

        String lowCaseStr;

        private MyType(String txt){
            lowCaseStr = txt;
        }

        public static class MyConverter implements CommandLine.ITypeConverter<MyType> {

            @Override
            public MyType convert(String arg) throws Exception {
                if (!arg.toLowerCase().equals(arg))
                    throw new CommandLine.TypeConversionException("Text must be lower case");

                return new MyType(arg);
            }

        }
        public String toString() {return lowCaseStr;}
    }

    public void run() {
        int a = 0;
    }


    public static void main(String[] args) {
        Issue1383 msgs = new Issue1383();
        // Case 1 - list

        new CommandLine(msgs).execute("-t","some txt", "-t","Bad input");
        //		Results in message: Unmatched argument at index 2: 'Bad input'


        // Case 2 - single argument
        /*
        new CommandLine(msgs).execute("-t","Bad input");
        //  Results in message: Invalid value for option '-t' (<args>): Text must be lower case

         */

    }

}
/*
// Fixed example use case
public class Issue1383 implements Runnable{

    @CommandLine.Option(names="-t",
            split="\\s"
            ,converter = TestLowerCaseConverter.class
    )

    List<String> args;

    static class TestStringList {
        @CommandLine.Option(names = "-t", split = "\\s", converter = MyType.MyConverter.class, description = "List of Strings")
        ArrayList<MyType> args;
    }

    public static class TestLowerCaseConverter implements CommandLine.ITypeConverter<String> {

        @Override
        public String convert(String arg) throws Exception {
            if (!arg.toLowerCase().equals(arg))
                throw new CommandLine.TypeConversionException("Text must be lower case");

            return new String(arg);
        }

    }

    public void run() {
        int a = 0;
    }


    public static void main(String[] args) {
        Issue1383 msgs = new Issue1383();

        new CommandLine(msgs).execute("-t","some txt", "-t","Bad input");
	/*
	/ Invalid value for option '-t' (<args>): Text must be lower case
	/ Usage: <main class> [-t=<args>[\s<args>...]]...
 	/ -t=<args>[\s<args>...]
	*/
	/*
            }

            }



            */