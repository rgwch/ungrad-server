import {HttpClient} from 'aurelia-http-client';
import {RequestMessage} from "aurelia-http-client/dist/aurelia-http-client";
import {IService} from './appstate'



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

  getServiceTitle(srv:IService):Promise<String> {
    return new Promise(resolve => {
      this.get(`/api/services/${srv.id}/getServiceName/dummy`, function (response) {
        resolve(JSON.parse(response.response).name)
      })
    })
  }

  getServiceParams(srv:IService):Promise<Object>{
    return new Promise(resolve => {
      this.get(`api/services/${srv.id}/getParams/dummy`,function(response){
        if(response.responseType== 'json') {
          resolve(JSON.parse(response.response))
        }else{
          console.log(response)
        }
      })
    })
  }
}
