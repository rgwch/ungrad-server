import {Router, RouterConfiguration} from 'aurelia-router';

export class App {
  router: Router;

  configureRouter(config: RouterConfiguration, router: Router){
    config.title = 'Contacts';
    config.map([
      { route: '',              moduleId: 'login',   title: 'Login'},
      { route: '/ui/configure',  moduleId: 'configure', name:'configure' },
      { route: 'test',moduleId: 'test', name:'Test'}
    ]);

    this.router = router;

  }
}
