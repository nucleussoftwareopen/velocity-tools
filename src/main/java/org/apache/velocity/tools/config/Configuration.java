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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 *
 * @author Nathan Bubna
 * @version $Id: Configuration.java 511959 2007-02-26 19:24:39Z nbubna $
 */
public class Configuration<T>
    implements Comparable<Configuration<T>>
{
    private final SortedSet<Property> properties = new TreeSet<Property>();

    public void addProperty(Property property)
    {
        if (property.getName() == null)
        {
            throw new IllegalArgumentException("All properties must be named before they can be added to the configuration.");
        }

        // remove any currently properties with the same key
        properties.remove(property);
        // then add the new one
        properties.add(property);
    }

    public boolean removeProperty(Property property)
    {
        return properties.remove(property);
    }

    public void setProperty(String name, Object value)
    {
        if (name == null)
        {
            throw new NullPointerException("Property name cannot be null");
        }

        Property prop = new Property();
        prop.setName(name);
        prop.setValue(value);
        addProperty(prop);
    }

    public boolean removeProperty(String name)
    {
        Property prop = getProperty(name);
        return properties.remove(prop);
    }

    public boolean hasProperties()
    {
        return !properties.isEmpty();
    }

    public Property getProperty(String name)
    {
        for (Property prop : properties)
        {
            if (name.equals(prop.getName()))
            {
                return prop;
            }
        }
        return null;
    }

    public SortedSet<Property> getProperties()
    {
        return new TreeSet<Property>(properties);
    }

    public Map<String,Object> getPropertyMap()
    {
        Map<String,Object> map = new HashMap<String,Object>();
        for (Property prop : properties)
        {
            map.put(prop.getName(), prop.getConvertedValue());
        }
        return map;
    }

    public void setProperties(Collection<Property> props)
    {
        for (Property newProp : props)
        {
            addProperty(newProp);
        }
    }

    public void addConfiguration(Configuration<T> config)
    {
        setProperties(config.getProperties());
    }

    public void validate()
    {
        for (Property property : properties)
        {
            property.validate();
        }
    }

    public int compareTo(Configuration<T> config)
    {
        throw new UnsupportedOperationException("Configuration is abstract and cannot be compared.");
    }

    @Override
    public int hashCode()
    {
        return properties.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (!(obj instanceof Configuration))
        {
            return false;
        }
        else
        {
            // they're of the same type
            Configuration<T> that = (Configuration<T>)obj;
            // if their properties are equal, they're equal
            return this.properties.equals(that.properties);
        }
    }

    protected void appendProperties(StringBuilder out)
    {
        if (hasProperties())
        {
            out.append("with ");
            out.append(properties.size());
            out.append(" properties [");
            for (Property prop : properties)
            {
                out.append(prop.getKey());
                out.append(" -");
                out.append(prop.getType());
                out.append("-> ");
                out.append(prop.getValue());
                out.append("; ");
            }
            out.append("]");
        }
    }

}