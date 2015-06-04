package org.scriptbox.selenium.driver;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by david on 5/18/15.
 */
public enum DriverType implements Serializable {
	FIREFOX("firefox") {
		public RemoteWebDriver create(DriverOptions options) {
			if (options.getUrl() != null) {
				DesiredCapabilities cap = DesiredCapabilities.firefox();
				if (options.getProfile() != null) {
					cap.setCapability(FirefoxDriver.PROFILE, options.getProfile());
				}
				RemoteWebDriver driver = new RemoteWebDriver(options.getUrl(), cap);
				return (RemoteWebDriver) new Augmenter().augment(driver);
			} else {
				File profileDir = new File(options.getProfile());
				return new FirefoxDriver(new FirefoxProfile(profileDir));
			}
		}
	},
	CHROME("chrome") {
		public RemoteWebDriver create(DriverOptions options) {
            DesiredCapabilities cap = DesiredCapabilities.chrome();
			ChromeOptions co = new ChromeOptions();

			/*
            if( options.isIgnoreCertificateErrors() ) {
				cap.setCapability("chrome.switches", Arrays.asList("--ignore-certificate-errors") );
			}
			*/

			if( options.isIgnoreCertificateErrors() ) {
				co.addArguments("ignore-certificate-errors" );
			}

			File dir = options.getDownloadDirectory();
			if( dir != null ) {
				HashMap<String, Object> prefs = new HashMap<String, Object>();
				prefs.put("profile.default_content_settings.popups", 0);
				prefs.put("download.default_directory", dir.getAbsolutePath());

				co.setExperimentalOption("prefs", prefs);

				// co.addArguments("--test-type");
			}

			List<File> exts = options.getExtensions();
			if( exts != null ) {
				Logger logger = LoggerFactory.getLogger( DriverType.class );

				for (File file : exts) {
					if (file.exists()) {
						if (file.isDirectory()) {
							logger.debug( "chrome: adding extension directory: '" + file.getAbsolutePath() + "'");
							co.addArguments("load-extension=" + file.getAbsolutePath());
						}
						else {
							logger.debug( "chrome: adding extension file: '" + file.getAbsolutePath() + "'");
							co.addExtensions(file);
						}
					}
					else {
						logger.error("chrome: browser extension does not exist: '" + file.getAbsolutePath() + "'");
					}
				}
			}

            cap.setCapability(ChromeOptions.CAPABILITY, co);

			RemoteWebDriver driver = new RemoteWebDriver(options.getUrl(), cap);
			return (RemoteWebDriver) new Augmenter().augment(driver);
		}
	},
	IE("ie") {
		public RemoteWebDriver create(DriverOptions options) {
			DesiredCapabilities cap = DesiredCapabilities.internetExplorer();
			cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, options.isAcceptCertificates());
			RemoteWebDriver driver = new RemoteWebDriver(options.getUrl(), cap);
			return (RemoteWebDriver) new Augmenter().augment(driver);
		}
	};

	private String name;

	private DriverType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public RemoteWebDriver create(DriverOptions options) {
		return null;
	}

	public static DriverType getByName( String name ) {
		DriverType[] dts = DriverType.values();
		for (DriverType dt : dts) {
			if (dt.getName().equals(name)) {
				return dt;
			}
		}
		return null;
	}
}
