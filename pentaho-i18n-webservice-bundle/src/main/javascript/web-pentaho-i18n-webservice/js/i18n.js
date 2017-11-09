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

      var moduleInfo = getModuleInfo(localRequire, bundlePath);

      var contextAndResourceKey;
      if (moduleInfo !== null) {
        contextAndResourceKey = moduleInfo.context + "/" + moduleInfo.resourceKey;
      } else {
        contextAndResourceKey = bundlePath;
      }

      console.log(contextAndResourceKey);

      var resourceLocale = locale !== null ? locale : "en";
      var url = baseUrl + "i18n/" + contextAndResourceKey + "/" + resourceLocale;

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

          throw new Error("Error accessing i18n OSGI web service with bundlePath: " + contextAndResourceKey + "'.");
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
          throw new Error("Error accessing i18n OSGI web service with bundlePath: " + contextAndResourceKey + "'.");
        });
    }
  };

  function getModuleInfo(localRequire, bundlePath) {
    var isGlobalRequire = localRequire.undef !== undefined;

    var module = !isGlobalRequire ? localRequire("module") : null;
    if (module === null) return null;

    var moduleIdTokens = module.id
      .replace( "pentaho/geo/visual", "pentaho-geo-visual") // Hack for geo
      .split("/"); /* example: det_8.1-SNAPSHOT/path/to/module */

    var context = moduleIdTokens.shift();      /* example: det_8.1-SNAPSHOT */
    var name = moduleIdTokens.pop();           /* example: amd module */

    return {
      context: context,
      name: name,
      resourceKey: getResourceKey(moduleIdTokens, bundlePath)
    };


  }

  function getResourceKey(basePathTokens, bundlePath) {
    var isAbsoluteBundlePath =  !bundlePath.indexOf("/");

    // 'path', ./path' or '../path' are relative
    // '/path' is absolute
    var extractPathReg = /^(\.?\/|(?:\.{2}\/)*)(.+)$/;

    var match = extractPathReg.exec(bundlePath);
    var trail = match[1] || "";
    var path = match[2];

    if (!isAbsoluteBundlePath) {
      trail.split("/").map(function(elem) {
        if (elem === "..") basePathTokens.pop();
      });
    }

    if (isAbsoluteBundlePath || !basePathTokens.length || trail === "") {
      return path;
    }

    return basePathTokens.join(".") + "." + path;
  }
});
