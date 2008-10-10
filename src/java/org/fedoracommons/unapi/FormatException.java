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
