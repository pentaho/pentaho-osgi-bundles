require.config({
 "paths": {
   "common-ui/angular": "/webjars/angular",
   "common-ui/angular-route": "/webjars/angular-route",
   "common-ui/angular-resource": "/webjars/angular-resource",
   "angular-mocks": "/webjars/angular-mocks"
 },
 "shim" : {
   "common-ui/angular" : { "exports": "angular" },
   "common-ui/angular-route" : { "deps": ["common-ui/angular"] },
   "common-ui/angular-resource" : { "deps": ["common-ui/angular"] },
   "angular-mocks" : { "deps": ["common-ui/angular"] }
 }
});