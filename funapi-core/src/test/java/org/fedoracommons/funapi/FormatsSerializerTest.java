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

package org.fedoracommons.funapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.HashMap;
import java.util.Map;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;

import org.junit.Before;
import org.junit.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import org.fedoracommons.funapi.FormatsSerializer;
import org.fedoracommons.funapi.UnapiException;
import org.fedoracommons.funapi.UnapiFormat;
import org.fedoracommons.funapi.UnapiFormats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FormatsSerializerTest {

    private XpathEngine engine;

    @Before
    public void setUp() {
        XMLUnit.setIgnoreWhitespace(true);
        engine = XMLUnit.newXpathEngine();
        Map<String, String> nsMap = new HashMap<String, String>();
        NamespaceContext ctx = new SimpleNamespaceContext(nsMap);
        engine.setNamespaceContext(ctx);
    }

    @Test
    public void testEmptyFormats() throws Exception {
        UnapiFormats formats = new UnapiFormats(null);
        FormatsSerializer ser = new FormatsSerializer();
        Diff diff = new Diff("<formats/>", ser.toString(formats));
        assertTrue("Expected empty formats element", diff.identical());
    }

    @Test
    public void test2Formats() throws Exception {
        UnapiFormats formats = new UnapiFormats(null);
        UnapiFormat oai_dc = new UnapiFormat("oai_dc", "text/xml", "http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
        formats.addFormat(oai_dc);
        UnapiFormat mods = new UnapiFormat("mods", "application/xml", "http://www.loc.gov/standards/mods/v3/mods-3-2.xsd");
        formats.addFormat(mods);

        Document doc = getDocument(formats);
        NodeList nodes;
        Node node;
        Element e;
        
        nodes = engine.getMatchingNodes("/formats/format[@name='oai_dc']", doc);
        assertEquals(1, nodes.getLength());
        node = nodes.item(0);
        assertEquals(Node.ELEMENT_NODE, node.getNodeType());
        e = (Element) node;
        assertEquals("text/xml", e.getAttribute("type"));
        assertEquals("http://www.openarchives.org/OAI/2.0/oai_dc.xsd", e.getAttribute("docs"));
        
        nodes = engine.getMatchingNodes("/formats/format[@name='mods']", doc);
        assertEquals(1, nodes.getLength());
        node = nodes.item(0);
        assertEquals(Node.ELEMENT_NODE, node.getNodeType());
        e = (Element) node;
        assertEquals("application/xml", e.getAttribute("type"));
        assertEquals("http://www.loc.gov/standards/mods/v3/mods-3-2.xsd", e.getAttribute("docs"));
    }
    
    private Document getDocument(UnapiFormats formats)
            throws UnsupportedEncodingException, SAXException, IOException, UnapiException {
        FormatsSerializer ser = new FormatsSerializer();
        return XMLUnit.buildControlDocument(ser.toString(formats));
    }

}
