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
  "pentaho/visual/base/model",
  "flat-table-viz/model"
], function(Context, BaseModel, VizModel) {

  "use strict";

  describe('VizModel', function() {

    var context, Model, dataSpec, props;

    beforeEach(function() {
      context = new Context();
      Model = context.get(VizModel);
      props = Model.type._props;
    });

    it("should be a function", function() {
      expect(typeof VizModel).toBe("function");
    });

    it("can be instantiated with a well-formed spec", function() {
      expect(function() {
        return new Model({
          width:    1,
          height:   1
        });
      }).not.toThrowError();
    });

    it("can be instantiated without arguments", function() {
      expect(function() {
        return new Model();
      }).not.toThrowError();
    });

    it('spec is valid if all required properties are defined', function() {
      expect(props).toContain(jasmine.objectContaining({name: 'fixedHeader'}));
      expect(props).toContain(jasmine.objectContaining({name: 'ordering'}));
      expect(props).toContain(jasmine.objectContaining({name: 'colReorder'}));
      expect(props).toContain(jasmine.objectContaining({name: 'scroller'}));
      expect(props).toContain(jasmine.objectContaining({name: 'scrollY'}));
    });
  });
});
