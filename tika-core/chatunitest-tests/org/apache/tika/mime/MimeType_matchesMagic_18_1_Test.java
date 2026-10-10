package org.apache.tika.mime;

import org.apache.tika.mime.Magic;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.function.Executable;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MimeType_matchesMagic_18_1_Test {

    @Mock
    private Magic magic1;

    @Mock
    private Magic magic2;

    private MimeType mimeType;

    @BeforeEach
    public void setUp() {
        mimeType = new MimeType(MediaType.OCTET_STREAM);
        List<Magic> magics = new ArrayList<>();
        magics.add(magic1);
        magics.add(magic2);
        try {
            java.lang.reflect.Field magicsField = MimeType.class.getDeclaredField("magics");
            magicsField.setAccessible(true);
            magicsField.set(mimeType, magics);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMatchesMagic_NoMagics() {
        try {
            java.lang.reflect.Field magicsField = MimeType.class.getDeclaredField("magics");
            magicsField.setAccessible(true);
            magicsField.set(mimeType, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        assertFalse(mimeType.matchesMagic(new byte[0]));
    }

    @Test
    public void testMatchesMagic_MagicDoesNotMatch() {
        when(magic1.eval(any(byte[].class))).thenReturn(false);
        when(magic2.eval(any(byte[].class))).thenReturn(false);
        assertFalse(mimeType.matchesMagic(new byte[0]));
    }

    @Test
    public void testMatchesMagic_MagicMatches() {
        when(magic1.eval(any(byte[].class))).thenReturn(false);
        when(magic2.eval(any(byte[].class))).thenReturn(true);
        assertTrue(mimeType.matchesMagic(new byte[0]));
    }

    @Test
    public void testMatchesMagic_ExceptionInMagic() {
        when(magic1.eval(any(byte[].class))).thenThrow(new RuntimeException());
        when(magic2.eval(any(byte[].class))).thenReturn(false);
        assertTrue(mimeType.matchesMagic(new byte[0]));
    }
}
