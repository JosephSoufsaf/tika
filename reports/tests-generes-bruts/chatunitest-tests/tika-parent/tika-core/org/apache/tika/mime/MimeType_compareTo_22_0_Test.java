package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class MimeType_compareTo_22_0_Test {

    @Mock
    private MediaType type;

    private MimeType mimeType;

    @BeforeEach
    public void setUp() {
        mimeType = new MimeType(type);
    }

    @Test
    public void testCompareTo() {
        MediaType otherType = mock(MediaType.class);
        when(otherType.compareTo(type)).thenReturn(1);
        MimeType otherMimeType = new MimeType(otherType);
        assertEquals(1, mimeType.compareTo(otherMimeType));
    }
}
