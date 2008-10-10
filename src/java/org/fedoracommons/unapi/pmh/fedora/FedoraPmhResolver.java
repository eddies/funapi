/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://www.fedora.info/license/).
 */
package org.fedoracommons.unapi.pmh.fedora;

import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Properties;

import org.fedoracommons.unapi.UnapiException;
import org.fedoracommons.unapi.pmh.AbstractPmhResolver;


/**
 *
 * @author Edwin Shin
 * @since 0.1
 * @version $Id$
 */
public class FedoraPmhResolver
        extends AbstractPmhResolver {

    private Properties props;
    private URL baseUrl;
    private String pmhIdPrefix;
    private String username;
    private String password;
    private final static String FEDORA_URI = "info:fedora/";
    
    public FedoraPmhResolver()
            throws UnapiException {
        InputStream in = this.getClass().getResourceAsStream("FedoraPmhResolver.properties");
        props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            throw new UnapiException(e.getMessage(), e);
        }
        
        // e.g. http://localhost:8080/fedora/oai
        String temp = props.getProperty("pmhBaseUrl");
        if (temp.endsWith("/")) {
            temp.substring(0, temp.length() -1);
        }
        try {
            baseUrl = new URL(temp);
        } catch (MalformedURLException e) {
            throw new UnapiException(e.getMessage(), e);
        }
        
        pmhIdPrefix = props.getProperty("pmhIdPrefix");
        if (pmhIdPrefix == null) {
            // e.g. oai:localhost:
            pmhIdPrefix = String.format("oai:%s:", baseUrl.getHost());
        }
        username = props.getProperty("username");
        password = props.getProperty("password");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPmhId(String id) {
        String oaiId = null;
        if (id.startsWith(FEDORA_URI)) {
            oaiId = pmhIdPrefix + id.substring(FEDORA_URI.length());
        } else {
            oaiId = pmhIdPrefix + id;
        }
        return oaiId;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected URL getPmhBaseUrl() {
        return baseUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPassword() {
        return password;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUsername() {
        return username;
    }
}
