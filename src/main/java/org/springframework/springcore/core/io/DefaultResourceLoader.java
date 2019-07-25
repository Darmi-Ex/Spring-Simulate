package org.springframework.springcore.core.io;

import com.sun.istack.internal.Nullable;
import org.springframework.springcore.utils.Assert;
import org.springframework.springcore.utils.ClassUtils;
import org.springframework.springcore.utils.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultResourceLoader implements ResourceLoader{
    @Nullable
    private ClassLoader classLoader;
    private final Set<ProtocolResolver> protocolResolvers = new LinkedHashSet(4);
    private final Map<Class<?>, Map<Resource, ?>> resourceCaches = new ConcurrentHashMap(4);
    public DefaultResourceLoader() {
        this.classLoader = ClassUtils.getDefaultClassLoader();
    }

    public DefaultResourceLoader(@Nullable ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setClassLoader(@Nullable ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Nullable
    public ClassLoader getClassLoader() {
        return this.classLoader != null ? this.classLoader : ClassUtils.getDefaultClassLoader();
    }

    public void addProtocolResolver(ProtocolResolver resolver) {
        Assert.notNull(resolver, "ProtocolResolver must not be null");
        this.protocolResolvers.add(resolver);
    }

    public Collection<ProtocolResolver> getProtocolResolvers() {
        return this.protocolResolvers;
    }

    public <T> Map<Resource, T> getResourceCache(Class<T> valueType) {
        return (Map)this.resourceCaches.computeIfAbsent(valueType, (key) -> {
            return new ConcurrentHashMap();
        });
    }

    public void clearResourceCaches() {
        this.resourceCaches.clear();
    }

    public Resource getResource(String location) {
        Assert.notNull(location, "Location must not be null");
        Iterator var2 = this.protocolResolvers.iterator();

        Resource resource;
        do {
            if (!var2.hasNext()) {
                if (location.startsWith("/")) {
                    return this.getResourceByPath(location);
                }

                if (location.startsWith("classpath:")) {
                    return new ClassPathResource(location.substring("classpath:".length()), this.getClassLoader());
                }

                try {
                    URL url = new URL(location);
                    return new UrlResource(url);
                } catch (MalformedURLException var5) {
                    return this.getResourceByPath(location);
                }
            }

            ProtocolResolver protocolResolver = (ProtocolResolver)var2.next();
            resource = protocolResolver.resolve(location, this);
        } while(resource == null);

        return resource;
    }

    protected Resource getResourceByPath(String path) {
        return new DefaultResourceLoader.ClassPathContextResource(path, this.getClassLoader());
    }

    protected static class ClassPathContextResource extends ClassPathResource implements ContextResource {
        public ClassPathContextResource(String path, @Nullable ClassLoader classLoader) {
            super(path, classLoader);
        }

        public String getPathWithinContext() {
            return this.getPath();
        }

        public Resource createRelative(String relativePath) {
            String pathToUse = StringUtils.applyRelativePath(this.getPath(), relativePath);
            return new DefaultResourceLoader.ClassPathContextResource(pathToUse, this.getClassLoader());
        }
    }
}
