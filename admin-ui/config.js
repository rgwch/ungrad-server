System.config({
  defaultJSExtensions: true,
  transpiler: false,
  paths: {
    "*": "dist/*",
    "github:*": "jspm_packages/github/*",
    "npm:*": "jspm_packages/npm/*"
  },
  meta: {
    "bootstrap": {
      "deps": [
        "jquery"
      ]
    }
  },
  map: {
    "aurelia-animator-css": "npm:aurelia-animator-css@1.0.0",
    "aurelia-bootstrapper": "npm:aurelia-bootstrapper@1.0.0",
    "aurelia-event-aggregator": "npm:aurelia-event-aggregator@1.0.0",
    "aurelia-fetch-client": "npm:aurelia-fetch-client@1.0.0",
    "aurelia-framework": "npm:aurelia-framework@1.0.2",
    "aurelia-history-browser": "npm:aurelia-history-browser@1.0.0",
    "aurelia-http-client": "npm:aurelia-http-client@1.0.0",
    "aurelia-loader-default": "npm:aurelia-loader-default@1.0.0",
    "aurelia-logging-console": "npm:aurelia-logging-console@1.0.0",
    "aurelia-materialize-bridge": "npm:aurelia-materialize-bridge@0.14.0",
    "aurelia-pal": "npm:aurelia-pal@1.0.0",
    "aurelia-pal-browser": "npm:aurelia-pal-browser@1.0.0",
    "aurelia-polyfills": "npm:aurelia-polyfills@1.0.0",
    "aurelia-router": "npm:aurelia-router@1.0.2",
    "aurelia-templating-binding": "npm:aurelia-templating-binding@1.0.0",
    "aurelia-templating-resources": "npm:aurelia-templating-resources@1.0.0",
    "aurelia-templating-router": "npm:aurelia-templating-router@1.0.0",
    "bluebird": "npm:bluebird@3.4.1",
    "bootstrap": "github:twbs/bootstrap@3.3.7",
    "fetch": "github:github/fetch@1.0.0",
    "font-awesome": "npm:font-awesome@4.6.3",
    "jquery": "npm:jquery@2.2.4",
    "materialize": "npm:materialize@1.0.0",
    "materialize-css": "npm:materialize-css@0.97.7",
    "text": "github:systemjs/plugin-text@0.0.8",
    "github:Dogfalo/materialize@0.97.7": {
      "css": "github:systemjs/plugin-css@0.1.27",
      "jquery": "npm:jquery@2.2.4"
    },
    "github:jspm/nodelibs-assert@0.1.0": {
      "assert": "npm:assert@1.4.1"
    },
    "github:jspm/nodelibs-buffer@0.1.0": {
      "buffer": "npm:buffer@3.6.0"
    },
    "github:jspm/nodelibs-process@0.1.2": {
      "process": "npm:process@0.11.9"
    },
    "github:jspm/nodelibs-util@0.1.0": {
      "util": "npm:util@0.10.3"
    },
    "github:jspm/nodelibs-vm@0.1.0": {
      "vm-browserify": "npm:vm-browserify@0.0.4"
    },
    "github:twbs/bootstrap@3.3.7": {
      "jquery": "npm:jquery@2.2.4"
    },
    "npm:assert@1.4.1": {
      "assert": "github:jspm/nodelibs-assert@0.1.0",
      "buffer": "github:jspm/nodelibs-buffer@0.1.0",
      "process": "github:jspm/nodelibs-process@0.1.2",
      "util": "npm:util@0.10.3"
    },
    "npm:aurelia-animator-css@1.0.0": {
      "aurelia-metadata": "npm:aurelia-metadata@1.0.0",
      "aurelia-pal": "npm:aurelia-pal@1.0.0",
      "aurelia-templating": "npm:aurelia-templating@1.0.1"
    },
    "npm:aurelia-binding@1.0.3": {
      "aurelia-logging": "npm:aurelia-logging@1.0.0",
      "aurelia-metadata": "npm:aurelia-metadata@1.0.0",
      "aurelia-pal": "npm:aurelia-pal@1.0.0",
      "aurelia-task-queue": "npm:aurelia-task-queue@1.0.0"
    },
    "npm:aurelia-bootstrapper@1.0.0": {
      "aurelia-event-aggregator": "npm:aurelia-event-aggregator@1.0.0",
      "aurelia-framework": "npm:aurelia-framework@1.0.2",
      "aurelia-history": "npm:aurelia-history@1.0.0",
      "aurelia-history-browser": "npm:aurelia-history-browser@1.0.0",
      "aurelia-loader-default": "npm:aurelia-loader-default@1.0.0",
      "aurelia-logging-console": "npm:aurelia-logging-console@1.0.0",
      "aurelia-pal": "npm:aurelia-pal@1.0.0",
      "aurelia-pal-browser": "npm:aurelia-pal-browser@1.0.0",
      "aurelia-polyfills": "npm:aurelia-polyfills@1.0.0",
      "aurelia-router": "npm:aurelia-router@1.0.2",
      "aurelia-templating": "npm:aurelia-templating@1.0.1",
      "aurelia-templating-binding": "npm:aurelia-templating-binding@1.0.0",
      "aurelia-templating-resources": "npm:aurelia-templating-resources@1.0.0",
      "aurelia-templating-router": "npm:aurelia-templating-router@1.0.0"
    },
    "npm:aurelia-dependency-injection@1.0.0": {
      "aurelia-metadata": "npm:aurelia-metadata@1.0.0",
      "aurelia-pal": "npm:aurelia-pal@1.0.0"
    },
    "npm:aurelia-event-aggregator@1.0.0": {
      "aurelia-logging": "npm:aurelia-logging@1.0.0"
    },
    "npm:aurelia-framework@1.0.2": {
      "aurelia-binding": "npm:aurelia-binding@1.0.3",
      "aurelia-dependency-injection": "npm:aurelia-dependency-injection@1.0.0",
      "aurelia-loader": "npm:aurelia-loader@1.0.0",
      "aurelia-logging": "npm:aurelia-logging@1.0.0",
      "aurelia-metadata": "npm:aurelia-metadata@1.0.0",
      "aurelia-pal": "npm:aurelia-pal@1.0.0",
      "aurelia-path": "npm:aurelia-path@1.0.0",
      "aurelia-task-queue": "npm:aurelia-task-queue@1.0.0",
      "aurelia-templating": "npm:aurelia-templating@1.0.1"
    },
    "npm:aurelia-history-browser@1.0.0": {
      "aurelia-history": "npm:aurelia-history@1.0.0",
      "aurelia-pal": "npm:aurelia-pal@1.0.0"
    },
    "npm:aurelia-http-client@1.0.0": {
      "aurelia-pal": "npm:aurelia-pal@1.0.0",
      "aurelia-path": "npm:aurelia-path@1.0.0"
    },
    "npm:aurelia-loader-default@1.0.0": {
      "aurelia-loader": "npm:aurelia-loader@1.0.0",
      "aurelia-metadata": "npm:aurelia-metadata@1.0.0",
      "aurelia-pal": "npm:aurelia-pal@1.0.0"
    },
    "npm:aurelia-loader@1.0.0": {
      "aurelia-metadata": "npm:aurelia-metadata@1.0.0",
      "aurelia-path": "npm:aurelia-path@1.0.0"
    },
    "npm:aurelia-logging-console@1.0.0": {
      "aurelia-logging": "npm:aurelia-logging@1.0.0"
    },
    "npm:aurelia-materialize-bridge@0.14.0": {
      "aurelia-binding": "npm:aurelia-binding@1.0.3",
      "aurelia-dependency-injection": "npm:aurelia-dependency-injection@1.0.0",
      "aurelia-logging": "npm:aurelia-logging@1.0.0",
      "aurelia-metadata": "npm:aurelia-metadata@1.0.0",
      "aurelia-router": "npm:aurelia-router@1.0.2",
      "aurelia-task-queue": "npm:aurelia-task-queue@1.0.0",
      "aurelia-templating": "npm:aurelia-templating@1.0.1",
      "jquery": "npm:jquery@2.2.4",
      "materialize": "github:Dogfalo/materialize@0.97.7"
    },
    "npm:aurelia-metadata@1.0.0": {
      "aurelia-pal": "npm:aurelia-pal@1.0.0"
    },
    "npm:aurelia-pal-browser@1.0.0": {
      "aurelia-pal": "npm:aurelia-pal@1.0.0"
    },
    "npm:aurelia-polyfills@1.0.0": {
      "aurelia-pal": "npm:aurelia-pal@1.0.0"
    },
    "npm:aurelia-route-recognizer@1.0.0": {
      "aurelia-path": "npm:aurelia-path@1.0.0"
    },
    "npm:aurelia-router@1.0.2": {
      "aurelia-dependency-injection": "npm:aurelia-dependency-injection@1.0.0",
      "aurelia-event-aggregator": "npm:aurelia-event-aggregator@1.0.0",
      "aurelia-history": "npm:aurelia-history@1.0.0",
      "aurelia-logging": "npm:aurelia-logging@1.0.0",
      "aurelia-path": "npm:aurelia-path@1.0.0",
      "aurelia-route-recognizer": "npm:aurelia-route-recognizer@1.0.0"
    },
    "npm:aurelia-task-queue@1.0.0": {
      "aurelia-pal": "npm:aurelia-pal@1.0.0"
    },
    "npm:aurelia-templating-binding@1.0.0": {
      "aurelia-binding": "npm:aurelia-binding@1.0.3",
      "aurelia-logging": "npm:aurelia-logging@1.0.0",
      "aurelia-templating": "npm:aurelia-templating@1.0.1"
    },
    "npm:aurelia-templating-resources@1.0.0": {
      "aurelia-binding": "npm:aurelia-binding@1.0.3",
      "aurelia-dependency-injection": "npm:aurelia-dependency-injection@1.0.0",
      "aurelia-loader": "npm:aurelia-loader@1.0.0",
      "aurelia-logging": "npm:aurelia-logging@1.0.0",
      "aurelia-metadata": "npm:aurelia-metadata@1.0.0",
      "aurelia-pal": "npm:aurelia-pal@1.0.0",
      "aurelia-path": "npm:aurelia-path@1.0.0",
      "aurelia-task-queue": "npm:aurelia-task-queue@1.0.0",
      "aurelia-templating": "npm:aurelia-templating@1.0.1"
    },
    "npm:aurelia-templating-router@1.0.0": {
      "aurelia-dependency-injection": "npm:aurelia-dependency-injection@1.0.0",
      "aurelia-logging": "npm:aurelia-logging@1.0.0",
      "aurelia-metadata": "npm:aurelia-metadata@1.0.0",
      "aurelia-pal": "npm:aurelia-pal@1.0.0",
      "aurelia-path": "npm:aurelia-path@1.0.0",
      "aurelia-router": "npm:aurelia-router@1.0.2",
      "aurelia-templating": "npm:aurelia-templating@1.0.1"
    },
    "npm:aurelia-templating@1.0.1": {
      "aurelia-binding": "npm:aurelia-binding@1.0.3",
      "aurelia-dependency-injection": "npm:aurelia-dependency-injection@1.0.0",
      "aurelia-loader": "npm:aurelia-loader@1.0.0",
      "aurelia-logging": "npm:aurelia-logging@1.0.0",
      "aurelia-metadata": "npm:aurelia-metadata@1.0.0",
      "aurelia-pal": "npm:aurelia-pal@1.0.0",
      "aurelia-path": "npm:aurelia-path@1.0.0",
      "aurelia-task-queue": "npm:aurelia-task-queue@1.0.0"
    },
    "npm:bluebird@3.4.1": {
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:buffer@3.6.0": {
      "base64-js": "npm:base64-js@0.0.8",
      "child_process": "github:jspm/nodelibs-child_process@0.1.0",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "ieee754": "npm:ieee754@1.1.6",
      "isarray": "npm:isarray@1.0.0",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:font-awesome@4.6.3": {
      "css": "github:systemjs/plugin-css@0.1.27"
    },
    "npm:inherits@2.0.1": {
      "util": "github:jspm/nodelibs-util@0.1.0"
    },
    "npm:materialize-css@0.97.7": {
      "css": "github:systemjs/plugin-css@0.1.27",
      "jquery": "github:components/jquery@3.1.0"
    },
    "npm:materialize@1.0.0": {
      "assert": "github:jspm/nodelibs-assert@0.1.0",
      "util": "github:jspm/nodelibs-util@0.1.0"
    },
    "npm:process@0.11.9": {
      "assert": "github:jspm/nodelibs-assert@0.1.0",
      "fs": "github:jspm/nodelibs-fs@0.1.2",
      "vm": "github:jspm/nodelibs-vm@0.1.0"
    },
    "npm:util@0.10.3": {
      "inherits": "npm:inherits@2.0.1",
      "process": "github:jspm/nodelibs-process@0.1.2"
    },
    "npm:vm-browserify@0.0.4": {
      "indexof": "npm:indexof@0.0.1"
    }
  },
  depCache: {
    "npm:materialize-css@0.97.7.js": [
      "npm:materialize-css@0.97.7/dist/js/materialize"
    ],
    "npm:materialize-css@0.97.7/dist/js/materialize.js": [
      "jquery",
      "../css/materialize.css!"
    ],
    "github:components/jquery@3.1.0.js": [
      "github:components/jquery@3.1.0/jquery.js"
    ],
    "npm:materialize@1.0.0.js": [
      "npm:materialize@1.0.0/index.js"
    ],
    "http.js": [
      "aurelia-http-client"
    ],
    "login.js": [
      "aurelia-framework",
      "./appstate",
      "./http",
      "aurelia-router"
    ],
    "main.js": [
      "materialize-css"
    ],
    "services/list.js": [
      "aurelia-framework",
      "../http",
      "../appstate",
      "../messages",
      "aurelia-event-aggregator"
    ],
    "services/servicedetail.js": [
      "aurelia-framework",
      "aurelia-event-aggregator",
      "../messages",
      "../http",
      "aurelia-materialize-bridge"
    ],
    "users.js": [
      "aurelia-framework",
      "aurelia-fetch-client",
      "fetch"
    ]
  },
  bundles: {
    "app-build.js": [
      "app-colors.html!github:systemjs/plugin-text@0.0.8.js",
      "app.html!github:systemjs/plugin-text@0.0.8.js",
      "app.js",
      "appstate.js",
      "css/ungrad.css!github:systemjs/plugin-text@0.0.8.js",
      "github:components/jquery@3.1.0.js",
      "github:components/jquery@3.1.0/jquery.js",
      "http.js",
      "login.html!github:systemjs/plugin-text@0.0.8.js",
      "login.js",
      "main.js",
      "messages.js",
      "nav-bar.html!github:systemjs/plugin-text@0.0.8.js",
      "npm:materialize-css@0.97.7.js",
      "npm:materialize-css@0.97.7/dist/css/materialize.css!github:systemjs/plugin-css@0.1.27.js",
      "npm:materialize-css@0.97.7/dist/js/materialize.js",
      "npm:materialize@1.0.0.js",
      "npm:materialize@1.0.0/index.js",
      "services/list.html!github:systemjs/plugin-text@0.0.8.js",
      "services/list.js",
      "services/servicedetail.html!github:systemjs/plugin-text@0.0.8.js",
      "services/servicedetail.js",
      "users.html!github:systemjs/plugin-text@0.0.8.js",
      "users.js"
    ],
    "aurelia.js": [
      "github:github/fetch@1.0.0.js",
      "github:github/fetch@1.0.0/fetch.js",
      "npm:aurelia-animator-css@1.0.0.js",
      "npm:aurelia-animator-css@1.0.0/aurelia-animator-css.js",
      "npm:aurelia-binding@1.0.3.js",
      "npm:aurelia-binding@1.0.3/aurelia-binding.js",
      "npm:aurelia-bootstrapper@1.0.0.js",
      "npm:aurelia-bootstrapper@1.0.0/aurelia-bootstrapper.js",
      "npm:aurelia-dependency-injection@1.0.0.js",
      "npm:aurelia-dependency-injection@1.0.0/aurelia-dependency-injection.js",
      "npm:aurelia-event-aggregator@1.0.0.js",
      "npm:aurelia-event-aggregator@1.0.0/aurelia-event-aggregator.js",
      "npm:aurelia-fetch-client@1.0.0.js",
      "npm:aurelia-fetch-client@1.0.0/aurelia-fetch-client.js",
      "npm:aurelia-framework@1.0.2.js",
      "npm:aurelia-framework@1.0.2/aurelia-framework.js",
      "npm:aurelia-history-browser@1.0.0.js",
      "npm:aurelia-history-browser@1.0.0/aurelia-history-browser.js",
      "npm:aurelia-history@1.0.0.js",
      "npm:aurelia-history@1.0.0/aurelia-history.js",
      "npm:aurelia-loader-default@1.0.0.js",
      "npm:aurelia-loader-default@1.0.0/aurelia-loader-default.js",
      "npm:aurelia-loader@1.0.0.js",
      "npm:aurelia-loader@1.0.0/aurelia-loader.js",
      "npm:aurelia-logging-console@1.0.0.js",
      "npm:aurelia-logging-console@1.0.0/aurelia-logging-console.js",
      "npm:aurelia-logging@1.0.0.js",
      "npm:aurelia-logging@1.0.0/aurelia-logging.js",
      "npm:aurelia-materialize-bridge@0.14.0.js",
      "npm:aurelia-materialize-bridge@0.14.0/autocomplete/autocomplete.js",
      "npm:aurelia-materialize-bridge@0.14.0/badge/badge.js",
      "npm:aurelia-materialize-bridge@0.14.0/box/box.js",
      "npm:aurelia-materialize-bridge@0.14.0/breadcrumbs/breadcrumbs.js",
      "npm:aurelia-materialize-bridge@0.14.0/breadcrumbs/instructionFilter.js",
      "npm:aurelia-materialize-bridge@0.14.0/button/button.js",
      "npm:aurelia-materialize-bridge@0.14.0/card/card.js",
      "npm:aurelia-materialize-bridge@0.14.0/carousel/carousel-item.js",
      "npm:aurelia-materialize-bridge@0.14.0/carousel/carousel.js",
      "npm:aurelia-materialize-bridge@0.14.0/char-counter/char-counter.js",
      "npm:aurelia-materialize-bridge@0.14.0/checkbox/checkbox.js",
      "npm:aurelia-materialize-bridge@0.14.0/chip/chip.js",
      "npm:aurelia-materialize-bridge@0.14.0/chip/chips.js",
      "npm:aurelia-materialize-bridge@0.14.0/collapsible/collapsible.js",
      "npm:aurelia-materialize-bridge@0.14.0/collection/collection-header.js",
      "npm:aurelia-materialize-bridge@0.14.0/collection/collection-item.js",
      "npm:aurelia-materialize-bridge@0.14.0/collection/collection.js",
      "npm:aurelia-materialize-bridge@0.14.0/collection/md-collection-selector.js",
      "npm:aurelia-materialize-bridge@0.14.0/colors/colorValueConverters.js",
      "npm:aurelia-materialize-bridge@0.14.0/common/attributeManager.js",
      "npm:aurelia-materialize-bridge@0.14.0/common/attributes.js",
      "npm:aurelia-materialize-bridge@0.14.0/common/constants.js",
      "npm:aurelia-materialize-bridge@0.14.0/common/events.js",
      "npm:aurelia-materialize-bridge@0.14.0/config-builder.js",
      "npm:aurelia-materialize-bridge@0.14.0/datepicker/datepicker.default-parser.js",
      "npm:aurelia-materialize-bridge@0.14.0/datepicker/datepicker.js",
      "npm:aurelia-materialize-bridge@0.14.0/dropdown/dropdown-element.js",
      "npm:aurelia-materialize-bridge@0.14.0/dropdown/dropdown.js",
      "npm:aurelia-materialize-bridge@0.14.0/fab/fab.js",
      "npm:aurelia-materialize-bridge@0.14.0/file/file.js",
      "npm:aurelia-materialize-bridge@0.14.0/footer/footer.js",
      "npm:aurelia-materialize-bridge@0.14.0/index.js",
      "npm:aurelia-materialize-bridge@0.14.0/input/input-prefix.js",
      "npm:aurelia-materialize-bridge@0.14.0/input/input-update-service.js",
      "npm:aurelia-materialize-bridge@0.14.0/input/input.js",
      "npm:aurelia-materialize-bridge@0.14.0/modal/modal-trigger.js",
      "npm:aurelia-materialize-bridge@0.14.0/navbar/navbar.js",
      "npm:aurelia-materialize-bridge@0.14.0/pagination/pagination.js",
      "npm:aurelia-materialize-bridge@0.14.0/parallax/parallax.js",
      "npm:aurelia-materialize-bridge@0.14.0/progress/progress.js",
      "npm:aurelia-materialize-bridge@0.14.0/pushpin/pushpin.js",
      "npm:aurelia-materialize-bridge@0.14.0/radio/radio.js",
      "npm:aurelia-materialize-bridge@0.14.0/range/range.js",
      "npm:aurelia-materialize-bridge@0.14.0/scrollfire/scrollfire-patch.js",
      "npm:aurelia-materialize-bridge@0.14.0/scrollfire/scrollfire-target.js",
      "npm:aurelia-materialize-bridge@0.14.0/scrollfire/scrollfire.js",
      "npm:aurelia-materialize-bridge@0.14.0/scrollspy/scrollspy.js",
      "npm:aurelia-materialize-bridge@0.14.0/select/select.js",
      "npm:aurelia-materialize-bridge@0.14.0/sidenav/sidenav-collapse.js",
      "npm:aurelia-materialize-bridge@0.14.0/sidenav/sidenav.js",
      "npm:aurelia-materialize-bridge@0.14.0/slider/slider.js",
      "npm:aurelia-materialize-bridge@0.14.0/switch/switch.js",
      "npm:aurelia-materialize-bridge@0.14.0/tabs/tabs.js",
      "npm:aurelia-materialize-bridge@0.14.0/toast/toastService.js",
      "npm:aurelia-materialize-bridge@0.14.0/tooltip/tooltip.js",
      "npm:aurelia-materialize-bridge@0.14.0/transitions/fadein-image.js",
      "npm:aurelia-materialize-bridge@0.14.0/transitions/staggered-list.js",
      "npm:aurelia-materialize-bridge@0.14.0/validation/validationRenderer.js",
      "npm:aurelia-materialize-bridge@0.14.0/waves/waves.js",
      "npm:aurelia-metadata@1.0.0.js",
      "npm:aurelia-metadata@1.0.0/aurelia-metadata.js",
      "npm:aurelia-pal-browser@1.0.0.js",
      "npm:aurelia-pal-browser@1.0.0/aurelia-pal-browser.js",
      "npm:aurelia-pal@1.0.0.js",
      "npm:aurelia-pal@1.0.0/aurelia-pal.js",
      "npm:aurelia-path@1.0.0.js",
      "npm:aurelia-path@1.0.0/aurelia-path.js",
      "npm:aurelia-polyfills@1.0.0.js",
      "npm:aurelia-polyfills@1.0.0/aurelia-polyfills.js",
      "npm:aurelia-route-recognizer@1.0.0.js",
      "npm:aurelia-route-recognizer@1.0.0/aurelia-route-recognizer.js",
      "npm:aurelia-router@1.0.2.js",
      "npm:aurelia-router@1.0.2/aurelia-router.js",
      "npm:aurelia-task-queue@1.0.0.js",
      "npm:aurelia-task-queue@1.0.0/aurelia-task-queue.js",
      "npm:aurelia-templating-binding@1.0.0.js",
      "npm:aurelia-templating-binding@1.0.0/aurelia-templating-binding.js",
      "npm:aurelia-templating-resources@1.0.0.js",
      "npm:aurelia-templating-resources@1.0.0/abstract-repeater.js",
      "npm:aurelia-templating-resources@1.0.0/analyze-view-factory.js",
      "npm:aurelia-templating-resources@1.0.0/array-repeat-strategy.js",
      "npm:aurelia-templating-resources@1.0.0/aurelia-hide-style.js",
      "npm:aurelia-templating-resources@1.0.0/aurelia-templating-resources.js",
      "npm:aurelia-templating-resources@1.0.0/binding-mode-behaviors.js",
      "npm:aurelia-templating-resources@1.0.0/binding-signaler.js",
      "npm:aurelia-templating-resources@1.0.0/compose.js",
      "npm:aurelia-templating-resources@1.0.0/css-resource.js",
      "npm:aurelia-templating-resources@1.0.0/debounce-binding-behavior.js",
      "npm:aurelia-templating-resources@1.0.0/dynamic-element.js",
      "npm:aurelia-templating-resources@1.0.0/focus.js",
      "npm:aurelia-templating-resources@1.0.0/hide.js",
      "npm:aurelia-templating-resources@1.0.0/html-resource-plugin.js",
      "npm:aurelia-templating-resources@1.0.0/html-sanitizer.js",
      "npm:aurelia-templating-resources@1.0.0/if.js",
      "npm:aurelia-templating-resources@1.0.0/map-repeat-strategy.js",
      "npm:aurelia-templating-resources@1.0.0/null-repeat-strategy.js",
      "npm:aurelia-templating-resources@1.0.0/number-repeat-strategy.js",
      "npm:aurelia-templating-resources@1.0.0/repeat-strategy-locator.js",
      "npm:aurelia-templating-resources@1.0.0/repeat-utilities.js",
      "npm:aurelia-templating-resources@1.0.0/repeat.js",
      "npm:aurelia-templating-resources@1.0.0/replaceable.js",
      "npm:aurelia-templating-resources@1.0.0/sanitize-html.js",
      "npm:aurelia-templating-resources@1.0.0/set-repeat-strategy.js",
      "npm:aurelia-templating-resources@1.0.0/show.js",
      "npm:aurelia-templating-resources@1.0.0/signal-binding-behavior.js",
      "npm:aurelia-templating-resources@1.0.0/throttle-binding-behavior.js",
      "npm:aurelia-templating-resources@1.0.0/update-trigger-binding-behavior.js",
      "npm:aurelia-templating-resources@1.0.0/with.js",
      "npm:aurelia-templating-router@1.0.0.js",
      "npm:aurelia-templating-router@1.0.0/aurelia-templating-router.js",
      "npm:aurelia-templating-router@1.0.0/route-href.js",
      "npm:aurelia-templating-router@1.0.0/route-loader.js",
      "npm:aurelia-templating-router@1.0.0/router-view.js",
      "npm:aurelia-templating@1.0.1.js",
      "npm:aurelia-templating@1.0.1/aurelia-templating.js",
      "npm:jquery@2.2.4.js",
      "npm:jquery@2.2.4/dist/jquery.js"
    ]
  }
});