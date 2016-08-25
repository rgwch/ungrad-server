/**
 * Copyright (c) 2016 by G. Weirich
 * Global state for the application
 */
export class AppState{
  bLoggedIn:Boolean=false;
  selectedService={}
}


export interface IService {
  id:String,
  address:String,
  name:String,
  params?:Array<IServiceParameter>
}

export interface IServiceParameter{
  name:String
  caption: String
  type: String
  writable: boolean
  value:any
}
