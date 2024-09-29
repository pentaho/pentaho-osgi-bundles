/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
