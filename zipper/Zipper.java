package zipper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Util class to create and manipulate zip files.
 * 
 * @author <a href="mailto:bcnyorch@gmail.com">Jordi Castilla</a>
 *
 */
public class Zipper {
	
	private static final String ZIP = ".zip";

	/**
	 * Exception states.
	 */
	public static enum ZIP_ERROR {
		FOLDER_IS_EMPTY, 
		FILE_ALREADY_EXISTS, 
		NOT_FOUND, 
		IO_EXCEPTION, 
		IS_NOT_A_FOLDER,
		IS_NOT_A_FILE,
		CANT_DELETE_FOLDER,
		CANT_RENAME;
	}
	
	
	/**
	 * Not allowing class instantiation.
	 */
	private Zipper() {}
	

  /**
	 * Creates a zip file from a given file array. 
	 * If zipped succesfully files will be deleted<br>
	 * Files added to zip will be renamed following next pattern:<br>
	 * filename # date(yyyyMMdd) # sequential.original extension of the file<br>
	 * <br>
	 * 
	 * @param toAdd file array to be added to zipFile.
	 * @param zipFile.
	 * @return zip file with files added..
	 *
	 * @throws ZipperException in case of error.
	 */
	public static synchronized File createZip(File[] toAdd, String filename) throws ZipperException, IOException {
		String pattern = getPatternName(toAdd[0], filename);
		toAdd = rename(toAdd, pattern, 1);
		return makeZip(toAdd, File.createTempFile(filename, ZIP));
	}
	
  /**
	 * Creates a zip file from a given file. 
	 * If zipped succesfully file will be deleted<br>
	 * File added to zip will be renamed following next pattern:<br>
	 * filename # date(yyyyMMdd) # sequential.original extension of the file<br>
	 * <br>
	 * 
	 * @param toAdd file to be added to zipFile.
	 * @param zipFile.
	 * @return zip file with files added..
	 *
	 * @throws ZipperException in case of error.
	 */
	public static synchronized File createZip(File toAdd, String filename) throws ZipperException, IOException {
		if (!toAdd.exists()) 
			throw new ZipperException(toAdd.getAbsolutePath(), ZIP_ERROR.NOT_FOUND);
		if (toAdd.isDirectory())
			throw new ZipperException(toAdd.getAbsolutePath(), ZIP_ERROR.IS_NOT_A_FILE);

		String pattern = getPatternName(toAdd, filename);
		File[] files = new File[1];
		files[0] = toAdd;
		
		files = rename(files, pattern, 1);

		return makeZip(files, File.createTempFile(filename, ZIP));
		
	}
	
	/**
	 * Add a file to an existing zip file.<br>
	 * If zipped succesfully file will be deleted<br>
	 * File added to zip will be renamed following next pattern:<br>
	 * filename # date(yyyyMMdd) # sequential.original extension of the file<br>
	 * <br>
	 * @param toAdd file to be added to zipFile.
	 * @param zipFile.
	 * @return zip file with files added..
	 * @throws ZipperException in case of error.
	 */
	public static synchronized File addToZip(File toAdd, File zipFile) throws ZipperException {
		if (!toAdd.exists()) 
			throw new ZipperException(toAdd.getAbsolutePath(), ZIP_ERROR.NOT_FOUND);
		if (toAdd.isDirectory())
			throw new ZipperException(toAdd.getAbsolutePath(), ZIP_ERROR.IS_NOT_A_FILE);
			
		File[] files = new File[1];
		files[0] = toAdd;
		return Zipper.addToZip(files, zipFile);
	}
	
	
	/**
	 * Add various files to an existing zip file<br>
	 * Files added to zip will be renamed following next pattern:
	 * <br>
	 * filename # date(yyyyMMdd) # sequential.original extension of the file<br>
	 * <br>
	 * If zipped succesfully files will be deleted<br>
	 * <br>
	 * @param toAdd file array to be added to zipFile.
	 * @param zipFile.
	 * @return zip file with files added..
	 * @throws ZipperException in case of error.
	 */
	public static synchronized File addToZip(File[] toAdd, File zipFile) throws ZipperException {
		int seq = 0;
		
		if (!zipFile.exists())
			throw new ZipperException(zipFile.getAbsolutePath(), ZIP_ERROR.NOT_FOUND);
		if (zipFile.isDirectory())
			throw new ZipperException(zipFile.getAbsolutePath(), ZIP_ERROR.IS_NOT_A_FILE);
		
		File tmpZip = null;
		
		try
	    {
			ZipFile z = new ZipFile(zipFile);
			seq = z.size() + 1;
			z.close();
			
			tmpZip = File.createTempFile(zipFile.getName(), null);
			
			tmpZip.delete();
			boolean isRenamed = zipFile.renameTo(tmpZip); 
			if(!isRenamed)
				throw new ZipperException(tmpZip.getAbsolutePath(), ZIP_ERROR.CANT_RENAME);
				
			String filename = zipFile.getName().substring(0, zipFile.getName().lastIndexOf("."));
			String pattern = getPatternName(zipFile, filename);
	
			toAdd = rename(toAdd, pattern, seq);
				
	        byte[] buffer = new byte[1024];
	        
	        ZipInputStream zin = new ZipInputStream(new FileInputStream(tmpZip));
	        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

	        for(int i = 0; i < toAdd.length; i++)
	        {
	            InputStream in = new FileInputStream(toAdd[i]);
	            out.putNextEntry(new ZipEntry(toAdd[i].getName()));
	            for(int read = in.read(buffer); read > -1; read = in.read(buffer))
	            {
	                out.write(buffer, 0, read);
	            }
	            out.closeEntry();
	            in.close();
	            
				// borramos los ficheros temporales
				toAdd[i].delete();
	        }

	        for(ZipEntry ze = zin.getNextEntry(); ze != null; ze = zin.getNextEntry())
	        {
	            out.putNextEntry(ze);
	            for(int read = zin.read(buffer); read > -1; read = zin.read(buffer))
	            {
	                out.write(buffer, 0, read);
	            }
	            out.closeEntry();
	        }

	        out.close();
	        zin.close();
	        
	    } catch (FileNotFoundException e) {
			throw new ZipperException(e.getLocalizedMessage(), ZIP_ERROR.NOT_FOUND);
		} catch (IOException e) {
			throw new ZipperException(e.getLocalizedMessage(), ZIP_ERROR.IO_EXCEPTION);
		}
			
		return zipFile;
	}
	
	
	/**
	 * Creates a zip file with filename from a given folder.<br>
	 * Files inside zip will be renamed following next pattern:
	 * <br>
	 * filename # date(yyyyMMdd) # sequential.original extension of the file<br>
	 * <br>
	 * If zipped succesfully files will be deleted<br>
	 * <br>
	 * @param folderOrigen where the files are located. Not recursive.
	 * @param filename of the zipped file.
	 * @return file compressed.
	 * @throws ZipperException in case of error.
	 */
	public static synchronized File zipFolder(File folderOrigen, String filename) throws ZipperException, IOException {
		if (!folderOrigen.isDirectory()) {
			throw new ZipperException(folderOrigen.getAbsolutePath(), ZIP_ERROR.IS_NOT_A_FOLDER);
		}

		int seq = 1;
		File zipFile = null;


		if (folderOrigen.listFiles().length < 1)
			throw new ZipperException(folderOrigen.getAbsolutePath(), ZIP_ERROR.FOLDER_IS_EMPTY);

		File[] files = folderOrigen.listFiles();
		
		String pattern = getPatternName(folderOrigen, filename);
		filename = folderOrigen.getAbsolutePath() + File.separator + filename;
		zipFile = File.createTempFile(filename, ZIP);
		files = rename(files, pattern, seq);

		return makeZip(files, zipFile);
	}
	
	/**
	 * Creates a zip file from a given array of files.<br>
	 *  <br>
	 * @param files array to add to zip file
	 * @param zipFile file to be filled.
	 * @return zipFile with files comressed inside.
	 * 
	 * @throws ZipperException in case of error.
	 */
	private static synchronized File makeZip(File[] files, File zipFile) throws ZipperException {
		try {
			FileOutputStream fos = new FileOutputStream(zipFile.getAbsolutePath());
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (File fileEntry : files) {
				System.out.println("AÑADIENDO " + fileEntry.getAbsolutePath() + " al ZIP " + zipFile.getAbsolutePath());
				FileInputStream fis = new FileInputStream(fileEntry);
				ZipEntry zipEntry = new ZipEntry(fileEntry.getName());
				zos.putNextEntry(zipEntry);

				byte[] bytes = new byte[1024];
				int length;
				while ((length = fis.read(bytes)) >= 0) 
				{
					zos.write(bytes, 0, length);
				}

				zos.closeEntry();
				fis.close();

				// borramos los ficheros temporales
				fileEntry.delete();
			}
			zos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			throw new ZipperException(e.getLocalizedMessage(), ZIP_ERROR.NOT_FOUND);
		} catch (IOException e) {
			throw new ZipperException(e.getLocalizedMessage(), ZIP_ERROR.IO_EXCEPTION);
		}
		
		return zipFile;
	}
	
	/**
	 * Renames an array of files using given pattern.<br>
	 *  <br>
	 * @param files to rename.
	 * @param pattern to use. 
	 * @param seq starting sequence number to start.
	 * @return a new array with renamed files.
	 */
	private static synchronized File[] rename(File[] files, String pattern, int seq) {
		File[] newFiles = new File[files.length];
		int count = 0;
		for (final File f : files) {
			// cogemos la extension del fichero original
			String extension = "";
			int i = f.getName().lastIndexOf('.');
			if (i > 0)
				extension = f.getName().substring(i);

			// renombramos todos los ficheros
			String newFileName = pattern + seq++ + extension;
			File newfile = new File(newFileName);
			f.renameTo(newfile);
			newFiles[count++] = newfile;
		}
		
		return newFiles;
	}
	
	/**
	 * Delete origin folder and it's files (not recursive).<br>
	 * <br>
	 * @param toDelete
	 * @return true if all files deleted, false otherwise.
	 */
	public static boolean cleanFolder(File toDelete) throws ZipperException {
		File[] files = toDelete.listFiles();
		if (null != files) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					throw new ZipperException(files[i].getAbsolutePath(), ZIP_ERROR.CANT_DELETE_FOLDER);
				}
				boolean isDeleted = files[i].delete();
				if (!isDeleted) return false;
			}
		}
		
		return (toDelete.delete());
	}
	
	private static String getPatternName(File file, String filename) {
		String fecha = new SimpleDateFormat("yyyyMMdd").format(new Date());
		String folder = file.isDirectory() ? file.getAbsolutePath() : file.getParent();
		// 2015 / 05 / 14 workaround para ficheros con rutas relativas
		if (folder == null || folder == "") 
			folder = "";
		else 
			folder += File.separator ;
			
		return folder + filename + "#" + fecha + "#";
	}
}
