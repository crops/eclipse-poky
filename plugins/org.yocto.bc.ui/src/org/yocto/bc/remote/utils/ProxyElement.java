package org.yocto.bc.remote.utils;

public class ProxyElement {
	private String type;
	private String host;
	private int port;
	
	public ProxyElement(String type, String host, int port){
		this.type = type;
		this.host = host;
		this.port = port;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
}
