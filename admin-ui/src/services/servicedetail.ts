import {inject} from 'aurelia-framework'
import {AppState,IService,IServiceParameter} from '../appstate'
import {EventAggregator} from 'aurelia-event-aggregator'
import {ServiceSelected} from '../messages'
import {Http} from '../http'
import {MdToastService} from 'aurelia-materialize-bridge';

@inject(EventAggregator,Http,MdToastService)
export class ServiceDetail {
  serviceID:String
  serviceCmd:String
  serviceName:String
  parameters:IServiceParameter[]

  constructor(private ea, private api,private toast){
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

  getValue(param){
    param.value="..loading.."
    this.api.getParameterValue(this.serviceID,param).then(result => {
      console.log("result:"+JSON.stringify(result))
      param.value=result['value']
    })
    return ""
  }
  run(name){
    this.api.get(`/api/services/${this.serviceID}/exec/${name}`, result =>{
      var ans=JSON.parse(result.response)
      if(ans.status === "ok"){
        this.showSuccessToast("atc update")
      }else{

      }
      //this.ea.publish(new ServiceSelected(JSON.parse(result.response).answer))
    })
  }
  showSuccessToast(msg) {
    this.toast.show('Success:'+msg, 4000, 'rounded blue');
  }

}
