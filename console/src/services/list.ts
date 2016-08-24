import {inject} from 'aurelia-framework'
import {Http} from '../http'
import {AppState,IService} from '../appstate'
import {ServiceSelected} from '../messages'
import {EventAggregator} from 'aurelia-event-aggregator'

@inject(Http,AppState,EventAggregator)
export class List {
  services:Array<IService>=[]

  constructor(private api,private appstate, private ea){
  }

  created(){
    this.api.getServices().then(services => {
      this.services=services
      this.services.forEach(service => {
        this.api.getServiceTitle(service).then(name => service['name']=name)
        this.services.sort((a,b)=>{
          if((typeof(a.name) == 'undefined') || (typeof(b.name)== 'undefined')){
            return 0;
          }
          if(a.name.toLowerCase()< b.name.toLowerCase()) {
            return -1
          } else {
            return 1
          }
        })
      })
      this.appstate.selectedService=services[0]
      this.ea.publish(new ServiceSelected(services[0]))
    })
  }

  doSelect(service){
    this.appstate.selectedService=service
    this.ea.publish(new ServiceSelected(service))
  }

}
