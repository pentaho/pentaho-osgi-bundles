var baseConfig = require('./karma.conf.js');

module.exports = function(config) {
  baseConfig(config);

  config.set({

    basePath: '${basedir}',

    frameworks: ['jasmine', 'requirejs'],

    plugins: [
      'karma-jasmine',
      'karma-requirejs',
      'karma-junit-reporter',
      'karma-html-reporter',
      'karma-coverage',
      'karma-phantomjs-launcher'
    ],

    reporters: ["progress", "junit", "coverage"],

    preprocessors: {
      '${basedir}/src/main/javascript/**/*.js': 'coverage'
    },

    junitReporter: {
      useBrowserName: false,
      outputFile: "target/js-reports/test-results.xml",
      suite: "unit"
    },

    coverageReporter: {
      useBrowserName: false,
      reporters: [
        {
          type: "html",
          dir: "${project.build.directory}/js-reports/jscoverage/html/"
        },
        {
          type: "cobertura",
          dir: "${project.build.directory}/js-reports/cobertura/xml/"
        }
      ],
      dir: "${project.build.directory}/js-reports/"
    },

    logLevel: config.LOG_INFO,

    autoWatch: false,

    browsers: ["PhantomJS"],

    singleRun: true
  });
};
