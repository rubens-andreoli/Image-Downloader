/*
 * Copyright (C) 2020 Rubens A. Andreoli Jr.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package rubensandreoli.imagedownloader.tasks.support;

import java.util.Objects;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.commons.utils.IntegerUtils;

public class ImageInfo implements Comparable<ImageInfo>{

    public final String path;
    public final int width;
    public final int height;

    private String parent;
    private String name;
    private String filename;
    private String extension;
    private long size = 0;

    public ImageInfo(String path, int width, int height) {
        this.path = path;
        this.width = width;
        this.height = height;
    }

    public ImageInfo(String path, String width, String height) {
        this(path, IntegerUtils.parseInteger(width), IntegerUtils.parseInteger(height));
    }

    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    public String getParent(){
        if(parent == null) parent = FileUtils.getParent(path);
        return parent;
    }

    public String getName(){
        if(name == null) name = FileUtils.getName(path);
        return name;
    }

    public String getFilename() {
        if(filename == null) filename = FileUtils.getFilename(path);
        return filename;
    }

    public String getExtension() {
        if(extension == null) extension = FileUtils.getExtension(path, ".jpg");
        return extension;
    }

    public long getSize() {
        return size;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setSize(long size) {
        this.size = size;
    }
    // </editor-fold>

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
    public int compareTo(ImageInfo i) { //image is larger only if in both dimensions
        if(width > i.width && height > i.height) return 1;
        return (width == i.width && height == i.height)? 0: -1;
    }

}
