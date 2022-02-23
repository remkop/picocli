/**
 * Provides classes and interfaces for picocli Spring Boot Auto-configuration.
 * <p>
 * The existence of this class allows Spring to auto-configure any fields of
 * type {@code picocli.CommandLine.IFactory} in a {@code @org.springframework.stereotype.Component}-annotated class.
 * See the example below.
 * </p>
 * <pre>
 * import picocli.CommandLine;
 * import picocli.CommandLine.IFactory;
 *
 * import org.springframework.boot.CommandLineRunner;
 * import org.springframework.boot.ExitCodeGenerator;
 * import org.springframework.stereotype.Component;
 *
 * &#64;Component
 * public class MyApplicationRunner implements CommandLineRunner, ExitCodeGenerator {
 *
 * 	private final MyCommand myCommand;
 *
 * 	private final IFactory factory; // auto-configured to inject PicocliSpringFactory
 *
 * 	private int exitCode;
 *
 * 	  public MyApplicationRunner(MyCommand myCommand, IFactory factory) {
 * 		this.myCommand = myCommand;
 * 		this.factory = factory;
 *    }
 *
 *    &#64;Override
 *    public void run(String... args) throws Exception {
 * 		exitCode = new CommandLine(myCommand, factory).execute(args);
 *    }
 *
 *    &#64;Override
 *    public int getExitCode() {
 * 		return exitCode;
 *    }
 * }
 * </pre>
 */
package picocli.spring.boot.autoconfigure;
