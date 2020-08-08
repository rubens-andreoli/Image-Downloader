/*
 * Copyright (C) 2020 Rubens A. Andreoli Jr.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package rubensandreoli.commons.others;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import rubensandreoli.commons.utils.FileUtils;
import rubensandreoli.commons.utils.StringUtils;

public class CachedFile extends File{ //TODO: implement all methods
    private static final long serialVersionUID = 1L;
 
    public static final int SIGNATURE_BYTES = 4;
    
    private Long size;
    private String parent, filename, name, extension; //TODO: map with time read?
    private byte[] signature;
    private byte[] content;
    
    public CachedFile(String pathname){
        super(pathname);
    }
    
    public CachedFile(File file){
        this(file.getPath());
    }
    
    public CachedFile(String root, String...nodes){
        super(FileUtils.buildPathname(root, nodes));
    }
    
    public CachedFile(File root, String...nodes) {
        super(FileUtils.buildPathname(root, nodes));
    }

    public CachedFile(URI uri) {
        super(uri);
    }

    @Override
    public boolean delete(){
        boolean removed = false;
        try{
            removed = super.delete();
        }catch(SecurityException ex){}
        return removed;
    }

    public boolean matchSignature(byte...bytes){
        if(signature == null) return bytes == null;
        if(bytes.length == 0 && signature.length == 0) return true;
        final int lenght = Math.min(bytes.length, signature.length);
        return Arrays.compare(bytes, 0, lenght, signature, 0, lenght) == 0;
    }
    
    public File toFile(){
        return new File(this.getPath());
    }
  
    // <editor-fold defaultstate="collapsed" desc=" GETTERS "> 
    @Override
    public long length() {
        if(size == null) size = FileUtils.getFileSize(this);
        return size;
    }
    
    public long chachedLength(){
        long l = Long.BYTES;
        if(signature != null) l += SIGNATURE_BYTES;
        l += StringUtils.getStringSize(parent);
        l += StringUtils.getStringSize(filename);
        l += StringUtils.getStringSize(name);
        l += StringUtils.getStringSize(extension);
        return content!=null? length()+l : l;
    }
    
    @Override
    public String getParent() {
        if(parent == null) parent = FileUtils.getParent(super.getPath());
        return parent;
    }
    
    @Override
    public String getName() {
        if(name == null) name = FileUtils.getName(super.getPath());
        return name;
    }
    
    public String getExtension() {
        if(extension == null) name = FileUtils.getExtension(super.getPath());
        return extension;
    }
    
    public String getFilename() {
        if(filename == null) filename = FileUtils.getFilename(super.getPath());
        return filename;
    }
 
    public byte[] getRawSignature() {
        if(signature == null) signature = FileUtils.readFirstBytes(this, SIGNATURE_BYTES);
        return signature;
    }
    
    public String getSignature() {
        byte[] rawSignature = getRawSignature();
        return new String(rawSignature);
    }
    
    public byte[] getContent() {
        if(content == null){
            content = FileUtils.readAllBytes(this);
            size = (long) content.length;
        }
        return content;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" SETTERS "> 
    public void setSize(long size) {
        this.size = size;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }
    
    public void setContent(byte[] content) {
        this.content = content;
    }
    // </editor-fold>

}
