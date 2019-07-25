package org.springframework.springcore.core;

import org.jetbrains.annotations.Nullable;
import org.springframework.springcore.utils.Assert;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AttributeAccessorSupport implements AttributeAccessor, Serializable {
    private final Map<String, Object> attributes = new LinkedHashMap(0);

    public AttributeAccessorSupport() {
    }

    public void setAttribute(String name, @Nullable Object value) {
        Assert.notNull(name, "Name must not be null");
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            this.removeAttribute(name);
        }

    }

    @Nullable
    public Object getAttribute(String name) {
        Assert.notNull(name, "Name must not be null");
        return this.attributes.get(name);
    }

    @Nullable
    public Object removeAttribute(String name) {
        Assert.notNull(name, "Name must not be null");
        return this.attributes.remove(name);
    }

    public boolean hasAttribute(String name) {
        Assert.notNull(name, "Name must not be null");
        return this.attributes.containsKey(name);
    }

    public String[] attributeNames() {
        return (String[])this.attributes.keySet().toArray(new String[this.attributes.size()]);
    }

    protected void copyAttributesFrom(AttributeAccessor source) {
        Assert.notNull(source, "Source must not be null");
        String[] attributeNames = source.attributeNames();
        String[] var3 = attributeNames;
        int var4 = attributeNames.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String attributeName = var3[var5];
            this.setAttribute(attributeName, source.getAttribute(attributeName));
        }

    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof AttributeAccessorSupport)) {
            return false;
        } else {
            AttributeAccessorSupport that = (AttributeAccessorSupport)other;
            return this.attributes.equals(that.attributes);
        }
    }

    public int hashCode() {
        return this.attributes.hashCode();
    }
}
