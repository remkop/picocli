/*
   Copyright 2017 Remko Popma

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package picocli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class StringPrintStream {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public PrintStream stream() {
        baos.reset();
        return new PrintStream(baos);
    }

    public PrintStream stream(String charsetName) throws UnsupportedEncodingException {
        baos.reset();
        return new PrintStream(baos, false, charsetName);
    }

    public String toString() {
        return baos.toString();
    }

    public String toString(String charsetName) throws UnsupportedEncodingException {
        return baos.toString(charsetName);
    }
}
