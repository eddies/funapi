/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also
 * available online at http://www.fedora.info/license/).
 */
package org.fedoracommons.funapi.fedora;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.fedoracommons.funapi.ObjectResolver;
import org.fedoracommons.funapi.UnapiFormat;
import org.fedoracommons.funapi.UnapiFormats;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.yourmediashelf.fedora.client.FedoraClient;


/**
 *
 * @author Edwin Shin
 * @since
 * @version $Id$
 */
@RunWith(JMock.class)
public class FedoraResolverTest {
    private Mockery context;

    private final String name = "oai_dc";
    private final String type = "text/xml";
    private final String docs = "http://www.openarchives.org/OAI/2.0/oai_dc.xsd";

    private final String json = String.format("<json>[[\"info:fedora/*/DC\",\"%s\",\"%s\",\"%s\"]]</json>", name, type, docs);

    private FedoraClient fedoraClient;
    byte[] bytes;

    @Before
    public void setUp() {
        context = new JUnit4Mockery();
        context.setImposteriser(ClassImposteriser.INSTANCE);
        fedoraClient = context.mock(FedoraClient.class);
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

    /*
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
    */
}
