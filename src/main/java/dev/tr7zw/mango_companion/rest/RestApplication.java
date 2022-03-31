package dev.tr7zw.mango_companion.rest;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.wicketstuff.rest.utils.mounting.PackageScanner;

public class RestApplication extends WebApplication {
  /** @see org.apache.wicket.Application#getHomePage() */
  @Override
  public Class<? extends WebPage> getHomePage() {
    return HomePage.class;
  }

  /** @see org.apache.wicket.Application#init() */
  @Override
  public void init() {
    super.init();

    PackageScanner.scanPackage("dev.tr7zw.mango_companion.rest.api");
  }
}
