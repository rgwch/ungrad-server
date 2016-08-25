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
      this.parameters=msg.service.params
      this.serviceID=msg.service.id
      this.serviceCmd=msg.service.address
      this.serviceName=msg.service.name
    })
  }

  isType(typ:String, param:IServiceParameter):Boolean{
    return(param.type.toLowerCase()==typ)
  }
  checkedState(param:IServiceParameter) : Boolean{
    if(param.value===true){
      return true
    }else{
      return false
    }
  }
  writeState(param:IServiceParameter):Boolean{
    if(param.writable===true){
      return false
    }else{
      return true
    }
  }
  disabledState(param:IServiceParameter){
    if(param.writable===true){
      return "enabled"
    }else{
      return "disabled"
    }
  }
}
