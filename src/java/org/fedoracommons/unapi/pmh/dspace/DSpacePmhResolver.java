/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://www.fedora.info/license/).
 */
package org.fedoracommons.unapi.pmh.dspace;

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
public class DSpacePmhResolver
        extends AbstractPmhResolver {

    private Properties props;
    private URL baseUrl;
    private String pmhIdPrefix;
    private String username;
    private String password;
    
    public DSpacePmhResolver()
            throws UnapiException {
        InputStream in = this.getClass().getResourceAsStream("DSpacePmhResolver.properties");
        props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            throw new UnapiException(e.getMessage(), e);
        }
        
        // e.g. http://localhost:8080/oai/request
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
        if (id.startsWith("hdl:")) {
            oaiId = pmhIdPrefix + id.substring("hdl:".length());
        } else if (id.startsWith("http://")) {
            oaiId = pmhIdPrefix + (id.substring(id.indexOf('/', "http://".length()) + 1));
        } else {
            oaiId = id;
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
