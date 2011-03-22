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

/**
 * 
 *
 * @author Edwin Shin
 * @since 0.1
 * @version $Id$
 */
public interface ObjectResolver {
    
    /**
     * Provide the object formats which should be supported for all objects 
     * available through the unAPI service.
     * 
     * @return UnapiFormats representing the object formats supported for all
     *         objects.
     * @throws UnapiException
     */
    UnapiFormats getFormats() throws UnapiException;
    
    /**
     * Provide a list of object formats available from the unAPI service for the 
     * object identified by <code>id</code>. 
     * It is similar to the {@link #getFormats() getFormats} response, but the
     * returned UnapiFormats object must have the requested <code>id</code> set.
     * 
     * @param id
     * @return UnapiFormats representing the object formats supported for the
     *         requested object.
     * @throws UnapiException
     */
    UnapiFormats getFormats(String id) throws UnapiException;
    
    /**
     * <p>Return an {@link UnapiObject UnapiObject} representing the object 
     * specified by <code>id</code> in the format specified by 
     * <code>format</code>.</p>
     * 
     * <p>Implementations should throw an {@link IdentifierException 
     * IdentifierException} for requests for an identifier that is not available 
     * on the server.
     * Implementations should throw a {@link FormatException FormatException} 
     * for requests for an identifier that is available on the server in a 
     * format that is not available for that identifier.</p>
     * 
     * @param id
     * @param format
     * @return UnapiObject
     * @throws UnapiException
     */
    UnapiObject getObject(String id, String format) throws UnapiException;
}
