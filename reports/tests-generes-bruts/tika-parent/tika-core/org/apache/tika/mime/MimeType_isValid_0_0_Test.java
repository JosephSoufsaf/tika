package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.function.Executable;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MimeType_isValid_0_0_Test {

    private MimeType mimeType;

    @BeforeEach
    public void setUp() {
        mimeType = new MimeType(MediaType.OCTET_STREAM);
    }

    @Test
    public void testIsValid_WithNullInput_ThrowsIllegalArgumentException() throws Exception {
        Method method = MimeType.class.getDeclaredMethod("isValid", String.class);
        method.setAccessible(true);
        Executable executable = () -> method.invoke(null, (String) null);
        assertThrows(IllegalArgumentException.class, executable, "Expected IllegalArgumentException");
    }

    @Test
    public void testIsValid_WithEmptyString_ReturnsFalse() throws Exception {
        Method method = MimeType.class.getDeclaredMethod("isValid", String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(null, "");
        assertFalse(result, "Expected false for empty string");
    }

    @Test
    public void testIsValid_WithValidString_ReturnsTrue() throws Exception {
        Method method = MimeType.class.getDeclaredMethod("isValid", String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(null, "application/xml");
        assertTrue(result, "Expected true for valid string");
    }

    @Test
    public void testIsValid_WithInvalidString_ReturnsFalse() throws Exception {
        Method method = MimeType.class.getDeclaredMethod("isValid", String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(null, "application/xml/");
        assertFalse(result, "Expected false for invalid string");
    }

    @Test
    public void testIsValid_WithLeadingSlash_ReturnsFalse() throws Exception {
        Method method = MimeType.class.getDeclaredMethod("isValid", String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(null, "/application/xml");
        assertFalse(result, "Expected false for string with leading slash");
    }

    @Test
    public void testIsValid_WithTrailingSlash_ReturnsFalse() throws Exception {
        Method method = MimeType.class.getDeclaredMethod("isValid", String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(null, "application/xml/");
        assertFalse(result, "Expected false for string with trailing slash");
    }

    @Test
    public void testIsValid_WithInvalidCharacters_ReturnsFalse() throws Exception {
        Method method = MimeType.class.getDeclaredMethod("isValid", String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(null, "application/xml(abc)");
        assertFalse(result, "Expected false for string with invalid characters");
    }

    @Test
    public void testIsValid_WithMultipleSlashes_ReturnsFalse() throws Exception {
        Method method = MimeType.class.getDeclaredMethod("isValid", String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(null, "application/xml;type=abc/xml");
        assertFalse(result, "Expected false for string with multiple slashes");
    }

    @Test
    public void testIsValid_WithValidParameters_ReturnsTrue() throws Exception {
        Method method = MimeType.class.getDeclaredMethod("isValid", String.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(null, "application/xml;type=abc");
        assertTrue(result, "Expected true for string with valid parameters");
    }
}
