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
  "whatwg-fetch"
], function (MessageBundle, environment) {
  "use strict";

  return {

    load: function(bundlePath, localRequire, onLoad, config) {
      if(config.isBuild) {
        // Indicate that the optimizer should not wait for this resource and complete optimization.
        // This resource will be resolved dynamically during run time in the web browser.
        onLoad();
        return;
      }

      var baseUrl = environment.server.services;
      var locale = environment.locale;

      // TODO check if the config object as any useful info to use in the resource key
      var resourceKey = _resourceKey(localRequire, bundlePath);
      var resourceLocale = locale !== null ? locale : "en";

      var url = baseUrl + "i18n/" + resourceKey + "/" + resourceLocale;
      console.log("Url: " + url);

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

          throw new Error("Error accessing i18n OSGI web service with bundlePath: " + resourceKey + "'.");
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
          throw new Error("Error accessing i18n OSGI web service with bundlePath: " + resourceKey + "'.");
        });
    }
  };

  function _resourceKey(localRequire, bundlePath) {
    console.log( "Bundle Path: " + bundlePath);

    var isGlobalRequire = localRequire.undef !== undefined;
    // TODO how to make sure that bundle path is correct when we can't use 'require("module")'?
    var module = !isGlobalRequire ? localRequire("module") : null;
    if (module === null) return bundlePath;
    console.log("Module ID: " + module.id);

    var moduleIdSplit = module.id.split("/"); /* example: det_8.1-SNAPSHOT/path/to/module */

    var moduleAndVersion = moduleIdSplit.shift(); /* example: det_8.1-SNAPSHOT */
    var moduleName = moduleIdSplit.pop();         /* example: amd module */
    var pathToBundle = moduleIdSplit.join( "." ); /* example: path.to */

    // var resourceKey = bundlePath; /* moduleID.version || moduleID - version -> promote it out of key */
    // TODO I think something as to be done in order to merge pathToBundle with bundlePath
    return moduleAndVersion + "/" + pathToBundle + "." + bundlePath;
  }

});
