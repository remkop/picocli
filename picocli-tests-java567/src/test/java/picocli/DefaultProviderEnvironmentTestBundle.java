package picocli;

import java.util.ListResourceBundle;

/**
 * Resource bundle for DefaultProviderTest#testDefaultValueProviderWithVariablesResolvesResourceBundle
 */
public class DefaultProviderEnvironmentTestBundle extends ListResourceBundle {
    protected Object[][] getContents() {
        return new Object[][] {
            // KEY VALUE PAIRS
            {"VARIABLE", "789"},
            // END OF MATERIAL TO LOCALIZE
        };
    }
}
