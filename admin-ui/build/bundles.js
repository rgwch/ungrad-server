module.exports = {
  "bundles": {
    "dist/app-build": {
      "includes": [
        "[**/*.js]",
        "**/*.html!text",
        "**/*.css!text",
        "materialize",
        "materialize-css",
        "text",
        "aurelia-materialize-bridge",
        "aurelia-materialize-bridge/**/*.js",
        "aurelia-materialize-bridge/**/*.html!text",
        "aurelia-materialize-bridge/**/*.css!text"
      ],
      "options": {
        "inject": true,
        "minify": true,
        "depCache": false,
        "rev": false
      }
    },/*
    "dist/materialize":{
      "includes":[
        "aurelia-materialize-bridge",
        "materialize-css",
        "materialize"
      ],
      "options":{
        "inject":true,
        "minify":true
      }
    },*/
    "dist/aurelia": {
      "includes": [
        "aurelia-framework",
        "aurelia-bootstrapper",
        "aurelia-fetch-client",
        "aurelia-router",
        "aurelia-animator-css",
        "aurelia-templating-binding",
        "aurelia-polyfills",
        "aurelia-templating-resources",
        "aurelia-templating-router",
        "aurelia-loader-default",
        "aurelia-history-browser",
        "aurelia-logging-console",
        "aurelia-http-client",
        "aurelia-event-aggregator",
        "fetch",
        "jquery"
      ],
      "options": {
        "inject": true,
        "minify": true,
        "depCache": false,
        "rev": false
      }
    }
  }
};
