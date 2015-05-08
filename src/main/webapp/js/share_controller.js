var shareAppControllers = angular.module('shareAppControllers',[]);

shareAppControllers.controller("ShareIntroCtrl", ['$scope', '$http', 
    function( $scope, $http ) {
        document.getElementById("legacy-duck-game").style.display = "none";
        $http.get("test_data/share_sample.json").success( function(data) {
            $scope.share_data = data;
        })
    }
])

shareAppControllers.controller("PlayCtrl", ['$scope', '$routeParams',
    function( $scope, $routeParams ) {
        //the duck game is just not that fit for any mobile right now :/
        if(  !(new shareplaylearn.Utils()).isMobile() ) {
            document.getElementById("legacy-duck-game").style.display = "block";
            if( document.getElementById("duck-instructions") == null ) {
                var instructionsNode = document.createElement("div");
                instructionsNode.setAttribute("id", "duck-instructions");
                instructionsNode.appendChild(document.createTextNode("Ducks!! :D"));
                instructionsNode.appendChild(document.createElement("br"));
                instructionsNode.appendChild(document.createTextNode("Arrows move you around"));
                instructionsNode.appendChild(document.createElement("br"));
                instructionsNode.appendChild(document.createTextNode("Press l to toggle look mode"));
                instructionsNode.appendChild(document.createElement("br"));
                instructionsNode.appendChild(document.createTextNode("Click ducks to make them rotate, click water to make new ducks"));
                document.getElementById("legacy-duck-game").appendChild(instructionsNode);
            }
        }
    }
])


shareAppControllers.controller("ShareMyStuffCtrl", ['$scope', '$http',
    function( $scope, $http ) {
        document.getElementById("legacy-duck-game").style.display = "none";
        $scope.user_name = window.sessionStorage.getItem("user_name");
        $scope.user_id = window.sessionStorage.getItem("user_id");
        $scope.access_token = window.sessionStorage.getItem("access_token");

        $scope.itemlist = [];

        if( $scope.access_token != null &&
            $scope.access_token != undefined &&
            $scope.user_id != null &&
            $scope.user_id != undefined )
        {
            $http.get("api/file/" + $scope.user_id + "/filelist",
                { headers:
                   {'Authorization':'Bearer ' + $scope.access_token
                   }
                }).success(
                    function( data, status, headers, config, statusText ) {
                        $scope.itemlist = data;
                    }
                ).error(
                    function( data, status, headers, config, statusText ) {
                        alert( status + " " + statusText );
                        alert( data );
                    }
                )
        }
    }
])

shareAppControllers.controller("LogoutCtrl", ['$scope', '$routeParams',
    function( $scope ) {
        window.sessionStorage.removeItem('user_id');
        window.sessionStorage.removeItem('access_token');
        window.sessionStorage.removeItem('auth_code');
        window.sessionStorage.removeItem('user_email');
        window.sessionStorage.removeItem('user_name');

        document.getElementById("login-control").style.display = "block";
        document.getElementById("logout-control").style.display = "none";

    }
])

/**
 * 
 * @param {type} str
 * @returns {unresolved}
 *
 **/
function base64urlDecode(str) {
  return atob(str.replace(/\-/g, '+').replace(/_/g, '/'));
};

shareAppControllers.controller("LoginCtrl",['$scope','$routeParams',
    function( $scope, $routeParams ) {
        document.getElementById("legacy-duck-game").style.display = "none";
        $scope.user_info = {};

        /**
         * Sample User token (see jwt):
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
                //might want to calculate expiration as soon as it gets back, so I can have it anchored to a time?
                //will need to be UTC, etc.
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
                
                //window.sessionStorage.setItem("id_token", $scope.user_info.id_token);
                //window.sessionStorage.setItem("id_token_header", header);
                //window.sessionStorage.setItem("id_token_payload", payload);
                window.sessionStorage.setItem("user_id", payload.sub);
                window.sessionStorage.setItem("user_email", payload.email);
                window.sessionStorage.setItem("user_name",$scope.user_info.user_name)
                //window.sessionStorage.setItem("id_token_signature", signature);

                document.getElementById("login-control").style.display = "none";
                document.getElementById("logout-control").style.display = "block";
            }
        }
        else {
            //TODO: try to retrieve user_id, user email, and token from session storage, and validate it
            //if it's all there and valid, you're still logged in.
            //need some kind of marker in the model that you're logged in, and token is confirmed.
        }
    }
])