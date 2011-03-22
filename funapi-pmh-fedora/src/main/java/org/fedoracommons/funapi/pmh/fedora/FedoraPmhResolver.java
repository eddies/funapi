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
package org.fedoracommons.funapi.pmh.fedora;

import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Properties;

import org.fedoracommons.funapi.UnapiException;
import org.fedoracommons.funapi.pmh.AbstractPmhResolver;


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
        InputStream in = getClass().getResourceAsStream("/FedoraPmhResolver.properties");
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
