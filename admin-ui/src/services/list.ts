import {inject} from 'aurelia-framework'
import {Http} from '../http'
import {AppState,IService} from '../appstate'
import {ServiceSelected} from '../messages'
import {EventAggregator} from 'aurelia-event-aggregator'

@inject(Http,AppState,EventAggregator)
export class List {
  services:Array<IService>=[]
  running:Boolean=true

  constructor(private api,private appstate, private ea){
  }

  /**
   * Fetch a service list from the server and sort it alphabetically
   */
  created(){
    console.log("created; "+this.appstate.loggedIn)
    this.api.getServices().then(services => {
      this.services=services.sort((a,b) =>{
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
  }

  get loggedIn(){
    if(this.appstate.loggedIn || (typeof(this.appstate.username) != 'undefined' )){
      return true
    }else{
      return false
    }
  }

  doSelect(service){
    this.appstate.selectedService=service
    this.ea.publish(new ServiceSelected(service))
  }

  attached(){
    console.log("attached; "+this.appstate.loggedIn)
   this.doSelect(this.services[0])
  }
}
