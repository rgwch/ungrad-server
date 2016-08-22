import {inject} from 'aurelia-framework'
import {Http} from '../http'

@inject(Http)
export class List {
  api:Http
  services:Array<Object>=[]

  constructor(api){
    this.api=api
  }

  created(){
    this.api.getServices().then(services => {
      this.services=services
    })
  }
  test="Liste-Test"
}
