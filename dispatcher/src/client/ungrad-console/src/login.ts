import {inject} from 'aurelia-framework'
import {AppState} from './appstate'
import {Http} from "./http";
import {Router} from 'aurelia-router'

@inject(Http, AppState, Router)
export class Login {
  username= ""
  password = ""

  private client
  private state
  private router

  constructor(http,appstate,router) {
    this.client = http
    this.state=appstate
    this.username=this.state.hans
    this.router=router
  }

  doLogin = function () {

    this.client.post("/dologin",  `username=${this.username}&pwd=${this.password}`, response => {
      if(response.statusCode==200){
        this.state.bLoggedIn=true
        this.state.username=this.username
        this.state.password=this.password
        this.router.navigateToRoute("configure")
      }else{
        if(typeof response === 'string'){
          alert("Error: "+response)
        }else if(typeof response === 'object'){
          alert("Error: " + response.message +" "+ response.statusCode + ", " + response.response)
        }else{
          alert(response.toString)
        }
      }
    })
  }
}
