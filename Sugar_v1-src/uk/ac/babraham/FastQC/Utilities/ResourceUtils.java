package uk.ac.babraham.FastQC.Utilities;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ResourceUtils {

	private static final String RESOURCES_INDEX_TXT = "resources.index.txt";

	public static String[] listResourceNames(String packageName) throws IOException {

// 		The below does not work for jar files 		
//		URL dirURL = getJarResource(packageName);
//				
//		if (dirURL != null && dirURL.getProtocol().equals("file")) {
//			/* A file path: easy enough */			
//			return new File(dirURL.getFile()).list();
//		}

		
		List<String> result = new ArrayList<String>();

		/* 
		 * In case of a jar file, we can't actually find a directory.
		 * So let us read the resources.index.txt file.
		 */
		InputStream is = getJarResourceAsStream(packageName + RESOURCES_INDEX_TXT);
		
		if (is != null) {
			
			try {
					
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
				String line = null;
				while ((line = br.readLine()) != null)
				{
					result.add(line);
				}
			} finally {
				is.close();				
			}			
		}
		else
		{
			System.err.println("The resource.index.txt file was not found in the package: " + packageName);
		}
		
		return result.toArray(new String[0]);
	}
	
	public static InputStream getJarResourceAsStream(String filename) {
		InputStream result = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(filename);
				
		return result;
	}
	
	public static URL getJarResource(String url) {
		URL result = Thread.currentThread().getContextClassLoader().getResource(url);
				
		return result;
	}

	public static boolean hasResourceIndex(String packageName) {
		 return getJarResource(packageName + RESOURCES_INDEX_TXT) != null;
	}

	public static BufferedReader getJarResourceAsBufferredReader(String filename) {
		return new BufferedReader(new InputStreamReader(ResourceUtils.getJarResourceAsStream(filename)));
	}

	public static File extractPackage(String packageName, File targetDir) {		
		
		if (!packageName.endsWith("/"))
			packageName += "/";
		
		if (!hasResourceIndex(packageName)) {
			throw new IllegalArgumentException("Couldn't find help file directory at '"+packageName+"'");
		}
		
		File resultDir = new File(targetDir, packageName);
		extractSubfiles(packageName, resultDir);		
		
		return resultDir;
	}
	
	private static void extractSubfiles (String packageName, File targetDir) {
		
		if (!hasResourceIndex(packageName))
			return;
		
		String[] filenames = new String[0];
		try {
			filenames = listResourceNames(packageName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String f : filenames) {
			String path = packageName + f;
			if (path.endsWith("/")) {
				extractSubfiles(path, new File(targetDir, f));
			}
			else if (getJarResource(path) != null) {
				extractFile(path, new File(targetDir,f));
			}
		}
	}

	private static void extractFile(String path, File targetFile) {
		
		InputStream is = ResourceUtils.getJarResourceAsStream(path);
		OutputStream os = null;
		
		if (is != null)
		{

			try {
				File parentDir = targetFile.getParentFile();
				if (!parentDir.exists())
					parentDir.mkdirs();
				
				os = new BufferedOutputStream(new FileOutputStream(targetFile));
		
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = is.read(buffer)) > 0) { 
					os.write(buffer, 0, len); 
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			finally {
				try {
					is.close();
					if (os != null)
						os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// Extract only once
	static File helpDir = null; 
	
	public static File extractHelpPackage(String startingLocation) {
		
		if (helpDir == null) {
			String programDir = startingLocation.equals("Help") ? ".fastqc" : ".sugar";		
			File targetDir = new File(System.getProperty("user.home"), programDir);
			
			// remove the old help, it could be out-dated
			// delete all HTML files recursively
			helpDir = new File(targetDir, startingLocation);
			deleteHTMLFiles(helpDir);
			
			helpDir = extractPackage(startingLocation, targetDir);
		}
		return helpDir;
	}

	private static void deleteHTMLFiles(File file) {
		
		if (file.isDirectory()) {
			
			for (File childFiles : file.listFiles())
			{
				deleteHTMLFiles(childFiles);
			}
			
		}
		else if (file.isFile())
		{
			if (file.getName().endsWith(".html") || 
				file.getName().endsWith(".htm") || 
				file.getName().endsWith(".png"))
				file.delete();
		}
			
		
	}


}



