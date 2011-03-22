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
