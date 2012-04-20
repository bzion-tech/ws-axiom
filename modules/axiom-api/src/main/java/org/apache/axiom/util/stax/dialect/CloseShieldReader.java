/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axiom.util.stax.dialect;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

public class CloseShieldReader extends Reader {
    private final Reader parent;

    public CloseShieldReader(Reader parent) {
        this.parent = parent;
    }

    public void close() throws IOException {
    }

    public void mark(int readAheadLimit) throws IOException {
        parent.mark(readAheadLimit);
    }

    public boolean markSupported() {
        return parent.markSupported();
    }

    public int read() throws IOException {
        return parent.read();
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        return parent.read(cbuf, off, len);
    }

    public int read(char[] cbuf) throws IOException {
        return parent.read(cbuf);
    }

    public int read(CharBuffer target) throws IOException {
        return parent.read(target);
    }

    public boolean ready() throws IOException {
        return parent.ready();
    }

    public void reset() throws IOException {
        parent.reset();
    }

    public long skip(long n) throws IOException {
        return parent.skip(n);
    }
}
