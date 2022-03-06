/**
 * Picocli: Custom converter demo
 * Explanation: <a href="https://picocli.info/#_handling_invalid_input">Picocli user manual</a>
 * Discussion: <a href="https://github.com/remkop/picocli/issues/972">GitHub issue</a>
 * Origin of source code: <a href="https://github.com/remkop/picocli/tree/main/picocli-examples/src/main/java/picocli/examples/typeconverter">GitHub</a>
 * @author Remko Popma
 */
package picocli.examples.typeconverter;

import picocli.CommandLine;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import picocli.CommandLine.TypeConversionException;

import java.net.InetSocketAddress;

public class InetSocketAddressConverterDemo implements Runnable {

    static class InetSocketAddressConverter implements ITypeConverter<InetSocketAddress> {
        @Override
        public InetSocketAddress convert(String value) {
            int pos = value.lastIndexOf(':');
            if (pos < 0) {
                //throw new IllegalArgumentException("Invalid format: must be 'host:port' but was '" + value + "'");
                throw new TypeConversionException("Invalid format: must be 'host:port' but was '" + value + "'");
            }
            String adr = value.substring(0, pos);
            int port = Integer.parseInt(value.substring(pos + 1)); // invalid port shows the generic error message
            return new InetSocketAddress(adr, port);
        }
    }

    @Option(names = { "-s", "--socket-address" }, converter = InetSocketAddressConverter.class)
    InetSocketAddress address;

    public static void main(String[] args) {
        new CommandLine(new InetSocketAddressConverterDemo()).execute("--socket-address=invalidInputWithoutColon");

        // try this also:
        //new CommandLine(new InetSocketAddressConverterDemo()).execute("--socket-address=invalidInputWithColon:xxx");
    }

    @Override
    public void run() {
        System.out.println(String.format("Valid socket address given: host: %s, port: %s", address.getHostString(), address.getPort()));
    }
}
