# Webelexis

## Summary

This subproject is a generic way to access the [Elexis](http://www.elexis.ch/ungrad)-Database via REST API. It contains of several Verticles for different data types:
 
 * Appointments
 * Patients
 * ... more to come ...

## Preparation

    mvn clean package

The configuration must contain instructions to connect to the database, as in:
    
    lucindaConfig:{
         "host":"localhost",
         "database":"elexis",
         "charset":"utf-8",
         "username":"elexisuser",
         "password":"elexispassword"
    }
    
   
## Usage
   
This module is called via    
   