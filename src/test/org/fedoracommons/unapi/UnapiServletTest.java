/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://www.fedora.info/license/).
 */
package org.fedoracommons.unapi;

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
