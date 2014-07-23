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
 *    FastQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    FastQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with FastQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.csml.tommo.sugar.sequence;

import org.csml.tommo.sugar.utils.StringUtils;

import uk.ac.babraham.FastQC.Sequence.Sequence;


public class SequenceCoordinates extends TileCoordinates {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 4932105036556035112L;

	/**
	 * 'x-coordinate' of the cluster within the tile
	 */
	Integer x;
	
	/**
	 * 'y-coordinate' of the cluster within the tile
	 */
	Integer y;

	
	/**
	 * @param flowCell
	 * @param lane
	 * @param tile
	 * @param x
	 * @param y
	 */
	public SequenceCoordinates(String flowCell, Integer lane, Integer tile, Integer x, Integer y) {
		super(flowCell, lane, tile);
		this.x = x;
		this.y = y;
	}
	
	/**
	 * @param lane
	 * @param tile
	 * @param x
	 * @param y
	 */
	public SequenceCoordinates(Integer lane, Integer tile, Integer x, Integer y) {
		this("", lane, tile, x, y);
	}

	/**
	 * createSequenceCoordinates(Sequence seq)
	 * 
	 * get the sequence location described by sequence id line from FASTQ file
	 * 
	 * @param seq - Sequence object imported from FASTQ file 
	 * @return the sequence coordinates of the input Sequence
	 * 
	 */
	public static SequenceCoordinates createSequenceCoordinates(Sequence seq) {
		return getSequenceCoordinates(seq.getID());
	}
	

	/**
	 * getSequenceCooridnates(String id)
	 * 
	 * split the sequence id line from FASTQ file into the following "coordinates":
	 * flowCell, lane, tile, x-Coordinate, y-Coordinate 
	 * 
	 * @param id - id line from FASTQ file 
	 * @return the sequence coordinates of the input Sequence
	 * 
	 */
	public static SequenceCoordinates getSequenceCoordinates(String seqeunceIdLine) {
		SequenceCoordinates result = null;
		
//		String[] lineParts = seqeunceIdLine.split(":");
		String[] lineParts = StringUtils.splitString(seqeunceIdLine, ":");
		
		if (lineParts.length == 5)
		{
			// first try '#' delimiter (separator)
//			String lastCoordinate = lineParts[4].split("#")[0];
			String lastCoordinate = lineParts[4];
			if(lastCoordinate.contains("#")){
				lastCoordinate = lastCoordinate.substring(0, lastCoordinate.indexOf("#"));
			}
			
			// next try space delimiter (separator)
//			lastCoordinate = lastCoordinate.split(" ")[0];
			if(lastCoordinate.contains(" ")){
				lastCoordinate = lastCoordinate.substring(0, lastCoordinate.indexOf(" "));
			}
			
			// standard Illumina indentifier
			result = new SequenceCoordinates(
					Integer.valueOf(lineParts[1]), 
					Integer.valueOf(lineParts[2]),
					Integer.valueOf(lineParts[3]),
					Integer.valueOf(lastCoordinate));
			
		} 
		else if (lineParts.length >=7 )
		{
			// try space delimiter (separator)
//			String lastCoordinate = lineParts[6].split(" ")[0];
			String lastCoordinate = lineParts[6];
			if(lastCoordinate.contains(" ")){
				lastCoordinate = lastCoordinate.substring(0, lastCoordinate.indexOf(" "));
			}			

			// Casava 1.8 format
			result = new SequenceCoordinates(
					lineParts[2],
					Integer.valueOf(lineParts[3]), 
					Integer.valueOf(lineParts[4]),
					Integer.valueOf(lineParts[5]),
					Integer.valueOf(lastCoordinate));
		}
		
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SequenceCoordinates other = (SequenceCoordinates) obj;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		return true;
	}

	public Integer getX() {
		return x;
	}

	public Integer getY() {
		return y;
	}
}
