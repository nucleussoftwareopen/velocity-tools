package org.apache.velocity.tools.config;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Collection;
import java.util.SortedSet;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.ToolboxFactory;

/**
 * 
 *
 * @author Nathan Bubna
 * @version $Id: ToolboxConfiguration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class ToolboxConfiguration
    extends CompoundConfiguration<ToolConfiguration>
{
    private String scope = ToolboxFactory.DEFAULT_SCOPE;

    public ToolboxConfiguration()
    {
        // ensure that even the default scope is set as a property
        setProperty("scope", this.scope);
    }

    public void setScope(String scope)
    {
        if (scope == null)
        {
            throw new NullPointerException("Toolbox scope cannot be null");
        }
        this.scope = scope;

        // ensure the scope is also set as a property of the toolbox
        setProperty("scope", scope);
    }

    public String getScope()
    {
        return this.scope;
    }

    public void addTool(ToolConfiguration tool)
    {
        addChild(tool);
    }

    public void removeTool(ToolConfiguration tool)
    {
        removeChild(tool);
    }

    public ToolConfiguration getTool(String key)
    {
        for (ToolConfiguration tool : getTools())
        {
            if (key.equals(tool.getKey()))
            {
                return tool;
            }
        }
        return null;
    }

    public SortedSet<ToolConfiguration> getTools()
    {
        return getChildren();
    }

    public void setTools(Collection<ToolConfiguration> tools)
    {
        setChildren(tools);
    }

    @Override
    public void validate()
    {
        super.validate();

        if (getScope() == null)
        {
            throw new ConfigurationException(this, "Toolbox scope cannot be null");
        }
        if (!Scope.exists(getScope()))
        {
            throw new ConfigurationException(this, "Scope '"+getScope()+"' is not recognized. Please correct or add your new custom scope with "+Scope.class.getName()+".add(\""+getScope()+"\").");
        }

        // validate that all tools are allowed in this scope
        for (ToolConfiguration tool : getTools())
        {
            // check if this toolbox's scope has been declared invalid
            for (String invalidScope : tool.getInvalidScopes())
            {
                if (getScope().equals(invalidScope))
                {
                    throw new InvalidScopeException(this, tool);
                }
            }
 
            // if the set of valid scopes has been limited, check to be
            // sure that this toolbox's scope is in the set
            String[] validScopes = tool.getValidScopes();
            if (validScopes != null && validScopes.length > 0)
            {
                boolean found = false;
                for (String validScope : validScopes)
                {
                    if (getScope().equals(validScope))
                    {
                        found = true;
                        break;
                    }
                }
                if (!found)
                {
                    throw new InvalidScopeException(this, tool);
                }
            }
        }
    }

    @Override
    public int compareTo(Configuration conf)
    {
        if (!(conf instanceof ToolboxConfiguration))
        {
            throw new UnsupportedOperationException("ToolboxConfigurations can only be compared to other ToolboxConfigurations");
        }

        ToolboxConfiguration toolbox = (ToolboxConfiguration)conf;
        return getScope().compareTo(toolbox.getScope());
    }

    @Override
    public int hashCode()
    {
        return scope.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ToolboxConfiguration)
        {
            return scope.equals(((ToolboxConfiguration)obj).scope);
        }
        return false;
    }

    public String toString()
    {
        StringBuilder out = new StringBuilder();
        out.append("Toolbox '");
        out.append(this.scope);
        out.append("' ");
        appendProperties(out);
        appendChildren(out, "tools: \n  ", "\n  ");
        return out.toString();
    }

}