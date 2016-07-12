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
  "pentaho/type/Context",
  "pentaho/data/Table",
  "pentaho/visual/base/View",
  "flat-table-viz/view",
  "flat-table-viz/model"
], function(Context, Table, BaseView, VizView, VizModel) {
  "use strict";

  describe("VizView", function() {

    var context = new Context();
    var Model = context.get(VizModel);

    var tableSpec = {
      model: [
        {name: "foo", type: "string", label: "Foo"}
      ],
      rows: [
        {c: [{v: "Col_1"}]},
        {c: [{v: "Col_2"}]}
      ]
    };

    var model = new Model({
      width:    100,
      height:   100,
      columns: {attributes: [{name: "foo"}]},
      data:     new Table(tableSpec)
    });

    it("should be a function", function() {
      expect(typeof VizView).toBe("function");
    });

    it("should be a sub-class of 'base.View'", function() {
      expect(VizView.prototype instanceof BaseView).toBe(true);
    });

    it("should be possible to create an instance given an element and a model", function() {
      var elem = document.createElement("div");
      var view = new VizView(elem, model);
    });

    it("should be possible to render an instance", function(done) {
      var elem = document.createElement("div");
      var view = new VizView(elem, model);

      spyOn(view, "_render");

      view.render().then(function() {
        expect(view._render).toHaveBeenCalled();
        done();
      }, done.fail);
    });

    it("should be able to access the required properties exposed by the model", function() {
      expect(model.getv("fixedHeader")).toBeDefined();
      expect(model.getv("ordering")).toBeDefined();
      expect(model.getv("colReorder")).toBeDefined();
      expect(model.getv("scroller")).toBeDefined();
      expect(model.getv("scrollY")).toBeDefined();
    });

    describe("DataTables", function() {

      it("should call DataTable with the given params", function() {
        var data = { data: {} };
        var dataTablesSpy = spyOn($.fn, "DataTable").and.callFake(function(props) {
          expect(props).toEqual(data);
        });
        $.fn.DataTable(data);
        expect(dataTablesSpy).toHaveBeenCalledWith(data);
      });
    });
  });
});
