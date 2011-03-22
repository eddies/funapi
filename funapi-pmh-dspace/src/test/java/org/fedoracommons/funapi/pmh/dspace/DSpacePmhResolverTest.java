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
package org.fedoracommons.funapi.pmh.dspace;

import static org.junit.Assert.assertNull;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.fedoracommons.funapi.UnapiException;
import org.fedoracommons.funapi.UnapiObject;
import org.fedoracommons.funapi.pmh.AbstractPmhResolver;
import org.fedoracommons.funapi.pmh.AbstractPmhResolverTest;
import org.junit.Test;

/**
 *
 * @author Edwin Shin
 * @since
 */
public class DSpacePmhResolverTest extends AbstractPmhResolverTest {
    @Override
    public AbstractPmhResolver getResolver() throws UnapiException {
        return new DSpacePmhResolver();
    }

    @Test
    public void testGetObject(final DefaultHttpClient httpClient, final HttpGet httpGet) throws Exception {
        String id = "foo";
        String format = "oai_dc";

        String response;
        response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                   "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" " +
                   "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                   "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/ " +
                   "         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\"> " +
                   "  <responseDate>2008-10-07T19:22:48Z</responseDate>" +
                   "  <request/>" +
                   "  <GetRecord> " +
                   "    <record> " +
                   "      <header>" +
                   "        <identifier>foo:bar</identifier>" +
                   "        <datestamp>2005-11-21T17:08:59Z</datestamp>" +
                   "      </header>" +
                   "      <metadata> " +
                   "        <oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" " +
                   "                   xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
                   "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                   "                   xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ " +
                   "                   http://www.openarchives.org/OAI/2.0/oai_dc.xsd\"> " +
                   "          <dc:title>blah</dc:title> " +
                   "          <dc:creator>Shin, Edwin</dc:creator> " +
                   "        </oai_dc:dc> " +
                   "      </metadata> " +
                   "    </record> " +
                   "  </GetRecord> " +
                   "</OAI-PMH>";
        
        setResolverResponse(response);
        AbstractPmhResolver resolver = getResolver();
        UnapiObject obj = resolver.getObject(id, format);
        assertNull(obj.getRedirectUrl());
    }
}
