/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://www.fedora.info/license/).
 */
package org.fedoracommons.funapi.pmh.fedora;

import org.fedoracommons.funapi.UnapiException;
import org.fedoracommons.funapi.pmh.AbstractPmhResolver;
import org.fedoracommons.funapi.pmh.AbstractPmhResolverTest;



/**
 *
 * @author Edwin Shin
 * @since
 * @version $Id$
 */
public class FedoraPmhResolverTest extends AbstractPmhResolverTest {

    @Override
    public AbstractPmhResolver getResolver() throws UnapiException {
        return new FedoraPmhResolver();
    }
}
