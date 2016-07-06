// Find and inject tests using requirejs
var tests = Object.keys(window.__karma__.files).filter(function(file) {
  return (/.spec\.js$/).test(file);
});
var depDir = "target/dependency";
var depWebJars = depDir + "/META-INF/resources/webjars";
var src = "src/main/javascript/web";

requirejs.config({
  baseUrl: "/base",
  paths: {
    "css": depWebJars + "/require-css/${require-css.version}/css",
    "text": depWebJars + "/requirejs-text/${requirejs-text.version}/text",
    "pentaho": depDir + "/common-ui/resources/web/pentaho",
    "pentaho/i18n": src + "/lib/i18nMock",
    "flat-table-viz": src + "/flat-table-viz",

    "underscore": depWebJars + "/underscore/${underscore.version}/underscore",
    "jquery": depWebJars + "/jquery/${jquery.version}/jquery",
    "bootstrap-css": depWebJars + "/bootstrap-css/${bootstrap.version}/css/bootstrap",

    "datatables.net": depWebJars + "/datatables.net/${datatables.net.version}/js/jquery.dataTables",
    "datatables.net-bs": depWebJars + "/datatables.net-bs/${datatables.net-bs.version}/js/dataTables.bootstrap",
    "datatables.net-fixedheader": depWebJars + "/datatables.net-fixedheader/${datatables.net-fixedheader.version}/js/dataTables.fixedHeader",
    "datatables.net-fixedheader-bs": depWebJars + "/datatables.net-fixedheader-bs/${datatables.net-fixedheader-bs.version}",
    "datatables.net-scroller": depWebJars + "/datatables.net-scroller/${datatables.net-scroller.version}/js/dataTables.scroller",
    "datatables.net-scroller-bs": depWebJars + "/datatables.net-scroller-bs/${datatables.net-scroller-bs.version}",
    "datatables.net-colreorder": depWebJars + "/datatables.net-colreorder/${datatables.net-colreorder.version}/js/dataTables.colReorder",
    "datatables.net-colreorder-bs": depWebJars + "/datatables.net-colreorder-bs/${datatables.net-colreorder-bs.version}"
  },
  map: {
    "*": {
      "i18n": "pentaho/i18n",
      "pentaho/type/theme": "pentaho/type/themes/crystal"
    }
  },
  bundles: {},
  config: {
    service: {}
  },
  packages: [],
  deps: tests,
  callback: function() {
    window.__karma__.start();
  }
});
