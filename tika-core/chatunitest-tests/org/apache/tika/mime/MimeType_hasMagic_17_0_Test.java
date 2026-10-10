package org.apache.tika.mime;

import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class MimeType_hasMagic_17_0_Test {

    @Mock
    private MediaType type;

    private MimeType mimeType;

    @BeforeEach
    public void setUp() {
        mimeType = new MimeType(type);
    }

    @Test
    public void testHasMagicWhenMagicsIsNull() {
        when(mimeType.getMagics()).thenReturn(null);
        assertFalse(mimeType.hasMagic());
    }

    @Test
    public void testHasMagicWhenMagicsIsNotEmpty() {
        List<Magic> magics = new ArrayList<>();
        when(mimeType.getMagics()).thenReturn(magics);
        assertTrue(mimeType.hasMagic());
    }
}
