/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.att.authz.env.AuthzTrans;
import com.att.inno.env.APIException;

public abstract class FileCassBatch extends CassBatch {

	public FileCassBatch(AuthzTrans trans, String log4jName) throws APIException, IOException {
		super(trans, log4jName);
	}
	
	protected List<File> findAllFiles(String regex) {
		List<File> files = new ArrayList<File>();
		FileSystem fileSystem = FileSystems.getDefault();
		PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:" + regex);
		Path path = Paths.get(System.getProperty("user.dir"), "data");

		try {
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
					path, regex);
			for (Path file : directoryStream) {
				if (pathMatcher.matches(file.getFileName())) {
					files.add(file.toFile());
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (DirectoryIteratorException ex) {
			ex.printStackTrace();
		}

		return files;
	}



}
