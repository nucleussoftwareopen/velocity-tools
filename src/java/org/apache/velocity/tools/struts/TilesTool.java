/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.velocity.tools.struts;

import java.util.Stack;
import java.util.Map;
import java.util.Iterator;

import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.AttributeDefinition;
import org.apache.struts.tiles.DirectStringAttribute;
import org.apache.struts.tiles.DefinitionAttribute;
import org.apache.struts.tiles.DefinitionNameAttribute;
import org.apache.struts.tiles.PathAttribute;
import org.apache.struts.tiles.TilesUtil;
import org.apache.struts.tiles.DefinitionsFactoryException;
import org.apache.struts.tiles.Controller;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.ImportSupport;
import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ViewTool;

/**
 * <p>View tool to use struts-tiles with Velocity</p>
 * <p><pre>
 * Template example(s):
 *  &lt;!-- insert a tile --&gt;
 *  $tiles.myTileDefinition
 *
 *  &lt;!-- get named attribute value from the current tiles-context --&gt;
 *  $tiles.getAttribute("myTileAttribute")
 *
 *  &lt;!-- import all attributes of the current tiles-context into the velocity-context. --&gt;
 *  $tiles.importAttributes()
 *
 * Toolbox configuration:
 * &lt;tool&gt;
 *   &lt;key&gt;tiles&lt;/key&gt;
 *   &lt;scope&gt;request&lt;/scope&gt;
 *   &lt;class&gt;org.apache.velocity.tools.struts.TilesTool&lt;/class&gt;
 * &lt;/tool&gt;
 * </pre></p>
 *
 * <p>This tool should only be used in the request scope.</p>
 *
 * @author <a href="mailto:marinoj@centrum.is">Marino A. Jonsson</a>
 * @since VelocityTools 1.1
 * @version $Revision: 1.9 $ $Date: 2004/01/27 01:42:32 $
 */
public class TilesTool extends ImportSupport
        implements ViewTool
{
    static final String PAGE_SCOPE = "page";
    static final String REQUEST_SCOPE = "request";
    static final String SESSION_SCOPE = "session";
    static final String APPLICATION_SCOPE = "application";

    protected ViewContext context;

    /**
     * A stack to hold ComponentContexts while nested tile-definitions
     * are rendered.
     */
    protected Stack contextStack;

    /******************************* Constructors ****************************/

    /**
     * Default constructor. Tool must be initialized before use.
     */
    public TilesTool() {}

    /**
     * Initializes this tool.
     *
     * @param obj the current ViewContext
     * @throws IllegalArgumentException if the param is not a ViewContext
     */
    public void init(Object obj)
    {
        if (!(obj instanceof ViewContext))
        {
            throw new IllegalArgumentException(
                    "Tool can only be initialized with a ViewContext");
        }

        this.context = (ViewContext)obj;
        this.request = context.getRequest();
        this.response = context.getResponse();
        this.application = context.getServletContext();
    }

    /***************************** View Helpers ******************************/

    /**
     * Imports all attributes in the current tiles-context into the velocity-context.
     *
     * <p>This is functionally equivalent to
     * <code>&lt;tiles:importAttribute /&gt;</code>.</p>
     */
    public void importAttributes()
    {
        this.importAttributes(PAGE_SCOPE);
    }

    /**
     * Imports all attributes in the current tiles-context into the named
     * context ("page", "request", "session", or "application").
     *
     * <p>This is functionally equivalent to
     * <code>&lt;tiles:importAttribute scope="scopeValue" /&gt;</code>.</p>
     *
     * @param scope the named context scope to put the attributes into.
     */
    public void importAttributes(String scope)
    {
        ComponentContext currentContext = ComponentContext.getContext(this.
                request);
        Iterator names = currentContext.getAttributeNames();

        if (scope.equals(PAGE_SCOPE))
        {
            while (names.hasNext())
            {
                String name = (String)names.next();
                context.getVelocityContext().put(name,
                                                 currentContext.
                                                 getAttribute(name));
            }
        }
        else if (scope.equals(REQUEST_SCOPE))
        {
            while (names.hasNext())
            {
                String name = (String)names.next();
                request.setAttribute(name, currentContext.getAttribute(name));
            }
        }
        else if (scope.equals(SESSION_SCOPE))
        {
            while (names.hasNext())
            {
                String name = (String)names.next();
                request.getSession().setAttribute(name,
                                                  currentContext.
                                                  getAttribute(name));
            }
        }
        else if (scope.equals(APPLICATION_SCOPE))
        {
            while (names.hasNext())
            {
                String name = (String)names.next();
                application.setAttribute(name, currentContext.getAttribute(name));
            }
        }
    }


    /**
     * Fetches a named attribute-value from the current tiles-context.
     *
     * <p>This is functionally equivalent to
     * <code>&lt;tiles:getAsString name="attributeName" /&gt;</code>.</p>
     *
     * @param attributeName the name of the tiles-attribute to fetch
     * @return attribute value for the named attribute
     */
    public Object getAttribute(String attributeName)
    {
        ComponentContext currentContext =
            ComponentContext.getContext(this.request);
        Object value = currentContext.getAttribute(attributeName);
        if (value == null)
        {
            Velocity.warn("TilesTool: Tile attribute '"
                           + attributeName
                           + "' was not found in context.");
        }
        return value;
    }

    /**
     * Imports the named attribute-value from the current tiles-context into the
     * current Velocity context.
     *
     * <p>This is functionally equivalent to
     * <code>&lt;tiles:importAttribute name="attributeName" /&gt;</code>
     *
     * @param attributeName the name of the tiles-attribute to import
     */
    public void importAttribute(String attributeName)
    {
        this.importAttribute(attributeName, PAGE_SCOPE);
    }

    /**
     * Imports the named attribute-value from the current tiles-context into the
     * named context ("page", "request", "session", or "application").
     *
     * <p>This is functionally equivalent to
     * <code>&lt;tiles:importAttribute name="attributeName" scope="scopeValue" /&gt;</code>
     *
     * @param attributeName the name of the tiles-attribute to import
     * @param scope the named context scope to put the attribute into.
     */
    public void importAttribute(String attributeName, String scope)
    {
        ComponentContext currentContext =
            ComponentContext.getContext(this.request);
        Object value = currentContext.getAttribute(attributeName);
        if (value == null)
        {
            Velocity.warn("TilesTool: Tile attribute '"
                           + attributeName
                           + "' was not found in context.");
        }

        if (scope.equals(PAGE_SCOPE))
        {
            context.getVelocityContext().put(attributeName, value);
        }
        else if (scope.equals(REQUEST_SCOPE))
        {
            request.setAttribute(attributeName, value);
        }
        else if (scope.equals(SESSION_SCOPE))
        {
            request.getSession().setAttribute(attributeName, value);
        }
        else if (scope.equals(APPLICATION_SCOPE))
        {
            application.setAttribute(attributeName, value);
        }

    }



    /**
     * <p>A generic tiles insert function</p>
     *
     * <p>This is functionally equivalent to
     * <code>&lt;tiles:insert attribute="menu" /&gt;</code>.</p>
     *
     * @param attr - can be any of the following:
     *        AttributeDefinition,
     *        tile-definition name,
     *        tile-attribute name,
     *        regular uri.
     *        (checked in that order)
     * @return the rendered template or value as a String
     * @throws Exception on failure
     */
    public String get(Object attr)
    {
        try
        {
            ComponentContext currentContext =
                ComponentContext.getContext(this.request);
            Object attrValue = currentContext.getAttribute(attr.toString());
            if (attrValue != null)
            {
                return processObjectValue(attrValue);
            }
            return processAsDefinitionOrURL(attr.toString());
        }
        catch (Exception e)
        {
            Velocity.error("TilesTool: Exeption while rendering Tile "
                           + attr + ": " + e.getMessage());
            return null;
        }
    }

    /************************** Protected Methods ****************************/

    /**
     * Process an object retrieved as a bean or attribute.
     *
     * @param value - Object can be a typed attribute, a String, or anything
     *        else. If typed attribute, use associated type. Otherwise, apply
     *        toString() on object, and use returned string as a name.
     * @throws Exception - Throws by underlying nested call to
     *         processDefinitionName()
     * @return the fully processed value as String
     */
    protected String processObjectValue(Object value) throws Exception
    {
        /* First, check if value is one of the Typed Attribute */
        if (value instanceof AttributeDefinition)
        {
            /* We have a type => return appropriate IncludeType */
            return processTypedAttribute((AttributeDefinition)value);
        }
        else if (value instanceof ComponentDefinition)
        {
            return processDefinition((ComponentDefinition)value);
        }
        /* Value must denote a valid String */
        return processAsDefinitionOrURL(value.toString());
    }

    /**
     * Process typed attribute according to its type.
     *
     * @param value Typed attribute to process.
     * @return the fully processed attribute value as String.
     * @throws Exception - Throws by underlying nested call to processDefinitionName()
     */
    protected String processTypedAttribute(AttributeDefinition value) throws
            Exception
    {
        if (value instanceof DirectStringAttribute)
        {
            return (String)value.getValue();
        }
        else if (value instanceof DefinitionAttribute)
        {
            return processDefinition((ComponentDefinition)value.getValue());
        }
        else if (value instanceof DefinitionNameAttribute)
        {
            return processAsDefinitionOrURL((String)value.getValue());
        }
        /* else if( value instanceof PathAttribute ) */
        return doInsert((String)value.getValue(), null, null);
    }

    /**
     * Try to process name as a definition, or as an URL if not found.
     *
     * @param name Name to process.
     * @return the fully processed definition or URL
     * @throws Exception
     */
    protected String processAsDefinitionOrURL(String name) throws Exception
    {
        try
        {
            ComponentDefinition definition =
                    TilesUtil.getDefinition(name, this.request, this.application);
            if (definition != null)
            {
                return processDefinition(definition);
            }
        }
        catch (DefinitionsFactoryException ex)
        {
        /* silently failed, because we can choose to not define a factory. */
        }
        /* no definition found, try as url */
        return processUrl(name);
    }

    /**
     * End of Process for definition.
     *
     * @param definition Definition to process.
     * @return the fully processed definition.
     * @throws Exception from InstantiationException Can't create requested controller
     */
    protected String processDefinition(ComponentDefinition definition) throws
            Exception
    {
        Controller controller = null;
        try
        {
            controller = definition.getOrCreateController();

            String role = definition.getRole();
            String page = definition.getTemplate();

            return doInsert(definition.getAttributes(),
                            page,
                            role,
                            controller);
        }
        catch (InstantiationException ex)
        {
            throw new Exception(ex.getMessage());
        }
    }

    /**
     * Processes an url
     *
     * @param url the URI to process.
     * @return the rendered template as String.
     * @throws Exception
     */
    protected String processUrl(String url) throws Exception
    {
        return doInsert(url, null, null);
    }

    /**
     * Use this if there is no nested tile.
     *
     * @param page the page to process.
     * @param role possible user-role
     * @param controller possible tiles-controller
     * @return the rendered template as String.
     * @throws Exception
     */
    protected String doInsert(String page, String role, Controller controller) throws
            Exception
    {
        if (role != null && !this.request.isUserInRole(role))
        {
            return null;
        }

        ComponentContext subCompContext = new ComponentContext();
        return doInsert(subCompContext, page, role, controller);
    }

    /**
     * Use this if there is a nested tile.
     *
     * @param attributes attributes for the sub-context
     * @param page the page to process.
     * @param role possible user-role
     * @param controller possible tiles-controller
     * @return the rendered template as String.
     * @throws Exception
     */
    protected String doInsert(Map attributes,
                              String page,
                              String role,
                              Controller controller) throws Exception
    {
        if (role != null && !this.request.isUserInRole(role))
        {
            return null;
        }

        ComponentContext subCompContext = new ComponentContext(attributes);
        return doInsert(subCompContext, page, role, controller);
    }

    /**
     * An extension of the other two doInsert functions
     *
     * @param subCompContext the sub-context to set in scope when the
     *        template is rendered.
     * @param page the page to process.
     * @param role possible user-role
     * @param controller possible tiles-controller
     * @return the rendered template as String.
     * @throws Exception
     */
    protected String doInsert(ComponentContext subCompContext,
                              String page,
                              String role,
                              Controller controller) throws Exception
    {
        pushTilesContext();
        try
        {
            ComponentContext.setContext(subCompContext, this.request);

            /* Call controller if any */
            if (controller != null)
            {
                controller.perform(subCompContext,
                                   this.request,
                                   this.response,
                                   this.application);
            }
            /* pass things off to ImportSupport */
            return this.acquireString(page);
        }
        finally
        {
            popTilesContext();
        }
    }

    /**
     * <p>pushes the current tiles context onto the context-stack.
     * preserving the context is necessary so that a sub-context can be
     * put into request scope and lower level tiles can be rendered</p>
     */
    protected void pushTilesContext()
    {
        if (this.contextStack == null)
        {
            this.contextStack = new Stack();
        }
        contextStack.push(ComponentContext.getContext(this.request));
    }

    /**
     * <p>pops the tiles sub-context off the context-stack after the lower level
     * tiles have been rendered</p>
     */
    protected void popTilesContext()
    {
        ComponentContext.setContext((ComponentContext)this.contextStack.pop(),
                                    this.request);
    }

}
