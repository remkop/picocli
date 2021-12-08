package picocli;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

public class Issue1351 {
    static boolean testUsed;
    static class MyIterator implements Iterator<String> {
        private int cursor;
        private final String[] a;

        MyIterator(String[] a) {
            this.a = a;
        }

        //@Override
        public boolean hasNext() {
            // Do something in the iterator, maybe talking to a server as was mentioned in issue 1351.
            testUsed = true;
            return this.cursor < this.a.length;
        }

        //@Override
        public String next() {
            int i = this.cursor;
            if (i >= this.a.length) {
                throw new NoSuchElementException();
            } else {
                this.cursor = i + 1;
                return this.a[i];
            }
        }

        //@Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    static class MyIterable implements Iterable<String> {
        //@Override
        public Iterator<String> iterator() {
            return new MyIterator(new String[]{"A", "B", "C"});
        }
    }

    @CommandLine.Command
    class TestCommandWithoutCompletion {
        @CommandLine.Option(names = "-o", completionCandidates = MyIterable.class,
                description = "Candidates: A, B, C")
        String option;
    }

    @CommandLine.Command
    class TestCommandWithCompletion{
        @CommandLine.Option(names = "-o", completionCandidates = MyIterable.class,
                description = "Candidates: ${COMPLETION-CANDIDATES}")
        String option;
    }

    @Test
    public void testCompletionCandidatesUnused() {
        testUsed = false;
        CommandLine.usage(new TestCommandWithoutCompletion(), System.out);
        assertEquals(false, testUsed);
    }

    @Test
    public void testCompletionCandidatesUsed(){
        testUsed = false;
        CommandLine.usage(new TestCommandWithCompletion(), System.out);
        assertEquals(true, testUsed);
    }
}
