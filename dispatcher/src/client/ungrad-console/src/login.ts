import {Http} from './http';
import hans from './resources/common'

export class Login {
  username= hans.hans
  password = ""

  private client:Http

  constructor() {
    this.client = new Http()
  }

  doLogin = function () {

    this.client.post("dologin", `username=${this.username}&pwd=${this.password}`, response => {
      if(response.statusCode==200){

      }else{
        alert("Error: "+response.statusCode+", "+response.response)
      }
      console.log(response)
    })
  }
}
