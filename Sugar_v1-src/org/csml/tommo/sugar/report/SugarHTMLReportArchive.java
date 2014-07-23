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
package org.csml.tommo.sugar.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;

import org.csml.tommo.sugar.SugarApplication;

import uk.ac.babraham.FastQC.FastQCApplication;
import uk.ac.babraham.FastQC.Modules.QCModule;
import uk.ac.babraham.FastQC.Report.HTMLReportArchive;
import uk.ac.babraham.FastQC.Sequence.SequenceFile;
import uk.ac.babraham.FastQC.Utilities.ResourceUtils;

public class SugarHTMLReportArchive extends HTMLReportArchive {

	public SugarHTMLReportArchive(SequenceFile sequenceFile,
			QCModule[] modules, File file) throws IOException {
		super(sequenceFile, modules, file);
	}

	
	
	@Override
	protected void startDocument() throws IOException {
		dataDocument().append("##SUGAR\t");
		dataDocument().append(SugarApplication.VERSION);
		dataDocument().append("\tbased on \n");

		super.startDocument();
	}



	@Override
	protected void closeDocument() {
		
		htmlDocument().append("</div><div class=\"footer\">Produced by SUGAR (version ");
		htmlDocument().append(SugarApplication.VERSION);
		htmlDocument().append(") based on <a href=\"http://www.bioinformatics.babraham.ac.uk/projects/fastqc/\">FastQC</a> (version ");
		htmlDocument().append(FastQCApplication.VERSION);
		htmlDocument().append(")</div>\n");
		
		htmlDocument().append("</body></html>");

	}

	@Override
	protected void addTemplate(String filename, String date) throws IOException {
//		BufferedReader br = new BufferedReader(new FileReader(new File(URLDecoder.decode(ClassLoader.getSystemResource("Templates/sugar_header_template.html").getFile(),"UTF-8"))));
		BufferedReader br = ResourceUtils.getJarResourceAsBufferredReader("Templates/sugar_header_template.html");
		String line;
		while ((line = br.readLine())!=null) {
			
			line = line.replaceAll("@@FILENAME@@", filename);
			line = line.replaceAll("@@DATE@@", date);
			
			htmlDocument().append(line);
			htmlDocument().append("\n");
		}
		
		
		br.close();
	}
	
	@Override
	protected String getHtmlOutput(){
		return "sugar_report.html";
	}
	
	@Override
	protected String getDataOutput(){
		return "sugar_data.txt";
	}

}
