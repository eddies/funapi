/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://www.fedora.info/license/).
 */
package org.fedoracommons.unapi;


/**
 *
 * @author Edwin Shin
 * @since 0.1
 * @version $Id$
 */
public class UnapiException
        extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private int code;
    
    public UnapiException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        code = errorCode;
    }
    
    public UnapiException(int errorCode, String message) {
        this(errorCode, message, null);
    }

    public UnapiException(String message, Throwable cause) {
        this(500, message, cause);
    }
    
    public UnapiException(String message) {
        this(message, null);
    }
    
    public int getErrorCode() {
        return code;
    }
}
