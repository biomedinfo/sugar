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

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.FastQC.Sequence.Sequence;
import uk.ac.babraham.FastQC.Sequence.SequenceFile;

public class BAMSequence extends Sequence implements SAMInfo{
	
	protected SAMRecord samRecord;
	protected int mappingQuality;

	public BAMSequence(SequenceFile file, String sequence, String quality,
			String id, int mappingQuality, SAMRecord record) {
		super(file, sequence, quality, id);
		this.mappingQuality = mappingQuality;
		samRecord = record;
	}

	public SAMRecord getSAMRecord() {
		return samRecord;
	}
	
	public int getMappingQuality() {
		return mappingQuality;
	}
	
	public static byte[] reverseComplement (byte[] sequence) {
		
		byte [] letters = reverse(sequence);
		byte [] rc = new byte[letters.length];
		
		for (int i=0;i<letters.length;i++) {
			switch(letters[i]) {
			case 'G': rc[i] = 'C';break;
			case 'A': rc[i] = 'T';break;
			case 'T': rc[i] = 'A';break;
			case 'C': rc[i] = 'G';break;
			default: rc[i] = letters[i];
			}
		}
	
		return rc;

	}
	
	public static byte[] reverse (byte[] sequence) {
		byte [] reversed = new byte[sequence.length];
		
		for (int i=0;i<sequence.length;i++) {
			reversed[reversed.length-(1+i)] = sequence[i];
		}
		
		return reversed;
	}


}
