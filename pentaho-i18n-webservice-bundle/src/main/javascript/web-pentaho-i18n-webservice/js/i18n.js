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
  "pentaho/util/module",
  "whatwg-fetch",
  "pentaho/shim/es6-promise"
], function (MessageBundle, environment, moduleUtil) {

  "use strict";

  return {

    load: function(bundlePath, localRequire, onLoad, config) {
      if(config.isBuild) {
        // Indicate that the optimizer should not wait for this resource and complete optimization.
        // This resource will be resolved dynamically during run time in the web browser.
        onLoad();
        return;
      }

      var baseUrl = environment.server.services + "i18n";
      var locale = environment.locale;

      var resourceModuleId = "moduleID=" + getResourceModuleId(localRequire, bundlePath);

      var resourceLocale = locale !== null ? ("locale=" + locale) : "";
      var url = baseUrl + "?" + resourceModuleId + "&" + resourceLocale;

      var options = {
        method: "GET",
        headers: {
          "Accept": "application/json"
        }
      };

      var request = new Request(url, options);

      fetch(request)
        .then(function(res) {
          if (res.status === 200) {
            return res.text();
          }

          throw new Error("Error accessing i18n OSGI web service with bundlePath: " + resourceModuleId + "'.");
        })
        .then(function(data) {
          if (data) {
            var bundle = new MessageBundle(JSON.parse(data));
            onLoad(bundle);
          } else {
            onLoad();
          }
        })
        .catch(function(/* error */) {
          throw new Error("Error accessing i18n OSGI web service with bundlePath: " + resourceModuleId + "'.");
        });
    }
  };

  function getResourceModuleId(localRequire, bundlePath) {

    var callerModuleId = moduleUtil.getId(localRequire);

    /*
    TODO: Check if really needed / how to fix.
    // Hack for geo
    moduleId = moduleId && moduleId.replace("pentaho/geo/visual", "pentaho-geo-visual");
    */

    return moduleUtil.absolutizeIdRelativeToSibling(bundlePath, callerModuleId);
  }
});
