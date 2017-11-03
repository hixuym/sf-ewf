Sunflower Web Application Framework
======================================
1.3.0 - 

    - add subRouter support
    - add servlet support
    - improve jaxy routes scan performance
    - jaxy routes disable default, register JaxyRoutes in guice environment
    - controller method return Results.badRequest(), ResultHandler auto send the Validation errors if have.
    - UndertowContext creating out guice
    
1.2.0 - 2017-10-28

  - add ewf archetypes
  - add ewf auth module, include basic and token auth
  - add ewf assets module, disabled by default, enabled by register AssetsRoutes
  - add ewf jaxy routes, just like jax-rs style, set ewf.scanPkgs property
  - content type of HTML disabled by default, enabled by register TemplateFreemarker
  - content type of XML disabled by default, enabled by register XmlModule
  - upgrade sf-framework to 1.4.0
  
1.1.0 - 2017-10-18

  - change name from sf-gizmo to sf-ewf
  - sf-ewf-core no dependency to runtime
  - add websocket support
  - performance improved.
  
1.0.0 - 2017-10-15

 - Initial release
