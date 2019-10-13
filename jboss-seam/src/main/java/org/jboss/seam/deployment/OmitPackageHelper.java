package org.jboss.seam.deployment;

import javax.servlet.ServletContext;

import org.jboss.seam.util.Strings;

public class OmitPackageHelper {

	public static final String KEY_OMIT_PACKAGES = "org.jboss.seam.deployment.OMIT_PACKAGES";

	public static OmitPackageHelper getInstance(ServletContext ctx) {

		if (ctx.getAttribute(KEY_OMIT_PACKAGES) == null) {
			String omittedPackageString = ctx.getInitParameter(KEY_OMIT_PACKAGES);
			String[] filter = null;
			if (Strings.isEmpty(omittedPackageString)) {
				filter = new String[0];
			} else {
				filter = omittedPackageString.split(",");
			}
			OmitPackageHelper instance = new OmitPackageHelper(filter);

			ctx.setAttribute(KEY_OMIT_PACKAGES, instance);
			return instance;
		}
		return (OmitPackageHelper) ctx.getAttribute(KEY_OMIT_PACKAGES);

	}

	private String[] ignoredPackages;

	private OmitPackageHelper(String[] ignored) {
		super();
		assert ignored != null;
		this.ignoredPackages = ignored;
	}

	public boolean acceptClass(String fullClassFileName) {
		if (Strings.isEmpty(fullClassFileName)) {
			return false;
		}
		if (ignoredPackages.length == 0) {
			return true;
		}
		int idx = fullClassFileName.lastIndexOf('/');

		String packageName = fullClassFileName;
		if (idx >= 0) {
			packageName = fullClassFileName.substring(0, idx);
		}
		return acceptPackage1(packageName);
	}

	public boolean acceptPackage(String packageName) {
		if (Strings.isEmpty(packageName)) {
			return true;
		}
		if (ignoredPackages.length == 0) {
			return true;
		}
		if (packageName.endsWith("/") || packageName.endsWith("\\")) {
			packageName = packageName.substring(0, packageName.length() - 1);
		}
		return acceptPackage1(packageName);
	}

	private boolean acceptPackage1(String packageName) {
		packageName = packageName.replace('/', '.').replace('\\', '.');
		for (String ignored : ignoredPackages) {
			if (packageName.startsWith(ignored)) {
				return false;
			}
		}
		return true;
	}
}
