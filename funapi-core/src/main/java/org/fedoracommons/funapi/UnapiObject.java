/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://www.fedora.info/license/).
 */
package org.fedoracommons.funapi;

import java.io.InputStream;


/**
 * Wrapper object that contains either a redirectUrl or InputStream.
 * 
 * @author Edwin Shin
 * @since 0.1
 * @version $Id$
 */
public class UnapiObject {
    private InputStream is;
    private String redirectUrl;
    private String contentType;
    
    public UnapiObject(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
    
    public UnapiObject(InputStream is, String contentType) {
        this.is = is;
        this.contentType = contentType;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public InputStream getInputStream() {
        return is;
    }
    
    public String getContentType() {
        return contentType;
    }
}
