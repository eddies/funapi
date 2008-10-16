package org.fedoracommons.funapi.pmh;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import org.fedoracommons.funapi.FormatException;
import org.fedoracommons.funapi.IdentifierException;
import org.fedoracommons.funapi.ObjectResolver;
import org.fedoracommons.funapi.UnapiException;
import org.fedoracommons.funapi.UnapiFormat;
import org.fedoracommons.funapi.UnapiFormats;
import org.fedoracommons.funapi.UnapiObject;
import org.fedoracommons.funapi.utilities.NamespaceContextImpl;


import static org.apache.commons.httpclient.HttpStatus.SC_OK;

/**
 *
 * @author Edwin Shin
 * @since 0.1
 * @version $Id$
 */
public abstract class AbstractPmhResolver
        implements ObjectResolver {
    
    private final static String FORMATS = "%s?verb=ListMetadataFormats";
    private final static String RECORD = "%s?verb=GetRecord&metadataPrefix=%s&identifier=%s";
    private NamespaceContextImpl nsCtx;
    private HttpClient httpClient;
    private HttpMethod httpMethod;
    
    public UnapiFormats getFormats() throws UnapiException {
        String mdFormats = listMetadataFormats();
        UnapiFormats formats = new UnapiFormats(null);
        XPath xpath = getXPath();
        NodeList nodelist = null;
        try {
            nodelist = (NodeList)xpath.evaluate("//oai:metadataFormat", 
                                                new InputSource(new StringReader(mdFormats)), 
                                                XPathConstants.NODESET);
            for(int i = 0; i < nodelist.getLength(); i++) {
                Node node = nodelist.item(i);
                String format = xpath.evaluate("oai:metadataPrefix", node);
                String docs = xpath.evaluate("oai:schema", node);
                UnapiFormat uFormat = new UnapiFormat(format, "application/xml", docs);
                formats.addFormat(uFormat);
            }
        } catch (XPathExpressionException e) {
            throw new UnapiException(e.getMessage(), e);
        }
        return formats;
    }
    
    public UnapiFormats getFormats(String id) throws UnapiException {
        UnapiFormats formats = getFormats();
        formats.setId(id);
        return formats;
    }

    public UnapiObject getObject(String id, String format) throws UnapiException {
        try {
            String record = getRecord(id, format);         
            XPath xpath = getXPath();

            Node pmh = (Node)xpath.evaluate("//oai:OAI-PMH", 
                                            new InputSource(new StringReader(record)), 
                                            XPathConstants.NODE);
            
            Node metadata = (Node)xpath.evaluate("//oai:metadata/*", 
                                                 pmh, 
                                                 XPathConstants.NODE);

            if (metadata == null) {
                String error = xpath.evaluate("//oai:error/@code", pmh);
                if (error.equalsIgnoreCase("idDoesNotExist")) {
                    throw new IdentifierException(error);
                } else if (error.equalsIgnoreCase("cannotDisseminateFormat")) {
                    throw new FormatException(error);
                } else {
                    throw new UnapiException(error);
                }
            }
            
            TransformerFactory xformFactory = TransformerFactory.newInstance();
            Transformer transformer = xformFactory.newTransformer();  
            
            Source source = new DOMSource(metadata);
            StringWriter sw = new StringWriter();            
            transformer.transform(source, new StreamResult(sw));
            InputStream in = new ByteArrayInputStream(sw.toString().getBytes("UTF-8"));
            return new UnapiObject(in, "application/xml");
        } catch (XPathExpressionException e) {
            throw new UnapiException(e.getMessage(), e);
        } catch (TransformerException e) {
            throw new UnapiException(e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            throw new UnapiException(e.getMessage(), e);
        }
    }
    
    private XPath getXPath() {
        XPathFactory xpFactory = XPathFactory.newInstance();
        XPath xpath = xpFactory.newXPath();
        if (nsCtx == null) {
            nsCtx = new NamespaceContextImpl();
            nsCtx.addNamespace("oai", "http://www.openarchives.org/OAI/2.0/");
            nsCtx.addNamespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
            nsCtx.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        }
        xpath.setNamespaceContext(nsCtx);
        return xpath;
    }
    
    protected HttpClient getHttpClient() {
        if (httpClient != null) {
            return httpClient;
        }
        MultiThreadedHttpConnectionManager connectionManager = 
            new MultiThreadedHttpConnectionManager();

        HttpClient client = new HttpClient(connectionManager);
        client.getParams().setAuthenticationPreemptive(true);
        if (getUsername() != null && getPassword() != null) {
            client.getState().setCredentials(
                 new AuthScope(getPmhBaseUrl().getHost(), 
                               getPmhBaseUrl().getPort(), null),
                 new UsernamePasswordCredentials(getUsername(), getPassword())
                 );
        }
        return client;
    }
    
    protected void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    
    protected void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }
    
    private String listMetadataFormats() throws UnapiException {
        String url = String.format(FORMATS, getPmhBaseUrl());
        return getResponse(url);
    }
    
    private String getRecord(String id, String format) throws UnapiException {
        String url = String.format(RECORD, getPmhBaseUrl(), format, getPmhId(id));
        return getResponse(url);
    }
    
    private String getResponse(String url) throws UnapiException {
        HttpMethod getMethod;
        if (httpMethod == null) {
            getMethod = new GetMethod(url);
        } else {
            getMethod = httpMethod;
        }
        try {
            int status = getHttpClient().executeMethod(getMethod);
            if (status != SC_OK) {
                throw new UnapiException(status, getMethod.getStatusText());
            }
            return getMethod.getResponseBodyAsString();
        } catch (IOException e) {
            throw new UnapiException(e.getMessage(), e);
        } finally {
            getMethod.releaseConnection();
        }
    }
    
    /**
     * @param id an unAPI identifier
     * @return the corresponding OAI-PMH identifier
     */
    protected abstract String getPmhId(String id);

    /**
     * 
     * @return The base URL of the OAI-PMH service, e.g. http://localhost:8080/oai/request.
     */
    protected abstract URL getPmhBaseUrl();
    
    /**
     * @return The username, if any, required to access the OAI-PMH service.
     */
    protected abstract String getUsername();
    
    /**
     * @return The password, if any, required to access the OAI-PMH service.
     */
    protected abstract String getPassword();
}
