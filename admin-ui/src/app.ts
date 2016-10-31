/**
 * Copyright (c) 2016 by G. Weirich
 */
import {Router, RouterConfiguration} from 'aurelia-router';

/**
 * This is the entry module of the application. Create and define the router.
 */
export class App {
  router:Router;

  configureRouter(config:RouterConfiguration, router:Router) {
    config.title = 'Contacts';
    config.map([
      {route: '', moduleId: 'login', name: 'login'},
      {route: 'configure', moduleId: 'services/servicedetail', name: 'configure'}
    ]);

    this.router = router;

  }
}
