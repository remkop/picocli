package picocli.examples.typeconverter;

import picocli.CommandLine;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import picocli.CommandLine.TypeConversionException;

import java.net.InetSocketAddress;

public class InetSocketAddressConverterDemo {

    static class InetSocketAddressConverter implements ITypeConverter<InetSocketAddress> {
        @Override
        public InetSocketAddress convert(String value) throws Exception {
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

    @Option(names = "-a", converter = InetSocketAddressConverter.class)
    InetSocketAddress address;

    public static void main(String[] args) {
        new CommandLine(new InetSocketAddressConverterDemo()).execute("-a=xxxinvalidinput");

        // try this also:
        //new CommandLine(new InetSocketAddressConverterDemo()).execute("-a=xxxinvalidinput:xxx");
    }
}
