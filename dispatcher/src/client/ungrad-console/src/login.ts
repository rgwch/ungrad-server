import {inject} from 'aurelia-framework'
import {AppState} from './appstate'
import {HttpClient} from "aurelia-http-client";

@inject(HttpClient, AppState)
export class Login {
  username= ""
  password = ""

  private client
  private state

  constructor(http,appstate) {
    this.client = http
    this.state=appstate
    this.username=this.state.hans
  }

  doLogin = function () {

    this.client.post("/dologin", `username=${this.username}&pwd=${this.password}`, response => {
      if(response.statusCode==200){

      }else{
        alert("Error: "+response.statusCode+", "+response.response)
      }
      console.log(response)
    })
  }
}
