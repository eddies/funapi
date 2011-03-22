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
 * Representation of an UnAPI format element.
 *
 * @author Edwin Shin
 * @since 0.1
 * @version $Id$
 */
public class UnapiFormat {
    private String name;
    private String type;
    private String docs;
    
    public UnapiFormat(String name, String type, String docs) {
        this(new String[] {name, type, docs});
    }
    
    public UnapiFormat(String[] nameTypeDocs) {
        if (nameTypeDocs == null || nameTypeDocs.length == 0) {
            throw new IllegalArgumentException("nameTypeDocs cannot be empty");
        } else if (nameTypeDocs.length == 2) {
            setName(nameTypeDocs[0]);
            setType(nameTypeDocs[1]);
            setDocs(null);
        } else if (nameTypeDocs.length == 3) {
            setName(nameTypeDocs[0]);
            setType(nameTypeDocs[1]);
            setDocs(nameTypeDocs[2]);
        } else {
            throw new IllegalArgumentException("Expected [name, type, docs]");
        }
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("name must be provided");
        }
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        if (type == null || type.length() == 0) {
            throw new IllegalArgumentException("type must be provided");
        }
        this.type = type;
    }
    
    public String getDocs() {
        return docs;
    }
    
    public void setDocs(String docs) {
        this.docs = docs;
    }
}
