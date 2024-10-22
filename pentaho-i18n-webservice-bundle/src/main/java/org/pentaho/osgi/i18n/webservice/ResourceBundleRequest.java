/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.osgi.i18n.webservice;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by bryan on 12/8/14.
 */
@XmlRootElement
public class ResourceBundleRequest {
  private List<ResourceBundleWildcard> wildcards;
  private String locale;

  public String getLocale() {
    return locale;
  }

  public void setLocale( String locale ) {
    this.locale = locale;
  }

  public List<ResourceBundleWildcard> getWildcards() {
    return wildcards;
  }

  public void setWildcards( List<ResourceBundleWildcard> wildcards ) {
    this.wildcards = wildcards;
  }
}
