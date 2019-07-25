package org.springframework.springcore.core.io;

import com.sun.istack.internal.Nullable;
import org.springframework.springcore.utils.Assert;
import org.springframework.springcore.utils.ResourceUtils;
import org.springframework.springcore.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class UrlResource extends AbstractFileResolvingResource {
    @org.jetbrains.annotations.Nullable
    private final URI uri;
    private final URL url;
    private final URL cleanedUrl;

    public UrlResource(URI uri) throws MalformedURLException {
        Assert.notNull(uri, "URI must not be null");
        this.uri = uri;
        this.url = uri.toURL();
        this.cleanedUrl = this.getCleanedUrl(this.url, uri.toString());
    }

    public UrlResource(URL url) {
        Assert.notNull(url, "URL must not be null");
        this.url = url;
        this.cleanedUrl = this.getCleanedUrl(this.url, url.toString());
        this.uri = null;
    }

    public UrlResource(String path) throws MalformedURLException {
        Assert.notNull(path, "Path must not be null");
        this.uri = null;
        this.url = new URL(path);
        this.cleanedUrl = this.getCleanedUrl(this.url, path);
    }

    public UrlResource(String protocol, String location) throws MalformedURLException {
        this(protocol, location, (String)null);
    }

    public UrlResource(String protocol, String location, @Nullable String fragment) throws MalformedURLException {
        try {
            this.uri = new URI(protocol, location, fragment);
            this.url = this.uri.toURL();
            this.cleanedUrl = this.getCleanedUrl(this.url, this.uri.toString());
        } catch (URISyntaxException var6) {
            MalformedURLException exToThrow = new MalformedURLException(var6.getMessage());
            exToThrow.initCause(var6);
            throw exToThrow;
        }
    }

    private URL getCleanedUrl(URL originalUrl, String originalPath) {
        try {
            return new URL(StringUtils.cleanPath(originalPath));
        } catch (MalformedURLException var4) {
            return originalUrl;
        }
    }

    public InputStream getInputStream() throws IOException {
        URLConnection con = this.url.openConnection();
        ResourceUtils.useCachesIfNecessary(con);

        try {
            return con.getInputStream();
        } catch (IOException var3) {
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection)con).disconnect();
            }

            throw var3;
        }
    }

    public URL getURL() throws IOException {
        return this.url;
    }

    public URI getURI() throws IOException {
        return this.uri != null ? this.uri : super.getURI();
    }

    public boolean isFile() {
        return this.uri != null ? super.isFile(this.uri) : super.isFile();
    }

    public File getFile() throws IOException {
        return this.uri != null ? super.getFile(this.uri) : super.getFile();
    }

    public Resource createRelative(String relativePath) throws MalformedURLException {
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        return new UrlResource(new URL(this.url, relativePath));
    }

    public String getFilename() {
        return StringUtils.getFilename(this.cleanedUrl.getPath());
    }

    public String getDescription() {
        return "URL [" + this.url + "]";
    }

    public boolean equals(Object obj) {
        return obj == this || obj instanceof UrlResource && this.cleanedUrl.equals(((UrlResource)obj).cleanedUrl);
    }

    public int hashCode() {
        return this.cleanedUrl.hashCode();
    }
}
