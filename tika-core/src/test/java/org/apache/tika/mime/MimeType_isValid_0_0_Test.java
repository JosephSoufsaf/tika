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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MimeType_isValid_0_0_Test {

    @Mock
    private MediaType type;

    private MimeType mimeType;

    @BeforeEach
    public void setUp() {
        mimeType = new MimeType(type);
    }

    @Test
    public void testIsValid_withNullName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mimeType.isValid(null);
        });
    }

    @Test
    public void testIsValid_withEmptyName_returnsFalse() {
        assertFalse(mimeType.isValid(""));
    }

    @Test
    public void testIsValid_withNameContainingInvalidCharacters_returnsFalse() {
        assertFalse(mimeType.isValid("text/plain; charset=UTF-8"));
    }

    @Test
    public void testIsValid_withNameContainingValidCharactersAndSlash_returnsTrue() {
        assertTrue(mimeType.isValid("text/plain"));
    }

    @Test
    public void testIsValid_withNameStartingWithSlash_returnsFalse() {
        assertFalse(mimeType.isValid("/text/plain"));
    }

    @Test
    public void testIsValid_withNameEndingWithSlash_returnsFalse() {
        assertFalse(mimeType.isValid("text/plain/"));
    }

    @Test
    public void testIsValid_withNameContainingConsecutiveSlashes_returnsFalse() {
        assertFalse(mimeType.isValid("text/plain//"));
    }

    @Test
    public void testIsValid_withNameContainingSlashAtStartAndEnd_returnsFalse() {
        assertFalse(mimeType.isValid("/text/plain/"));
    }
}
