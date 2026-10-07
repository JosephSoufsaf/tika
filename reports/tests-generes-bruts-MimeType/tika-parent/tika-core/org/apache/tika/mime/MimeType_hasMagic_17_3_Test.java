package org.apache.tika.mime;

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

public class MimeType_hasMagic_17_3_Test {

    private MimeType mimeType;

    @BeforeEach
    public void setUp() {
        mimeType = new MimeType(new MediaType("application", "octet-stream"));
    }

    @Test
    public void testHasMagicWithNoMagics() {
        assertFalse(mimeType.hasMagic());
    }

    @Test
    public void testHasMagicWithMagics() {
        Magic mockMagic = mock(Magic.class);
        mimeType.addMagic(mockMagic);
        assertTrue(mimeType.hasMagic());
    }
}
