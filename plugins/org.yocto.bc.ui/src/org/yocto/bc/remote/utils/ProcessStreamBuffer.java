package org.yocto.bc.remote.utils;

import java.util.ArrayList;
import java.util.List;

public class ProcessStreamBuffer {
	private static final String WHITESPACES = "\\s+";
	List<String> errorLines;
	List<String> outputLines;
	
	ProcessStreamBuffer(){
		errorLines = new ArrayList<String>();
		outputLines = new ArrayList<String>();
	}
	
	public void addErrorLine(String line){
		errorLines.add(line);
	}
	public void addOutputLine(String line){
		outputLines.add(line);
	}
	
	public List<String> getOutputLines(){
		return outputLines;
	}
	
	public List<String> getErrorLines(){
		return errorLines;
	}
	
	public String getMergedOutputLines(){
		String returnVal = "";
		for (int i = 0; i < outputLines.size(); i++) {
			String line = outputLines.get(i);
			returnVal += line;
			if (outputLines.size() > 1 && i != outputLines.size() - 1)
				returnVal += "\n";
		}
		return returnVal;
	}

	public boolean hasErrors() {
		return errorLines.size() != 0;
	}

	public String getLastOutputLineContaining(String str) {
		if (!errorLines.isEmpty())
			return null;
		for (int i = outputLines.size() - 1; i >= 0; i--){
			String line = outputLines.get(i);
			if (line.replaceAll(WHITESPACES, "").contains(str.replaceAll(WHITESPACES, "")))
				return line;
		}
		return null;
	}

	public String getOutputLineContaining(String str) {
		int index = 0;
		for (int i = outputLines.size() - 1; i >= 0; i--){
			String line = outputLines.get(i);
			if (line.contains(str)) {
				index = i + 1;
				break;
			}
		}
		if (index >= 0 && index < outputLines.size())
			return outputLines.get(index);
		return null;
	}
}
