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
public class MimeType_isValid_0_0_Test {

    @Mock
    private MediaType type;

    private MimeType mimeType;

    @BeforeEach
    public void setUp() {
        mimeType = new MimeType(type);
    }

    @Test
    public void testIsValid_withNullName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mimeType.isValid(null);
        });
    }

    @Test
    public void testIsValid_withEmptyName_returnsFalse() {
        assertFalse(mimeType.isValid(""));
    }

    @Test
    public void testIsValid_withNameContainingInvalidCharacters_returnsFalse() {
        assertFalse(mimeType.isValid("text/plain; charset=UTF-8"));
    }

    @Test
    public void testIsValid_withNameContainingValidCharactersAndSlash_returnsTrue() {
        assertTrue(mimeType.isValid("text/plain"));
    }

    @Test
    public void testIsValid_withNameStartingWithSlash_returnsFalse() {
        assertFalse(mimeType.isValid("/text/plain"));
    }

    @Test
    public void testIsValid_withNameEndingWithSlash_returnsFalse() {
        assertFalse(mimeType.isValid("text/plain/"));
    }

    @Test
    public void testIsValid_withNameContainingConsecutiveSlashes_returnsFalse() {
        assertFalse(mimeType.isValid("text/plain//"));
    }

    @Test
    public void testIsValid_withNameContainingSlashAtStartAndEnd_returnsFalse() {
        assertFalse(mimeType.isValid("/text/plain/"));
    }
}
