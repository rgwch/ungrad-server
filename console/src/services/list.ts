import {inject} from 'aurelia-framework'
import {Http,IService} from '../http'
import {AppState} from '../appstate'

@inject(Http,AppState)
export class List {
  api:Http
  state:AppState
  services:Array<IService>=[]
  serviceNames:Array<String>=[]

  constructor(api,appstate){
    this.api=api
    this.state=appstate
  }

  created(){
    this.api.getServices().then(services => {
      this.services=services
      this.services.forEach(service => {
        this.api.getServiceTitle(service).then(name => service['name']=name)
      })
      this.state.selectedService=services[0]
    })
  }
  test="Liste-Test"
}
