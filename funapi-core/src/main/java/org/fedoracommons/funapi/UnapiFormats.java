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

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of an UnAPI formats element.
 *
 * @author Edwin Shin
 * @since 0.1
 * @version $Id$
 */
public class UnapiFormats {
    private String id;
    private List<UnapiFormat> formats;
    
    public UnapiFormats(String id, List<UnapiFormat> formats) {
        setId(id);
        setFormats(formats);
    }
    
    public UnapiFormats(String id) {
        this(id, null);
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public List<UnapiFormat> getFormats() {
        return formats;
    }

    public void setFormats(List<UnapiFormat> formats) {
        if (formats == null) {
            this.formats = new ArrayList<UnapiFormat>();
        } else {
            this.formats = formats;
        }
    }
    
    public void addFormat(UnapiFormat format) {
        formats.add(format);
    }
    
    public int size() {
        return formats.size();
    }
}