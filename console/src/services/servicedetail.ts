import {inject} from 'aurelia-framework'
import {AppState,IService,IServiceParameter} from '../appstate'
import {EventAggregator} from 'aurelia-event-aggregator'
import {ServiceSelected} from '../messages'
import {Http} from '../http'

@inject(EventAggregator,Http)
export class ServiceDetail {
  serviceID:String
  serviceCmd:String
  serviceName:String
  parameters:IServiceParameter[]

  constructor(private ea, private api){
    ea.subscribe(ServiceSelected, msg =>{
      this.parameters=[]
      api.getServiceParams(msg.service).then(result =>{
        this.parameters=result.result
      })
      this.serviceID=msg.service.id
      this.serviceCmd=msg.service.address
      this.serviceName=msg.service.name
    })
  }

}
