package com.mycompany.imagedownloader.model;

import java.util.Objects;

public class GoogleImage{
    public final String url;
    private String filename;
    private String extension;
    public final int width;
    public final int height;

    public GoogleImage(String url, int width, int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }
    
    public GoogleImage(String url, String width, String height) {
        this(url, Utils.parseInteger(width), Utils.parseInteger(height));
    }

    public GoogleImage(String url) {
        this(url, 0 ,0);
    }

    public String getFilename() {
        if(filename == null) filename = Utils.getFilename(url);
        return filename;
    }

    public String getExtension() {
        if(extension == null) extension = Utils.getExtension(url);
        return extension;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.url);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GoogleImage other = (GoogleImage) obj;
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        return true;
    }

}
