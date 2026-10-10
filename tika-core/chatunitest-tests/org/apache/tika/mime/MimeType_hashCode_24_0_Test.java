package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.junit.jupiter.api.function.Executable;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class MimeType_hashCode_24_0_Test {

    private MimeType mimeType;

    @BeforeEach
    public void setUp() {
        MediaType type = new MediaType("application", "octet-stream", Collections.emptyMap());
        mimeType = new MimeType(type);
    }

    @Test
    public void testHashCode() {
        int hashCode = mimeType.hashCode();
        assertEquals("application/octet-stream".hashCode(), hashCode);
    }
}
