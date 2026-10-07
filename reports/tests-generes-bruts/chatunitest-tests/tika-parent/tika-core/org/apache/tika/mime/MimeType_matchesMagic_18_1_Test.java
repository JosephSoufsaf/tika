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
public class MimeType_matchesMagic_18_1_Test {

    @Mock
    private Magic magic1;

    @Mock
    private Magic magic2;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testMatchesMagic_noMagics() throws Exception {
        MimeType mimeType = new MimeType(MediaType.OCTET_STREAM);
        boolean result = invokePrivateMethod(mimeType, "matchesMagic", new byte[0]);
        assertFalse(result);
    }

    @Test
    public void testMatchesMagic_noMatch() throws Exception {
        MimeType mimeType = new MimeType(MediaType.OCTET_STREAM);
        List<Magic> magics = new ArrayList<>();
        magics.add(magic1);
        magics.add(magic2);
        when(magic1.eval(any(byte[].class))).thenReturn(false);
        when(magic2.eval(any(byte[].class))).thenReturn(false);
        setPrivateField(mimeType, "magics", magics);
        boolean result = invokePrivateMethod(mimeType, "matchesMagic", new byte[0]);
        assertFalse(result);
    }

    @Test
    public void testMatchesMagic_match() throws Exception {
        MimeType mimeType = new MimeType(MediaType.OCTET_STREAM);
        List<Magic> magics = new ArrayList<>();
        magics.add(magic1);
        magics.add(magic2);
        when(magic1.eval(any(byte[].class))).thenReturn(false);
        when(magic2.eval(any(byte[].class))).thenReturn(true);
        setPrivateField(mimeType, "magics", magics);
        boolean result = invokePrivateMethod(mimeType, "matchesMagic", new byte[0]);
        assertTrue(result);
    }

    private <T> T invokePrivateMethod(Object obj, String methodName, Object... args) throws Exception {
        java.lang.reflect.Method method = obj.getClass().getDeclaredMethod(methodName, extractParameterTypes(args));
        method.setAccessible(true);
        return (T) method.invoke(obj, args);
    }

    private Class<?>[] extractParameterTypes(Object... args) {
        Class<?>[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return parameterTypes;
    }

    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
