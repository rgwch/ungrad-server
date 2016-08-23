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
  name?:String
}
