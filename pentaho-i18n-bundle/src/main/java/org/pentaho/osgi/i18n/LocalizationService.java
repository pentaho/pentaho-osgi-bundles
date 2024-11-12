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

package org.pentaho.osgi.i18n;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Created by bryan on 9/5/14.
 */
public interface LocalizationService {
  public ResourceBundle getResourceBundle( String name, Locale locale );
  public List<ResourceBundle> getResourceBundles( Pattern keyRegex, Locale locale );
}
