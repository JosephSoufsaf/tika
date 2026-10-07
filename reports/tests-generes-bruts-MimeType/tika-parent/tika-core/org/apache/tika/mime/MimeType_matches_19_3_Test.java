package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypeException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.util.Collections;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MimeType_matches_19_3_Test {

    @Mock
    private Magic mockMagic;

    @InjectMocks
    private MimeType mimeType;

    @BeforeEach
    public void setUp() {
        mimeType = new MimeType(MediaType.OCTET_STREAM);
        try {
            java.lang.reflect.Field field = MimeType.class.getDeclaredField("magics");
            field.setAccessible(true);
            field.set(mimeType, Collections.singletonList(mockMagic));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMatchesMagicTrue() throws MimeTypeException, IOException {
        when(mockMagic.eval(any(byte[].class))).thenReturn(true);
        assertTrue(mimeType.matches(new byte[0]));
    }

    @Test
    public void testMatchesMagicFalse() throws MimeTypeException, IOException {
        when(mockMagic.eval(any(byte[].class))).thenReturn(false);
        assertFalse(mimeType.matches(new byte[0]));
    }

    @Test
    public void testMatchesMagicNull() throws MimeTypeException, IOException {
        when(mockMagic.eval(any(byte[].class))).thenReturn(null);
        assertFalse(mimeType.matches(new byte[0]));
    }

    @Test
    public void testMatchesMagicException() throws MimeTypeException, IOException {
        when(mockMagic.eval(any(byte[].class))).thenThrow(new IOException());
        assertFalse(mimeType.matches(new byte[0]));
    }

    @Test
    public void testMatchesMagicNullMagic() throws MimeTypeException, IOException {
        try {
            java.lang.reflect.Field field = MimeType.class.getDeclaredField("magics");
            field.setAccessible(true);
            field.set(mimeType, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        assertFalse(mimeType.matches(new byte[0]));
    }

    @Test
    public void testMatchesMagicEmptyMagic() throws MimeTypeException, IOException {
        try {
            java.lang.reflect.Field field = MimeType.class.getDeclaredField("magics");
            field.setAccessible(true);
            field.set(mimeType, Collections.emptyList());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        assertFalse(mimeType.matches(new byte[0]));
    }
}
