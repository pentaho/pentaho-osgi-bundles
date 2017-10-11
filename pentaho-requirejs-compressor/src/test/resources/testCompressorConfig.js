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
 * Copyright 2014 - 2017 Hitachi Vantara. All rights reserved.
 */

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
