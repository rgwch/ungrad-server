import {BindingEngine,inject,computedFrom} from 'aurelia-framework'
import {AppState,IService,IServiceParameter} from '../appstate'
import {EventAggregator} from 'aurelia-event-aggregator'
import {ServiceSelected} from '../messages'
import {Http} from '../http'
import {MdToastService} from 'aurelia-materialize-bridge';

@inject(EventAggregator, Http, MdToastService,BindingEngine)
export class ServiceDetail {
  serviceID:String = ""
  serviceCmd:String = ""
  serviceName:String = ""
  parameters:IServiceParameter[] = []
  originalParameters:IServiceParameter[] = []
  modified:Boolean=false

  constructor(private ea, private api, private toast, private binding) {
    ea.subscribe(ServiceSelected, msg => {
      //subscription.dispose()
      this.parameters = msg.service.params
      this.serviceID = msg.service.id
      this.serviceCmd = msg.service.address
      this.serviceName = msg.service.name
      this.originalParameters = []
      for(var i=0;i<this.parameters.length;i++){
        this.originalParameters[i]=this.clone(this.parameters[i])
      }
      /** observing arrays doesn't seem to work at this time. So we have to poll instead */
      //let subscription=this.binding.collectionObserver(this.parameters).subscribe(splices => console.log(splices))
    })

  }

  /**
   * Test if a property can be written to the server (used to enable/disable the "send" button)
   * @returns {boolean} true, if the property has the "writable" attribute set to true, and is different from its
   * original value.
   */
  //@computedFrom('parameters')
  get canWrite() {
    for (var i = 0; i < this.parameters.length; i++) {
      // console.log(this.parameters[i].value + " - " + this.originalParameters[i].value)
      if ((this.parameters[i].writable==true) && this.parameters[i].value != this.originalParameters[i].value) {
        return true
      }
    }
    return false
  }

  /**
   * Test if the currently displayed service has any writable properties (used to show/hide the "send" button)
   * @returns {boolean} true if at least one of the displayed properties is writable
     */
  get hasWritableProperties(){
    var found=(this.parameters.find(parm =>{
      return parm.writable
    }))
    if(found == undefined) {
      return false
    }else{
      return true
    }
  }

  /**
   * Check if a parameter is of the given type
   * @param typ type to check (all lowercase)
   * @param param the parameter in question
   * @returns {boolean} true if types match
     */
  isType(typ:String, param:IServiceParameter):Boolean {
    return (param.type.toLowerCase() == typ)
  }

  checkedState(param:IServiceParameter):Boolean {
    if (param.value === true) {
      return true
    } else {
      return false
    }
  }

  writeState(param:IServiceParameter):Boolean {
    if (param.writable === true) {
      return false
    } else {
      return true
    }
  }

  disabledState(param:IServiceParameter) {
    if (param.writable === true) {
      return "enabled"
    } else {
      return "disabled"
    }
  }

  /**
   * Fetch the value of a parameter from the server
   * @param param name of the parameter
   * @returns {string} the value
   */
  getValue(param: IServiceParameter) {
    param.value = "..loading.."
    this.api.getParameterValue(this.serviceID, param).then(result => {
     // console.log("result:" + JSON.stringify(result))
      param.value = result['value']
      this.originalParameters.forEach(parameter => {
        if(parameter.name==param.name){
          parameter['value']=param.value
          return
        }
      })
    })
  }

  /**
   * execute a command on the server
   * @param name name of the command
   * @returns a status message describung the success
   */
  run(name) {
    this.api.get(`/api/services/${this.serviceID}/exec/${name}`, result => {
      var ans = JSON.parse(result['response'])
      if (ans.status === "ok") {
        this.showSuccessToast("atc update")
      } else {

      }
      //this.ea.publish(new ServiceSelected(JSON.parse(result.response).answer))
    })
  }
  send(){
    let self=this
    for(let i=0;i<this.parameters.length;i++){
      if(this.parameters[i].value!=this.originalParameters[i].value){
        this.api.setParameterValue(this.serviceID,this.parameters[i]).then(result =>{
          if(result.status=="ok"){
            self.showSuccessToast(self.parameters[i].name)
          }
        })
      }
    }
  }

  /**
   * Reload all parameters of the current service
   */
  reload() {
    this.parameters.forEach(param => {
      this.getValue(param)
    })
  }

  showSuccessToast(msg) {
    this.toast.show('Success:' + msg, 4000, 'rounded blue');
  }

  hello() {
    alert("hello")
  }

  clone(obj) {
    if (null == obj || "object" != typeof obj) return obj;
    var copy = obj.constructor();
    for (var attr in obj) {
      if (obj.hasOwnProperty(attr)) copy[attr] = obj[attr];
    }
    return copy;
  }

}
