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
{
  baseUrl: '/Public/js',
  paths: {
    jquery: '../../Scripts/jquery-1.10.2.min',
    jqueryui: '../../Scripts/jquery-ui-1.10.2.min'
  },
  shim: {
    jqueryui: {
      deps: ['jquery'],
      exports: 'foobar'
//      init: function() {
//
//      }
    }
  },
  waitSeconds: 3
}