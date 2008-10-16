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