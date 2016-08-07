import {HttpClient} from 'aurelia-http-client';

export class Http{

  client=new HttpClient()
    .configure(x => {
      x.withBaseUrl(location.origin) // location.origin
    })

  get(url:string, callback){
    this.client.get(url).then(result => callback(result))
  }
}
