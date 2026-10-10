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
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;

public class MimeType_hasMagic_17_0_Test {

    private MimeType mimeType;

    @BeforeEach
    public void setUp() {
        mimeType = new MimeType(MediaType.OCTET_STREAM);
    }

    @Test
    public void testHasMagicWhenNoMagicAdded() {
        assertFalse(mimeType.hasMagic());
    }

    @Test
    public void testHasMagicAfterAddingMagic() {
        mimeType.addMagic(mock(Magic.class));
        assertTrue(mimeType.hasMagic());
    }

    @Test
    public void testHasMagicIgnoresNullMagic() {
        mimeType.addMagic(null);
        assertFalse(mimeType.hasMagic());
    }
}
