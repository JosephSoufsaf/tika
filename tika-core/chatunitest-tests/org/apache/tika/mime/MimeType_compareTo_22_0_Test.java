package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        assertEquals(1, mimeType1.compareTo(mimeType2));
        assertEquals(-1, mimeType2.compareTo(mimeType1));
    }
}
