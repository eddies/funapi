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
package org.fedoracommons.funapi.fedora;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.fedoracommons.funapi.ObjectResolver;
import org.fedoracommons.funapi.UnapiFormat;
import org.fedoracommons.funapi.UnapiFormats;
import org.fedoracommons.funapi.UnapiObject;
import org.junit.Before;
import org.junit.Test;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.request.GetDatastreamDissemination;
import com.yourmediashelf.fedora.client.request.GetRelationships;
import com.yourmediashelf.fedora.client.request.ListDatastreams;
import com.yourmediashelf.fedora.client.response.FedoraResponse;
import com.yourmediashelf.fedora.client.response.ListDatastreamsResponse;
import com.yourmediashelf.fedora.generated.access.DatastreamType;


/**
 *
 * @author Edwin Shin
 * @since
 */
public class FedoraResolverTest {
    private final String name = "oai_dc";
    private final String type = "text/xml";
    private final String docs = "http://www.openarchives.org/OAI/2.0/oai_dc.xsd";
    private final String json = String.format("<json>[[\"info:fedora/*/DC\",\"%s\",\"%s\",\"%s\"]]</json>", name, type, docs);

    @Mocked FedoraClient fedoraClient;
    @Mocked GetRelationships getRelationships;
    @Mocked FedoraResponse fedoraResponseA;
    @Mocked FedoraResponse fedoraResponseB;
    @Mocked ListDatastreams listDatastreams;
    @Mocked ListDatastreamsResponse listDatastreamsResponse;
    @Mocked GetDatastreamDissemination getDatastreamDissemination;
    @Mocked DatastreamType datastreamType;
    private List<DatastreamType> cmodelDatastreams;
    private InputStream relationshipsStream;
    private InputStream datastreamStream;
    byte[] bytes;

    @Before
    public void setUp() throws Exception {
        cmodelDatastreams = new ArrayList<DatastreamType>();
        cmodelDatastreams.add(datastreamType);
        relationshipsStream = new ByteArrayInputStream("<info:fedora/test-rest:1> <info:fedora/fedora-system:def/model#hasModel> <info:fedora/demo:cmodel> .".getBytes("UTF-8"));
        datastreamStream = new ByteArrayInputStream(json.getBytes("UTF-8"));
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
        new NonStrictExpectations() {
            {
                FedoraClient.getRelationships(anyString); returns(getRelationships);
                getRelationships.subject(anyString); returns(getRelationships);
                getRelationships.predicate(anyString); returns(getRelationships);
                getRelationships.format(anyString); returns(getRelationships);
                getRelationships.execute(fedoraClient); returns(fedoraResponseA);
                fedoraResponseA.getEntityInputStream(); returns(relationshipsStream);
                FedoraClient.listDatastreams(anyString); returns(listDatastreams);
                listDatastreams.execute(fedoraClient); returns(listDatastreamsResponse);
                listDatastreamsResponse.getDatastreams(); returns(cmodelDatastreams);
                datastreamType.getDsid(); returns("UNAPI-FORMATS");
                FedoraClient.getDatastreamDissemination(anyString, anyString); returns(getDatastreamDissemination);
                getDatastreamDissemination.execute(fedoraClient); returns(fedoraResponseB);
                fedoraResponseB.getEntityInputStream(); returns(datastreamStream);
            }
        };

        FedoraResolver resolver = new FedoraResolver();
        resolver.setFedoraClient(fedoraClient);
        String id = "info:fedora/demo:cmodel";
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
        new NonStrictExpectations() {
            {
                FedoraClient.getRelationships(anyString); returns(getRelationships);
                getRelationships.subject(anyString); returns(getRelationships);
                getRelationships.predicate(anyString); returns(getRelationships);
                getRelationships.format(anyString); returns(getRelationships);
                getRelationships.execute(fedoraClient); returns(fedoraResponseA);
                fedoraResponseA.getEntityInputStream(); returns(relationshipsStream);
                FedoraClient.listDatastreams(anyString); returns(listDatastreams);
                listDatastreams.execute(fedoraClient); returns(listDatastreamsResponse);
                listDatastreamsResponse.getDatastreams(); returns(cmodelDatastreams);
                datastreamType.getDsid(); returns("UNAPI-FORMATS");
                FedoraClient.getDatastreamDissemination(anyString, anyString); returns(getDatastreamDissemination);
                getDatastreamDissemination.execute(fedoraClient); returns(fedoraResponseB);
                fedoraResponseB.getEntityInputStream(); returns(datastreamStream);
            }
        };

        FedoraResolver resolver = new FedoraResolver();
        resolver.setFedoraClient(fedoraClient);
        String id = "info:fedora/demo:cmodel";
        UnapiObject obj = resolver.getObject(id, name);
        assertEquals("http://localhost:8080/fedora/get/demo:cmodel/DC", obj.getRedirectUrl());
    }
}
