/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
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
