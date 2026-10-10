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

import org.junit.jupiter.api.*;
import org.mockito.*;

public class MimeType_compareTo_22_0_Test {

    private MimeType mimeType1;

    private MimeType mimeType2;

    @BeforeEach
    public void setUp() {
        mimeType1 = new MimeType(MediaType.OCTET_STREAM);
        mimeType2 = new MimeType(MediaType.TEXT_PLAIN);
    }

    @Test
    public void testCompareTo() {
        assertEquals(0, mimeType1.compareTo(mimeType1));
        assertTrue(mimeType1.compareTo(mimeType2) < 0);
        assertTrue(mimeType2.compareTo(mimeType1) > 0);
    }
}
