/**
 * Copyright (C) 2008 MediaShelf <http://www.yourmediashelf.com/>
 *
 * This file is part of funapi.
 *
 * funapi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * funapi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with funapi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fedoracommons.funapi;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;



/**
 *
 * @author Edwin Shin
 * @since
 * @version $Id$
 */
public class UnapiServletTest {
    @Test
    public void testFoo() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/main.app");
        request.setSession(new MockHttpSession(null));
        
        request.addParameter("choice", "expanded");
        request.addParameter("contextMenu", "left");
        
        HttpServletResponse response = new MockHttpServletResponse();
        response.hashCode();
    }
}
