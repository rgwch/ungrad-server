import {inject} from 'aurelia-framework'
import {AppState,IService} from '../appstate'
import {EventAggregator} from 'aurelia-event-aggregator'
import {ServiceSelected} from '../messages'

@inject(EventAggregator)
export class ServiceDetail {
  serviceID:String
  serviceCmd:String
  serviceName:String


  constructor(private ea){
    ea.subscribe(ServiceSelected, msg =>{
      this.serviceID=msg.service.id
      this.serviceCmd=msg.service.address
      this.serviceName=msg.service.name
    })
  }

}
