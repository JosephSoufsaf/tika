package org.apache.tika.mime;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.Magic;
import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class MimeType_matches_19_0_Test {

    @Test
    public void testConstructor() throws Exception {
        MediaType type = new MediaType("application", "octet-stream");
        MimeType mimeType = new MimeType(type);
        assertEquals(type, mimeType.getType());
    }

    @Test
    public void testGetMinLength() throws Exception {
        MediaType type = new MediaType("application", "octet-stream");
        MimeType mimeType = new MimeType(type);
        assertEquals(0, mimeType.getMinLength());
    }

    @Test
    public void testSetInterpreted() throws Exception {
        MediaType type = new MediaType("application", "octet-stream");
        MimeType mimeType = new MimeType(type);
        mimeType.setInterpreted(true);
        assertTrue(mimeType.isInterpreted());
    }

    @Test
    public void testGetLinks() throws Exception {
        MediaType type = new MediaType("application", "octet-stream");
        MimeType mimeType = new MimeType(type);
        assertEquals(Collections.emptyList(), mimeType.getLinks());
    }

    @Test
    public void testGetAcronym() throws Exception {
        MediaType type = new MediaType("application", "octet-stream");
        MimeType mimeType = new MimeType(type);
        assertEquals("", mimeType.getAcronym());
    }

    @Test
    public void testGetUniformTypeIdentifier() throws Exception {
        MediaType type = new MediaType("application", "octet-stream");
        MimeType mimeType = new MimeType(type);
        assertEquals("", mimeType.getUniformTypeIdentifier());
    }
}
