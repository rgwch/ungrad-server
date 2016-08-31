import {HttpClient} from 'aurelia-http-client';
import {RequestMessage} from "aurelia-http-client/dist/aurelia-http-client";
import {IService} from './appstate'
import {IServiceParameter} from "./appstate";



export class Http {
  private client:HttpClient

  constructor() {
    this.client = new HttpClient()
      .configure(x => {
        x.withBaseUrl(location.origin)
      })
  }

  get(url:string, callback) {
    this.client.get(url).then(result => callback(result))
  }

  post(url:string, params:string, callback) {

    this.client.post(url, params).then(result => callback(result)).catch(error => {
      callback(error)
    })

  }


  getServices():Promise<Array<IService>> {
    return new Promise(resolve => {
      this.get("/api/getServices", function (response) {
        resolve(JSON.parse(response.response))
      })
    })
  }

  getParameterValue(serviceID:String, param:IServiceParameter){
    return new Promise(resolve =>{
      this.get(`/api/services/${serviceID}/getParam/${param.name}`, function(response){
        resolve(JSON.parse(response.response))
      })
    })

  }

}
