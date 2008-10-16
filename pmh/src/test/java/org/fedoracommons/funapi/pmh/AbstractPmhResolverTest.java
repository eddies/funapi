/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://www.fedora.info/license/).
 */
package org.fedoracommons.funapi.pmh;

import java.io.InputStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;

import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;

import org.fedoracommons.funapi.FormatException;
import org.fedoracommons.funapi.IdentifierException;
import org.fedoracommons.funapi.UnapiException;
import org.fedoracommons.funapi.UnapiFormat;
import org.fedoracommons.funapi.UnapiFormats;
import org.fedoracommons.funapi.UnapiObject;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.apache.commons.httpclient.HttpStatus.SC_OK;

/**
 *
 * @author Edwin Shin
 * @since
 * @version $Id$
 */
@RunWith(value=JMock.class)
public abstract class AbstractPmhResolverTest {
    private Mockery context;
    private HttpClient httpClient;
    private HttpMethod httpMethod;
    
    private XpathEngine engine;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        context = new JUnit4Mockery();
        context.setImposteriser(ClassImposteriser.INSTANCE);
        httpClient = context.mock(HttpClient.class);
        httpMethod = context.mock(HttpMethod.class);
        
        XMLUnit.setIgnoreWhitespace(true);
        engine = XMLUnit.newXpathEngine();
        Map<String, String> nsMap = new HashMap<String, String>();
        nsMap.put("oai", "http://www.openarchives.org/OAI/2.0/");
        nsMap.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        nsMap.put("dc", "http://purl.org/dc/elements/1.1/");
        NamespaceContext ctx = new SimpleNamespaceContext(nsMap);
        engine.setNamespaceContext(ctx);
    }
    
    public abstract AbstractPmhResolver getResolver() throws UnapiException;
    
    @Test
    public void testGetFormats() throws Exception {
        final String response;
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

        context.checking(new Expectations() {{
            one(httpClient).executeMethod(with(any(HttpMethod.class)));
                will(returnValue(SC_OK));
            one(httpMethod).getResponseBodyAsString();
                will(returnValue(response));
            one(httpMethod).releaseConnection();
        }});
        
        AbstractPmhResolver resolver = getResolver();
        resolver.setHttpClient(httpClient);
        resolver.setHttpMethod(httpMethod);
        UnapiFormats formats = resolver.getFormats();
        assertNull(formats.getId());
        List<UnapiFormat> formatList = formats.getFormats();
        assertEquals(1, formatList.size());
    }
    
    @Test
    public void getObject() throws Exception {
        String id = "foo";
        String format = "oai_dc";
        
        final String response;
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
        
        context.checking(new Expectations() {{
            one(httpClient).executeMethod(with(any(HttpMethod.class)));
                will(returnValue(SC_OK));
            one(httpMethod).getResponseBodyAsString();
                will(returnValue(response));
            one(httpMethod).releaseConnection();
        }});
        
        AbstractPmhResolver resolver = getResolver();
        resolver.setHttpClient(httpClient);
        resolver.setHttpMethod(httpMethod);
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
        
        final String response;
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
        
        context.checking(new Expectations() {{
            one(httpClient).executeMethod(with(any(HttpMethod.class)));
                will(returnValue(SC_OK));
            one(httpMethod).getResponseBodyAsString();
                will(returnValue(response));
            one(httpMethod).releaseConnection();
        }});
        
        AbstractPmhResolver resolver = getResolver();
        resolver.setHttpClient(httpClient);
        resolver.setHttpMethod(httpMethod);
        UnapiObject obj = resolver.getObject(id, format);
        assertNull(obj.getRedirectUrl());
        InputStream is = obj.getInputStream();
        Document doc = XMLUnit.buildControlDocument(new InputSource(is));
        assertEquals("blah", engine.evaluate("/oai_dc:dc/dc:title", doc));
        is.close();
    }
    
    @Test(expected=IdentifierException.class)
    public void testIdDoesNotExist() throws Exception {
        final String response;
        response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                   "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" " +
                   "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                   "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/ " +
                   "         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\"> " +
                   "  <responseDate>2008-10-07T19:22:48Z</responseDate>" + 
                   "  <request/>" +
                   "  <error code=\"idDoesNotExist\"/> " +
                   "</OAI-PMH>";

        context.checking(new Expectations() {{
            one(httpClient).executeMethod(with(any(HttpMethod.class)));
                will(returnValue(SC_OK));
            one(httpMethod).getResponseBodyAsString();
                will(returnValue(response));
            one(httpMethod).releaseConnection();
        }});
        
        AbstractPmhResolver resolver = getResolver();
        resolver.setHttpClient(httpClient);
        resolver.setHttpMethod(httpMethod);
        resolver.getObject("foo", "bar");
    }
    
    @Test(expected=FormatException.class)
    public void testCannotDisseminateFormat() throws Exception {
        final String response;
        response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                   "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" " +
                   "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                   "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/ " +
                   "         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\"> " +
                   "  <responseDate>2008-10-07T19:22:48Z</responseDate>" + 
                   "  <request/>" +
                   "  <error code=\"cannotDisseminateFormat\"/> " +
                   "</OAI-PMH>";

        context.checking(new Expectations() {{
            one(httpClient).executeMethod(with(any(HttpMethod.class)));
                will(returnValue(SC_OK));
            one(httpMethod).getResponseBodyAsString();
                will(returnValue(response));
            one(httpMethod).releaseConnection();
        }});
        
        AbstractPmhResolver resolver = getResolver();
        resolver.setHttpClient(httpClient);
        resolver.setHttpMethod(httpMethod);
        resolver.getObject("foo", "bar");
    }
}
