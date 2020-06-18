package com.mycompany.imagedownloader.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;

/** References:
 * https://dzone.com/articles/java-comparable-interface-in-five-minutes
 */
public class ImageInfo implements Comparable<ImageInfo>{
    
    public final String path;
    private String filename;
    private String extension;
    public final int width;
    public final int height;
    private long size;

    public ImageInfo(String path, int width, int height, String filename, String extension){
        this.path = path;
        this.filename = filename;
        this.extension = extension;
        this.width = width;
        this.height = height;
    }
    
    public ImageInfo(String path, int width, int height) {
        this(path, width, height, null, null);
    }

    public ImageInfo(String path, String width, String height) {
        this(path, Utils.parseInteger(width), Utils.parseInteger(height));
    }
    
    public ImageInfo(String path, BufferedImage image){
        this(path, image.getWidth(), image.getHeight());
    }
    
    public ImageInfo(String path, byte[] imageBytes) throws IOException{
        this(path, ImageIO.read(new ByteArrayInputStream(imageBytes)));
        this.size = imageBytes.length;
    }

    public ImageInfo(String path) {
        this(path, 0 ,0);
    }

    public String getFilename() {
        if(filename == null) filename = Utils.parseFilename(path);
        return filename;
    }

    public String getExtension() {
        if(extension == null) extension = Utils.parseExtension(path);
        return extension;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
    
    public boolean largerThan(ImageInfo i){
        return compareTo(i)>0;
    }
    
    public boolean smallerThan(ImageInfo i){
        return compareTo(i)<0;
    }

    @Override
    public int hashCode() {
        return 61 + Objects.hashCode(this.path);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        return Objects.equals(this.path, ((ImageInfo) obj).path);
    }

    @Override
    public int compareTo(ImageInfo i) { //can't subtract one from the other because of images of diferent proportions
        if(width < i.width || height < i.height) return -1;
        return (width == i.width && height == i.height)? 0: 1;
    }

}
