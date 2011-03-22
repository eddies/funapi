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
 * @author Edwin Shin
 * @since 0.1
 * @version $Id$
 */
public class FormatException
        extends UnapiException {
    
    private static final long serialVersionUID = 1L;
    
    public FormatException(String message, Throwable cause) {
        super(406, message, cause);
    }
    
    public FormatException(String message) {
        this(message, null);
    }
}
