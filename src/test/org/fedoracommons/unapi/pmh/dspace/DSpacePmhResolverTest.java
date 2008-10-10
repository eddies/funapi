/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://www.fedora.info/license/).
 */
package org.fedoracommons.unapi.pmh.dspace;

import org.fedoracommons.unapi.UnapiException;
import org.fedoracommons.unapi.pmh.AbstractPmhResolver;
import org.fedoracommons.unapi.pmh.AbstractPmhResolverTest;



/**
 *
 * @author Edwin Shin
 * @since
 * @version $Id$
 */
public class DSpacePmhResolverTest extends AbstractPmhResolverTest {

    @Override
    public AbstractPmhResolver getResolver() throws UnapiException {
        return new DSpacePmhResolver();
    }
}
