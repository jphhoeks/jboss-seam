package org.jboss.seam.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * Implementation of {@link Scanner} which can scan a {@link URLClassLoader}
 * 
 * @author Thomas Heute
 * @author Gavin King
 * @author Norman Richards
 * @author Pete Muir
 *
 */
public class URLScanner extends AbstractScanner {
	private static final LogProvider log = Logging.getLogProvider(URLScanner.class);

	private long timestamp;

	public URLScanner(DeploymentStrategy deploymentStrategy) {
		super(deploymentStrategy);
	}

	@Override
	public void scanDirectories(File[] directories) {
		scanDirectories(directories, new File[0]);
	}

	@Override
	public void scanDirectories(File[] directories, File[] excludedDirectories) {
		for (File directory : directories) {
			handleDirectory(directory, null, excludedDirectories);
		}
	}

	@Override
	public void scanResources(String[] resources) {
		long startTime = System.currentTimeMillis();
		Set<String> paths = new HashSet<String>();
		for (String resourceName : resources) {
			try {
				Enumeration<URL> urlEnum = getDeploymentStrategy().getClassLoader().getResources(resourceName);
				while (urlEnum.hasMoreElements()) {
					String urlPath = urlEnum.nextElement().getFile();
					urlPath = URLDecoder.decode(urlPath, "UTF-8");
					if (urlPath.startsWith("file:")) {
						urlPath = urlPath.substring(5);
					}
					if (urlPath.indexOf('!') > 0) {
						urlPath = urlPath.substring(0, urlPath.indexOf('!'));
					} else {
						File dirOrArchive = new File(urlPath);
						if (resourceName != null && resourceName.lastIndexOf('/') > 0) {
							//for META-INF/components.xml
							dirOrArchive = dirOrArchive.getParentFile();
						}
						urlPath = dirOrArchive.getParent();
					}
					paths.add(urlPath);
				}
			} catch (IOException ioe) {
				if (log.isWarnEnabled()) {
					log.warn("could not read: " + resourceName, ioe);
				}
			}
		}
		long finishTime = System.currentTimeMillis();
		if (log.isInfoEnabled()) {
			log.info("found " + paths.size() + " resources in " + (finishTime - startTime) + " ms");
		}

		startTime = System.currentTimeMillis();
		handle(paths);
		finishTime = System.currentTimeMillis();

		if (log.isInfoEnabled()) {
			log.info("handled all resources in " + (finishTime - startTime) + " ms");
		}

	}

	protected void handle(Set<String> paths) {
		for (String urlPath : paths) {
			long startTime = System.currentTimeMillis();
			try {
				if (log.isTraceEnabled()) {
					log.trace("scanning: " + urlPath);
				}
				File file = new File(urlPath);
				if (file.isDirectory()) {
					handleDirectory(file, null);
				} else if (file.isFile() && file.exists()) {
					handleArchiveByFile(file);
				} else {
					if (log.isWarnEnabled()) {
						log.warn("file not found:" + urlPath);
					}
				}
			} catch (IOException ioe) {
				if (log.isWarnEnabled()) {
					log.warn("could not read entries", ioe);
				}
			}
			long finishTime = System.currentTimeMillis();
			if (log.isInfoEnabled()) {
				log.info("loaded " + urlPath + " in " + (finishTime - startTime) + " ms");
			}
		}
	}

	private void handleArchiveByFile(File file) throws IOException {
		ZipFile zip = null;
		try {
			if (log.isTraceEnabled()) {
				log.trace("archive: " + file);
			}
			touchTimestamp(file);
			zip = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String name = entry.getName();
				if (omitPackage.acceptClass(name)) {
					handle(name);
				}
			}

		} catch (ZipException e) {
			throw new RuntimeException("Error handling file " + file, e);
		} finally {
			if (zip != null) {
				try {
					zip.close();
				} catch (IOException ignored) {
					//
				}
			}
		}
	}

	private void handleDirectory(File file, String path) {
		handleDirectory(file, path, new File[0]);
	}

	private void handleDirectory(File file, String path, File[] excludedDirectories) {
		for (File excludedDirectory : excludedDirectories) {
			if (file.equals(excludedDirectory)) {
				if (log.isTraceEnabled()) {
					log.trace("skipping excluded directory: " + file);
				}
				return;
			}
		}

		if (log.isTraceEnabled()) {
			log.trace("handling directory: " + file);
		}
		for (File child : file.listFiles()) {
			String newPath = path == null ? child.getName() : path + '/' + child.getName();
			if (child.isDirectory()) {
				if (omitPackage.acceptPackage(newPath)) {
					handleDirectory(child, newPath, excludedDirectories);
				}
			} else {
				if (handle(newPath)) {
					// only try to update the timestamp on this scanner if the file was actually handled
					touchTimestamp(child);
				}
			}
		}
	}

	private void touchTimestamp(File file) {
		if (file.lastModified() > timestamp) {
			timestamp = file.lastModified();
		}
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

}
