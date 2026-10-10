/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.mime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.*;
import org.mockito.*;

public class MimeType_matches_19_0_Test {

    @Test
    public void testConstructor() throws Exception {
        MediaType type = new MediaType("application", "octet-stream");
        MimeType mimeType = new MimeType(type);
        assertEquals(type, mimeType.getType());
    }

    @Test
    public void testGetMinLength() throws Exception {
        MediaType type = new MediaType("application", "octet-stream");
        MimeType mimeType = new MimeType(type);
        assertEquals(0, mimeType.getMinLength());
    }

    @Test
    public void testSetInterpreted() throws Exception {
        MediaType type = new MediaType("application", "octet-stream");
        MimeType mimeType = new MimeType(type);
        mimeType.setInterpreted(true);
        assertTrue(mimeType.isInterpreted());
    }

    @Test
    public void testGetLinks() throws Exception {
        MediaType type = new MediaType("application", "octet-stream");
        MimeType mimeType = new MimeType(type);
        assertEquals(Collections.emptyList(), mimeType.getLinks());
    }

    @Test
    public void testGetAcronym() throws Exception {
        MediaType type = new MediaType("application", "octet-stream");
        MimeType mimeType = new MimeType(type);
        assertEquals("", mimeType.getAcronym());
    }

    @Test
    public void testGetUniformTypeIdentifier() throws Exception {
        MediaType type = new MediaType("application", "octet-stream");
        MimeType mimeType = new MimeType(type);
        assertEquals("", mimeType.getUniformTypeIdentifier());
    }
}
