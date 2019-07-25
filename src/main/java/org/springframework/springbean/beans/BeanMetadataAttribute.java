package org.springframework.springbean.beans;

import org.jetbrains.annotations.Nullable;
import org.springframework.springcore.utils.Assert;
import org.springframework.springcore.utils.ObjectUtils;

public class BeanMetadataAttribute implements BeanMetadataElement {
    private final String name;
    @Nullable
    private final Object value;
    @Nullable
    private Object source;

    public BeanMetadataAttribute(String name, @Nullable Object value) {
        Assert.notNull(name, "Name must not be null");
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public Object getValue() {
        return this.value;
    }

    public void setSource(@Nullable Object source) {
        this.source = source;
    }

    @Nullable
    public Object getSource() {
        return this.source;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof BeanMetadataAttribute)) {
            return false;
        } else {
            BeanMetadataAttribute otherMa = (BeanMetadataAttribute)other;
            return this.name.equals(otherMa.name) && ObjectUtils.nullSafeEquals(this.value, otherMa.value) && ObjectUtils.nullSafeEquals(this.source, otherMa.source);
        }
    }

    public int hashCode() {
        return this.name.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.value);
    }

    public String toString() {
        return "metadata attribute '" + this.name + "'";
    }
}
