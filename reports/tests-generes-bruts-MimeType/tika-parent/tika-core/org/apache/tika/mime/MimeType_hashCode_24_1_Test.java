package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
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

public class MimeType_hashCode_24_1_Test {

    private MimeType mimeType;

    @BeforeEach
    public void setUp() {
        mimeType = new MimeType(MediaType.TEXT_PLAIN);
    }

    @Test
    public void testHashCode() {
        MimeType mimeType1 = new MimeType(MediaType.TEXT_PLAIN);
        MimeType mimeType2 = new MimeType(MediaType.TEXT_HTML);
        assertEquals(mimeType1.hashCode(), mimeType1.hashCode());
        assertNotEquals(mimeType1.hashCode(), mimeType2.hashCode());
    }
}
