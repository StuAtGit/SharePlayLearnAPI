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
        $scope.user_name = window.sessionStorage.getItem("user_name");
        $scope.user_id = window.sessionStorage.getItem("user_id");

    }
])


/**
 * 
 * @param {type} str
 * @returns {unresolved}
 * 
 * atob triggers:
 * DOMException [InvalidCharacterError: "String contains an invalid character"
 * code: 5
 * nsresult: 0x80530005
 
 * TODO: Write some javascript code that can decode, btoa gets error above
 * 
 * login data: ya29.6gDX_l3SORL7DJa4yk0huk7diA_gwLAyj9apn_xJ-CC5qXzZ1qAC2vfE
login id (as jwt): eyJhbGciOiJSUzI1NiIsImtpZCI6IjQ3MGIyMWIzMjA2NmI1NTEzMTY3NWY2MjU4Y2MzMGIyOWU2YTAzYTgifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMTEwODMxNjM0MzU1MjI2MzY0OTQwIiwiYXpwIjoiNzI2ODM3ODY1MzU3LXRxczIwdTZsdXFjOW9hdjFicDN2YjhuZGdhdmpucmtmLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1haWwiOiJzdHUyNmNvZGVAZ21haWwuY29tIiwiYXRfaGFzaCI6IllRRFpSTjRmdnI1Vms4SXM0cEJ2LWciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXVkIjoiNzI2ODM3ODY1MzU3LXRxczIwdTZsdXFjOW9hdjFicDN2YjhuZGdhdmpucmtmLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiaWF0IjoxNDE5NzM2NTEwLCJleHAiOjE0MTk3NDA0MTB9.RwTHP4M-RA7lZilUi74OR8NrDknujfZON7PJiJ30Oae1uWFwnlRE3VrQxmfRLDYz4md-K21YbghdWqu90xjVhKAdHkI1v72YYuPm5I-mUAFCGHvuGvyk3bMKOE-RbCo3MPq6WnYMkOeUF0um_IX9flut2T2WOXmPE_w4gK_8obE
login id (jwt header): eyJhbGciOiJSUzI1NiIsImtpZCI6IjQ3MGIyMWIzMjA2NmI1NTEzMTY3NWY2MjU4Y2MzMGIyOWU2YTAzYTgifQ
login id (jwt signature): RwTHP4M-RA7lZilUi74OR8NrDknujfZON7PJiJ30Oae1uWFwnlRE3VrQxmfRLDYz4md-K21YbghdWqu90xjVhKAdHkI1v72YYuPm5I-mUAFCGHvuGvyk3bMKOE-RbCo3MPq6WnYMkOeUF0um_IX9flut2T2WOXmPE_w4gK_8obE
login id (jwt payload): eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMTEwODMxNjM0MzU1MjI2MzY0OTQwIiwiYXpwIjoiNzI2ODM3ODY1MzU3LXRxczIwdTZsdXFjOW9hdjFicDN2YjhuZGdhdmpucmtmLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1haWwiOiJzdHUyNmNvZGVAZ21haWwuY29tIiwiYXRfaGFzaCI6IllRRFpSTjRmdnI1Vms4SXM0cEJ2LWciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXVkIjoiNzI2ODM3ODY1MzU3LXRxczIwdTZsdXFjOW9hdjFicDN2YjhuZGdhdmpucmtmLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiaWF0IjoxNDE5NzM2NTEwLCJleHAiOjE0MTk3NDA0MTB9
 
 * TODO: fix deployment - use maven tomcat deploy, the following is already set up:
 * <user username="playManager" password="yalpt1m3!" roles="manager-gui"/>
 *  <user username="playScript" password="yalpt1m3!" roles="manager-script"/>
 **/
function base64urlDecode(str) {
  return atob(str.replace(/\-/g, '+').replace(/_/g, '/'));
};

/*
function base64urlUnescape(str) {
  str += Array(5 - str.length % 4).join('=');
  return str.replace(/\-/g, '+').replace(/_/g, '/');
}
*/

shareAppControllers.controller("LoginCtrl",['$scope','$routeParams','$http',
    function( $scope, $routeParams, $http ) {
        $scope.user_info = {};

        /**
         * Sample returned URL:
        * http://www.shareplaylearn.com/SharePlayLearn2/api/oauth2callback?
        * state=insecure_test_token
        * &code=4/1Oxqgx2PRd8y4YxC7ByfJOLNiN-2.4hTyImQWEVMREnp6UAPFm0EEMmr5kAI
        * &authuser=0&num_sessions=1
        * &prompt=consent
        * &session_state=3dd372aa714b1b2313a838f8c4a4145b928da51f..8b83
         * @returns {undefined}
         * 
         * eyJhbGciOiJSUzI1NiIsImtpZCI6IjljNjMxNDFjMzAzNjkyY2E3Y2Q4MDAxZTUxNmNhNDVhZDdlNTJiZTIifQ.
         * eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMTEwODMxNjM0MzU1MjI2MzY0OTQwIiwiYXpwIjoiNzI2ODM3ODY1MzU3LXRxczIwdTZsdXFjOW9hdjFicDN2YjhuZGdhdmpucmtmLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1haWwiOiJzdHUyNmNvZGVAZ21haWwuY29tIiwiYXRfaGFzaCI6Iml3NWg3NUlnZlJzdkdKLUdDcTJNQWciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXVkIjoiNzI2ODM3ODY1MzU3LXRxczIwdTZsdXFjOW9hdjFicDN2YjhuZGdhdmpucmtmLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiaWF0IjoxNDEyNTQ5NTIzLCJleHAiOjE0MTI1NTM0MjN9.
         * iMrciLGvWA__B-PY_1_POk1eus4C5W7K4LdOzZ4DNa3Fi2HaD5t8Wg9usq1-MzswZG4um55abkzlZ6IlmWNc-sJ_wwXXdO-cK4Bj8ucdBjCYWOCnZwx1akjH8Ettv3MGTa76mh7CuipTYpes8Ka_Wn2SPH7mmD1PK-asuj1t8U8
         */
        if( "client_state" in $routeParams &&
            "access_token" in $routeParams &&
            "expires_in" in $routeParams &&
            "id_token" in $routeParams ) {
            if( $routeParams["client_state"] === "insecure_test_token" ) { 
                $scope.user_info.client_state = $routeParams["session_state"];
                $scope.user_info.access_token = $routeParams["access_token"];
                $scope.user_info.token_expiration = $routeParams["expires_in"];
                $scope.user_info.id_token = $routeParams["id_token"];
                window.sessionStorage.setItem("auth_code",$scope.user_info.auth_code);
                //window.sessionStorage.setItem("client_state",$scope.user_info.client_state);
                window.sessionStorage.setItem("access_token", $scope.user_info.access_token);
                window.sessionStorage.setItem("expires_in", $scope.user_info.token_expiration);
                /**
                 * Parse jwt in id_token to get user info
                 * Pulled in jwsjs library so I could do this...
                 * I have just added header, payload, signature to scope so I can print them in the logged in template..
                 * I vaguely recall that I may have already confirmed this? But adding it back in for debugging while I decode jwt payload.
                 * Soo.. just FYI: the payload will decode to the JSON with the info I need - the jsjws library will just verify
                 * the signature of the js passed.
                 * The access token (not the id_token) is what is used to authorize with google.
                 * If we talk https to the oauth endpoint (we need SSL for the site, then!), and the secret comes back OK,
                 * it should be secure-ish (secure, according to google).. so I suppose we can delay verification for a little bit???
                 * We may not even need all this info.. mebbe just use email.
                 */
                var id_token_elements = $scope.user_info.id_token.split('.');
                var header = base64urlDecode(id_token_elements[0]);
                var payload = JSON.parse(base64urlDecode(id_token_elements[1]));
                //do we need to escape this? Gibberish either way.. (coz binary sig)
                var signature = base64urlDecode(id_token_elements[2]);
              
                $scope.user_info.id_token_header = header;
                $scope.user_info.id_token_payload = payload;
                $scope.user_info.id_token_signature = signature;
                $scope.user_info.user_name = payload.email.split('@')[0];
                $scope.user_info.user_id = payload.sub;
                
                window.sessionStorage.setItem("id_token", $scope.user_info.id_token);
                window.sessionStorage.setItem("id_token_header", header);
                window.sessionStorage.setItem("id_token_payload", payload);
                window.sessionStorage.setItem("user_id", payload.sub);
                window.sessionStorage.setItem("user_email", payload.email);
                window.sessionStorage.setItem("user_name",$scope.user_info.user_name)
                window.sessionStorage.setItem("id_token_signature", signature);
            }
        }

        //? what was I doing with this?
        $scope.saveUser = function () {
            window.localStorage.setItem("user_name",$scope.user_info.user_name);
        }
    }
])