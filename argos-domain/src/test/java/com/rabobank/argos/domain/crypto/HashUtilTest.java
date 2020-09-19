/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.domain.crypto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.PathHelper;
import com.rabobank.argos.domain.SupplyChainHelper;

class HashUtilTest {
    private String EXPECTED_NORMALIZED_HASH = "16ef5bb126378df2f98d6a742df6ff0f7cad8cc81bfe7ce742615c7d7919024f";
    private String EXPECTED_CRLF_HASH = "8a6fddbbd8f3cc80042d6df6db9488df61905d4eddfa2f25b7e0da4be41314d6";
    private String EXPECTED_CR_HASH = "13968940f9e8a1eec5e60ab0deffe2f7b874b0c5e3fe03c5fe8840961aaf6fdc";

    @BeforeEach
    void setUp() throws Exception {
    }

    @Test
    void createHashTest() throws IOException {
        String unixString = "dit is een file voor testen van hashing.\n" + 
                "nog een regel\n" + 
                "";
        String windowsString = "dit is een file voor testen van hashing.\n" + 
                "nog een regel\r\n" + 
                "";
        String appleString = "dit is een file voor testen van hashing.\n" + 
                "nog een regel\r" + 
                "";
        InputStream input = new ByteArrayInputStream(unixString.getBytes());
        String hash = HashUtil.createHash(input, "unix_string", false);
        assertThat(hash, is(EXPECTED_NORMALIZED_HASH));
        input.close();
        input = new ByteArrayInputStream(unixString.getBytes());
        hash = HashUtil.createHash(input, "unix_string", true);
        assertThat(hash, is(EXPECTED_NORMALIZED_HASH));
        input.close();
        input = new ByteArrayInputStream(windowsString.getBytes());
        hash = HashUtil.createHash(input, "windowsString", true);
        assertThat(hash, is(EXPECTED_NORMALIZED_HASH));
        input.close();
        input = new ByteArrayInputStream(appleString.getBytes());
        hash = HashUtil.createHash(input, "appleString", true);
        assertThat(hash, is(EXPECTED_NORMALIZED_HASH));
        input.close();
        input = new ByteArrayInputStream(windowsString.getBytes());
        hash = HashUtil.createHash(input, "windowsString", false);
        assertThat(hash, is(EXPECTED_CRLF_HASH));
        input.close();
        input = new ByteArrayInputStream(appleString.getBytes());
        hash = HashUtil.createHash(input, "appleString", false);
        assertThat(hash, is(EXPECTED_CR_HASH));
    }
    
    @Test
    void ioExceptionTest() throws IOException {
        File file = new File("src/test/resources/files/a_file.txt");
        InputStream input = new FileInputStream(file);
        input.close();
        Throwable exception = assertThrows(ArgosError.class, () -> {
            HashUtil.createHash(input, "unix_string", false);
          });
        assertEquals("The file unix_string couldn't be recorded: Stream Closed", exception.getMessage());
    }
    
    @Test
    void emptyFileTest() throws IOException {
        String expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        String unixString = "";
        
        InputStream input = new ByteArrayInputStream(unixString.getBytes());
        String hash = HashUtil.createHash(input, "emptyfile", false);
        assertThat(hash, is(expected));
    }
    
    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
      Constructor<HashUtil> constructor = HashUtil.class.getDeclaredConstructor();
      assertThat(Modifier.isPrivate(constructor.getModifiers()), is(true));
      constructor.setAccessible(true);
      constructor.newInstance();
    }

}
