/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
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
define([
  "require",
  "pentaho/visual/base/model",
  "flat-table-viz/view"
], function(req, baseModelFactory, view) {
  "use strict";

  var moduleId = req.toUrl('flat-table-module-id').substring(1);
  console.log("Module ID -", moduleId);

  return function(context) {

    var BaseModel = context.get(baseModelFactory);
    return BaseModel.extend({
      type: {
        id: moduleId,
        view: view,
        label: 'Flat Table',
        isBrowsable: true,
        isAbstract: false,
        props: [
          {
            name: "fixedHeader",
            type: "boolean",
            value: true
          },
          {
            name: "ordering",
            type: "boolean",
            value: true
          },
          {
            name: "colReorder",
            type: "boolean",
            value: true
          },
          {
            name: "scroller",
            type: "boolean",
            value: true
          },
          {
            name: "scrollY",
            type: "number",
            value: 400
          },
          {
            header: 'Columns',
            description: 'Drag and drop here the fields that you want to see in the table.',
            name: "columns",
            type: "pentaho/visual/role/ordinal"
          }
        ]
      }
    });
  };
});
