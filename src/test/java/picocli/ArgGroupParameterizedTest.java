package picocli;

import junitparams.JUnitParamsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import picocli.ArgGroupTest.CommandMethodsWithGroupsAndMixins.InvokedSub;

import java.util.EnumSet;

import static org.junit.Assert.*;

import static picocli.ArgGroupTest.CommandMethodsWithGroupsAndMixins.InvokedSub.groupFirst;
import static picocli.ArgGroupTest.CommandMethodsWithGroupsAndMixins.InvokedSub.posAndMixin;
import static picocli.ArgGroupTest.CommandMethodsWithGroupsAndMixins.InvokedSub.posAndOptAndMixin;
import static picocli.ArgGroupTest.CommandMethodsWithGroupsAndMixins.InvokedSub.withMixin;

/**
 * This test uses junitparams.JUnitParamsRunner.
 * It cannot be run with Java 5.
 */
@RunWith(JUnitParamsRunner.class)
public class ArgGroupParameterizedTest {

    private Object[] commandMethodArgs() {
        ArgGroupTest.SomeMixin _000_000 = new ArgGroupTest.SomeMixin();
        ArgGroupTest.SomeMixin _123_000 = new ArgGroupTest.SomeMixin(123, 0);
        ArgGroupTest.SomeMixin _000_543 = new ArgGroupTest.SomeMixin(0, 543);
        ArgGroupTest.SomeMixin _123_321 = new ArgGroupTest.SomeMixin(123, 321);
        ArgGroupTest.Composite AB = new ArgGroupTest.Composite(true, true, false, false);
        ArgGroupTest.Composite X = new ArgGroupTest.Composite(false, false, true, false);
        ArgGroupTest.Composite Y = new ArgGroupTest.Composite(false, false, false, true);
        int[] _987 = new int[] {9, 8, 7};
        String[] _sXY = new String[] {"X", "Y"};
        return new Object[][]{
                {"withMixin",                     withMixin, _000_000, null, null, null},
                {"withMixin -i=123",              withMixin, _123_000, null, null, null},
                {"withMixin -L=543",              withMixin, _000_543, null, null, null},
                {"withMixin -i=123 -L=321",       withMixin, _123_321, null, null, null},
                {"withMixin -a -b",               withMixin, _000_000, AB, null, null},
                {"withMixin -i=123 -a -b",        withMixin, _123_000, AB, null, null},
                {"withMixin -L=543 -a -b",        withMixin, _000_543, AB, null, null},
                {"withMixin -i=123 -L=321 -a -b", withMixin, _123_321, AB, null, null},
                {"withMixin -x",                  withMixin, _000_000, X, null, null},
                {"withMixin -i=123 -x",           withMixin, _123_000, X, null, null},
                {"withMixin -L=543 -y",           withMixin, _000_543, Y, null, null},
                {"withMixin -i=123 -L=321 -y",    withMixin, _123_321, Y, null, null},

                {"posAndMixin 9 8 7",                     posAndMixin, _000_000, null, _987, null},
                {"posAndMixin 9 8 7 -i=123",              posAndMixin, _123_000, null, _987, null},
                {"posAndMixin 9 8 7 -L=543",              posAndMixin, _000_543, null, _987, null},
                {"posAndMixin 9 8 7 -i=123 -L=321",       posAndMixin, _123_321, null, _987, null},
                {"posAndMixin 9 8 7 -a -b",               posAndMixin, _000_000, AB, _987, null},
                {"posAndMixin 9 8 7 -i=123 -a -b",        posAndMixin, _123_000, AB, _987, null},
                {"posAndMixin 9 8 7 -L=543 -a -b",        posAndMixin, _000_543, AB, _987, null},
                {"posAndMixin 9 8 7 -i=123 -L=321 -a -b", posAndMixin, _123_321, AB, _987, null},
                {"posAndMixin 9 8 7 -x",                  posAndMixin, _000_000, X, _987, null},
                {"posAndMixin 9 8 7 -i=123 -x",           posAndMixin, _123_000, X, _987, null},
                {"posAndMixin 9 8 7 -L=543 -y",           posAndMixin, _000_543, Y, _987, null},
                {"posAndMixin 9 8 7 -i=123 -L=321 -y",    posAndMixin, _123_321, Y, _987, null},

                {"posAndOptAndMixin 9 8 7 -sX -sY ",                    posAndOptAndMixin, _000_000, null, _987, _sXY},
                {"posAndOptAndMixin 9 8 7 -sX -sY -i=123",              posAndOptAndMixin, _123_000, null, _987, _sXY},
                {"posAndOptAndMixin 9 8 7 -sX -sY -L=543",              posAndOptAndMixin, _000_543, null, _987, _sXY},
                {"posAndOptAndMixin 9 8 7 -sX -sY -i=123 -L=321",       posAndOptAndMixin, _123_321, null, _987, _sXY},
                {"posAndOptAndMixin 9 8 7 -sX -sY -a -b",               posAndOptAndMixin, _000_000, AB, _987, _sXY},
                {"posAndOptAndMixin 9 8 7 -sX -sY -i=123 -a -b",        posAndOptAndMixin, _123_000, AB, _987, _sXY},
                {"posAndOptAndMixin 9 8 7 -sX -sY -L=543 -a -b",        posAndOptAndMixin, _000_543, AB, _987, _sXY},
                {"posAndOptAndMixin 9 8 7 -sX -sY -i=123 -L=321 -a -b", posAndOptAndMixin, _123_321, AB, _987, _sXY},
                {"posAndOptAndMixin 9 8 7 -sX -sY -x",                  posAndOptAndMixin, _000_000, X, _987, _sXY},
                {"posAndOptAndMixin 9 8 7 -sX -sY -i=123 -x",           posAndOptAndMixin, _123_000, X, _987, _sXY},
                {"posAndOptAndMixin 9 8 7 -sX -sY -L=543 -y",           posAndOptAndMixin, _000_543, Y, _987, _sXY},
                {"posAndOptAndMixin 9 8 7 -sX -sY -i=123 -L=321 -y",    posAndOptAndMixin, _123_321, Y, _987, _sXY},

                {"groupFirst 9 8 7 -sX -sY ",                    groupFirst, _000_000, null, _987, _sXY},
                {"groupFirst 9 8 7 -sX -sY -i=123",              groupFirst, _123_000, null, _987, _sXY},
                {"groupFirst 9 8 7 -sX -sY -L=543",              groupFirst, _000_543, null, _987, _sXY},
                {"groupFirst 9 8 7 -sX -sY -i=123 -L=321",       groupFirst, _123_321, null, _987, _sXY},
                {"groupFirst 9 8 7 -sX -sY -a -b",               groupFirst, _000_000, AB, _987, _sXY},
                {"groupFirst 9 8 7 -sX -sY -i=123 -a -b",        groupFirst, _123_000, AB, _987, _sXY},
                {"groupFirst 9 8 7 -sX -sY -L=543 -a -b",        groupFirst, _000_543, AB, _987, _sXY},
                {"groupFirst 9 8 7 -sX -sY -i=123 -L=321 -a -b", groupFirst, _123_321, AB, _987, _sXY},
                {"groupFirst 9 8 7 -sX -sY -x",                  groupFirst, _000_000, X, _987, _sXY},
                {"groupFirst 9 8 7 -sX -sY -i=123 -x",           groupFirst, _123_000, X, _987, _sXY},
                {"groupFirst 9 8 7 -sX -sY -L=543 -y",           groupFirst, _000_543, Y, _987, _sXY},
                {"groupFirst 9 8 7 -sX -sY -i=123 -L=321 -y",    groupFirst, _123_321, Y, _987, _sXY},
        };
    }

    @Test
    @junitparams.Parameters(method = "commandMethodArgs")
    public void testCommandMethod(String args,
                                  InvokedSub invokedSub,
                                  ArgGroupTest.SomeMixin expectedMixin,
                                  ArgGroupTest.Composite expectedArgGroup,
                                  int[] expectedPositionalInt,
                                  String[] expectedStrings) {
        ArgGroupTest.CommandMethodsWithGroupsAndMixins bean = new ArgGroupTest.CommandMethodsWithGroupsAndMixins();
        new CommandLine(bean).execute(args.split(" "));
        assertTrue(bean.invoked.contains(invokedSub));
        EnumSet<InvokedSub> notInvoked = EnumSet.allOf(InvokedSub.class);
        notInvoked.remove(invokedSub);
        for (InvokedSub sub : notInvoked) {
            assertFalse(bean.invoked.contains(sub));
        }
        assertTrue(bean.invoked.contains(invokedSub));
        assertEquals(expectedMixin, bean.myMixin);
        assertEquals(expectedArgGroup, bean.myComposite);
        assertArrayEquals(expectedPositionalInt, bean.myPositionalInt);
        assertArrayEquals(expectedStrings, bean.myStrings);
    }

}
