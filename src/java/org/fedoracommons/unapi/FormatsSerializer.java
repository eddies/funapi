package org.fedoracommons.unapi;

import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Provides an UnAPI formats serialization. The serialization format is given 
 * by the following Relax-NG schema:
 * <pre>
   element formats {
     attribute id { text }?, 
     element format {
       attribute name { text }, 
       attribute type { text }, 
       attribute docs { text }?,
     }*
   }</pre>
 *
 * @author Edwin Shin
 * @since 0.1
 * @version $Id$
 * @see <a href="http://unapi.info/specs/">unAPI Specs</a>
 */
public class FormatsSerializer {
    
    private DOMImplementation impl;
    
    private TransformerFactory xformFactory;
    
    public FormatsSerializer() throws UnapiException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);

        try {
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            impl = builder.getDOMImplementation();

            xformFactory = TransformerFactory.newInstance();
        } catch (ParserConfigurationException e) {
            throw new UnapiException(e.getMessage(), e);
        }
    }
    
    public void serialize(UnapiFormats formats, OutputStream out) throws UnapiException { 
        transform(formats, new StreamResult(out));
    }
    
    public Document toDocument(UnapiFormats formats) {
        Document doc = impl.createDocument(null, "formats", null);
        Element rootElement = doc.getDocumentElement();
        
        if (formats.getId() != null) {
            rootElement.setAttribute("id", formats.getId());
        }

        for (UnapiFormat format : formats.getFormats()) {
            Element f = doc.createElement("format");
            f.setAttribute("name", format.getName());
            f.setAttribute("type", format.getType());
            if (format.getDocs() != null) {
                f.setAttribute("docs", format.getDocs());
            }
            rootElement.appendChild(f);
        }
        return doc;
    }
    
    public String toString(UnapiFormats formats) throws UnapiException {
        StringWriter sw = new StringWriter();
        transform(formats, new StreamResult(sw));
        return sw.toString();
    }
    
    private void transform(UnapiFormats formats, Result output) throws UnapiException {
        Transformer idTransform;
        try {
            idTransform = xformFactory.newTransformer();
            Source input = new DOMSource(toDocument(formats));
            idTransform.transform(input, output);
        } catch (TransformerConfigurationException e) {
            throw new UnapiException(e.getMessage(), e);
        } catch (TransformerException e) {
            throw new UnapiException(e.getMessage(), e);
        }
    }
}
