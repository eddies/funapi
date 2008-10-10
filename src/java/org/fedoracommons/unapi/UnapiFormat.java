package org.fedoracommons.unapi;

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
