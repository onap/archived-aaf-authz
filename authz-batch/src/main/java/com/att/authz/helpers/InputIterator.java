/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;

public class InputIterator implements Iterable<String> {
	private BufferedReader in;
	private final PrintStream out;
	private final String prompt, instructions;
	
	public InputIterator(BufferedReader in, PrintStream out, String prompt, String instructions) {
		this.in = in;
		this.out = out;
		this.prompt = prompt;
		this.instructions = instructions;
	}
	
	@Override
	public Iterator<String> iterator() {
		out.println(instructions);
		return new Iterator<String>() {
			String input;
			@Override
			public boolean hasNext() {
				out.append(prompt);
				try {
					input = in.readLine();
				} catch (IOException e) {
					input = null;
					return false;
				}
				return input.length()>0;
			}

			@Override
			public String next() {
				return input;
			}

			@Override
			public void remove() {
			}
		};
	}
}

