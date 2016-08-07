import {Http} from './http';

export class App {
  private services: Array<Object>
  private client: Http
  private actService:Object

  constructor() {
    this.services = []
    this.client = new Http()
  }

  public testme = function(){

    this.client.get("/api/getServices", data => {
      if (data['responseType'] === "json") {

        this.services = JSON.parse(data['response']);
      } else if (data["responseType"] === "html") {
        //location.href="/login"
        //client.post("dologin","username=admin&pwd=secret)").then( answer => {
        // console.log(answer)
        //})
      } else {
        console.log(data)
      }
    })
    return "waiting..."
  }

  public getDetails=function(id: String){
    this.client.get(`/api/services/${id}/getParams/none`, answer => {
      if(answer.isSuccess){
        let result=JSON.parse(answer.response)
        this.services.forEach(service => {
          if(service['name'] === id){
            service['params']=result
            service['details']=true
          }
        })
      }

      console.log(answer)
    })
  }
}
