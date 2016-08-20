import {inject} from 'aurelia-framework'
import {AppState} from '../appstate'
import {Http} from "../http";

@inject(Http,AppState)
export class ServiceList{
  link:Http
  cfg:AppState
  services:Array<Object>

  constructor(http,state){
    this.link=http
    this.cfg=state
    this.getServices()
  }

  public getServices=function(){
    this.link.get("/api/getServices", response => {
      if(response.responseType === 'json'){
        this.services=JSON.parse(response.response)
      }
    })
  }
}
