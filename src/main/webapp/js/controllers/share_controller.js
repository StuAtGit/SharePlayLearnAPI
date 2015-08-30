shareAppControllers.controller("ShareIntroCtrl", ['$scope', '$http',
    function( $scope, $http ) {
        checkLoginStatus($scope, document);
    }
]);

shareAppControllers.controller("ShareMyStuffCtrl", ['$scope', '$http','$routeParams',
    '$location', '$anchorScroll', '$user',
    function( $scope, $http, $routeParams, $location, $anchorScroll, $user ) {

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

        //Angular can't deal with input type file models right now
        //so we'll need a more complex solution for the async upload
        $scope.submitUpload = function( file_upload, user_info ) {
            //$http.post("api/file/form")
        };

        if( "uploaded" in $routeParams ) {
            document.getElementById("file-uploaded").style.display = "block";
        } else if ( document.getElementById("file-uploaded") != null &&
            document.getElementById("file-uploaded") != undefined ) {
            document.getElementById("file-uploaded").style.display = "none";
        }

        if( $user.getCurrentUser() !== undefined ) {
            $user.getCurrentUser().itemListDeferred.promise.then(
                function success( data ) {
                    $scope.itemlist = data;
                },
                function error( msg ) {
                    alert( msg );
                }
            );
        }
    }
]);