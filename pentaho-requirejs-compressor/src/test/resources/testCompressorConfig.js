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


{
  //appDir: "./libs",
  appDir: "./module-scripts",
  optimize: "uglify2",
  baseUrl: ".",
  dir: "../bin/scriptOutput",
  optimizeCss: "none",
  skipDirOptimize: true,
  //Put in a mapping so that 'requireLib' in the
  //modules section below will refer to the require.js
  //contents.
  paths: {
    requireLib: 'require',
    'dojo/selector/_loader' : "empty:",
    'dojo/query': 'empty:',
    'dojo/request': 'empty:',
    'dijit/layout/ContentPane': 'empty:',
    'dijit/Dialog' : 'empty:',
    'dojo/text' : 'analyzer/text',
    'dijit/layout/StackController': 'empty:',
    'dijit/layout/StackContainer': 'empty:',
    'dijit/layout/TabController': 'empty:',
    'dijit/form/ValidationTextBox':'empty:',
    'dijit/form/_ComboBoxMenuMixin':'empty:',
    'dijit/form/Select':'empty:',
    'dijit/ColorPalette':'empty:',
    'dojo/date/locale':'empty:'
  },


  mainConfigFile: 'requireCfg.js',
  modules: [
    {
      name: "require-compressed",
      include: ["requireLib"],
      create: true
    },
    {
      name: "oss-module",
      include: ["analyzer/oss-module"],
      create: true
    },
    {
      name: "analyzer-editor",
      include: ["analyzer/analyzer-editor"],
      create: true,
      exclude: [
        "analyzer/oss-module",
        'analyzer/viz-plugins'
      ]
    },
    {
      name: "analyzer-selectSchema",
      include: ["analyzer/analyzer-selectSchema"],
      create: true,
      excludeShallow: ['dojo/i18n!../nls/loading']
    },
    {
      name: "analyzer-viewer",
      include: ["analyzer/analyzer-viewer"],
      create: true,
      exclude: [
        "analyzer/oss-module",
        'analyzer/viz-plugins'
      ]
    }

  ]
}
