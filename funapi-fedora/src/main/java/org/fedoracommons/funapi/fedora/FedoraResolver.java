package org.fedoracommons.funapi.fedora;

import static com.yourmediashelf.fedora.client.request.FedoraRequest.getDatastreamDissemination;
import static com.yourmediashelf.fedora.client.request.FedoraRequest.getRelationships;
import static com.yourmediashelf.fedora.client.request.FedoraRequest.listDatastreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonNode;
import org.codehaus.jackson.map.JsonTypeMapper;
import org.fedoracommons.funapi.ObjectResolver;
import org.fedoracommons.funapi.UnapiException;
import org.fedoracommons.funapi.UnapiFormat;
import org.fedoracommons.funapi.UnapiFormats;
import org.fedoracommons.funapi.UnapiObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileUtils;
import com.sun.jersey.api.client.ClientResponse;
import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.FedoraCredentials;

/**
 * Implementation of ObjectResolver for a Fedora repository.
 * This implementation will attempt to load a properties file with the name
 * <code>FedoraResolver.properties</code>.
 *
 * Required properties:
 * <dl>
 *   <dt>baseURL</dt>
 *     <dd>The base URL of the Fedora repository, e.g. http://localhost:8080/fedora/</dd>
 *   <dt>formatsDatastream</dt>
 *     <dd>The id of a content model object's inline XML datastream which
 *     describes the unAPI formats available for a Fedora object, e.g.
 *     UNAPI-FORMATS</dd>
 * </dl>
 *
 * The formatsDatastream must return an XML document that contains a JSON array
 * describing the available formats. For example:
 * <pre>&lt;json&gt;
[[&quot;info:fedora/&#42;/DC&quot;,&quot;oai_dc&quot;,&quot;text/xml&quot;,&quot;http://www.openarchives.org/OAI/2.0/oai_dc.xsd&quot;],
 [&quot;info:fedora/&#42;/demo:dc2mods.sdef/transform&quot;,&quot;mods&quot;,&quot;application/xml&quot;,&quot;http://www.loc.gov/standards/mods/&quot;]]
&lt;/json&gt;</pre>
 *
 * The above is a JSON array of arrays surrounded by &lt;json&gt; tags.
 * Each four-element inner array represents a single unAPI format.
 * The first element is the dissemination type URI which will requested object
 * in the requested format.
 * The remaining three elements are the unAPI format, type and docs.
 *
 * @author Edwin Shin
 * @since 0.1
 * @version $Id$
 */
public class FedoraResolver
        implements ObjectResolver {

    private final Properties props;
    private URL baseURL;
    private final String username;
    private final String password;
    private final String formatsDS;
    private final String defaultFormats;
    private final static String FEDORA_URI = "info:fedora/";
    private final static String HAS_MODEL_URI = "info:fedora/fedora-system:def/model#hasModel";
    private FedoraClient fedoraClient;
    private final JsonFactory jsonFactory;

    public FedoraResolver() throws UnapiException {
        InputStream in = getClass().getResourceAsStream("/FedoraResolver.properties");
        props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            throw new UnapiException(e.getMessage(), e);
        }

        // e.g. http://localhost:8080/fedora
        String temp = props.getProperty("baseURL");
        if (!temp.endsWith("/")) {
            temp += "/";
        }
        try {
            baseURL = new URL(temp);
        } catch (MalformedURLException e) {
            throw new UnapiException(e.getMessage(), e);
        }

        username = props.getProperty("username");
        password = props.getProperty("password");
        formatsDS = props.getProperty("formatsDatastream");
        defaultFormats = props.getProperty("defaultFormats");
        FedoraCredentials credentials = new FedoraCredentials(baseURL, username, password);
        fedoraClient = new FedoraClient(credentials);
        jsonFactory = new JsonFactory();
    }

    public UnapiFormats getFormats() throws UnapiException {
        UnapiFormats formats;
        if (defaultFormats == null) {
            formats = new UnapiFormats(null);
            UnapiFormat format = new UnapiFormat("oai_dc","text/xml","http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
            formats.addFormat(format);
        } else {
            try {
                formats = parseJsonArray(null, defaultFormats);
            } catch (IOException e) {
                throw new UnapiException(e.getMessage(), e);
            }
        }
        return formats;
    }

    public UnapiFormats getFormats(String id) throws UnapiException {
        UnapiFormats formats;
        try {
            formats = parseJsonArray(id, getJsonArray(id));
        } catch (IOException e) {
            throw new UnapiException(e.getMessage(), e);
        } catch (FedoraClientException e) {
            throw new UnapiException(e.getMessage(), e);
        }
        return formats;
    }

    public UnapiObject getObject(String id, String format) throws UnapiException {
        String url;
        try {
            url = getDisseminationUrl(id, format);
        } catch (IOException e) {
            throw new UnapiException(e.getMessage(), e);
        } catch (FedoraClientException e) {
            throw new UnapiException(e.getMessage(), e);
        }
        return new UnapiObject(url);
    }

    /**
     * Returns a resolvable Fedora URL for a PID or info:fedora uri.
     *
     * @param id
     * @return
     */
    private String getURL(String id) {
        String newId = null;
        if (id.startsWith(FEDORA_URI)) {
            newId = id.substring(FEDORA_URI.length());
        } else {
            newId = id;
        }
        return baseURL + "get/" + newId;
    }

    /**
     *
     * @param id an info:fedora uri (e.g. info:/fedora/demo:1)
     * @return a JSON array of arrays, e.g. [["disstype","format","type","docs"],[...]]
     * @throws IOException
     * @throws ServiceException
     * @throws UnapiException
     * @throws FedoraClientException
     */
    private String getJsonArray(String id) throws IOException, UnapiException, FedoraClientException {
        String pid = id.substring(FEDORA_URI.length());
        ClientResponse response = fedoraClient.execute(getRelationships(pid).subject(id).predicate(HAS_MODEL_URI).build());

        List<String> cmodels = new ArrayList<String>();
        Model model = ModelFactory.createDefaultModel();
        model.read(response.getEntityInputStream(), null, FileUtils.langNTriple);
        StmtIterator sit = model.listStatements();
        while (sit.hasNext()) {
            cmodels.add(sit.next().getObject().toString());
        }

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc = null;
        try {
            builder = domFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new UnapiException(e.getMessage(), e);
        }
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        XPathExpression expr = null;
        try {
            expr = xpath.compile(String.format("count(/objectDatastreams/datastream[@dsid='%s'])", formatsDS));
        } catch (XPathExpressionException e) {
            throw new UnapiException(e.getMessage(), e);
        }

        JsonTypeMapper jtMapper = new JsonTypeMapper();
        JsonNode rootNode = null;
        boolean hasFormatsDS;
        for (String cmodel : cmodels) {
            response = fedoraClient.execute(listDatastreams(cmodel).format("xml").build());
            try {
                doc = builder.parse(response.getEntityInputStream());
                Object xpathResult = expr.evaluate(doc, XPathConstants.BOOLEAN);
                hasFormatsDS = (Boolean)xpathResult;

                if (hasFormatsDS) {
                    response = fedoraClient.execute(getDatastreamDissemination(cmodel, formatsDS).format("xml").build());
                    doc = builder.parse(response.getEntityInputStream());
                }
            } catch (SAXException e) {
                throw new UnapiException(e.getMessage(), e);
            } catch (XPathExpressionException e) {
                throw new UnapiException(e.getMessage(), e);
            }

            Element jsonElement = (Element)doc.getElementsByTagName("json").item(0);

            String json = jsonElement.getTextContent();
            JsonNode node = jtMapper.read(jsonFactory.createJsonParser(new StringReader(json)));
            if (rootNode == null) {
                rootNode = node;
            } else {
                Iterator<JsonNode> it = node.getElements();
                while(it.hasNext()) {
                    rootNode.appendElement(it.next());
                }
            }
        }

        if (rootNode == null) {
            return null;
        } else {
            StringWriter sw = new StringWriter();
            JsonGenerator gen = jsonFactory.createJsonGenerator(sw);
            rootNode.writeTo(gen);
            gen.close();
            return sw.toString();
        }
    }

    private UnapiFormats parseJsonArray(String id, String jsonContent) throws IOException, UnapiException {
        UnapiFormats uFormats = new UnapiFormats(id);
        if (jsonContent == null) {
            return uFormats;
        }
        Set<String> formats = new HashSet<String>();

        JsonNode rootNode = new JsonTypeMapper().read(jsonFactory.createJsonParser(new StringReader(jsonContent)));
        Iterator<JsonNode> it = rootNode.getElements();
        while (it.hasNext()) {
            JsonNode innerArray = it.next();
            String format = innerArray.getElementValue(1).getTextValue();
            String type = innerArray.getElementValue(2).getTextValue();
            String docs = innerArray.getElementValue(3).getTextValue();

            if (formats.add(format)) {
                UnapiFormat uFormat = new UnapiFormat(format, type, docs);
                uFormats.addFormat(uFormat);
            }
        }
        return uFormats;
    }

    private String getDisseminationUrl(String id, String format) throws IOException, UnapiException, FedoraClientException {
        String pid = id.substring(FEDORA_URI.length());
        String dissType = null;

        String jsonContent = getJsonArray(id);
        JsonNode rootNode = new JsonTypeMapper().read(jsonFactory.createJsonParser(new StringReader(jsonContent)));

        Iterator<JsonNode> it = rootNode.getElements();
        while (it.hasNext()) {
            JsonNode innerArray = it.next();

            String fmt = innerArray.getElementValue(1).getTextValue();
            if (format.equalsIgnoreCase(fmt)) {
                dissType = innerArray.getElementValue(0).getTextValue();
                break;
            }
        }
        if (dissType == null) {
            throw new UnapiException("No dissType for " + id);
        }
        // dissType example: info:fedora/*/sdef:1/methodFoo
        dissType = dissType.replaceAll("\\*", pid);
        return getURL(dissType);
    }

    protected void setFedoraClient(FedoraClient fedoraClient) {
        this.fedoraClient = fedoraClient;
    }
}
