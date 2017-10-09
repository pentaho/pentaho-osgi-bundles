/*!
 * Copyright 2010 - 2017 Hitachi Vantara. All rights reserved.
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
 */

 /**
 * RequireJS loader plugin for loading localized messages via the OSGI i18n web service.
 */
define([
  "pentaho/i18n/MessageBundle",
  "pentaho/environment",
  "dojo/request"
], function (MessageBundle, environment, request) {
  "use strict";

  return {

    load: function(bundlePath, localRequire, onLoad, config) {
      if(config.isBuild) {
        // Indicate that the optimizer should not wait for this resource and complete optimization.
        // This resource will be resolved dynamically during run time in the web browser.
        onLoad();
      } else {
        var baseUrl = environment.server.services;
        var locale = environment.locale;

        // TODO prevent access outside bundle context?
        // TODO use localRequire to get module.id
        // Use solution above and avoid localRequire.toUrl
        // var fullUrl = localRequire.toUrl(bundlePath);

        var resourceKey = bundlePath; /* moduleID.version || moduleID - version -> promote it out of key */
        var resourceLocale = locale !== null ? locale : "en";

        var url = baseUrl + "i18n/" + resourceKey + "/" + resourceLocale;
        var options = {
          "headers": {
            "Accept": "application/JSON"
          }
        };

        request(url, options).then(function (data) {
          if (data) {
            var bundle = new MessageBundle(JSON.parse(data));
            onLoad(bundle);
          } else {
            onLoad();
          }
        }, function (/* error */) {
          throw new Error("Error accessing i18n OSGI web service with bundlePath: " + bundlePath + "'.");
        });
      }
    }
  };

});
