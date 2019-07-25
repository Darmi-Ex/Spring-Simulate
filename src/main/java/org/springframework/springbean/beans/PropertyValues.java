package org.springframework.springbean.beans;

import org.jetbrains.annotations.Nullable;

public interface PropertyValues {
    PropertyValue[] getPropertyValues();

    @Nullable
    PropertyValue getPropertyValue(String var1);

    PropertyValues changesSince(PropertyValues var1);

    boolean contains(String var1);

    boolean isEmpty();
}