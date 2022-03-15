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
 define(["pentaho/i18n/MessageBundle", "dojo/request"],
  function (MessageBundle, request) {
  "use strict";

  return {

    load: function(bundlePath, require, onLoad, config) {
      if(config.isBuild) {
        // Indicate that the optimizer should not wait for this resource and complete optimization.
        // This resource will be resolved dynamically during run time in the web browser.
        onLoad();
      } else {
        var baseUrl = CONTEXT_PATH && CONTEXT_PATH == '/' ? CONTEXT_PATH : CONTEXT_PATH + "osgi/";
        var locale = typeof SESSION_LOCALE !== "undefined" ? SESSION_LOCALE : "en";
        var url = baseUrl + "cxf/i18n/" + bundlePath + "/" + locale;
        var options = {
          "headers": {
            "Accept": "application/JSON"
          }
        };

        if ( typeof IS_RUNNING_ON_WEBSPOON_MODE !== 'undefined' && IS_RUNNING_ON_WEBSPOON_MODE !== null
            && IS_RUNNING_ON_WEBSPOON_MODE ) {
          baseUrl = CONTEXT_PATH;
          url = baseUrl + "/cxf/i18n/" + bundlePath + "/" + locale;
        }

        request(url, options).then(function (data) {
          if (data) {
            var bundle = new MessageBundle(JSON.parse(data));
            onLoad(bundle);
          } else {
            onLoad();
          }
        }, function (err) {
          throw new Error("Error accessing i18n OSGI web service with bundlePath: " + bundlePath + "'.");
        });
      }
    }
  };

});
