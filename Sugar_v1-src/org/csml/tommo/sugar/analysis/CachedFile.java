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

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class CachedFile implements JSONSerializable {
	
	private final static int CACHE_VERSION = 1;
	
	private static final String JSON_ATTR_VERSION = "version";
	private static final String JSON_ATTR_MATRIX_SIZE = "matrixSize";
	private static final String JSON_ATTR_FILE_SIZE = "fileSize";
	private static final String JSON_ATTR_LAST_MODIFIED = "lastModified";
	private static final String JSON_ATTR_FILE_PATH = "filePath";
	private static final String JSON_ATTR_QUALITY_THRESHOLD = "qualityThreshold";


	protected String filePath;
	protected Long lastModified;
	protected Long fileSize;

	protected int matrixSize;
	protected int qualityThreshold;
	protected int version;
	
	public CachedFile(File file, int matrixSize, int qualityThreshold) {
		filePath = file.getAbsolutePath();
		lastModified = file.lastModified();
		fileSize = file.length();
		this.matrixSize = matrixSize;
		this.qualityThreshold = qualityThreshold;
		version = CACHE_VERSION;
	}

	// customized JSON Serialization

	protected CachedFile() {
	}

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

	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
        obj.put(JSON_ATTR_FILE_PATH, filePath);
        obj.put(JSON_ATTR_LAST_MODIFIED, lastModified);
        obj.put(JSON_ATTR_FILE_SIZE, fileSize);
        obj.put(JSON_ATTR_MATRIX_SIZE, matrixSize);
        obj.put(JSON_ATTR_QUALITY_THRESHOLD, qualityThreshold);
        obj.put(JSON_ATTR_VERSION, version);
		return obj;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		
        filePath = jsonObject.get(JSON_ATTR_FILE_PATH).toString();
        lastModified = new Long(jsonObject.get(JSON_ATTR_LAST_MODIFIED).toString());
        fileSize = new Long(jsonObject.get(JSON_ATTR_FILE_SIZE).toString());
        matrixSize = new Integer(jsonObject.get(JSON_ATTR_MATRIX_SIZE).toString());
        qualityThreshold = new Integer(jsonObject.get(JSON_ATTR_QUALITY_THRESHOLD).toString());        
        version = new Integer(jsonObject.get(JSON_ATTR_VERSION).toString());

	}
	
	// customized JSON Serialization

	@Override
	public String toString() {
		return "CachedFile [filePath=" + filePath + ", lastModified="
				+ lastModified + ", fileSize=" + fileSize + ", matrixSize="
				+ matrixSize + ", qualityThreshold=" + qualityThreshold
				+ ", version=" + version + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result
				+ ((fileSize == null) ? 0 : fileSize.hashCode());
		result = prime * result
				+ ((lastModified == null) ? 0 : lastModified.hashCode());
		result = prime * result + matrixSize;
		result = prime * result + qualityThreshold;
		result = prime * result + version;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CachedFile other = (CachedFile) obj;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (fileSize == null) {
			if (other.fileSize != null)
				return false;
		} else if (!fileSize.equals(other.fileSize))
			return false;
		if (lastModified == null) {
			if (other.lastModified != null)
				return false;
		} else if (!lastModified.equals(other.lastModified))
			return false;
		if (matrixSize != other.matrixSize)
			return false;
		if (qualityThreshold != other.qualityThreshold)
			return false;
		if (version != other.version)
			return false;
		return true;
	}

	public File getFile() {
		return new File(filePath);
	}
	
	public int getMatrixSize() {
		return matrixSize;
	}

	public int getQualityThreshold() {
		return qualityThreshold;
	}	

}
