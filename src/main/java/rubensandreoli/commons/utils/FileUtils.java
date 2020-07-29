package rubensandreoli.commons.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import javax.swing.ImageIcon;

/** References:
 * https://stackoverflow.com/questions/265769/maximum-filename-length-in-ntfs-windows-xp-and-windows-vista#:~:text=14%20Answers&text=Individual%20components%20of%20a%20filename,files%2C%20248%20for%20folders).
 * https://stackoverflow.com/questions/57807466/what-is-the-maximum-filename-length-in-windows-10-java-would-try-catch-would
 * https://docs.oracle.com/javase/6/docs/technotes/tools/solaris/javadoc.html#@inheritDoc
 */
public class FileUtils {
    
    public static final int DEFAULT_CONNECTION_TIMEOUT = 2000; //ms
    public static final int DEFAULT_READ_TIMEOUT = 4000; //ms
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 4; //bytes
    public static final String EXTENSION_REGEX = "^.[a-z]{3,}$";
    public static final String FILENAME_INVALID_CHARS_REGEX = "[\\/\\\\:\\*?\\\"<\\>|]";
    public static final String DEFAULT_EXTENSION = ".jpg";
    public static final String FILENAME_MASK = "%s/%s%s";
    public static final String DUPLICATED_FILENAME_MASK = "%s/%s (%d)%s";
    public static final int FILEPATH_MAX_LENGTH = 255;
 
    private FileUtils(){}
    
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
                filename = filename.substring(0, filename.length()-toRemove); //TODO: removing 1 more than needed?
                file = new File(String.format(FILENAME_MASK, folder, filename, extension));
            }else{
                return null;//TODO: throw exception?
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
     * @param file filename with extension, must not be {@code null}
     * @return valid {@code File} ready to be saved
     */
    public static File createValidFile(File folder, String file){
        return createValidFile(folder, parseFilename(file), parseExtension(file));
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
     * @param file filename with extension, must not be {@code null}
     * @return valid {@code File} ready to be saved
     */
    public static File createValidFile(String folder, String file){
        return createValidFile(folder, parseFilename(file), parseExtension(file));
    }

    public static long getFileSize(File file){
        long size = 0L;
        try{
            size = file.length();
        }catch(SecurityException ex){}
        return size;
    }

    /**
     * Replaces all separators with {@literal '/'} and returns a {@Code String} 
     * containing the file or directory denoted by this abstract pathname.
     * This is just the last name in the pathname's name sequence.
     * 
     * @param path abstract pathname from which the file/directory will be parsed
     * @return last item of the pathname or an empty {@code String} if 
     *          this pathname's name sequence is empty
     */
    public static String parseFile(String path){
        if(path == null) return "";
        String file = path.replaceAll("[/\\\\]", "/");
        int index = file.lastIndexOf('/');
        if(index != -1){
            if(index+1 >= path.length()){
                file = file.substring(0, index);
            }else{
                file = file.substring(index+1);
            }
        }
        return file;
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
     * @param path abstract pathname from which the extension of the file will be parsed
     * @return {@code String} containing the extension of the file without invalid characters; 
     *          or a default value if this pathname doesn't contain one
     */
    public static String parseExtension(String path){
        String file = parseFile(path);
        String ext = DEFAULT_EXTENSION;
        
        int extIndex = file.lastIndexOf("."); //indexOf()?
        if(extIndex > 0){ //?.xxx
            String tempExt = file.substring(extIndex);
            while(!tempExt.isEmpty() && !tempExt.matches(EXTENSION_REGEX)){
                tempExt = tempExt.substring(0, tempExt.length()-1);
            }
            if(!tempExt.isEmpty()){
                ext = tempExt;
            }
        }
        return ext;
    }
    
    /**
     * Returns the name of the file or directory denoted by this abstract pathname,
     * without any characters considered invalid by Windows OS.
     * 
     * @see Utils#parseFilename(String, boolean) 
     * @param path abstract pathname from which the name of the file/directory will be parsed
     * @return last item of the pathname without extension and without invalid
     *          characters; or an empty {@code String} if this pathname's name sequence is empty
     */
    public static String parseFilename(String path){
        return parseFilename(path, true);
    }
    
    /**
     * Returns the name of the file until the first {@literal '.'} (dot) 
     * (without extension) or directory denoted by this abstract pathname. 
     * This is just the last name in the pathname's  name sequence. 
     * It can also remove any characters considered invalid  by Windows OS.
     * 
     * @see Utils#parseFile(String) 
     * @see Utils#FILENAME_INVALID_CHARS_REGEX
     * @param path abstract pathname from which the name of the file will be parsed
     * @param removeInvalid {@code true} to remove invalid characters; {@code false} otherwise
     * @return last item of the pathname without extension {@code .ext} or an empty 
     *          {@code String} if this pathname's name sequence is empty
     */
    public static String parseFilename(String path, boolean removeInvalid){ //TODO: fix if too big?
        String filename = parseFile(path);
        int extIndex = filename.lastIndexOf("."); //indexOf()?
        if(extIndex != -1){
            filename = filename.substring(0, extIndex);
        }
        if(removeInvalid){
            filename = filename.replaceAll(FILENAME_INVALID_CHARS_REGEX, "");
        }
        return filename;
    }
    
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
        if(!source.isFile()) return false;
       
        boolean moved = false;
        File folder = new File(source.getParent(), subfolder);
        File dest = new File(folder, source.getName());
        try{
            folder.mkdir();
            moved = source.renameTo(dest);
            if(!moved){ //costly method only if failed above
                dest = createValidFile(
                    folder.getPath()+"/", 
                    parseFilename(source.getName()), 
                    parseExtension(source.getName())
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

    public static ImageIcon loadIcon(String url){
        try{
            ImageIcon icon = new ImageIcon(FileUtils.class.getClassLoader().getResource(url));
            return icon;
        }catch(NullPointerException ex){
            return null;
        }
    }
    
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

    public static long downloadToFile(String path, File file) throws IOException{
        return downloadToFile(path, file, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT, DEFAULT_BUFFER_SIZE);
    }
    
    public static long downloadToFile(String path, File file, int connectionTimeout, int readTimeout) throws IOException{
        return downloadToFile(path, file, connectionTimeout, readTimeout, DEFAULT_BUFFER_SIZE);
    }
      
    public static long downloadToFile(String path, File file, int connectionTimeout, int readTimeout, int bufferSize) throws IOException{
        long bytesWritten = 0;
        try (InputStream in = openInputStream(new URL(path), connectionTimeout, readTimeout);
             OutputStream out = openOutputStream(file)) {
            int bytesRead;
            byte[] buffer = new byte[bufferSize];
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                bytesWritten += bytesRead;
            }
            return bytesWritten;
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
    
}
