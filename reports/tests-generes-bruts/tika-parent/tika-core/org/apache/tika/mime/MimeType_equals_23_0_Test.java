package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
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

public class MimeType_equals_23_0_Test {

    @Test
    public void testEqualsWithSameObject() throws Exception {
        MimeType mimeType = new MimeType(MediaType.OCTET_STREAM);
        assertTrue(reflectEquals(mimeType, mimeType));
    }

    @Test
    public void testEqualsWithDifferentObject() throws Exception {
        MimeType mimeType1 = new MimeType(MediaType.OCTET_STREAM);
        MimeType mimeType2 = new MimeType(MediaType.TEXT_PLAIN);
        assertFalse(reflectEquals(mimeType1, mimeType2));
    }

    @Test
    public void testEqualsWithNull() throws Exception {
        MimeType mimeType = new MimeType(MediaType.OCTET_STREAM);
        assertFalse(reflectEquals(mimeType, null));
    }

    @Test
    public void testEqualsWithDifferentClass() throws Exception {
        MimeType mimeType = new MimeType(MediaType.OCTET_STREAM);
        assertFalse(reflectEquals(mimeType, new Object()));
    }

    private boolean reflectEquals(Object obj1, Object obj2) throws Exception {
        Method equalsMethod = MimeType.class.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);
        return (boolean) equalsMethod.invoke(obj1, obj2);
    }
}
