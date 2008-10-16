/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://www.fedora.info/license/).
 */
package org.fedoracommons.funapi;


/**
 *
 * @author Edwin Shin
 * @since 0.1
 * @version $Id$
 */
public class IdentifierException
        extends UnapiException {

    private static final long serialVersionUID = 1L;

    public IdentifierException(String message, Throwable cause) {
        super(404, message, cause);
    }
    
    public IdentifierException(String message) {
        this(message, null);
    }
}
