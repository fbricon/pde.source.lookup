/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.pde.sourcelookup.core.internal.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.publisher.eclipse.BundlesAction;

public class BundleUtil {

	private BundleUtil() {
		//prevent instantiation
	}

	public static boolean isBundle(File file) {
		return getArtifactKey(file) != null;
	}

	public static boolean isBundle(ZipFile jar) {
		return getArtifactKey(jar) != null;
	}

	/**
	 * @param jar
	 * @return
	 * @throws IOException
	 */
	private static Manifest getManifest(ZipFile jar) throws IOException {
		Manifest manifest = null;
		if (jar instanceof JarFile) {
			manifest = ((JarFile) jar).getManifest();
		} else {
			ZipEntry manifestEntry = jar.getEntry(JarFile.MANIFEST_NAME);
			if (manifestEntry != null) {
				try (InputStream manifestStream = jar.getInputStream(manifestEntry)) {
					manifest = new Manifest(manifestStream);
				}
			}
		}
		return manifest;
	}

	public static IArtifactKey getArtifactKey(File file) {
		if (file == null || !file.isFile()) {
			return null;
		}
		String extension = new Path(file.getName()).getFileExtension();
		if (!"jar".equals(extension)) {
			return null;
		}
		try (JarFile jar = new JarFile(file)) {
			return getArtifactKey(jar.getManifest());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static IArtifactKey getArtifactKey(ZipFile file) {
		if (file == null) {
			return null;
		}
		try {
			Manifest manifest = getManifest(file);
			IArtifactKey bundleModel = getArtifactKey(manifest);
			return bundleModel;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static IArtifactKey getArtifactKey(Manifest manifest) {
		if (manifest == null) {
			return null;
		}
		String name = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
		name = StringUtils.substringBefore(name, ";");
		String version = manifest.getMainAttributes().getValue("Bundle-Version");
		if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(version)) {
			return BundlesAction.createBundleArtifactKey(name, version);
		}
		return null;
	}

	public static IArtifactKey toSourceKey(IArtifactKey key) {
		if (key == null) {
			return null;
		}
		String name = key.getId();
		if (name.endsWith(".source")) {
			return key;
		}
		return BundlesAction.createBundleArtifactKey(name + ".source", key.getVersion().toString());
	}

}
