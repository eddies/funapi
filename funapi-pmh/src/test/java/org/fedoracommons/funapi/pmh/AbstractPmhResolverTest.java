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
package org.fedoracommons.funapi.pmh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mocked;
import mockit.Mockit;

import org.apache.http.impl.client.DefaultHttpClient;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.fedoracommons.funapi.FormatException;
import org.fedoracommons.funapi.IdentifierException;
import org.fedoracommons.funapi.ObjectResolver;
import org.fedoracommons.funapi.UnapiException;
import org.fedoracommons.funapi.UnapiFormat;
import org.fedoracommons.funapi.UnapiFormats;
import org.fedoracommons.funapi.UnapiObject;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author Edwin Shin
 * @since
 */
public abstract class AbstractPmhResolverTest {
	@Mocked("execute") DefaultHttpClient httpClient;

    private XpathEngine engine;
    
    private String response;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        engine = XMLUnit.newXpathEngine();
        Map<String, String> nsMap = new HashMap<String, String>();
        nsMap.put("oai", "http://www.openarchives.org/OAI/2.0/");
        nsMap.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        nsMap.put("dc", "http://purl.org/dc/elements/1.1/");
        NamespaceContext ctx = new SimpleNamespaceContext(nsMap);
        engine.setNamespaceContext(ctx);
        
        Mockit.setUpMocks(new MockAbstractPmhResolver());
    }

    public abstract AbstractPmhResolver getResolver() throws UnapiException;

    @Test
    public void testGetFormats() throws Exception {
        response = "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" " +
                   "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                   "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/ " +
                   "         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\"> " +
                   "  <responseDate>2008-10-08T04:44:29Z</responseDate> " +
                   "  <request/> " +
                   "  <ListMetadataFormats> " +
                   "    <metadataFormat> " +
                   "      <metadataPrefix>oai_dc</metadataPrefix> " +
                   "        <schema>http://www.openarchives.org/OAI/2.0/oai_dc.xsd</schema> " +
                   "        <metadataNamespace>http://www.openarchives.org/OAI/2.0/oai_dc/</metadataNamespace> " +
                   "    </metadataFormat> " +
                   "  </ListMetadataFormats> " +
                   "</OAI-PMH>";

        /*
        new MockUp<AbstractPmhResolver>() {
            @SuppressWarnings("unused") // referenced indirectly by getFormats()
			@Mock
            String getResponse(String url) {
               return response;
            }
        };
        */
        
        AbstractPmhResolver resolver = getResolver();
        UnapiFormats formats = resolver.getFormats();
        assertNull(formats.getId());
        List<UnapiFormat> formatList = formats.getFormats();
        assertEquals(1, formatList.size());
    }

    @Test
    public void testGetObject() throws Exception {
        String id = "foo";
        String format = "oai_dc";

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

        ObjectResolver resolver = getResolver();
        UnapiObject obj = resolver.getObject(id, format);
        assertNull(obj.getRedirectUrl());
        InputStream is = obj.getInputStream();
        Document doc = XMLUnit.buildControlDocument(new InputSource(is));
        assertEquals("blah", engine.evaluate("/oai_dc:dc/dc:title", doc));
        is.close();
    }

    /**
     * This tests the XPath expression used to select the requested metadata
     * format.
     * For instance, the Fedora OAI response has whitespace, while the DSpace
     * response does not.
     *
     * @throws Exception
     */
    @Test
    public void getObjectWithNoWhitespaceResponse() throws Exception {
        String id = "foo";
        String format = "oai_dc";

        response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                   "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" " +
                   "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                   "xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/ " +
                   "http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\">" +
                   "<responseDate>2008-10-07T19:22:48Z</responseDate><request/>" +
                   "<GetRecord><record><header><identifier>foo:bar</identifier>" +
                   "<datestamp>2005-11-21T17:08:59Z</datestamp></header>" +
                   "<metadata><oai_dc:dc " +
                   "xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" " +
                   "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
                   "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                   "xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ " +
                   "http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">" +
                   "<dc:title>blah</dc:title><dc:creator>Shin, Edwin</dc:creator>" +
                   "</oai_dc:dc></metadata></record></GetRecord></OAI-PMH>";

        AbstractPmhResolver resolver = getResolver();
        UnapiObject obj = resolver.getObject(id, format);
        assertNull(obj.getRedirectUrl());
        InputStream is = obj.getInputStream();
        Document doc = XMLUnit.buildControlDocument(new InputSource(is));
        assertEquals("blah", engine.evaluate("/oai_dc:dc/dc:title", doc));
        is.close();
    }

    @Test(expected=IdentifierException.class)
    public void testIdDoesNotExist() throws Exception {
        response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                   "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" " +
                   "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                   "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/ " +
                   "         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\"> " +
                   "  <responseDate>2008-10-07T19:22:48Z</responseDate>" +
                   "  <request/>" +
                   "  <error code=\"idDoesNotExist\"/> " +
                   "</OAI-PMH>";

        AbstractPmhResolver resolver = getResolver();
        resolver.getObject("foo", "bar");
    }

    @Test(expected=FormatException.class)
    public void testCannotDisseminateFormat() throws Exception {
        response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                   "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" " +
                   "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                   "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/ " +
                   "         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\"> " +
                   "  <responseDate>2008-10-07T19:22:48Z</responseDate>" +
                   "  <request/>" +
                   "  <error code=\"cannotDisseminateFormat\"/> " +
                   "</OAI-PMH>";

        AbstractPmhResolver resolver = getResolver();
        resolver.getObject("foo", "bar");
    }
    
    public void setResolverResponse(String response) {
    	this.response = response;
    }
    
    /**
     * Mock for AbstractPmhResolver.getResponse()
     * 
     * @author Edwin Shin
     *
     */
    @MockClass(realClass = AbstractPmhResolver.class)
    public final class MockAbstractPmhResolver {
    	@Mock
        String getResponse(String url) {
           return response;
        }
    }
}
