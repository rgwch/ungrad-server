import {inject} from 'aurelia-framework'
import {Http} from '../http'
import {AppState,IService} from '../appstate'
import {ServiceSelected} from '../messages'
import {EventAggregator} from 'aurelia-event-aggregator'

@inject(Http,AppState,EventAggregator)
export class List {
  services:Array<IService>=[]
  serviceNames:Array<String>=[]

  constructor(private api,private appstate, private ea){
  }

  created(){
    this.api.getServices().then(services => {
      this.services=services
      this.services.forEach(service => {
        this.api.getServiceTitle(service).then(name => service['name']=name)
      })
      this.appstate.selectedService=services[0]
      this.ea.publish(new ServiceSelected(services[0]))
    })
  }

  doSelect(service){
    this.ea.publish(new ServiceSelected(service))
  }
}
