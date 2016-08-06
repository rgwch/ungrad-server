import {HttpClient} from 'aurelia-http-client';

export class App {
  message = 'Hello World!';
  someText=function(){
    return 'ein Text';
  }

  liste=function(){
    return ["eins","zwei","drei"]
  }

  services = ["pi","pa","po"]

  testme(){
    let client=new HttpClient()
      .configure(x => {
        x.withBaseUrl(location.origin) // location.origin
      })
    client.get("/api/getServices").then(data => {
      if(data['responseType']==="json") {
        var ans = JSON.parse(data.response);
        ans.forEach( el => {
          this.services.push(JSON.stringify(el))
        })

      }else if(data["responseType"]==="html"){
        //location.href="/login"
        client.post("dologin","username=admin&pwd=secret)").then( answer => {
          debugger
          console.log(answer)
        })
      }else {
        console.log(data)
      }
    })
    return "waiting..."
  }

}
