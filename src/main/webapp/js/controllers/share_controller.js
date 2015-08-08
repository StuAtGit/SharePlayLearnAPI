shareAppControllers.controller("ShareIntroCtrl", ['$scope', '$http',
    function( $scope, $http ) {
        checkLoginStatus($scope, document);
    }
]);

shareAppControllers.controller("ShareMyStuffCtrl", ['$scope', '$http','$routeParams','$location', '$anchorScroll',
    function( $scope, $http, $routeParams, $location, $anchorScroll ) {

        $scope.toggleOpacity = function( itemId, opacity ) {
            if( document.getElementById(itemId).style.opacity > 0 ) {
                document.getElementById(itemId).style.opacity = 0;
                document.getElementById(itemId).style.pointerEvents = "none";
            } else {
                document.getElementById(itemId).style.opacity = 1;
                document.getElementById(itemId).style.pointerEvents = "auto";
            }
            /*if( opacity > 0 ) {
             alert( "toggling opacity, argument was: " + opacity);
             }*/
        };

        $scope.gotoAnchorHash = function(anchorHash) {
            alert("Attempting to scroll to " + anchorHash);
            $location.hash(anchorHash);
            $anchorScroll();
        };

        checkLoginStatus($scope, document);

        $scope.itemlist = [];

        //Angular can't deal with input type file models right now
        //so we'll need a more complex solution for the async upload
        $scope.submitUpload = function( file_upload, user_info ) {
            //$http.post("api/file/form")
        }

        if( "uploaded" in $routeParams ) {
            document.getElementById("file-uploaded").style.display = "block";
        } else if ( document.getElementById("file-uploaded") != null &&
            document.getElementById("file-uploaded") != undefined ) {
            document.getElementById("file-uploaded").style.display = "none";
        }

        if( $scope.user_info.access_token != null &&
            $scope.user_info.access_token != undefined &&
            $scope.user_info.user_id != null &&
            $scope.user_info.user_id != undefined )
        {
            $http.get("api/file/" + $scope.user_info.user_id + "/filelist",
                { headers:
                {'Authorization':'Bearer ' + $scope.user_info.access_token
                }
                }).success(
                function( data, status, headers, config, statusText ) {
                    $scope.itemlist = data;
                }
            ).error(
                function( data, status, headers, config, statusText ) {
                    if( status == 401 ) {
                        alert( "Not authorized to access your files? Did your login expire? (Try logging out and logging in)");
                        //JSON.stringify(data) );
                    }
                    else if( status != 400 ) {
                        alert(status + " " + statusText);
                        alert(data);
                    }
                }
            )
        }
    }
]);