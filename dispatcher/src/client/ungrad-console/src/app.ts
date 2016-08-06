import {HttpClient} from 'aurelia-http-client';

export class App {

  message = 'Hello World!';
  liste=function(){
    return ["eins","zwei","drei"]
  }

  getServices=function(){
    let client=new HttpClient()
      .configure(x => {
        x.withBaseUrl("http://localhost:2016") // location.origin
      })
    client.get("/getServices").then(data => {
      console.log(data)
    })
  /*
    let client = new HttpClient();

    client.get('test')
      .then(data => {
        console.log(data)
      });
*/
  }
}
