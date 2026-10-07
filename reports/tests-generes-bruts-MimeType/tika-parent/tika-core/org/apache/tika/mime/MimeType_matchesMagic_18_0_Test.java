package org.apache.tika.mime;

import org.apache.tika.mime.Magic;
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

public class MimeType_matchesMagic_18_0_Test {

    private MimeType mimeType;

    private Magic magic1;

    private Magic magic2;

    @BeforeEach
    public void setUp() {
        mimeType = new MimeType(MediaType.OCTET_STREAM);
        magic1 = mock(Magic.class);
        magic2 = mock(Magic.class);
        List<Magic> magics = new ArrayList<>();
        magics.add(magic1);
        magics.add(magic2);
        try {
            java.lang.reflect.Field field = MimeType.class.getDeclaredField("magics");
            field.setAccessible(true);
            field.set(mimeType, magics);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMatchesMagic_MagicDoesNotMatch() throws Exception {
        when(magic1.eval(new byte[100])).thenReturn(false);
        when(magic2.eval(new byte[100])).thenReturn(false);
        boolean result = mimeType.matchesMagic(new byte[100]);
        assertFalse(result);
        verify(magic1, times(1)).eval(new byte[100]);
        verify(magic2, times(1)).eval(new byte[100]);
    }

    @Test
    public void testMatchesMagic_NoMagics() throws Exception {
        List<Magic> magics = new ArrayList<>();
        try {
            java.lang.reflect.Field field = MimeType.class.getDeclaredField("magics");
            field.setAccessible(true);
            field.set(mimeType, magics);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        boolean result = mimeType.matchesMagic(new byte[100]);
        assertFalse(result);
    }
}
