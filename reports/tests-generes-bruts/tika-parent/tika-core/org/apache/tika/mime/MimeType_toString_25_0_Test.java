package org.apache.tika.mime;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MimeType_toString_25_0_Test {

    private MimeType mimeType;

    @BeforeEach
    public void setUp() {
        MediaType type = new MediaType("text", "plain", Collections.emptyMap());
        mimeType = new MimeType(type);
    }

    @Test
    public void testToString() {
        assertEquals("text/plain", mimeType.toString());
    }
}
