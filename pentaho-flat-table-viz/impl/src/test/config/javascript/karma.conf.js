module.exports = function(config) {
  config.set({

    basePath: '${basedir}',

    frameworks: ['jasmine', 'requirejs'],

    plugins: [
      'karma-jasmine',
      'karma-requirejs',
      'karma-chrome-launcher',
      'karma-mocha-reporter'
    ],

    files: [
      {pattern: '${project.build.directory}/dependency/*/**/*.+(js|css|properties|map)', included: false},
      {pattern: '${basedir}/src/main/javascript/**/*.+(js|css|html)', included: false},
      {pattern: '${basedir}/src/test/javascript/**/*.js', included: false},

      '${project.build.directory}/context.js'
    ],

    reporters: ["mocha"],

    colors: true,

    logLevel: config.LOG_INFO,

    autoWatch: true,

    browsers: ["Chrome"]
  });
};
