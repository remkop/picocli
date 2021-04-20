package picocli;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

public class Issue1351 {
    static int flag = 0;

    static class MyIterator implements Iterator<String> {
        private int cursor;
        private final String[] a;

        MyIterator(String[] a) {
            this.a = a;
        }

        @Override
        public boolean hasNext() {
            // Do something in the iterator, maybe talking to a server as was mentioned in issue 1351.
            flag = flag + 1;
            return this.cursor < this.a.length;
        }

        @Override
        public String next() {
            int i = this.cursor;
            if (i >= this.a.length) {
                throw new NoSuchElementException();
            } else {
                this.cursor = i + 1;
                return this.a[i];
            }
        }
    }

    static class MyIterable implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            return new MyIterator(new String[]{"A", "B", "C"});
        }
    }

    @CommandLine.Command
    class TestCommand {
        @CommandLine.Option(names = "-o", completionCandidates = MyIterable.class,
                description = "Candidates: A, B, C")
        String option;
    }

    @Test
    public void testIssue1351() {
        CommandLine.usage(new TestCommand(), System.out);
        assertEquals(0, flag);
    }
}
