import {inject} from 'aurelia-framework'
import {Http,IService} from '../http'

@inject(Http)
export class List {
  api:Http
  services:Array<IService>=[]
  serviceNames:Array<String>=[]

  constructor(api){
    this.api=api
  }

  created(){
    this.api.getServices().then(services => {
      this.services=services
      this.services.forEach(service => {
        this.api.getServiceTitle(service).then(name => this.serviceNames.push(name))
      })
    })
  }
  test="Liste-Test"
}
