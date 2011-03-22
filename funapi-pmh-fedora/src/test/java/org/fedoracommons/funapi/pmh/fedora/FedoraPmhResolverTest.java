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
package org.fedoracommons.funapi.pmh.fedora;

import org.fedoracommons.funapi.UnapiException;
import org.fedoracommons.funapi.pmh.AbstractPmhResolver;
import org.fedoracommons.funapi.pmh.AbstractPmhResolverTest;



/**
 *
 * @author Edwin Shin
 * @since
 */
public class FedoraPmhResolverTest extends AbstractPmhResolverTest {

    @Override
    public AbstractPmhResolver getResolver() throws UnapiException {
        return new FedoraPmhResolver();
    }
}
