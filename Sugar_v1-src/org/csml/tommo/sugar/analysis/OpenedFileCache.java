/**
 *    Copyright Masao Nagasaki
 *    Nagasaki Lab
 *    Laboratory of Biomedical Information Analysis,
 *    Department of Integrative Genomics,
 *    Tohoku Medical Megabank Organization, Tohoku University 
 *    @since 2013
 *
 *    This file is part of SUGAR (Subtile-based GUI-Assisted Refiner).
 *    SUGAR is an extension of FastQC (copyright 2010-12 Simon Andrews)
 *
 *    SUGAR is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SUGAR is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SUGAR; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.csml.tommo.sugar.analysis;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.csml.tommo.sugar.SugarApplication;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import uk.ac.babraham.FastQC.Analysis.AnalysisListener;
import uk.ac.babraham.FastQC.Modules.QCModule;
import uk.ac.babraham.FastQC.Sequence.SequenceFile;

public class OpenedFileCache implements Serializable, JSONFileSerializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4741929940366094412L;

	private static final String JSON_ATTR_CACHE_VALUES = "cache.values";
	private static final String JSON_ATTR_CACHE_KEYS = "cache.keys";

	
	private static final String CACHE_FILE_SUFFIX = ".cache";
	private static final String CACHE_FILE_PREFIX = "tmp";
	private static final String CACHE_DIR_NAME = "tmp";
	private static final String CACHE_FILE_NAME = "openedFileCache";
	
	private static final FilenameFilter CACHE_FILE_FILTER = new FilenameFilter() {
		
		@Override
		public boolean accept(File dir, String name) {
			// TODO Auto-generated method stub
			return name.startsWith(CACHE_FILE_PREFIX);
		}
	};
	
	private static final String MAX_SIZE_PROPERTY_NAME = "cache.maxSize";
	private static final String EXPIRATION_PERIOD_PROPERTY_NAME = "cache.expirationPeriod";
	private static final String DIRECTORY_PROPERTY_NAME = "cache.directory";

	protected static File CACHE_DIR = new File(SugarApplication.getAPP_HOME_DIR(), CACHE_DIR_NAME); 
	protected static File CACHE_MAP_FILE = new File(getCACHE_DIR(), CACHE_FILE_NAME );

	final protected static long MEGABYTE = 1024*1024; // 1 MB
	final protected static long GIGABYTE = 1024*MEGABYTE; // 1 GB
	protected static long MAXIMUM_CACHE_SIZE = 100*GIGABYTE; // 100 GB
	
	final protected static long HOUR = 1000*3600; // 1 hour in milliseconds
	final protected static long DAY = 24*HOUR; // 1 hour in milliseconds
	protected static long EXPIRATION_TIME = 30*DAY; // one month n milliseconds	
		
	protected static OpenedFileCache INSTANCE; 
	
	
	protected Map<CachedFile, File> cache;		
	
	protected OpenedFileCache() {
		cache = Collections.synchronizedMap(new HashMap<CachedFile, File>());
		initProperties();
	}

	private void initProperties() {
		
		Properties properties = SugarApplication.getApplicationPropeties();
		
		if (properties.getProperty(MAX_SIZE_PROPERTY_NAME) != null)
		{
			int size = new Integer(properties.getProperty(MAX_SIZE_PROPERTY_NAME));
			MAXIMUM_CACHE_SIZE = size * MEGABYTE;
		}
		if (properties.getProperty(EXPIRATION_PERIOD_PROPERTY_NAME) != null)
		{
			int days = new Integer(properties.getProperty(EXPIRATION_PERIOD_PROPERTY_NAME));
			EXPIRATION_TIME = days * DAY;
		}
		if (properties.getProperty(DIRECTORY_PROPERTY_NAME) != null)
		{
			CACHE_DIR = new File(properties.getProperty(DIRECTORY_PROPERTY_NAME));
			CACHE_MAP_FILE = new File(getCACHE_DIR(), CACHE_FILE_NAME );
		}
	}

	static public OpenedFileCache getInstance() {
		
		// construct instance
		if (INSTANCE == null)
		{
			INSTANCE = new OpenedFileCache(); 			

			// try to read from disk file
			if (CACHE_MAP_FILE.exists())
			{
				try {
					INSTANCE.fromJSONFile(CACHE_MAP_FILE);
				} catch (Exception e) {
					
				}
			}
		}
		
		return INSTANCE;
	}
	
	public static File getCACHE_DIR() {
		if (!CACHE_DIR.exists())
			CACHE_DIR.mkdirs();
		return CACHE_DIR;
	}
	
	public boolean readModulesFromCache(File sequenceFile,
			QCModule[] modules, List<AnalysisListener> listeners, int matrixSize, int qualityThreshold) {
		boolean result = false;
		
		CachedFile cachedFile = new CachedFile(sequenceFile, matrixSize, qualityThreshold);
		if (cache.containsKey(cachedFile))
		{
			File cacheFileBasename = cache.get(cachedFile);

			try {


				// first check if all modules are serializable and if cached files for all modules exist
				for (int i=0; i<modules.length; i++)
				{
					QCModule m = modules[i];
					File moduleFile = new File(cacheFileBasename.getAbsolutePath() + m.getClass().getSimpleName());

//					if (!(m instanceof JSONFileSerializable))
//						throw new Exception("Module " + m.name() + "cannot be serialized to JSON");

					if (m instanceof JSONFileSerializable && !moduleFile.isFile())
						throw new Exception("Could not find cache file " + moduleFile.getAbsolutePath());					
				}

				for (int i=0; i<modules.length; i++)
				{
					QCModule m = modules[i];
					
					if (m instanceof JSONFileSerializable)
					{
						File moduleFile = new File(cacheFileBasename.getAbsolutePath() + m.getClass().getSimpleName());					

						fireCacheFileStartedEvent(listeners, m, moduleFile, SugarAnalysisListener.READING_FILE);
						long startTime = System.currentTimeMillis();

						((JSONFileSerializable) m).fromJSONFile(moduleFile);

						long time = System.currentTimeMillis() - startTime;
						fireCacheFileCompletedEvent(listeners, m, time, SugarAnalysisListener.READING_FILE);
					}
				}

				result = true;

			} catch (Throwable e) {
				// an error occurred
				e.printStackTrace();

				// module could not be read from cache 
				// remove the entry from the map
				cache.remove(cachedFile);

				for (QCModule m : modules)
				{
					// reset each module
					m.reset();

					// and remove the corresponding files from the cache folder
					File moduleFile = new File(cacheFileBasename.getAbsolutePath() + m.getClass().getSimpleName());	
					if (moduleFile.isFile())
						moduleFile.delete();
				}

				try {
					toJSONFile(CACHE_MAP_FILE);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				result = false;

			}

		}

		return result;
	}

	private void fireCacheFileStartedEvent(List<AnalysisListener> listeners,
			QCModule m, File moduleFile, int operation) {
		if (listeners != null)
		{
			for (AnalysisListener l : listeners)
			{
				if (l instanceof SugarAnalysisListener)
				{
					((SugarAnalysisListener) l).cacheFileStarted(m, moduleFile.length(), operation);
				}
			}
		}
	}

	private void fireCacheFileCompletedEvent(List<AnalysisListener> listeners,
			QCModule m, long time, int operation) {
		if (listeners != null)
		{
			for (AnalysisListener l : listeners)
			{
				if (l instanceof SugarAnalysisListener)
				{
					((SugarAnalysisListener) l).cacheFileCompleted(m, time, operation);
				}
			}
		}
	}

	public void writeModulesToCache(File sequenceFile, QCModule[] modules, List<AnalysisListener> listeners, int matrixSize, int qualityThreshold) {
				
		if (!cache.containsKey(new CachedFile(sequenceFile, matrixSize, qualityThreshold)))
		{
			try {
				File cacheFileBasename = File.createTempFile(CACHE_FILE_PREFIX, CACHE_FILE_SUFFIX, CACHE_DIR); 

				for (QCModule m : modules)
				{
					if (m instanceof JSONFileSerializable)
					{
						File moduleFile = new File(cacheFileBasename.getAbsolutePath() + m.getClass().getSimpleName());

						fireCacheFileStartedEvent(listeners, m, moduleFile, SugarAnalysisListener.WRITING_FILE);
						long startTime = System.currentTimeMillis();

						((JSONFileSerializable) m).toJSONFile(moduleFile);					

						long time = System.currentTimeMillis() - startTime;
						fireCacheFileCompletedEvent(listeners, m, time, SugarAnalysisListener.WRITING_FILE);
					}
				}
				cache.put(new CachedFile(sequenceFile, matrixSize, qualityThreshold), cacheFileBasename);
				cleanupCacheFolder();
				//				toFile(CACHE_MAP_FILE, this);
				toJSONFile(CACHE_MAP_FILE);
			} catch (Exception e) {

			}			
		}
	}
	
	public void cleanupCacheFolder() {

		File cacheDir = getCACHE_DIR();
		if(cacheDir.exists()) {
			cleanUpByMaximumSize(cacheDir);
			cleanUpByTime(cacheDir);		
			cleanUpByInvalidMapEntries(cacheDir);		
		}
		
	}

	private void cleanUpByInvalidMapEntries(File cacheDir) {
		
		List<CachedFile> entriesToRemove = new ArrayList<CachedFile>();
		for (CachedFile cf: cache.keySet())
		{
			File f= cf.getFile();
			if (!f.isFile())
				entriesToRemove.add(cf);
		}
		
		for (CachedFile cf : entriesToRemove) {
			cache.remove(cf);
		}		
	}
	
	private void cleanUpByTime(File cacheDir) {
		
		long currentTime = System.currentTimeMillis();
		long minTime = currentTime - EXPIRATION_TIME;

		File[] cacheFiles = cacheDir.listFiles(CACHE_FILE_FILTER);
		for(File file: cacheFiles) {
			if (file.lastModified() < minTime)
				file.delete();
		}
	}
	
	private void cleanUpByMaximumSize(File cacheDir) {
		int totalSize = 0;

		File[] cacheFiles = cacheDir.listFiles(CACHE_FILE_FILTER);
		for(File file: cacheFiles) {
			totalSize += file.length();
		}
		
		if (totalSize > MAXIMUM_CACHE_SIZE)
		{
			// sort the array from oldest to newest
			Arrays.sort(cacheFiles, new Comparator<File>() {

				public int compare(File f1, File f2)
				{
					return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
				} 

			});
			
			for (File file : cacheFiles)
			{
				totalSize -= file.length();
				file.delete();
				
				if (totalSize < MAXIMUM_CACHE_SIZE)
					break;
			}
		}
	}
	
	@Override
	public String toString() {
		return "OpenedFileCache [cache=" + cache + "]";
	}

	// default Java Serialization
	
	static public File toFile(File file, Object obj){
		ObjectOutputStream oos= null;
		try{
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
		    oos = new ObjectOutputStream(bos);
		    oos.writeObject(obj);
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
		finally{
			if(oos != null){
				try{
					oos.close();
				}
				catch(IOException ex){}
			}
		}	
		return file;
	}
	
	public static Object fromFile(File file) throws FileNotFoundException, IOException, ClassNotFoundException{
		Object result= null;
		ObjectInputStream ois= null;
		try{
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
		    ois = new ObjectInputStream(bis);
		    result= ois.readObject();
		}
		finally{
			if(ois != null){
				try{
					ois.close();
				}
				catch(IOException ex){}
			}
		}	
		return result;
	}
	
	// default Java Serialization

	// customized JSON Serialization

	@Override
	public String toJSONString() {
        JSONObject obj = toJSONObject();
        return obj.toString();		        
	}

	@Override
	public void writeJSONString(Writer out) throws IOException {
        JSONObject obj = toJSONObject();
        JSONValue.writeJSONString(obj, out);		
	}
		
	@Override
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		
		JSONArray keyArray = new JSONArray();
		JSONArray valueArray = new JSONArray();
		for(CachedFile cachedFile : cache.keySet()) {
			keyArray.add(cachedFile);
			valueArray.add(cache.get(cachedFile).getAbsolutePath());
		}		
		
        obj.put(JSON_ATTR_CACHE_KEYS, keyArray);
        obj.put(JSON_ATTR_CACHE_VALUES, valueArray);
		return obj;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		JSONArray keys = (JSONArray) jsonObject.get(JSON_ATTR_CACHE_KEYS);
		JSONArray values = (JSONArray) jsonObject.get(JSON_ATTR_CACHE_VALUES);

		if (keys.size() == values.size()) {
			
			for (int i = 0 ; i < keys.size(); i++)
			{
				JSONObject jsonCachedFile = (JSONObject) keys.get(i);
				CachedFile cachedFile = new CachedFile();
				cachedFile.fromJSONObject(jsonCachedFile); 
				File file = new File(values.get(i).toString());
				cache.put(cachedFile, file);
				
			}			
		}
	}

	@Override
	public void toJSONFile(File file) throws IOException {		
		JSONSerializationUtils.writeJSONFile(file, this);
	}

	@Override
	public void fromJSONFile(File file) throws IOException, ParseException {
		JSONSerializationUtils.fromJSONFile(file, this);		
	}
	
	// customized JSON Serialization
	
	public Integer[] getAvailableQualityThresholds(
			SequenceFile selectedSequenceFile, int matrixSize) {
		
		List<Integer> result = new ArrayList<Integer>();
		
		for (CachedFile cachedFile : cache.keySet())
		{
			if (cachedFile.getFile().equals(selectedSequenceFile.getFile()) &&
				cachedFile.getMatrixSize() == matrixSize)
			{
				result.add(cachedFile.getQualityThreshold());
			}
		}
		
		Collections.sort(result, Collections.reverseOrder());
				
		return result.toArray(new Integer[0]);
	}



}
