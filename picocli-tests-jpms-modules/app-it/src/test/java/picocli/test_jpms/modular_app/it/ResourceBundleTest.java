package picocli.test_jpms.modular_app.it;

import java.util.Locale;
import java.util.ResourceBundle;
import picocli.test_jpms.modular_app.JpmsModularApp;
import org.junit.jupiter.api.Test;


public class ResourceBundleTest {
    public static void main(String... args) throws Exception {
        new ResourceBundleTest().testLoadBundleModular();
    }

    @Test
    public void testLoadBundle() throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("picocli.test_jpms.modular_app.messages");
        System.out.println(bundle);
        System.out.println(bundle.getString("usage.headerHeading"));
    }

    @Test
    public void testLoadBundleJp() throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("picocli.test_jpms.modular_app.messages", Locale.JAPANESE);
        System.out.println(bundle);
        System.out.println(bundle.getString("usage.headerHeading"));
    }

    @Test
    public void testLoadBundleModular() throws Exception {
        Module module = JpmsModularApp.class.getModule();
        ResourceBundle bundle = ResourceBundle.getBundle("picocli.test_jpms.modular_app.messages", module);
        System.out.println(bundle);
        System.out.println(bundle.getString("usage.headerHeading"));
    }
}
