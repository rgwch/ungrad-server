import {HttpClient} from 'aurelia-http-client';
import {RequestMessage} from "aurelia-http-client/dist/aurelia-http-client";

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

  getServices(){
    return new Promise(resolve => {
      this.get("/api/getServices", function(response){
        resolve(JSON.parse(response.response))
      })
    })
  }
}
