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
package rubensandreoli.commons.utils;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import rubensandreoli.commons.others.CachedFile;

/** 
 * References:
 https://stackoverflow.com/questions/265769/maximum-name-length-in-ntfs-windows-xp-and-windows-vista#:~:text=14%20Answers&text=Individual%20components%20of%20a%20filename,files%2C%20248%20for%20folders).
 * https://stackoverflow.com/questions/57807466/what-is-the-maximum-filename-length-in-windows-10-java-would-try-catch-would
 * https://docs.oracle.com/javase/6/docs/technotes/tools/solaris/javadoc.html#@inheritDoc
 * https://examples.javacodegeeks.com/desktop-java/imageio/determine-format-of-an-image/
 * https://www.sparkhound.com/blog/detect-image-file-types-through-byte-arrays
 * https://stackoverflow.com/questions/27476845/what-is-the-difference-between-a-null-array-and-an-empty-array
 */
public class FileUtils {
    
    public static final String IMAGES_REGEX = ".*\\.jpg|jpeg|bmp|png|gif";
    public static final String IMAGES_GLOB = "*.{jpg,jpeg,bmp,png,gif}";
    
    public static final String EXTENSION_REGEX = "^.[a-z]{3,}$";
    public static final String SEPARATOR = "/";
    public static final String FILENAME_INVALID_CHARS_REGEX = "[\\/\\\\:\\*?\\\"<\\>|]";
    public static final int MASKED_FILENAME_MIN_LENGTH = 5;
    
    public static final String FILENAME_MASK = "%s"+SEPARATOR+"%s%s";
    public static final String SUBFOLDER_MASK = "%s"+SEPARATOR+"%s";
    public static final String DUPLICATED_FILENAME_MASK = "%s"+SEPARATOR+"%s (%d)%s";
    public static final int FILEPATH_MAX_LENGTH = 255;
    
    public static final int DEFAULT_CONNECTION_TIMEOUT = 2000; //ms
    public static final int DEFAULT_READ_TIMEOUT = 4000; //ms
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 4; //bytes

    private FileUtils(){}
    
    // <editor-fold defaultstate="collapsed" desc=" PARSE PATHNAME "> 
    /**
     * Replaces all separators with {@literal '/'} and returns a {@Code String} 
     * containing the file or directory denoted by this abstract pathname.
     * This is just the last name in the pathname's name sequence.
     * 
     * @param pathname abstract pathname from which the file/directory will be parsed
     * @return last item of the pathname or an empty {@code String} if 
          this pathname's name sequence is empty
     */
    public static String getName(String pathname){
        final String name = getNode(pathname, 1);
        return name.isEmpty()? pathname : name;
    }
    
    public static String getParentName(String pathname){
        return getNode(pathname, 2);
    }
    
    public static String getParent(String pathname){
        pathname = pathname.replaceAll("[/\\\\]", SEPARATOR).trim();
        final int sepIndex = pathname.lastIndexOf(SEPARATOR);
        if(sepIndex > 0) return pathname.substring(0, sepIndex);
        return pathname;
    }
    
//    public static String getRoot(String pathname){
//        pathname = pathname.replaceAll("[/\\\\]", Matcher.quoteReplacement(SEPARATOR));
//        final String[] tokens = pathname.split(Matcher.quoteReplacement(SEPARATOR));
//        return tokens[0];
//    }

    public static String getNode(String pathname, int level) { //FIX: '//...' -> 'x/x/...' 'x' are not nodes
        pathname = pathname.replaceAll("[/\\\\]", SEPARATOR);
        final String[] tokens = pathname.split(SEPARATOR);
        if(tokens.length >= level) return tokens[tokens.length-level];
        return "";
    }
    
    /**
     * Returns the name of the file or directory denoted by this abstract pathname,
 without any characters considered invalid by Windows OS.
     * 
     * @see Utils#parseFilename(String, boolean) 
     * @param pathname abstract pathname from which the name of the file/directory will be parsed
     * @return last item of the pathname without extension and without invalid
     *          characters; or an empty {@code String} if this pathname's name sequence is empty
     */
    public static String getFilename(String pathname){
        return FileUtils.getFilename(pathname, true);
    }
    
    /**
     * Returns the name of the file until the first {@literal '.'} (dot) 
     * (without extension) or directory denoted by this abstract pathname. 
     * This is just the last name in the pathname's  name sequence. 
     * It can also remove any characters considered invalid  by Windows OS.
     * 
     * @see Utils#parseFile(String) 
     * @see Utils#FILENAME_INVALID_CHARS_REGEX
     * @param pathname abstract pathname from which the name of the file will be parsed
     * @param normalize {@code true} to remove invalid characters; {@code false} otherwise
     * @return last item of the pathname without extension {@code .ext} or an empty 
     *          {@code String} if this pathname's name sequence is empty
     */
    public static String getFilename(String pathname, boolean normalize){
        String name = getName(pathname);
        final int extIndex = name.lastIndexOf(".");
        if(extIndex != -1) name = name.substring(0, extIndex);
        if(normalize) name = name.replaceAll(FILENAME_INVALID_CHARS_REGEX, "");
        return name;
    }
 
    /**
     * Extracts the file from the abstract pathname and then returns a 
     * {@code String} containing anything after the first {@literal '.'} (dot) 
     * removing anything from the end of the {@Code String} until it matches
     * a common extension regex.
     * 
     * @see Utils#parseFile(String) 
     * @see Utils#DEFAULT_EXTENSION
     * @see Utils#EXTENSION_REGEX
     * @param pathname abstract pathname from which the extension of the file will be parsed
     * @param defaultValue default extension in case none is found
     * @return {@code String} containing the extension of the file without invalid characters; 
     *          or a given default value if this pathname doesn't contain one
     */
    public static String getExtension(String pathname, String defaultValue){
        final String name = getName(pathname);
        String ext = defaultValue;
        final int extIndex = name.lastIndexOf(".");
        if(extIndex != -1){
            String tmpExt = name.substring(extIndex);
            boolean empty = false;
            while(!tmpExt.matches(EXTENSION_REGEX)){
                if(tmpExt.isEmpty()){
                    empty = true;
                    break;
                }
                tmpExt = tmpExt.substring(0, tmpExt.length()-1);
            }
            if(!empty) ext = tmpExt;
        }
        return ext;
    }
    
    public static String getExtension(String pathname){
        return getExtension(pathname, "");
    }
    
    public static final String buildPathname(File root, String...nodes){
        return buildPathname(root.getPath(), nodes);
    }
    
    public static final String buildPathname(String root, String...nodes){
        root = root.replaceAll("[/\\\\]", SEPARATOR);
        if(nodes.length == 0) return root;
        StringBuilder sb = new StringBuilder(root);
        for (String node : nodes) {
            sb.append(SEPARATOR);
            sb.append(node);
        }
        return sb.toString();
    }
    
    public static String normalize(String pathname){
        return pathname.replaceAll("[/\\\\]", SEPARATOR);
    }
    
    public static String maskPathname(String pathname, int maxLenght){
        if(pathname.isEmpty()) return "";
        pathname = normalize(pathname);
        if((maxLenght < MASKED_FILENAME_MIN_LENGTH) || (pathname.length() <= maxLenght)) return pathname;

        String root = Path.of(pathname).getRoot().toString(); //TODO: implement FileUtils.getRoot(String pathname);
        root = normalize(root);
        
        String formated = pathname.substring(pathname.length()-maxLenght, pathname.length());
        formated = formated.replaceFirst("([^\\"+SEPARATOR
                +"]{3}(?=\\"+SEPARATOR
                +"))|(.{3})(?=[^\\"+SEPARATOR
                +"]*$)", "..."); //or only (.{3,}?(?=\/))|(.{3})
        
        if(root != null){
            formated = formated.replaceFirst(".{"+root.length()
                    +",}(\\.{3})|(^.{"+(root.length()+3)
                    +",}?(?=\\"+SEPARATOR+"))", root+"..."); //if deparator is "\" Matcher.quoteReplacement(root)
            int index = formated.indexOf(".");
            if(index < root.length()) formated = formated.substring(index);
        }

        return formated;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" CREATE VALID FILE "> 
    /**
     * Returns a valid {@code File} conforming the filename to Windows OS
     * standards. It removes all invalid characters; 
     * reduces the maximum filename length to fit the given pathname;
     * and resolve duplicated files.
     * 
     * @param folder directory {@code File} where the file will be saved, 
     *              must not be {@code null}
     * @param filename name of the file without extension, must not be {@code null}
     * @param extension extension of the file with {@literal '.'} (Dot) at the start, 
     *                  must not be {@code null}
     * @return valid {@code File} ready to be saved
     */
    public static File createValidFile(File folder, String filename, String extension){
        //FIX INVALID CHARACTERS
        filename = filename.replaceAll("[\\\\\\/:*?\"<>|]", "");
        extension = extension.replaceAll("[^a-z-A-Z\\.]", "");
        File file = new File(folder, String.format("%s%s", filename, extension));
        
        //FIX FILEPATH LENGTH
        if(file.getAbsolutePath().length() > FILEPATH_MAX_LENGTH){
            int toRemove = file.getAbsolutePath().length() - FILEPATH_MAX_LENGTH;
            if(filename.length() > toRemove){
                filename = filename.substring(0, filename.length()-toRemove); //TODO: check if removing 1 more than needed?
                file = new File(String.format(FILENAME_MASK, folder, filename, extension));
            }else{
                return null;
            }
        }

        //FIX DUPLICATED NAME
        for(int n=1; file.exists(); n++){
            file = new File(String.format(DUPLICATED_FILENAME_MASK, folder, filename, n, extension));
        }
        return file;
    }

    /**
     * Returns a valid {@code File} conforming the filename by Windows OS
     * standards. It removes all invalid characters; 
     * reduces the maximum filename length to fit the given pathname;
     * and resolve duplicated files.
     * 
     * @see Utils#createValidFile(File, String, String)
     * @param folder directory {@code File} where the file will be saved, 
     *              must not be {@code null}
     * @param file name with extension, must not be {@code null}
     * @return valid {@code File} ready to be saved
     */
    public static File createValidFile(File folder, String file){
        return createValidFile(folder, FileUtils.getFilename(file), getExtension(file));
    }

    /**
     * Returns a valid {@code File} conforming the filename by Windows OS
     * standards. It removes all invalid characters; 
     * reduces the maximum filename length to fit the given pathname;
     * and resolve duplicated files.
     * 
     * @see Utils#createValidFile(File, String, String)
     * @param folder abstract directory pathname where the file will be saved, 
     *              must not be {@code null}
     * @param filename name of the file without extension, must not be {@code null}
     * @param extension extension of the file with {@literal '.'} (Dot) at the start, 
     *                  must not be {@code null}
     * @return valid {@code File} ready to be saved
     */
    public static File createValidFile(String folder, String filename, String extension){
        folder = folder.replaceAll("[*?\"<>|]", "");
        return createValidFile(new File(folder), filename, extension);
    }
    
    /**
     * Returns a valid {@code File} conforming the filename by Windows OS
     * standards. It removes all invalid characters; 
     * reduces the maximum filename length to fit the given pathname;
     * and resolve duplicated files.
     * 
     * @see Utils#createValidFile(String, String, String)
     * @param folder abstract directory pathname where the file will be saved, 
     *              must not be {@code null}
     * @param file name with extension, must not be {@code null}
     * @return valid {@code File} ready to be saved
     */
    public static File createValidFile(String folder, String file){
        return createValidFile(folder, FileUtils.getFilename(file), getExtension(file));
    }
    // </editor-fold>
 
    // <editor-fold defaultstate="collapsed" desc=" MODIFY "> 
    /**
     * Creates child directory if doesn't exists and try to move the source file to it.
     * If there is already a file with the same pathname, a new filename will be created.
     * This method doesn't throw {@code SecurityException} of the {@code java.io.File} 
     * methods called.
     * 
     * @see Utils#createValidFile(String, String, String)
     * @param source file to be moved
     * @param subfolder name of the sub-folder
     * @return {@code true} if and only if the file was moved; 
     *         {@code false} otherwise
     */
    public static boolean moveFileToChild(File source, String subfolder){
        if(!source.isFile()) throw new IllegalArgumentException();
       
        boolean moved = false;
        File folder = new File(source.getParent(), subfolder);
        File dest = new File(folder, source.getName());
        try{
            folder.mkdir();
            moved = source.renameTo(dest);
            if(!moved){ //costly method only if failed above
                dest = createValidFile(folder.getPath()+"/", 
                    getFilename(source.getName()), 
                    getExtension(source.getName())
                );
                moved = source.renameTo(dest);
            }
        }catch(Exception ex){
            System.err.println(ex.getMessage());
        }
        if(!moved) System.err.println("ERROR: Failed moving "+source.getAbsolutePath()+" to "+dest.getAbsolutePath());
        return moved;
    }
    
    /**
     * Deletes the file or directory. If it is a directory, it must be empty.
     * Convenience method for {@code java.io.File#delete()} that doesn't
     * throw {@code SecurityException}.
     * 
     * @param file file or directory to be deleted
     * @return {@code true} if and only if the file or directory is successfully deleted; 
     *         {@code false} otherwise
     */
    public static boolean deleteFile(File file){
        boolean removed = false;
        try{
            removed = file.delete();
        }catch(SecurityException ex){}
        if(!removed) System.err.println("ERROR: Failed removing "+file.getAbsolutePath());
        return removed;
    }
    
    public static File createSubfolder(String parent, String child) throws IOException{
        final String folder = String.format(SUBFOLDER_MASK, normalize(parent), child);
        final File subfolder = new File(folder);
        if(!subfolder.isDirectory()){
            if(!subfolder.mkdir()){
                throw new IOException("ERROR: failed creating folder "+folder);
            }
        }
        return subfolder;
    }
    // </editor-fold>
      
    // <editor-fold defaultstate="collapsed" desc=" READ "> 
    public static long getFileSize(File file){
        try{
            return file.length();
        }catch(SecurityException ex){
            return 0L;
        }
    }
    
    public static ImageIcon loadIcon(String url){
        try{
            return new ImageIcon(FileUtils.class.getClassLoader().getResource(url));
        }catch(NullPointerException ex){
            return new ImageIcon();
        }
    }
    
    public static ImageIcon loadIcon(String url, int size){
        if(size < 1) throw new IllegalArgumentException();
        try{
            final Image i = new ImageIcon(FileUtils.class.getClassLoader().getResource(url)).getImage().getScaledInstance(size, size, Image.SCALE_DEFAULT);
            return new ImageIcon(i);
        }catch(NullPointerException ex){
            return null;
        }
    }
    
    public static byte[] readAllBytes(File file){
        try(var bi = new BufferedInputStream(new FileInputStream(file))){
            return bi.readAllBytes();
        }catch(IOException ex){
            return null;
        }
    }

    public static byte[] readFirstBytes(File file, int amount){
        try(var bi = new BufferedInputStream(new FileInputStream(file), amount)){
            return bi.readNBytes(amount);
        } catch (IOException ex) {
            return null;
        }
    }

    public static Byte readFirstByte(File file){
        try(var r = new FileReader(file)){
            return (byte) r.read(); //does NOT support extend chars (2 bytes)
        } catch (Exception ex) {
            return null;
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" SCAN FILESYSTEM "> 
    public static List<File> scanFiles(File root){
	if(!root.isDirectory()) return null;
	Stack<File> folders = new Stack<>();
	folders.add(root);
	List<File> files = new ArrayList<>();
	
	while(!folders.empty()){
            //2700-3000ms
//	    File tempFolder = folders.pop();
//	    File[] tempFolders = tempFolder.listFiles(f -> f.isDirectory());
//	    File[] tempFiles = tempFolder.listFiles(f -> f.isFile());
//	    if(tempFolders != null)folders.addAll(Arrays.asList(tempFolders));
//	    if(tempFiles != null) files.addAll(Arrays.asList(tempFiles));
	        
	    //1600-1800ms
            File[] tempFiles = folders.pop().listFiles();
	    if(tempFiles == null) continue;
	    for(File tempFile : tempFiles){
		if(tempFile.isDirectory()) folders.push(tempFile);
		else files.add(tempFile);
	    }
	}
	return files;
    }
    
    public static void scanFiles(List<File> files, File root) {
	File[] tempFiles = root.listFiles();
	if (tempFiles == null) return;
	for (File tempFile : tempFiles) {
	    if (tempFile.isFile()) files.add(tempFile);
	    else scanFiles(files, tempFile);
	}
    }
   
    public static List<File> scanFolders(File root){
	if(!root.isDirectory()) return null;
	List<File> folders = new ArrayList<>();
	folders.add(root);
	for(int i=0; i<folders.size(); i++){
	    File[] tempFolders = folders.get(i).listFiles(f -> f.isDirectory());
	    if(tempFolders != null) folders.addAll(Arrays.asList(tempFolders));
	}
	return folders;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" DOWNLOAD ">
    @Deprecated
    public static long downloadToFile(String url, File file) throws IOException{
        return downloadToFile(url, file, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_BUFFER_SIZE);
    }
    
    @Deprecated
    public static long downloadToFile(String url, File file, int connectionTimeout, int readTimeout) throws IOException{
        return downloadToFile(url, file, connectionTimeout, readTimeout, DEFAULT_BUFFER_SIZE);
    }
    
    @Deprecated
    public static long downloadToFile(String url, File file, int connectionTimeout, int readTimeout, int bufferSize) throws IOException{
        long bytesWritten = 0;
        try (InputStream in = openInputStream(new URL(url), connectionTimeout, readTimeout);
                OutputStream out = openOutputStream(file)) {
            int bytesRead;
            final byte[] buffer = new byte[bufferSize];
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                bytesWritten += bytesRead;
            }
            return bytesWritten;
        }
    }
    
    public static void downloadToFile(String url, CachedFile file) throws IOException{
        downloadToFile(url, file, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_BUFFER_SIZE);
    }
    
    public static void downloadToFile(String url, CachedFile file, int connectionTimeout, int readTimeout) throws IOException{
        downloadToFile(url, file, connectionTimeout, readTimeout, DEFAULT_BUFFER_SIZE);
    }
    
    public static void downloadToFile(String url, CachedFile file, int connectionTimeout, int readTimeout, int bufferSize) throws IOException{
        long bytesWritten = 0;
        try (InputStream in = openInputStream(new URL(url), connectionTimeout, readTimeout);
                OutputStream out = openOutputStream(file)) {
            int bytesRead;
            final byte[] buffer = new byte[bufferSize];
            byte[] signature = null;
            while ((bytesRead = in.read(buffer)) != -1) {
                if(signature == null) signature = Arrays.copyOf(buffer, CachedFile.SIGNATURE_BYTES);
                out.write(buffer, 0, bytesRead);
                bytesWritten += bytesRead;
            }
            file.setSize(bytesWritten);
            file.setSignature(signature);
        }
    }
    
    private static InputStream openInputStream(final URL path, int connectionTimeout, int readTimeout) throws IOException{
        try {
            var conn = path.openConnection();
            conn.setConnectTimeout(connectionTimeout);
            conn.setReadTimeout(readTimeout);
            return conn.getInputStream();
        } catch (IOException ex) {
             throw new IOException("URL '"+path+"' cannot be reached");
        }
    }
    
    private static FileOutputStream openOutputStream(final File file) throws IOException {
        if (file.isDirectory()) {
            throw new IOException("File '"+file+"' is a directory");
        }else if (file.isFile() && !file.canWrite()) {
            throw new IOException("File '"+file+"' cannot be overridden");
        } else {
            File parent = file.getParentFile();
            if (parent != null && !parent.mkdirs() && !parent.isDirectory()) { //if not given, not created, and not validated
                throw new IOException("Directory '"+parent+"' could not be created");
            }
        }
        return new FileOutputStream(file, false);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" VALIDATION "> 
    public static boolean isImage(File file){
        try {
            return ImageIO.read(file) != null;
        } catch (IOException e) {
            return false;
        }
    }
    // </editor-fold>
    
}
