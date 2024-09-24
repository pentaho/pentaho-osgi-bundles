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

var webjars = {
  path: function (module, file) {
    return module + "/" + file;
  }
}
var config, requirejs = {
  config: function (cfg) {
    config = cfg;
  }
}

{{EXTERNAL_CONFIG}};

function processConfig( ignored ){
  var result = JSON.stringify(config);
//  print(result);
  return result;
}



