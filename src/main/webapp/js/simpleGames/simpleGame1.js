// Tutorial 2: the javascript
// The models used need to be parsed before the page
// render. This code will parse the model files
// and when this complete the parser will call the
// main. The argument being passed - "tutorial" -
// is the id of the canvas element on the html page.

var planeWaterModel = "models/waterCircle.dae";
c3dl.addMainCallBack(canvasMain, "simpleGame1");
c3dl.addModel("models/duck.dae");
c3dl.addModel(planeWaterModel);

var timeSinceLastUpdate = 0;
var physicsSystem = jigLib.PhysicsSystem.getInstance();
physicsSystem.setGravity( [ 0.0, -32.0, 0.0, 0.0 ] );
var worldObjects = new Array();
var ground = null;
var sceneGraph = null;
var camera = new c3dl.FreeCamera();
var lookMode = false;

function updateScene(time)
{
	physicsSystem.integrate(time/1000);
	
	/**
	timeSinceLastChange += time;
	
	if( timeSinceLastChange >= 3000 )
	{
		timeSinceLastChange = 0;
	}
	**/
	
	for( var objIndex = 0; objIndex < worldObjects.length; ++objIndex )
	{
		worldObjects[objIndex].update();
	}
}

function makeGround( positionY )
{
	ground = new jigLib.JPlane( null, [0, 1, 0, 0 ] );
	ground.moveTo( [0,positionY,0,0] );
	ground.set_friction(0);
	ground.stuext_positionY = positionY;
	physicsSystem.addBody( ground );
	var groundMaterial = new c3dl.Material();
	groundMaterial.setDiffuse([0.8,0.3,0.8])
	groundMaterial.setAmbient([0.8,0.3,0.8])
	var groundModel = new c3dl.Collada();
	groundModel.init( planeWaterModel );
	groundModel.scale( [50.0, 50.0,50.0] );
	groundModel.setPosition( [0.0, positionY, 0.0] );
	ground.stuext_positionY = positionY;
	//groundModel.setAngularVel( new Array( 0.0, 0.001, 0.0 ) );
	groundModel.setMaterial( groundMaterial );
	groundModel.setPickable(false);
	sceneGraph.addObjectToScene( groundModel );
	
}

function makeDuck( position )
{
	var radius = 0.5
	var mass = 1.0;
	var friction = 0.0;

	duckModel = new c3dl.Collada();
	duckModel.init( "models/duck.dae" );
	duckModel.initialAngularVelocity = new Array( 0.0, 0.0, 0.0 );
	duckModel.setAngularVel( duckModel.initialAngularVelocity );
	duckModel.scale( [radius, radius, radius] );
	duckModel.setPosition( position );
	
	duckPhysics = new stuGames.PhysicsObject();		
	duckPhysics.init( radius*85, mass, position, friction, physicsSystem, ground );
	duck = new stuGames.WorldObject();
	duck.init( duckModel, duckPhysics );
	
	//Array.map => worldObjects.map(update { obj.update() } )
	worldObjects.push( duck );
	sceneGraph.addObjectToScene( duckModel );	
}

function moveCamera( distance, directionVec )
{
	if( distance == 0 )
	{
		return;
	}

	currentPos = camera.getPosition();
	
	directionVec[0] *= distance;
	directionVec[1] *= distance;
	directionVec[2] *= distance;
	
	camera.setPosition( new Array( currentPos[0] + directionVec[0], 
								  currentPos[1] + directionVec[1],
								  currentPos[2] + directionVec[2] ) )
}

function moveCameraForward( distance )
{
	directionVec = camera.getDir();
	moveCamera( distance, directionVec );
}

function moveCameraBackward( distance )
{
	moveCameraForward( distance * -1 );
}

function moveCameraLeft( distance )
{
	directionVec = camera.getLeft();
	//TODO: getLeft doesn't work for this?
	moveCamera( distance, directionVec );
}

function moveCameraRight( distance )
{
	moveCameraLeft( distance * -1 );
}

function keyDownCallback( event )
{
	/**
	 * Keycodes:
	 * Left, Up, Right, Down
	 *  37   38    39    40  :)
	 * TODO: handle move the camera up/down on it's up axis..
	 *       wasd key control? (with q/e?)
	 */
	switch( event.keyCode )
	{
	case 37:
		if(!lookMode)
		{
			moveCameraLeft( 2.0 );
		}
		else
		{
			camera.yaw(0.1);
		}
		break;
	case 38:
		if( !lookMode )
		{
			moveCameraForward( 2.0 );
		}
		else
		{
			camera.pitch(-0.1);
		}
		break;
	case 39:
		if( !lookMode )
		{
			moveCameraRight( 2.0 );
		}
		else
		{
			camera.yaw(-0.1);
		}
		break;
	case 40:
		if( !lookMode )
		{
			moveCameraBackward( 2.0 );
		}
		else
		{
			camera.pitch(0.1);
		}
		break;
	case 0x4C:
		lookMode = !lookMode;
		break;
	default:
		break;
	}
}

/*
 * http://www.w3.org/Bugs/Public/show_bug.cgi?id=9557
function mouseMoveCallback( event )
{
	if( lookMode )
	{
		
	}
}
*/

function keyUpCallback()
{
	//DO NOT REMOVE
	//if we don't have a do-nothing stub bound to the callback in the 
	//setKeyboardCallback method - everything breaks. Like everything!
}

function pickingCallback( pickResult )
{
	var button = pickResult.getButtonUsed();
	var objectsPicked = pickResult.getObjects();
	
	if( objectsPicked != undefined )
	{
		if( button == 1 && objectsPicked.length > 0 )
		{
			for( var i = 0; i < objectsPicked.length; ++i )
			{
				objectPicked = objectsPicked[i];
				//don't bother sqrt'ing if we don't have too
				angularSpeed = objectPicked.getAngularVel()[0] * objectPicked.getAngularVel()[0] +
							   objectPicked.getAngularVel()[1] * objectPicked.getAngularVel()[1] +
							   objectPicked.getAngularVel()[2] * objectPicked.getAngularVel()[2]
				if( angularSpeed > 0 )
				{
					objectPicked.setAngularVel( [0,0,0] );
				}
				else
				{
					objectPicked.setAngularVel( [0.0,0.001,0.0] );
				}
			}
		}
		else if( button == 1 )
		{
			var rayInitialPoint = pickResult.stuext_pickRayStart;
			var rayDir = pickResult.stuext_pickRayDir;
		    //TODO: allow user to select arbitrary z
		    //now we generate the (x,y,z) of the picked point by using the cartesian form
		    //of the pick ray & setting z to constant:
		    //(x-p1)/d1 = (y-p2)/d2 = (z-p3)/d3 => set z => 
		    //x = ((z-p3)*d1)/d3 + p1
		    //y = ((z-p3)*d2)/d3 + p2
		    //get the point on the Z plane that intersects the pick ray (this will be what the user picked in 3-space)
			/*
			 * var pickedZ = 0
		    var pickedY = ((pickedZ-rayInitialPoint[2])*rayDir[1])/rayDir[2] + rayInitialPoint[1];
		    var pickedX = ((pickedZ-rayInitialPoint[2])*rayDir[0])/rayDir[2] + rayInitialPoint[0];
			 */
			
			//hmm.. we actually need to change this to intersect this ray with the ground.
			//which means we pick Y, not z
			//later we can experiment with a user interface that lets them place ducks higher or lower
			//but the ground is a good default...
			// (x-p1)/d1 = (z-p3)/d3 = C, where C = (y-p2)/d2
			// x = C*d1 + p1
			// z = C*d2 + p2
			var pickedY = ground.stuext_positionY;;
			var cartesianY = (pickedY - rayInitialPoint[1]) / rayDir[1];
			var pickedX = cartesianY*rayDir[0] + rayInitialPoint[0];
			var pickedZ = cartesianY*rayDir[2] + rayInitialPoint[2];
		    
		    var pointPicked = new Array( pickedX,pickedY,pickedZ );
			makeDuck( pointPicked );
			//groundModel.setAngularVel( new Array( 0.0, 0.0, 0.0 ) );
		}
	}
	else
	{
		//makeDuck( new Array( worldObjects.length * 1, 0.0, 0.0 ) );
	}
}

/**
 * TODO: world boundaries. get pick location when empty.
 *       set physicsRadius = modelRadius
 *       figure out how to scale duck down
 * @param canvasName
 * @return
 */
function canvasMain( canvasName )
{
	sceneGraph = new c3dl.Scene();
	sceneGraph.setCanvasTag(canvasName);
	
	//create GL context
	renderer = new c3dl.WebGL();
	renderer.createRenderer( this );
	sceneGraph.setRenderer( renderer );
	sceneGraph.init( canvasName );
	
	//this check is only valid after 
	//attaching the renderer to the scene object
	if( renderer.isReady() )
	{
		sceneGraph.setAmbientLight( [0.8,0.8,0.8,1] );
		makeGround(-100.0);
	
		
		var diffuseLight = new c3dl.DirectionalLight();
		diffuseLight.setName( "directionalLight" );
		diffuseLight.setDirection( [ -2, 3, -5 ] );
		diffuseLight.setDiffuse( [0.5, 0.5, 0.5, 1] );
		diffuseLight.setOn(true);
		sceneGraph.addLight( diffuseLight );
		
		camera.setPosition( new Array( 0, 10, 500 ) );
		
		camera.setLookAtPoint( new Array( 0.0, 0.0, 0.0 ) );	
		sceneGraph.setCamera( camera );
		
		makeDuck( new Array( 100.0, 0.0, 0.0) );
		makeDuck( new Array( -100.0, 0.0, 0.0) );
		
		
		sceneGraph.setPickingCallback( pickingCallback );
		sceneGraph.setKeyboardCallback( keyUpCallback, keyDownCallback );
		//http://www.w3.org/Bugs/Public/show_bug.cgi?id=9557
		//sceneGraph.setMouseCallback( null, null, mouseMoveCallback, null)
		sceneGraph.setUpdateCallback( updateScene );
		sceneGraph.startScene();
	}
	else
	{
		document.write( "WebGL not working - something's wrong with your browser:( " );
	}
		
}