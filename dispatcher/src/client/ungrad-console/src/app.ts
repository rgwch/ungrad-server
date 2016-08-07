import {Http} from './http';

export class App {
  private services: Array<Object>
  private client: Http

  constructor() {
    this.services = []
    this.client = new Http()
  }

  public testme = () => {
    debugger
    this.services.forEach(x => console.log(x))
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

}
