package org.yocto.bc.remote.utils;

import java.io.IOException;
import java.io.Writer;

public class ConsoleWriter extends Writer {

	private StringBuffer sb;

	public ConsoleWriter() {
		sb = new StringBuffer();
	}

	@Override
	public void close() throws IOException {
	}

	public String getContents() {
		return sb.toString();
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		sb.append(cbuf);
	}

	@Override
	public void write(String str) throws IOException {
		sb.append(str);
	}

}