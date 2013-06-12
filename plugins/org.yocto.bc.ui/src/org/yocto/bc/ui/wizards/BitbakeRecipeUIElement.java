/*******************************************************************************
 * Copyright (c) 2013 Intel Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel - initial API and implementation
 * Ioana Grigoropol (Intel) - adapt class for remote support
 *******************************************************************************/
package org.yocto.bc.ui.wizards;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Text;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class BitbakeRecipeUIElement {
	
	private String container;
	private String file;
	private String description;
	private String license;
	private String checksum;
	private String homepage;
	private String author;
	private String section;
	private String srcuri;
	private String md5sum;
	private String sha256sum;
	private URI metaDir;
	private ArrayList inheritance;

	public BitbakeRecipeUIElement()
	{
		this.container = "";
		this.file = "";
		this.description = "";
		this.license = "";
		this.checksum = "";
		this.homepage = "";
		this.author = "";
		this.section = "";
		this.srcuri = "";
		this.md5sum = "";
		this.sha256sum = "";
		this.inheritance = new ArrayList();
		try {
			this.metaDir = new URI("");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public String getContainer() {
		return container;
	}
	public void setContainer(String value) {
		this.container = value;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String value) {
		this.file = value;
	}
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String value) {
		this.description = value;
	}
	
	public String getLicense() {
		return license;
	}
	
	public void setLicense(String value) {
		this.license = value;
	}
	
	public String getChecksum() {
		return checksum;
	}
	public void setChecksum(String value) {
		this.checksum = value;
	}
	
	public String getHomePage() {
		return homepage;
	}
	
	public void setHomePage(String value) {
		this.homepage = value;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String value) {
		this.author = value;
	}
	
	public String getSection() {
		return section;
	}
	public void setSection(String value) {
		this.section = value;
	}
	public String getSrcuri() {
		return srcuri;
	}
	public void setSrcuri(String value) {
		this.srcuri = value;
	}
	
	public String getMd5sum() {
		return md5sum;
	}
	
	public void setMd5sum(String value) {
		this.md5sum = value;
	}
	
	public String getsha256sum() {
		return sha256sum;
	}
	
	public void setSha256sum(String value) {
		this.sha256sum = value;
	}
	
	public ArrayList<String> getInheritance() {
		return inheritance;
	}
	
	public void setInheritance(ArrayList<String> value) {
		this.inheritance = value;
	}
	
	public URI getMetaDir() {
		return metaDir;
	}
	
	public void setMetaDir(URI value) {
		metaDir = value;
	}
}
