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
