

var stuGames = {};
/**
 * Basically, just an interface wrapper around whatever physics libraries
 * bounding objects we use.. jiglib for now..
 */
stuGames.PhysicsObject = function()
{
	this.boundingObject = null;
	this.physicsSystem = null;
	this.ground = null;

	/**
	 * ground is a plane that we won't go below
	 */
	this.init = function( radius, mass, position, friction, physicsSystem, ground )
	{
		//TODO: Do we want a sanity check on position here?
		//or just let the engine do restitution?
		this.physicsSystem = physicsSystem;
		this.ground = ground;
		
		this.boundingObject = new jigLib.JSphere(null, radius);
		this.boundingObject.set_mass(mass);
		this.boundingObject.set_friction(friction);
		this.physicsSystem.addBody( this.boundingObject );
		/**
		we just construct a new position array so the user
		can get away with a 3D vector, and if they pass a vector
		that happens to have 4 dimensions, the 4th is unlikely
		to be correct with respect to the physics engine
		**/
		this.boundingObject.moveTo( [position[0], position[1], position[2], 0] );
		
	}
	
	/**
	 * Retrieve a vector of at least elements, starting with the
	 * x,y,z positions of the object
	 */
	this.getPosition = function()
	{
		return this.boundingObject.get_currentState().position;
	}
	
	this.isValidPosition = function( position )
	{
		//TODO: check against this.ground (null would be valid -> pass)
		//TODO: check against all objects in physics system?
		return true;
	}
	
	this.setPosition = function ( newPosition )
	{
		if( !isValidPosition( newPosition ) )
		{
			return false;
		}

		
		var oldMass = this.boundingObject.get_mass();
		var oldFriction = this.boundingObject.get_friction();
		var oldRadius = this.boundingObject.get_radius();
		
		this.physicsSystem.removeBody( this.boundingObject );
		this.init( oldRadius, oldMass, newPosition, oldFriction, this.physicsSystem, this.ground );
		this.physicsSystem.addBody( this.boundingObject );
	}
}

stuGames.WorldObject = function()
{
	this.model = null;
	this.physicsObject = null;
	
	this.init = function( model, physicsObject )
	{
		this.model = model;
		this.physicsObject = physicsObject;
	}
	
	this.setPosition = function( position )
	{
		this.physicsObject.setPosition( position );
		this.model.setPosition( position );
	}
	
	this.update = function()
	{
		//print( "New duck position: " + )
		this.model.setPosition( this.physicsObject.getPosition() );
		//orientation.. ???
		/**
		 * 	var ori=GLGE.Mat4(links[i].jig.get_currentState().get_orientation().glmatrix);
		 *	links[i].glge.setRotMatrix(ori);
		 */
	}
}