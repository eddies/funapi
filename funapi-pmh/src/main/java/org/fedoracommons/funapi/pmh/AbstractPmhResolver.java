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

import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.fedoracommons.funapi.FormatException;
import org.fedoracommons.funapi.IdentifierException;
import org.fedoracommons.funapi.ObjectResolver;
import org.fedoracommons.funapi.UnapiException;
import org.fedoracommons.funapi.UnapiFormat;
import org.fedoracommons.funapi.UnapiFormats;
import org.fedoracommons.funapi.UnapiObject;
import org.fedoracommons.funapi.utilities.NamespaceContextImpl;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
    private HttpGet httpGet;

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public UnapiFormats getFormats(String id) throws UnapiException {
        UnapiFormats formats = getFormats();
        formats.setId(id);
        return formats;
    }

    /**
     * {@inheritDoc}
     */
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

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                 new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(
                 new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        HttpParams params = new BasicHttpParams();
        // Increase max total connection to 200
        params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 200);
        params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, 20);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);
        if (getUsername() != null && getPassword() != null) {
            httpClient.getCredentialsProvider()
                      .setCredentials(new AuthScope(getPmhBaseUrl().getHost(),
                                                    getPmhBaseUrl().getPort()),
                                      new UsernamePasswordCredentials(getUsername(),
                                                                      getPassword()));
        }
        setHttpClient(httpClient);
        return httpClient;
    }

    protected void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    protected void setGetRequest(HttpGet httpGet) {
        this.httpGet = httpGet;
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
        HttpGet getMethod;
        if (httpGet == null) {
            getMethod = new HttpGet(url);
        } else {
            getMethod = httpGet;
        }
        try {
            return getHttpClient().execute(getMethod, new BasicResponseHandler());
        } catch (IOException e) {
            throw new UnapiException(e.getMessage(), e);
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
