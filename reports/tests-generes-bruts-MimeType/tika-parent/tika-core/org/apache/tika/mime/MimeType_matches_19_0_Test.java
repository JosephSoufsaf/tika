package org.apache.tika.mime;

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
public class MimeType_matches_19_0_Test {

    @Mock
    private Magic mockMagic;

    @InjectMocks
    private MimeType mimeType;

    @BeforeEach
    public void setUp() {
        try {
            mimeType = (MimeType) MimeType.class.getDeclaredConstructor(MediaType.class).newInstance(MediaType.OCTET_STREAM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMatches_WithMatchingMagic() {
        // Arrange
        List<Magic> magics = new ArrayList<>();
        magics.add(mockMagic);
        try {
            MimeType.class.getDeclaredMethod("setMagics", List.class).invoke(mimeType, magics);
        } catch (Exception e) {
            e.printStackTrace();
        }
        when(mockMagic.eval(any(byte[].class))).thenReturn(true);
        byte[] data = new byte[10];
        // Act
        boolean result = mimeType.matches(data);
        // Assert
        assertTrue(result);
    }

    @Test
    public void testMatches_WithNonMatchingMagic() {
        // Arrange
        List<Magic> magics = new ArrayList<>();
        magics.add(mockMagic);
        try {
            MimeType.class.getDeclaredMethod("setMagics", List.class).invoke(mimeType, magics);
        } catch (Exception e) {
            e.printStackTrace();
        }
        when(mockMagic.eval(any(byte[].class))).thenReturn(false);
        byte[] data = new byte[10];
        // Act
        boolean result = mimeType.matches(data);
        // Assert
        assertFalse(result);
    }

    @Test
    public void testMatches_WithNoMagics() {
        // Arrange
        byte[] data = new byte[10];
        // Act
        boolean result = mimeType.matches(data);
        // Assert
        assertFalse(result);
    }
}
