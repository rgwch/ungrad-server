import {Http} from './http';

export class App {
  private services:Array<Object>
  private client:Http
  private actService:Array<Object>

  constructor() {
    this.services = []
    this.client = new Http()
    this.testme()
  }

  public testme = function () {

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

  public getDetails = function (id:String) {
    this.client.get(`/api/services/${id}/getParams/none`, answer => {
      if (answer.isSuccess) {
        let message = JSON.parse(answer.response)
        if (message.status === "ok") {
          var rcv_parameters = message.result
          this.services.forEach(service => {
            if (service['name'] === id) {
              service['params'] = rcv_parameters
              service['details'] = true
              let params=[]
              rcv_parameters.forEach(param => {
                params.push({"name":param.name, "caption":param.caption, "type":param.type, "value":param.value})
              })
              this.actService=params
            }
          })
        }

      }

      console.log(answer)
    })
  }


}
