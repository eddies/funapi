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
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * An implementation of the unAPI version 1 HTTP interface functions.
 * 
 * @author Edwin Shin
 * @since 0.1
 * @version $Id$
 * @see <a href="http://unapi.info/specs/">unAPI Specs</a>
 */
public class UnapiServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    /** Content type for xml. */
    private static final String CONTENT_TYPE_XML = "application/xml; charset=UTF-8";
    
    private List<UnapiFormat> defaultFormats = new ArrayList<UnapiFormat>();
    
    private FormatsSerializer serializer;
    
    private ObjectResolver resolver;
    
    @Override
    public void init() throws ServletException {
        String className = getServletConfig().getInitParameter("resolver");
        
        try {
            serializer = new FormatsSerializer();
            Class<?> klass  = Class.forName(className);
            resolver = (ObjectResolver)klass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (UnapiException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String id = request.getParameter("id");
        String format = request.getParameter("format");

        if (id == null && format == null) {
            listDefaultFormats(response);
        } else if (format == null) {
            listObjectFormats(response, id);
        } else {
            getObject(response, id, format);
        }
    }
    
    /**
     * Provide a list of object formats which should be supported for all 
     * objects available through the unAPI service. 
     * Content-type must be "application/xml".
     * 
     * @throws IOException 
     */
    private void listDefaultFormats(HttpServletResponse response) throws IOException {
        UnapiFormats formats = null;
        try {
            if (defaultFormats.size() > 0) {
                formats = new UnapiFormats(null, defaultFormats);
            } else {
                formats = resolver.getFormats();
            }
            response.setContentType(CONTENT_TYPE_XML);
            serializer.serialize(formats, response.getOutputStream());
        } catch (UnapiException e) {
            response.sendError(e.getErrorCode(), e.getMessage());
        }
    }
    
    /**
     * Provide a list of object formats available from the unAPI service for the 
     * object identified by <code>id</code>. Content-type must be 
     * "application/xml". 
     * It is similar to the {@link #listDefaultFormats(HttpServletResponse) 
     * listDefaultFormats} response, adding only an "id" attribute on the 
     * root "formats" element; this echoes the requested <code>id</code>.
     * 
     * @param id
     * @throws IOException 
     */
    private void listObjectFormats(HttpServletResponse response, String id) throws IOException {
        try {
            UnapiFormats formats = resolver.getFormats(id);
            response.setContentType(CONTENT_TYPE_XML);
            response.setStatus(HttpServletResponse.SC_MULTIPLE_CHOICES);
            //FIXME is SC_MULTIPLE_CHOICES only sent when there are more than 
            //one format, or always?
            //if (formats.getFormats().size() > 1) {
            //    response.setStatus(HttpServletResponse.SC_MULTIPLE_CHOICES);
            //}
            serializer.serialize(formats, response.getOutputStream());
        } catch(UnapiException e) {
            response.sendError(e.getErrorCode(), e.getMessage());
        }
    }
    
    /**
     * Provide the bare object specified by <code>id</code> in the format 
     * specified by <code>format</code>. <code>format</code> should be a format 
     * name as specified by the value of the "name" attribute in the 
     * UNAPI?id=<code>id</code> response, and the response content-type must be 
     * the content-type specified by the value of the "type" attribute in the 
     * UNAPI?id=<code>id</code> response for this format.
     * 
     * @param id
     * @param format
     */
    private void getObject(HttpServletResponse response, String id, String format) throws IOException {
        try {
            UnapiObject obj = resolver.getObject(id, format);
            if (obj.getRedirectUrl() != null) {
                response.sendRedirect(obj.getRedirectUrl());
            } else {
                InputStream in = obj.getInputStream();
                if (in == null) {
                    throw new UnapiException("Error getting " + id + " as " + 
                                             format + ". Neither redirectUrl " +
                                             "nor InputStream was provided.");
                }
                response.setContentType(obj.getContentType());
                copy(in, response.getOutputStream());
                in.close();
            }
        } catch(UnapiException e) {
            response.sendError(500, e.getMessage());
        }
    }
    
    private static final int BUFF_SIZE = 100000;
    
    /**
     * A static 100K buffer used by the copy operation.
     */
    private static final byte[] buffer = new byte[BUFF_SIZE];

    /**
     * Copy an InputStream to an OutputStream. 
     * While this method will automatically close the destination OutputStream,
     * the caller is responsible for closing the source InputStream.
     * 
     * @param source
     * @param destination
     * @return <code>true</code> if the operation was successful;
     *         <code>false</code> otherwise (which includes a null input).
     * @see <a href="http://java.sun.com/docs/books/performance/1st_edition/html/JPIOPerformance.fm.html#22980">
     * http://java.sun.com/docs/books/performance/1st_edition/html/JPIOPerformance.fm.html#22980</a>
     */
    public static boolean copy(InputStream source, OutputStream destination) {
        try {
            while (true) {
                synchronized (buffer) {
                    int amountRead = source.read(buffer);
                    if (amountRead == -1) {
                        break;
                    }
                    destination.write(buffer, 0, amountRead);
                }
            }
            destination.flush();
            destination.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
