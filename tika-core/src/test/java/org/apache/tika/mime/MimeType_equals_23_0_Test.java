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

public class MimeType_equals_23_0_Test {

    private MimeType mimeType;

    private MediaType mediaType;

    @BeforeEach
    public void setUp() {
        mediaType = new MediaType("image", "jpeg", Collections.emptyMap());
        mimeType = new MimeType(mediaType);
    }

    @Test
    public void testEqualsObject() {
        MimeType sameMimeType = new MimeType(mediaType);
        MimeType differentMimeType = new MimeType(new MediaType("text", "plain", Collections.emptyMap()));
        assertTrue(mimeType.equals(sameMimeType));
        assertFalse(mimeType.equals(differentMimeType));
        assertFalse(mimeType.equals(null));
        assertFalse(mimeType.equals(new Object()));
    }
}
