package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
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

    @Mock
    private MediaType type;

    @Test
    public void testToString() {
        MimeType mimeType = new MimeType(type);
        Mockito.when(type.toString()).thenReturn("application/pdf");
        assertEquals("application/pdf", mimeType.toString());
    }
}
