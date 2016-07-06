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
  "pentaho/visual/base/View",
  "underscore",
  "jquery",
  "datatables.net",
  "datatables.net-fixedheader",
  "datatables.net-colreorder",
  "datatables.net-scroller"
], function(BaseView, _, $) {
  "use strict";

  return BaseView.extend({

    /** @override */
    _init: function() {
      this.base();

      this.tElement = $('<table class="table table-striped table-bordered nowrap" cellspacing="0" width="100%">');

      $(this._element).empty();
      $(this._element).append(this.tElement);
    },

    /** @override */
    _render: function() {

      var cols = this.model.getv("columns");
      var fCols = filterCols(cols._values.attributes._elems);

      var data = this.model.getv("data");
      var tData = parseTableData(data, fCols);

      var iScroll = this.model.getv("scroller");
      var fHeader = this.model.getv("fixedHeader");

      if(this.dTable)this.dTable.destroy();
      this.tElement.empty();

      if(!tData.columns.length) return;
      this.dTable = this.tElement.DataTable({

        data:             tData.data,
        columns:          tData.columns,

        // Built-in features not exposed and disabled by default
        paging:           iScroll ? true : false,
        info:             false,
        filter:           false,

        deferRender:      iScroll ? true : false,
        scrollCollapse:   iScroll ? true : false,

        // Features exposed on viz model
        fixedHeader:      iScroll ? false : fHeader,
        ordering:         this.model.getv("ordering"),
        colReorder:       this.model.getv("colReorder"),
        scroller:         this.model.getv("scroller"),
        scrollY:          this.model.getv("scrollY")
      });

      this.dTable.colReorder.order( tData.order );
      this._resize();
    },

    /** @override */
    _resize: function() {

      var w  = this.model.getv("width");
      var h = this.model.getv("height");

      $(this._element).css({ width: w, height: h });
      this.dTable.columns.adjust().draw(false);
    },

    /** @override */
    dispose: function() {
      this.base();
    }
  });

  function filterCols(cols) {

    var filteredCols = [];

    _.each(cols, function(col, c){
      filteredCols.push(col._values.name._value);
    });

    return filteredCols;
  }

  function parseTableData(data, filteredCols) {

    var tData = { data: [], columns: [], order: [] };

    _.each(filteredCols, function(col){
      _.each(data.model.attributes, function(attr, a){
        if(attr.name == col) tData.order.push(a);
      });
    });

    _.each(data.model.attributes, function(attr, a){
      var cTitle = attr.name;
      var cVisible = !filteredCols.length || filteredCols.indexOf(cTitle) !== -1 ? true : false;
      tData.columns.push({title: cTitle, visible: cVisible});
      if(tData.order.indexOf(a) == -1) tData.order.push(a);
    });

    _.each(data.implem.rows, function(row, r){
      var rData = _.map(row.c, function(cData, c){ return cData.v; });
      tData.data.push(rData);
    });

    return tData;
  }
  });
