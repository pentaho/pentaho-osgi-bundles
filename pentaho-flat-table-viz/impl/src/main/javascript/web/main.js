define(
  [
    "pentaho/type/Context",
    "pentaho/data/Table",
    "pentaho-flat-table-viz-impl/flat-table-viz/model",
    "pentaho/type/configurationService"
  ], function(Context, Table, myModelFactory, configurationService) {
    "use strict";

    var context = new Context();

    var MyModel = context.get(myModelFactory);

    var model = new MyModel(
      {
        width: 800,
        height: 600,

        data: new Table(mockData(1000, 5))
      }
    );

    model.type.viewClass.then(function(MyView) {

      var output = document.getElementById("output");

      var view = new MyView(output, model);

      view.render();
    });

    function mockData(rowsNum, colsNum) {

      var dataset = {};

      dataset.model = [];
      for ( var c=0 ; c<colsNum ; c++ ) {
        dataset.model.push({name: "col_" + c, type: "number", label: "Col " + c});
      }

      dataset.rows = [];
      for ( var r=0 ; r<rowsNum ; r++ ) {

        var label = "Label " + randVal(10, rowsNum);
        dataset.rows.push({c: [{v: label, f: label}]});

        for (var i = 0; i < colsNum-1; i++) {
          dataset.rows[r]["c"].push(randVal(10, rowsNum));
        }
      }

      return dataset;
    }

    function randVal(min, max) {
      return Math.round(Math.random() * (max - min) + min);
    }
  });
