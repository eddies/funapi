/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://www.fedora.info/license/).
 */
package org.fedoracommons.funapi.fedora;

import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.fedoracommons.funapi.ObjectResolver;
import org.fedoracommons.funapi.UnapiFormat;
import org.fedoracommons.funapi.UnapiFormats;
import org.fedoracommons.funapi.UnapiObject;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import fedora.client.FedoraClient;

import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.DatastreamDef;
import fedora.server.types.gen.MIMETypedStream;
import fedora.server.types.gen.RelationshipTuple;

import static org.apache.commons.httpclient.HttpStatus.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 *
 * @author Edwin Shin
 * @since
 * @version $Id$
 */
@RunWith(JMock.class)
public class FedoraResolverTest {
    private Mockery context;
    
    private String name = "oai_dc";
    private String type = "text/xml";
    private String docs = "http://www.openarchives.org/OAI/2.0/oai_dc.xsd";
    
    private String json = String.format("<json>[[\"info:fedora/*/DC\",\"%s\",\"%s\",\"%s\"]]</json>", name, type, docs);
    
    private HttpClient httpClient;
    private FedoraClient fedoraClient;
    private FedoraAPIA apia;
    private FedoraAPIM apim;
    private DatastreamDef dsDef;
    private RelationshipTuple tuple;
    private MIMETypedStream ds;
    private DatastreamDef[] dsDefs;
    private RelationshipTuple[] tuples;
    byte[] bytes;

    @Before
    public void setUp() {
        context = new JUnit4Mockery();
        context.setImposteriser(ClassImposteriser.INSTANCE);
        httpClient = context.mock(HttpClient.class);
        fedoraClient = context.mock(FedoraClient.class);
        apia = context.mock(FedoraAPIA.class);
        apim = context.mock(FedoraAPIM.class);
        dsDef = context.mock(DatastreamDef.class);
        tuple = context.mock(RelationshipTuple.class);
        ds = context.mock(MIMETypedStream.class);
        dsDefs = new DatastreamDef[1];
        dsDefs[0] = dsDef;
        tuples = new RelationshipTuple[1];
        tuples[0] = tuple;
        bytes = json.getBytes();
    }

    @Test
    public void testGetFormats() throws Exception {
        ObjectResolver resolver = new FedoraResolver();
        UnapiFormats formats = resolver.getFormats();
        assertNull(formats.getId());
        List<UnapiFormat> formatList = formats.getFormats();
        assertEquals(1, formatList.size());
        UnapiFormat format = formatList.get(0);
        assertEquals(name, format.getName());
        assertEquals(type, format.getType());
        assertEquals(docs, format.getDocs());
    }
    
    @Test
    public void testGetFormatsWithId() throws Exception {
        context.checking(new Expectations() {{
            one(httpClient).executeMethod(with(any(HttpMethod.class)));
                will(returnValue(SC_OK));
            one(fedoraClient).getAPIM();
                will(returnValue(apim));
            one(fedoraClient).getAPIA();
                will(returnValue(apia));   
            one (apim).getRelationships(with(any(String.class)), 
                                          with(any(String.class)));
                will(returnValue(tuples));
            one(tuple).getObject();
                will(returnValue("demo:cmodel"));
            one(apia).listDatastreams(with(any(String.class)), with(any(String.class)));
                will(returnValue(dsDefs));
            one(dsDef).getID();
                will(returnValue("UNAPI-FORMATS"));
            one(apia).getDatastreamDissemination(with(any(String.class)), 
                                                    with(any(String.class)),
                                                    with(any(String.class)));
                will(returnValue(ds));
            one(ds).getStream();
                will(returnValue(bytes));
        }});

        FedoraResolver resolver = new FedoraResolver();
        resolver.setHttpClient(httpClient);
        resolver.setFedoraClient(fedoraClient);
        String id = "demo:cmodel";
        UnapiFormats formats = resolver.getFormats(id);
        assertEquals(id, formats.getId());
        List<UnapiFormat> formatList = formats.getFormats();
        assertEquals(1, formatList.size());
        UnapiFormat format = formatList.get(0);
        assertEquals(name, format.getName());
        assertEquals(type, format.getType());
        assertEquals(docs, format.getDocs());
    }
    
    @Test
    public void testGetObject() throws Exception {
        context.checking(new Expectations() {{
            allowing(httpClient).executeMethod(with(any(HttpMethod.class)));
                will(returnValue(SC_OK));
            one(fedoraClient).getAPIM();
                will(returnValue(apim));
            one(fedoraClient).getAPIA();
                will(returnValue(apia));
            one (apim).getRelationships(with(any(String.class)), 
                                          with(any(String.class)));
                will(returnValue(tuples));
            one(tuple).getObject();
                will(returnValue("demo:cmodel"));
            one(apia).listDatastreams(with(any(String.class)), with(any(String.class)));
                will(returnValue(dsDefs));
            one(dsDef).getID();
                will(returnValue("UNAPI-FORMATS"));
            one(apia).getDatastreamDissemination(with(any(String.class)), 
                                                    with(any(String.class)),
                                                    with(any(String.class)));
                will(returnValue(ds));
            one(ds).getStream();
                will(returnValue(bytes));
        }});

        FedoraResolver resolver = new FedoraResolver();
        resolver.setHttpClient(httpClient);
        resolver.setFedoraClient(fedoraClient);
        String id = "demo:cmodel";
        UnapiObject obj = resolver.getObject(id, name);
        assertEquals("http://localhost:8080/fedora/get/demo:cmodel/DC", obj.getRedirectUrl());
    }
}
