var shareAppControllers = angular.module('shareAppControllers',[]);

var user_id_global = "";

shareAppControllers.controller("ShareIntroCtrl", ['$scope', '$http', 
    function( $scope, $http ) {
        $http.get("test_data/share_sample.json").success( function(data) {
            $scope.share_data = data;
        })
    }
])

shareAppControllers.controller("ShareMyStuffCtrl", ['$scope', '$routeParams',
    function( $scope, $routeParams ) {
        $scope.user_name = window.localStorage.getItem("user_name");
        $scope.user_cred = window.sessionStorage.getItem("user_cred");
    }
])

shareAppControllers.controller("LoginCtrl",['$scope','$routeParams','$http',
    function( $scope, $routeParams, $http ) {
        $scope.user_info = {};
        $scope.user_info.user_name = window.localStorage.getItem("user_name");
        $scope.user_info.user_cred = window.sessionStorage.getItem("user_cred");
        
        /**
         * Sample returned URL:
        * http://www.shareplaylearn.com/SharePlayLearn2/api/oauth2callback?
        * state=insecure_test_token
        * &code=4/1Oxqgx2PRd8y4YxC7ByfJOLNiN-2.4hTyImQWEVMREnp6UAPFm0EEMmr5kAI
        * &authuser=0&num_sessions=1
        * &prompt=consent
        * &session_state=3dd372aa714b1b2313a838f8c4a4145b928da51f..8b83
         * @returns {undefined}
         */
        if( "session_state" in $routeParams ) {
            if( $routeParams["session_state"] === "insecure_test_token" ) {
                $scope.user_info.session_state = $routeParams["session_state"];
                window.sessionStorage.setItem("auth_code",$scope.user_info.auth_code);
                window.sessionStorage.setItem("session_state",$scope.user_info.session_state);
            }
        }
        
        $scope.saveUser = function () {
            window.localStorage.setItem("user_name",$scope.user_info.user_name);
            $scope.user_info.user_cred = $scope.user_info.user_name + "'s super secret token thing";
            window.sessionStorage.setItem("user_cred",$scope.user_info.user_cred);
        }
    }
])