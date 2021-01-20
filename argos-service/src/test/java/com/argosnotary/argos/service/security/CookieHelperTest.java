/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2021 Gerard Borst <gerard.borst@argosnotary.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.argosnotary.argos.service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CookieHelperTest {

    private CookieHelper cookieHelper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Cookie cookie;

    @Mock
    private HttpServletResponse response;

    @Captor
    private ArgumentCaptor<Cookie> cookieArgumentCaptor;

    @BeforeEach
    void setUp() {
        cookieHelper = new CookieHelper();
    }

    @Test
    void getCookie() {
        when(cookie.getName()).thenReturn("name");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        assertThat(cookieHelper.getCookie(request, "name"), is(Optional.of(cookie)));
    }

    @Test
    void getCookieNotFound() {
        when(cookie.getName()).thenReturn("name");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        assertThat(cookieHelper.getCookie(request, "other"), is(Optional.empty()));
    }

    @Test
    void addCookie() {
        cookieHelper.addCookie(response, "name", "value", 12);
        verify(response).addCookie(cookieArgumentCaptor.capture());
        Cookie cookie = cookieArgumentCaptor.getValue();
        assertThat(cookie.getName(), is("name"));
        assertThat(cookie.getValue(), is("value"));
        assertThat(cookie.getPath(), is("/"));
        assertThat(cookie.getMaxAge(), is(12));
        assertThat(cookie.isHttpOnly(), is(true));
    }

    @Test
    void deleteCookie() {
        when(cookie.getName()).thenReturn("name");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        cookieHelper.deleteCookie(request, response, "name");
        verify(cookie).setMaxAge(0);
        verify(cookie).setValue("");
        verify(cookie).setPath("/");
    }

    @Test
    void addCookieObject() {
        Cookie object = new Cookie("name", "value");

        cookieHelper.addCookie(response, "name", object, 12);
        verify(response).addCookie(cookieArgumentCaptor.capture());
        Cookie cookie = cookieArgumentCaptor.getValue();
        assertThat(cookie.getName(), is("name"));
        assertThat(cookie.getValue(), is("rO0ABXNyABlqYXZheC5zZXJ2bGV0Lmh0dHAuQ29va2llAAAAAAAAAAECAAlaAAhodHRwT25seUkABm1heEFnZVoABnNlY3VyZUkAB3ZlcnNpb25MAAdjb21tZW50dAASTGphdmEvbGFuZy9TdHJpbmc7TAAGZG9tYWlucQB-AAFMAARuYW1lcQB-AAFMAARwYXRocQB-AAFMAAV2YWx1ZXEAfgABeHAA_____wAAAAAAcHB0AARuYW1lcHQABXZhbHVl"));
        assertThat(cookie.getMaxAge(), is(12));
        assertThat(cookie.isHttpOnly(), is(true));
    }

    @Test
    void getCookieObject() {
        when(cookie.getName()).thenReturn("name");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(cookie.getValue()).thenReturn("rO0ABXNyABlqYXZheC5zZXJ2bGV0Lmh0dHAuQ29va2llAAAAAAAAAAECAAlaAAhodHRwT25seUkABm1heEFnZVoABnNlY3VyZUkAB3ZlcnNpb25MAAdjb21tZW50dAASTGphdmEvbGFuZy9TdHJpbmc7TAAGZG9tYWlucQB-AAFMAARuYW1lcQB-AAFMAARwYXRocQB-AAFMAAV2YWx1ZXEAfgABeHAA_____wAAAAAAcHB0AARuYW1lcHQABXZhbHVl");

        Optional<Cookie> optionalCookie = cookieHelper.getCookieValueAsObject(request, "name", Cookie.class);
        assertThat(optionalCookie.get().getValue(), is("value"));
        assertThat(optionalCookie.get().getName(), is("name"));
    }
}