package com.mycompany.imagedownloader.model;

public class GoogleImage{
    public final String url;
    private String filename;
    private String extension;
    public final int width;
    public final int height;

    public GoogleImage(String url, String width, String height) {
        this.width = Utils.parseInteger(width);
        this.height = Utils.parseInteger(height);
        this.url = url;
    }

    public String getFilename() {
        if(filename == null) filename = Utils.getFilename(url);
        return filename;
    }

    public String getExtension() {
        if(extension == null) extension = Utils.getExtension(url);
        return extension;
    }

}
