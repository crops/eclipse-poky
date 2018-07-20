/*******************************************************************************
 * Copyright (c) 2018 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.yocto.cmake.core.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class StreamGobbler implements Runnable {

	private OutputStream out;
	private InputStream in;

	public StreamGobbler(OutputStream out, InputStream in) {
		this.out = out;
		this.in = in;
	}

	@Override
	public void run() {
		String line;
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		OutputStreamWriter writer = new OutputStreamWriter(out);
		try {
			while((line = br.readLine()) != null) {
				writer.write(line + System.lineSeparator());
			}

			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
