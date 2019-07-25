package org.springframework.springbean.beans;

import org.jetbrains.annotations.Nullable;

public interface Mergeable {
    boolean isMergeEnabled();

    Object merge(@Nullable Object var1);
}
