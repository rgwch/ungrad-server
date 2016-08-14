import {Router, RouterConfiguration} from 'aurelia-router';

export class App {
  router: Router;

  configureRouter(config: RouterConfiguration, router: Router){
    config.title = 'Contacts';
    config.map([
      { route: '',              moduleId: 'login',   title: 'Login'},
      { route: 'configure',  moduleId: 'configure', name:'Konfiguration' },
      { route: 'test',moduleId: 'test', name:'Test'}
    ]);

    this.router = router;

  }
}
