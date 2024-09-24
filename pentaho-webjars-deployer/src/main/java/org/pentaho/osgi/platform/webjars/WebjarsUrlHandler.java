/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.osgi.platform.webjars;

import org.osgi.service.url.AbstractURLStreamHandlerService;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by nbaker on 9/6/14.
 */
public class WebjarsUrlHandler extends AbstractURLStreamHandlerService {
  private boolean minificationEnabled;
  private final boolean automaticNonAmdShimConfigEnabled;

  public WebjarsUrlHandler( boolean minificationEnabled, boolean automaticNonAmdShimConfigEnabled ) {
    this.minificationEnabled = minificationEnabled;
    this.automaticNonAmdShimConfigEnabled = automaticNonAmdShimConfigEnabled;
  }

  @Override public URLConnection openConnection( URL url ) throws IOException {
    return new WebjarsURLConnection( new URL( url.getPath() ), this.minificationEnabled, this.automaticNonAmdShimConfigEnabled );
  }
}
