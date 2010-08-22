/*******************************************************************************
 * Copyright (c) 2010 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Denis Solonenko - initial API and implementation
 ******************************************************************************/
package ru.orangesoftware.financisto.export;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import api.wireless.gdata.docs.client.DocsClient;
import api.wireless.gdata.docs.data.DocumentEntry;
import api.wireless.gdata.docs.data.FolderEntry;

public abstract class Export {
	
	public static final String EXPORT_PATH = "/sdcard/financisto/";

	public String export() throws Exception {
		String fileName = generateFilename();
		File path = new File(getPath());
		path.mkdirs();
		File file = new File(path, fileName);		
		FileOutputStream outputStream = new FileOutputStream(file);
		generateBackup(outputStream);
		return fileName;
	}
	
	/**
	 * Backup database to google docs
	 * 
	 * @param docsClient Google docs connection
	 * @param folder Google docs folder name 
	 * */
	public String exportOnline(DocsClient docsClient, String folder) throws Exception {
		// generation backup file
		String fileName = generateFilename();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		generateBackup(outputStream);
		
		// transforming streams
		InputStream backup = new ByteArrayInputStream(outputStream.toByteArray()); 
		
		// creating document on Google Docs
		DocumentEntry entry = new DocumentEntry();
		entry.setTitle(fileName);
		FolderEntry fd = docsClient.getFolderByTitle(folder);
		docsClient.createDocumentInFolder(entry, backup, "text/plain",fd.getKey());
		
		return fileName;
	}
	
	private String generateFilename()
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'_'HHmmss'_'SSS");
		return df.format(new Date())+getExtension();
	}
	
	private void generateBackup(OutputStream outputStream) throws Exception 
	{
		OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
		BufferedWriter bw = new BufferedWriter(osw, 65536);

		try {			
			writeHeader(bw);
			writeBody(bw);
			writeFooter(bw);
		}
		finally {
			bw.close();
		}	
	}

	protected abstract  void writeHeader(BufferedWriter bw) throws Exception;

	protected abstract  void writeBody(BufferedWriter bw) throws Exception;

	protected abstract  void writeFooter(BufferedWriter bw) throws Exception;

	protected abstract String getExtension();
	
	protected String getPath() {
		return EXPORT_PATH;
	}

}
