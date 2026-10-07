package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
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

public class MimeType_hashCode_24_0_Test {

    @Test
    public void testHashCode() throws Exception {
        Class<?> clazz = Class.forName("org.apache.tika.mime.MimeType");
        Method hashCodeMethod = clazz.getDeclaredMethod("hashCode");
        hashCodeMethod.setAccessible(true);
        MimeType mimeType = new MimeType(MediaType.OCTET_STREAM);
        int hashCode = (int) hashCodeMethod.invoke(mimeType);
        assertEquals(MediaType.OCTET_STREAM.hashCode(), hashCode);
    }
}
