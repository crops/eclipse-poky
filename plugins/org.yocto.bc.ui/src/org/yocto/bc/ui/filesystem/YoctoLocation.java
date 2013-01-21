package org.yocto.bc.ui.filesystem;

import java.net.URI;
import java.net.URISyntaxException;

public class YoctoLocation{
	URI oefsURI;
	URI originalURI;
	
	public YoctoLocation(){
		try {
			oefsURI = new URI("");
			originalURI = new URI("");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public URI getOEFSURI() {
		return oefsURI;
	}

	public URI getOriginalURI() {
		return originalURI;
	}

	public void setOriginalURI(URI originalURI) {
		this.originalURI = originalURI;
	}

	public void setOEFSURI(URI uri) {
		this.oefsURI = uri;
	}
	
	
}
