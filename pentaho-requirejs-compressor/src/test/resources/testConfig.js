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