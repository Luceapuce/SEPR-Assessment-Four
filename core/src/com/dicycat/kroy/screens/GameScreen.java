package com.dicycat.kroy.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dicycat.kroy.GameObject;
import com.dicycat.kroy.GameTextures;
import com.dicycat.kroy.Kroy;
import com.dicycat.kroy.debug.DebugCircle;
import com.dicycat.kroy.debug.DebugDraw;
import com.dicycat.kroy.debug.DebugRect;
import com.dicycat.kroy.entities.FireStation;
import com.dicycat.kroy.entities.FireTruck;
import com.dicycat.kroy.entities.Fortress;
import com.dicycat.kroy.entities.UFO;
import com.dicycat.kroy.gamemap.TiledGameMap;
import com.dicycat.kroy.misc.StatusIcon;
import com.dicycat.kroy.misc.StatBar;
import com.dicycat.kroy.powerups.PowerupBox;
import com.dicycat.kroy.scenes.HUD;
import com.dicycat.kroy.scenes.OptionsWindow;
import com.dicycat.kroy.scenes.PauseWindow;


/**
 * Contains the main game logic
 * 
 * @author Riju De
 * @author lnt20
 *
 */
public class GameScreen implements Screen{

	public static enum GameScreenState{
		PAUSE,
		RUN,
		RESUME,
		//MINIGAME_INTEGRATION - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE
		MINIGAME,
		//MINIGAME_INTEGRATION - END OF MODIFICATION - NPSTUDIOS
		OPTIONS
	}


	// DIFFICULTY_1 - START OF MODIFICATION - NP STUDIOS - BRUNO DAVIES
	private int difficultyChosen; //hold the int value of the difficulty selected : 0 - easy, 1 - med, 2 - hard
	private float[][] difficultyStats = { //difficultyChosen will relate to this 2D array and select the correct one
			{2f, 2f, 2f, 2f},        //Easy: time to spawn a patrol is 2x (30 -> 60), 2x the tank size, 2x the health, max of 2 aliens (UFOs) per fortress
			{1f, 1f, 1f, 4f},        //Medium :  time to spawn a patrol is 1x (30 -> 30), 1x the tank size, 1x the health, max of 4 aliens (UFOs) per fortress
			{0.5f, 0.5f, 0.5f, 8f}, //Hard:  time to spawn a patrol is 0.5x (30 -> 15), 0.5x the tank size, 0.5x the health, max of 8 aliens (UFOs) per fortress
	};

	//indicates the index number for the modification. Thus do not need to remember
	private int partolNumberIndex = 0;
	private int tankMultiplierIndex = 1;
	private int healthMultiplierIndex = 2;
	private int patrolMaxIndex = 3;

	private int numberOfPatrolsSpawned = 0; //The number of patrols already spawned

	// DIFFICULTY_1 - END OF MODIFICATION - NP STUDIOS - BRUNO DAVIES

	public Kroy game;
	public GameTextures textures;
	public static float gameTimer; //Timer to destroy station

	// SAVING_4 - START OF MODIFICATION  - NP STUDIOS - LUCY IVATT
	// Defines the saveslot variable and initializes the preferences file which is used to hold the save data.
	private int saveSlot;
	private Preferences saveData = Gdx.app.getPreferences("Kroy");
	// SAVING_4 - END OF MODIFICATION  - NP STUDIOS - LUCY IVATT

	public GameScreenState state = GameScreenState.RUN;
	
	public static TiledGameMap gameMap;
	
	private OrthographicCamera gamecam;	//follows along what the port displays
	private Viewport gameport;
	// MINIMAP_ADDITION_1 - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE ----
	private Texture minimap;
	private Boolean mapToggle;
	// MINIMAP_ADDITION_1 - END OF MODIFICATION - NPSTUDIOS
	
	public HUD hud;
	private PauseWindow pauseWindow;
	private OptionsWindow optionsWindow;


	private Float[][] truckStats = {	//extended
										//Each list is a configuration of a specific truck. {speed, speed + damage , damage , capacity+range, capacity, range}
			{400f, 1f, 400f, 300f},		//Speed
			{350f, 1.25f, 400f, 300f},	//Speed + Flow rate
			{300f, 1.5f, 400f, 300f},	//Flow rate
			{300f, 1f, 450f, 400f},  	//Capacity + Range
			{300f, 1f, 500f, 300f},		//Capacity
			{300f, 1f, 400f, 450f},		//Range
		};

	// UNIQUE_FORTRESS_HEALTH_DAMAGE_8 - START OF MODIFICATION  - NPSTUDIOS - CASSIE_LILLYSTONE----
	private float[][] fortressStats = { //Each list contains unique values for health and damage. One list for each fortress

            {300f, 5f},
            {400f, 10f},
            {500f, 15f},
            {600f, 20f},
            {700f, 25f},
            {800f, 30f},
    }; 

	// UNIQUE_FORTRESS_HEALTH_DAMAGE_8 - END OF MODIFICATION - NPSTUDIOS-----
	
	private int truckNum; // Identifies the truck thats selected in the menu screen
	private FireTruck currentTruck;
	private int lives = 6;
	private float zoom = 1;
	
	private int fortressesCount;
	private Vector2 spawnPosition;	//Coordinates the player spawns at
	
	private List<GameObject> gameObjects, deadObjects;	//List of active game objects
	private List<GameObject> objectsToRender = new ArrayList<GameObject>(); // List of game objects that have been updated but need rendering
	private List<GameObject> objectsToAdd;
	private List<DebugDraw> debugObjects; //List of debug items

	private float lastPatrol; //time passsed since we last spawned patrols
	private List<Vector2> fortressPositions, fortressSizes; //where our fortresses spawn
	private int patrolUpdateRate; //How many seconds should pass before we respawn patrols;
	//MINIGAME_INTEGRATION - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE
	private boolean start;
	//MINIGAME_INTEGRATION - END OF MODIFICATION - NPSTUDIOS

    private StatusIcon timeIncreaseIcon;
    private StatusIcon rainDanceIcon;
    private StatusIcon freezeEnemiesIcon;
    private StatusIcon revivedFireTruckIcon;
    private boolean timeIncrease;
    private boolean rainDance;
    private boolean freezeEnemies;
    private float freezeTimer;
    private boolean revivedFireTruck;
	private ArrayList<FireTruck> firetrucks = new ArrayList<FireTruck>();
	private ArrayList<Fortress> fortresses = new ArrayList<Fortress>();
	// STATBAR_REFACTOR_6 - START OF MODIFICATION  - NP STUDIOS - LUCY IVATT
	// Created new arrays for the firetruck statbars.
	private ArrayList<StatBar> healthbars = new ArrayList<StatBar>();
	private ArrayList<StatBar> tankbars = new ArrayList<StatBar>();
	private ArrayList<StatBar> fortressHealthBars = new ArrayList<>();
	private ArrayList<StatusIcon> activeStatusIcons = new ArrayList<>();
	// STATBAR_REFACTOR_6 - END OF MODIFICATION  - NP STUDIOS

	//POWERUPS_4 - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE
	private float timeSinceLastBoxSpawn;
	private int boxSpawnRate;
	private ArrayList<Vector2> boxSpawnLocations = new ArrayList<Vector2>();

	//POWERUPS_4 - END OF MODIFICATION - NPSTUDIOS

	/**
	 * extended
	 * @param _game
	 * @param truckNum
	 * @param saveSlot 0 if no save slot, otherwise between 1 and 3
	 */

	public GameScreen(Kroy _game, int truckNum, int difficultyChosen, int saveSlot) {
		this.difficultyChosen = difficultyChosen;
		game = _game;
		this.saveSlot = saveSlot;
		gamecam = new OrthographicCamera();
		gameport = new FitViewport(Kroy.width, Kroy.height, gamecam);	//Mic:could also use StretchViewPort to make the screen stretch instead of adapt
		gameMap = new TiledGameMap(); //or FitPort to make it fit into a specific width/height ratio
		// MINIMAP_ADDITION_2 - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE ----
		minimap = new Texture("MinimapBackground.png"); //adds a texture of the map as a .png to be the minimap
		mapToggle = true;
		// MINIMAP_ADDITION_2 - START OF MODIFICATION - NPSTUDIOS
		pauseWindow = new PauseWindow(game);
		pauseWindow.visibility(false);
		optionsWindow = new OptionsWindow(game);
		optionsWindow.visibility(false);
		textures = new GameTextures();
		spawnPosition = new Vector2(234 * 16, 3900);
		gameTimer = 60 * 5; //new    //Set timer to 5 minutes
		hud = new HUD(game.batch, gameTimer);
		this.truckNum = truckNum;

        timeIncreaseIcon = new StatusIcon(Vector2.Zero,"TimeIncrease.png");
        timeIncrease = false;
        freezeEnemiesIcon = new StatusIcon(Vector2.Zero,"FreezeEnemies.png");
        freezeEnemies = false;
        rainDanceIcon = new StatusIcon(Vector2.Zero,"RainDance.png");
        rainDance = false;
        revivedFireTruckIcon = new StatusIcon(Vector2.Zero,"Ressurected.png");
        revivedFireTruck = false;
        activeStatusIcons = new ArrayList<>();

		lastPatrol = Gdx.graphics.getDeltaTime();
		timeSinceLastBoxSpawn = Gdx.graphics.getDeltaTime();
		fortressPositions = new ArrayList<>();
		fortressPositions.add(new Vector2(2860, 3211));
		fortressPositions.add(new Vector2(3130, 5530));
		fortressPositions.add(new Vector2(2010, 1900));
		fortressPositions.add(new Vector2(4270, 870));
		fortressPositions.add(new Vector2(5940, 1150));
		fortressPositions.add(new Vector2(520, 3500));
		fortressSizes = new ArrayList<>();
		fortressSizes.add(new Vector2(256, 218));
		fortressSizes.add(new Vector2(256, 320));
		fortressSizes.add(new Vector2(400, 256));
		fortressSizes.add(new Vector2(450, 256));
		fortressSizes.add(new Vector2(400, 256));
		fortressSizes.add(new Vector2(450, 256));
		fortressesCount = 6;
		// DIFFICULTY_2 - START OF MODIFICATION - NP STUDIOS - BRUNO DAVIES
		//changes the spawn rate of UFO's, lower for easier, higher for harder.
		patrolUpdateRate = (int)((float)30* (difficultyStats[difficultyChosen][partolNumberIndex]));

		updateTruckStats(); //ensures that the stats are correct with current difficulty selected
		// DIFFICULTY_2 - END OF MODIFICATION - NP STUDIOS - BRUNO DAVIES


		//POWERUPS_5 - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE
		boxSpawnRate = 20;
		boxSpawnLocations.add(new Vector2(750, 1000));
		boxSpawnLocations.add(new Vector2(1600, 2675));
		boxSpawnLocations.add(new Vector2(750, 5000));
		boxSpawnLocations.add(new Vector2(5500, 2675));

		//POWERUPS_5 - END OF MODIFICATION - NPSTUDIOS

		//MINIGAME_INTEGRATION - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE
		start = true;
		//MINIGAME_INTEGRATION - END OF MODIFICATION - NPSTUDIOS

	}

	// DIFFICULTY_4 - START OF MODIFICATION - NP STUDIOS - BRUNO DAVIES
	//Updates the stats of firetruck with selected level.
	public void updateTruckStats() {
		for (int i=0; i<6; i++){ //loops through all of truckstats
			//modifies each trucks stat with the multiplier of that difficulty
			truckStats[i][2] = truckStats[i][2]*(float)difficultyStats[difficultyChosen][tankMultiplierIndex];
		}
	}
	// DIFFICULTY_4 - END OF MODIFICATION - NP STUDIOS - BRUNO DAVIES

	//POWERUPS_6 - START OF MODIFICATION - NPSTUDIOS - Alasdair Pilmore-Bedford

	// Sets the icons to enabled if they are currently active
    private void updateStatusIcons(){

        if (timeIncrease) {
			if (!timeIncreaseIcon.isEnabled()) {
				timeIncreaseIcon = new StatusIcon(Vector2.Zero, "TimeIncrease.png");
				activeStatusIcons.add(timeIncreaseIcon);
				timeIncreaseIcon.setEnabled(true);
			}
		} else {
			activeStatusIcons.remove(timeIncreaseIcon);
        	timeIncreaseIcon.setEnabled(false);
        	timeIncreaseIcon.setRemove(true);
		}
        if (rainDance){
			if (!rainDanceIcon.isEnabled()) {
				rainDanceIcon = new StatusIcon(Vector2.Zero, "RainDance.png");
				activeStatusIcons.add(rainDanceIcon);
				rainDanceIcon.setEnabled(true);
			}
        } else {
			activeStatusIcons.remove(rainDanceIcon);
        	rainDanceIcon.setEnabled(false);
			rainDanceIcon.setRemove(true);
		}
        if (freezeEnemies){
			if (!freezeEnemiesIcon.isEnabled()) {
				freezeEnemiesIcon = new StatusIcon(Vector2.Zero, "FreezeEnemies.png");
				activeStatusIcons.add(freezeEnemiesIcon);
				freezeEnemiesIcon.setEnabled(true);
			}
        } else {
        	activeStatusIcons.remove(freezeEnemiesIcon);
        	freezeEnemiesIcon.setEnabled(false);
			freezeEnemiesIcon.setRemove(true);
		}
        if (revivedFireTruck){
			if (!revivedFireTruckIcon.isEnabled()) {
				revivedFireTruckIcon = new StatusIcon(Vector2.Zero, "Ressurected.png");
				activeStatusIcons.add(revivedFireTruckIcon);
				revivedFireTruckIcon.setEnabled(true);
			}
        } else {
			activeStatusIcons.remove(revivedFireTruckIcon);
        	revivedFireTruckIcon.setEnabled(false);
			revivedFireTruckIcon.setRemove(true);
		}
    }
	//POWERUPS_6 - END OF MODIFICATION - NPSTUDIOS

	/**
	 * Screen first shown
	 */
	@Override
	public void show() {
		//MINIGAME_INTEGRATION - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE
		//Most of the show() method has been moved into this if statement that means the contents are only ran upon the
		//first starting of an instance of gameScreen.
		if (start) {
			objectsToAdd = new ArrayList<GameObject>();
			gameObjects = new ArrayList<GameObject>();
			deadObjects = new ArrayList<GameObject>();
			debugObjects = new ArrayList<DebugDraw>();

			// Initialises the FireTrucks
			for (int i = 0; i < 6; i++) {
				firetruckInit(spawnPosition.x - 135 + (i * 50), spawnPosition.y, i);
				fortressInit(i);
				// SAVING_5 - START OF MODIFICATION  - NP STUDIOS - LUCY IVATT
				// moved firetruck statbar code to be initialized after the firetrucks are created.
				tankbars.get(i).setPosition(firetrucks.get(i).getCentre().add(0,20));
				tankbars.get(i).setBarDisplay((firetrucks.get(i).getCurrentWater()/ firetrucks.get(i).getMaxWater())*50);

				healthbars.get(i).setPosition(firetrucks.get(i).getCentre().add(0,25));
				healthbars.get(i).setBarDisplay((firetrucks.get(i).getHealthPoints()*50)/firetrucks.get(i).getMaxHealthPoints());
			}

			gameObjects.add(new FireStation(textures.getFireStation(), textures.getFireStationDead()));
			activeStatusIcons.add(freezeEnemiesIcon);
			activeStatusIcons.add(revivedFireTruckIcon);
			activeStatusIcons.add(rainDanceIcon);
			activeStatusIcons.add(timeIncreaseIcon);
			switchTrucks(truckNum);

			// DIFFICULTY_6 - START OF MODIFICATION - NP STUDIOS - BRUNO DAVIES
			//loops through all the firetrucks
			for (FireTruck truck : firetrucks) {
				//sets the new max health to the standard max health times by a multiplier for the difficulty selected.
				truck.setMaxHealthPointsForDifficulty((int)(truck.getMaxHealthPoints()*
						(double)difficultyStats[difficultyChosen][healthMultiplierIndex]));
			}
			// DIFFICULTY_6 - END OF MODIFICATION - NP STUDIOS - BRUNO DAVIES
			loadGame(); // calls the load game function which updates all health values, positions and stats as required.
			// SAVING_5 - END OF MODIFICATION  - NP STUDIOS - LUCY IVATT
			}
			gamecam.translate(new Vector2(currentTruck.getX(), currentTruck.getY())); // sets initial Camera position
			start = false;
			//MINIGAME_INTEGRATION - END OF MODIFICATION - NPSTUDIOS
		}

	/**
	 * new
	 * 
	 * Initialises each fortress
	 * 
	 * @param num the fortress number
	 */
	private void fortressInit(int num) {
		// UNIQUE_FORTRESS_HEALTH_DAMAGE_9 - START OF MODIFICATION  - NPSTUDIOS - CASSIE_LILLYSTONE ----
		Fortress tempFortress = new Fortress(fortressPositions.get(num), textures.getFortress(num), textures.getDeadFortress(num),
				fortressSizes.get(num), textures.getBullet(), fortressStats[num]); //Added the list of stats corresponding
		// to the fortress being made as a parameter to pass to instantiate a fortress
		// UNIQUE_FORTRESS_HEALTH_DAMAGE_9 - END OF MODIFICATION  - NPSTUDIOS ----

		gameObjects.add(tempFortress);
		fortresses.add(tempFortress);
		fortressHealthBars.add(new StatBar(new Vector2(fortressPositions.get(num).x, fortressPositions.get(num).y + 100), "Red.png", 10));
	}

	/**
	 * new
	 * 
	 * Initialises each truck
	 * 
	 * @param num the truck number
	 */
	private void firetruckInit(float x, float y, int num) {
		// STATBAR_REFACTOR_7 - START OF MODIFICATION  - NP STUDIOS - LUCY IVATT
		// Created statbars alongside the firetrucks and updated the firetruck creation to use the new constructor.
		firetrucks.add(new FireTruck(new Vector2(x, y), truckStats[num], textures.getTruck(num)));
		healthbars.add(new StatBar(new Vector2(x, y + 25), "Green.png", 3));
		tankbars.add(new StatBar(new Vector2(x, y + 20), "Blue.png", 3));
		// STATBAR_REFACTOR_7 - END OF MODIFICATION  - NP STUDIOS
	}

	/**
	 * Called every frame
	 */
	@Override
	public void render(float delta) {
		
		Gdx.input.setInputProcessor(pauseWindow.stage);  //Set input processor
		pauseWindow.stage.act();

		switch (state) {
			case RUN:
				if (Gdx.input.isKeyPressed(Keys.P) || Gdx.input.isKeyPressed(Keys.O) || Gdx.input.isKeyPressed(Keys.M)|| Gdx.input.isKeyPressed(Keys.ESCAPE)){
					pauseWindow.visibility(true);
					pause();
				}
				
				gameTimer -= delta;		//Decrement timer

				updateLoop(); //Update all game objects positions but does not render them as to be able to render everything as quickly as possible

				gameMap.renderRoads(gamecam); // Render the background roads, fields and rivers


				game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
				game.batch.setProjectionMatrix(gamecam.combined);	//Mic:only renders the part of the map where the camera is
				game.batch.begin(); // Game loop Start

				hud.update(delta);

				renderObjects(); // Renders objects specified in the UpdateLoop() called previously
				game.batch.end();

				//RENDER_ORDER - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE
				gameMap.renderBuildings(gamecam); // Renders the buildings and the foreground items which are not entities, moved below renderObjects() so the firetrucks can no longer drive on the roofs.
				//RENDER_ORDER - END OF MODIFICATION - NPSTUDIOS

				game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
				game.batch.setProjectionMatrix(gamecam.combined);	//Mic:only renders the part of the map where the camera is
				game.batch.begin();
				for (StatusIcon icon : activeStatusIcons) {
					icon.render(game.batch);
				}
				game.batch.end();

				hud.stage.draw();
				//MINIMAP_ADDITION_3 - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE
				if (mapToggle) {
					drawMinimap();
				}
				//MINIMAP_ADDITION_3 - END OF MODIFICATION - NPSTUDIOS
				pauseWindow.stage.draw();

				if (Kroy.debug) {
					DrawDebug(); //Draw all debug items as they have to be drawn outside the batch
				}

				break;
			case PAUSE:
				pauseWindow.stage.draw();
				clickCheck();
				break;
			case RESUME:
				pauseWindow.visibility(false);
				setGameState(GameScreenState.RUN);
				break;
			//MINIGAME_INTEGRATION - START OF MODIFICATION - NPSTUDIOS
			case MINIGAME:

				break;
			//MINIGAME_INTEGRATION - END OF MODIFICATION - NPSTUDIOS
			default:
				break;
		}
	}

	/**
	 * Updates all the active gameobjects and adds them to the render queue.
	 * Removes gameobjects from the active pool if they are marked for removal.
	 * Adds new gameobjects.
	 * Adds dead objects to render queue.
	 * Respawns the player if necessary.
	 */
	private void updateLoop() {
		//MINIMAP_ADDITION_5 - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE
		checkMapToggle(); // Calls a method to check if the user presses the key to toggle the minimap on or off.
		//MINIMAP_ADDITION_5 - END OF MODIFICATION - NPSTUDIOS
        updateStatusIcons();
		List<GameObject> toRemove = new ArrayList<GameObject>();
		List<Vector2> patrolPositions = new ArrayList<>();
		for (GameObject gObject : gameObjects) {	//Go through every game object
			gObject.update();						//Update the game object
			if (gObject.isRemove()) {				//Check if game object is to be removed
				toRemove.add(gObject);					//Set it to be removed
			}else {
				objectsToRender.add(gObject);
				//it doesn't need to be removed; check if it is a fortress
				if (gObject instanceof  Fortress) {
					//it is. mark down its position so we can spawn an entity there later
					patrolPositions.add(gObject.getCentre());
				}
			}
		}

		currentTruck.update();
		

		for (GameObject rObject : toRemove) {	//Remove game objects set for removal
			gameObjects.remove(rObject);
			if (rObject.isDisplayable()) {
				deadObjects.add(rObject);
			}
		}
		for (GameObject aObject : objectsToAdd) {		//Add game objects to be added
			gameObjects.add(aObject);
		}
		objectsToAdd.clear();	// Clears list as not to add new objects twice

		for (GameObject dObject : deadObjects) { // loops through the destroyed but displayed items (such as destroyed bases)
			objectsToRender.add(dObject);
		}
		if (currentTruck.isRemove()) {	//If the player is set for removal, respawn
			updateLives();
			
		}
		switchTrucks();

		lastPatrol += Gdx.graphics.getDeltaTime();
		//UR_PATROLS_2 - START OF MODIFICATION - NPSTUDIOS - BRUNO DAVIES
		if(numberOfPatrolsSpawned < difficultyStats[difficultyChosen][patrolMaxIndex]) {
			if (lastPatrol >= patrolUpdateRate) {
				lastPatrol = 0;

				//we should spawn a patrol near every fortress if it given it's been 10 secs.
				for (Vector2 position : patrolPositions) {

					//Randomize the positions a little bit
					float oldX = position.x;
					float oldY = position.y;
					float randX = (float) (oldX - 400 + Math.random() * 400);
					float randY = (float) (oldY - 400 + Math.random() * 400);


					gameObjects.add(new UFO(new Vector2(randX, randY), textures.getUFO(), textures.getBullet()));


				}
				numberOfPatrolsSpawned++;
			}
		}
		//UR_PATROLS_2 - END OF MODIFICATION - NPSTUDIOS - BRUNO DAVIES

		//POWERUPS_2 - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE
		timeSinceLastBoxSpawn += Gdx.graphics.getDeltaTime(); //increments the timer
		if (timeSinceLastBoxSpawn >= boxSpawnRate){
			spawnBox();//calls the powerup box spawning function when the the timer has reached the the spawn rate
		}
		//The code above is a timer for the powerup box spawning
		if (freezeEnemies){
			freezeTimer += Gdx.graphics.getDeltaTime();
			if (freezeTimer >= (15 + (5*(1 - difficultyChosen)))){ // The powerups are stronger on easy mode / weaker on harder difficulties
				freezePatrols(false); //the patrols unfreeze when the timer reaches the limit
			}
		}
		// The code above is a timer for how long the freeze patrols powerup lasts.
		//POWERUPS_2 - END OF MODIFICATION - NPSTUDIOS
	}

	/**
	 *
	 */
	public void spawnBox(){
		if (!boxSpawnLocations.isEmpty()){
			gameObjects.add(new PowerupBox(boxSpawnLocations.get(0)));
			boxSpawnLocations.remove(0);
		}
		timeSinceLastBoxSpawn = 0;
	}

	/**
	 *
	 * @param location
	 */
	public void addSpawnLocation(Vector2 location){
		boxSpawnLocations.add(location);
	}

	//MINIMAP_ADDITION_4 - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE

	/**
	 * NOTs the toggle boolean.
	 */
	public void checkMapToggle(){
		if (Gdx.input.isKeyJustPressed(Keys.T)){
			mapToggle = !mapToggle;
		}
	}
	/**
	 * Draws the minimap to the bottom left hand corner of the screen.
	 * The minimap shows all the game objects and the firetrucks.
	 */
	public void drawMinimap(){
		game.batch.begin();
		game.batch.draw(minimap, 2, 2, 394, 350);

		for (GameObject object : gameObjects) {
			if (object instanceof PowerupBox) {
				game.batch.draw(object.getTexture(), object.getX()/19, object.getY()/19, 20, 20);
			}else {
				game.batch.draw(object.getTexture(), object.getX() / 19, object.getY() / 19, object.getWidth() / 10,
						object.getHeight() / 10);
			}
		}
		// Draws the fortresses and patrols to a minimap scaled down to the in the bottom left corner.
		for (FireTruck truck : firetrucks) {
			if (truck.getHealthPoints() > 0) {
				game.batch.draw(truck.getTexture(), truck.getX() / 19, truck.getY() / 19, 20, 25);
			}
			//Draws the firetrucks on their relative position on the minimap. Size is not to scale to make their position obvious and clear.
		}

		game.batch.end();
	}
	//MINIMAP_ADDITION_4 - END OF MODIFICATION - NPSTUDIOS

	/**
	 * Renders the objects in "objectsToRender" then clears the list
	 */
	private void renderObjects() {

		for (FireTruck truck : firetrucks) {
			if(truck.isAlive()) {
			truck.render(game.batch);

			// STATBAR_REFACTOR_8 - START OF MODIFICATION  - NP STUDIOS - LUCY IVATT
			// Updates the statbars for each firetruck
			tankbars.get(firetrucks.indexOf(truck)).setPosition(truck.getCentre().add(0,20));
			tankbars.get(firetrucks.indexOf(truck)).setBarDisplay((truck.getCurrentWater()/ truck.getMaxWater())*50);

			healthbars.get(firetrucks.indexOf(truck)).setPosition(truck.getCentre().add(0,25));
			healthbars.get(firetrucks.indexOf(truck)).setBarDisplay((truck.getHealthPoints()*50)/truck.getMaxHealthPoints());

			healthbars.get(firetrucks.indexOf(truck)).render(game.batch);
			tankbars.get(firetrucks.indexOf(truck)).render(game.batch);
			// STATBAR_REFACTOR_8 - END OF MODIFICATION  - NP STUDIOS
			}
		}


		for (GameObject object : objectsToRender) {
			object.render(game.batch);
		}


		// FORTRESS_COUNT_FIX_1 - START OF MODIFICATION  - NP STUDIOS - LUCY IVATT
		// Previously the game was ending when only 3 fortresses were destroyed so we added this code to
		// fix the fortress count, ensuring the game completed at the correct point.
		int alive = 0;
		for (Fortress fortress : fortresses) {
			if(fortress.isAlive()) {
				alive++;
				fortress.render(game.batch);

				fortressHealthBars.get(fortresses.indexOf(fortress)).setPosition(fortress.getCentre().add(0, 100));
				fortressHealthBars.get(fortresses.indexOf(fortress)).setBarDisplay(fortress.getHealthPoints()*500/fortress.getMaxHealthPoints());
				fortressHealthBars.get(fortresses.indexOf(fortress)).render(game.batch);
			}
		}
		fortressesCount = alive;
		// FORTRESS_COUNT_FIX_1 - END OF MODIFICATION  - NP STUDIOS

		objectsToRender.clear();
	}

	/**
	 * Add a game object next frame
	 * @param gameObject gameObject to be added
	 */
	public void addGameObject(GameObject gameObject) {
		objectsToAdd.add(gameObject);
	}

	/**
	 * Allows external classes to access the player
	 * @return player
	 */
	public FireTruck getPlayer() {
		return currentTruck;
	}
	//POWERUPS_3 - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE

	/**
	 * Restores a dead firetruck back to the game with it's max health and water.
	 */
	public void resurrectTruck(){
		revivedFireTruck = true;
		for (FireTruck truck : firetrucks){
			if (!truck.isAlive()){ //iterates through the list of firetrucks looking for one marked as not alive.
				truck.setRemove(false);
				truck.setPosition(spawnPosition); //The restored firetruck's position is set to by the fire station.
				truck.setHealthPoints(truck.getMaxHealthPoints());
				truck.setCurrentWater(truck.getMaxWater());
				break; // The break stops the function from resurrect more than one firetruck.
			}
		}
	}

	/**
	 * Freezes or unfreezes the patrols.
	 * @param flag
	 */
	public void freezePatrols(Boolean flag){
		freezeEnemies = flag;
		freezeTimer = 0; //resets the timer of how long enemies are frozen for.
		for (GameObject obj : gameObjects){
			if (obj instanceof UFO) {
				((UFO) obj).setFrozen(flag); //iterates through all the game objects and when it's a patrol sets it to frozen.
			}
		}
	}

	/**
	 * Kills all currently spawned patrols.
	 */
	public void rainDance(){
		rainDance = true;
		for (GameObject obj : gameObjects){
			if (obj instanceof UFO) {
				obj.die();
			}
		}
	}

	/**
	 * Adds time to game's timer (the time until the fire station is destroyed).
	 * @param time
	 */
	public void addTime(float time){
		timeIncrease = true;
		gameTimer = gameTimer + time;
	}
	//POWERUPS_3 - END OF MODIFICATION - NPSTUDIOS

	//DIFFICULTY_FOR_POWERUPS - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE

	/**
	 *
	 * @return
	 */
	public int getDifiicultyChosesn(){
		return difficultyChosen;
	}
	//DIFFICULTY_FOR_POWERUPS - END OF MODIFICATION - NPSTUDIOS

	/**
	 * Draws all debug objects for one frame
	 */
	private void DrawDebug() {
		for (DebugDraw dObject : debugObjects) {
			dObject.Draw(gamecam.combined);
		}
		debugObjects.clear();
	}

	/**
	 * Draw a debug circle (outline)
	 * @param position Centre of the circle
	 * @param radius Radius of the circle
	 * @param lineWidth Width of the outline
	 * @param colour Colour of the line
	 */
	public void DrawCircle(Vector2 position, float radius, int lineWidth, Color colour) {
		if (Kroy.debug) {
			debugObjects.add(new DebugCircle(position, radius, lineWidth, colour));
		}
	}

	/**
	 * Draw a debug rectangle (outline)
	 * @param bottomLefiretrucks Bottom lefiretrucks point of the rectangle
	 * @param dimensions Dimensions of the rectangle (Width, Length)
	 * @param lineWidth Width of the outline
	 * @param colour Colour of the line
	 */
	public void DrawRect(Vector2 bottomLefiretrucks, Vector2 dimensions, int lineWidth, Color colour) {
		if (Kroy.debug) {
			debugObjects.add(new DebugRect(bottomLefiretrucks, dimensions, lineWidth, colour));
		}
	}

	/**
	 * Updates the position of the camera to have the truck centre
	 * Ensures it never goes out of bounds, including when zoomed
	 * It does this by limiting the bounds of the camera
	 * additionally moves icons at the same time as the camera if they are enabled
	 */ 
	public void updateCamera() {
		//currentTruck;
		float cameraX = Math.max(0.5f*Kroy.width*zoom, Math.min(currentTruck.getX(), 6884-(0.5f*Kroy.width*zoom)));
		float cameraY = Math.max(0.5f*Kroy.height*zoom, Math.min(currentTruck.getY(), 6043-(0.5f*Kroy.height*zoom)));
		gamecam.position.lerp(new Vector3(cameraX, cameraY,gamecam.position.z),0.1f);// sets the new camera position based on the current position of the FireTruck
        Vector2 iconPosition = new Vector2(gamecam.position.x - 260, gamecam.position.y + 460);
        if (timeIncrease) {
            timeIncreaseIcon.setPosition(iconPosition);
        }
        if (freezeEnemies){
			iconPosition = new Vector2 (gamecam.position.x + 250, gamecam.position.y + 460);
            freezeEnemiesIcon.setPosition(iconPosition);
        }
        if (rainDance){
			iconPosition = new Vector2 (gamecam.position.x + 300, gamecam.position.y + 460);
            rainDanceIcon.setPosition(iconPosition);
        }
        if (revivedFireTruck){
			iconPosition = new Vector2 (gamecam.position.x  + 350, gamecam.position.y + 460);
            revivedFireTruckIcon.setPosition(iconPosition);
        }
		gamecam.update();

	}

	@Override
	public void resize(int width, int height) {
		gameport.update(width, height);
	}

	@Override
	public void pause() {
		setGameState(GameScreenState.PAUSE);
	}

	@Override
	public void resume() {
		setGameState(GameScreenState.RESUME);
	}

	@Override
	public void hide() {}

	@Override
	public void dispose() {
		Kroy.mainGameScreen = null;
	}

	public void setGameState(GameScreenState s){
	    state = s;
	}

	public GameScreenState getGameScreenState() {
		return state;
	}

	/**
	 * @return  the list of active game objects
	 */
	public List<GameObject> getGameObjects(){
		return gameObjects;
	}
	
	/**
	 * @return the number of alive trucks
	 */
	public int getLives() {
		return lives;
	}

	/**
	 * Checks the pause buttons
	 */
	private void clickCheck() {
		//resume button
		pauseWindow.resume.addListener(new ClickListener() {
	    	@Override
	    	public void clicked(InputEvent event, float x, float y) {
	    		pauseWindow.visibility(false);
				resume();
	    	}
	    });

		//exit button
		pauseWindow.exit.addListener(new ClickListener() {
	    	@Override
	    	public void clicked(InputEvent event, float x, float y) {
	    		Gdx.app.exit();
	    	}
	    });

		// SAVING_6 - START OF MODIFICATION  - NP STUDIOS - LUCY IVATT
		// Added input handling for the save buttons in the pause menu.
		//save1 button
		pauseWindow.save1.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				saveGame(1);
			}
		});

		//save2 button
		pauseWindow.save2.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				saveGame(2);
			}
		});

		//save3 button
		pauseWindow.save3.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				saveGame(3);
			}
		});
		// SAVING_6 - END OF MODIFICATION  - NP STUDIOS - LUCY IVATT

		//menu button
		pauseWindow.menu.addListener(new ClickListener() {
	    	@Override
	    	public void clicked(InputEvent event, float x, float y) {
	    		pauseWindow.visibility(false);
	    		dispose();
	    		game.backToMenu();
	    		return;
	    	}
	    });
	}

	/**
	 * How many fortresses are left?
	 * @return Number of fortresses remaining
	 */
	public int fortressesLeft() {
		return fortressesCount;
	}

	/**
	 * Switch to the game over screen
	 * @param won Did the player reach the win state?
	 */
	public void gameOver(boolean won) {
		game.setScreen(new GameOverScreen(game, truckNum, won, difficultyChosen));
	}

	/**
	 * extended
	 * switch to next truck if currentTruck dies
	 */
	public void updateLives() {
		if (lives>1) {
			lives -= 1;
			if(firetrucks.get(0).isAlive()) {
				switchTrucks(0);
			}else if(firetrucks.get(1).isAlive()) {
				switchTrucks(1);
			}else if(firetrucks.get(2).isAlive()) {
				switchTrucks(2);
			}else if(firetrucks.get(3).isAlive()) {
				switchTrucks(3);
			}else if(firetrucks.get(4).isAlive()) {
				switchTrucks(4);
			}else if(firetrucks.get(5).isAlive()) {
				switchTrucks(5);
			}
		} else {
			gameOver(false);
		}
	}
	
	/**
	 * new
	 * switch to FireTruck number n by calling changeToTruck function
	 * @param n
	 */
	private void switchTrucks(int n) {
		changeToTruck(firetrucks.get(n));
	}

	/**
	 * new
	 * Check for inputs to switch between trucks.
	 * It only works if the truck that has chosen is alive
	 */
	private void switchTrucks() {
		if (Gdx.input.isKeyPressed(Keys.NUM_1)) {
			if(firetrucks.get(0).isAlive())
				changeToTruck(firetrucks.get(0));
			else {
				System.out.println("This truck is dead");
			}
		} 
		else if (Gdx.input.isKeyPressed(Keys.NUM_2)) {
			if(firetrucks.get(1).isAlive())
				changeToTruck(firetrucks.get(1));
			else {
				System.out.println("This truck is dead");
			}
		}
		else if (Gdx.input.isKeyPressed(Keys.NUM_3)) {
			if(firetrucks.get(2).isAlive())
				changeToTruck(firetrucks.get(2));
		}
		else if (Gdx.input.isKeyPressed(Keys.NUM_4)) {
			if(firetrucks.get(3).isAlive())
				changeToTruck(firetrucks.get(3));
			else {
				System.out.println("This truck is dead");
			}
		}
		else if (Gdx.input.isKeyPressed(Keys.NUM_5)) {
			if(firetrucks.get(4).isAlive())
				changeToTruck(firetrucks.get(4));
			else {
				System.out.println("This truck is dead");
			}
		}
		else if (Gdx.input.isKeyPressed(Keys.NUM_6)) {
			if(firetrucks.get(5).isAlive())
				changeToTruck(firetrucks.get(5));
			else {
				System.out.println("This truck is dead");
			}
		}

	}

	/**
	 * new
	 * Switches the camera to the specified truck.
	 *
	 * @param t The truck to switch to
	 */
	private void changeToTruck(FireTruck t) {
		currentTruck = t;
	}  

	/**
	 * @return hud
	 */
	public HUD getHud(){
		return hud;
	}

	public Vector2 getSpawnPosition() {
		return spawnPosition;
	}

	/**
	 * Saves all of the game data needed to the preferences file 'Kroy' and flushes to ensure this persists. - NP STUDIOS
	 * @param saveSlot 1, 2 or 3
	 */
	public void saveGame(int saveSlot) {
		String prefix = "SLOT_" + saveSlot + "_";
		for(int i = 0; i < 6; i++){
			saveData.putFloat((prefix + "FORTRESS_HEALTH_" + i), fortresses.get(i).getHealthPoints());
			saveData.putFloat((prefix + "TRUCK_HEALTH_" + i), firetrucks.get(i).getHealthPoints());
			saveData.putFloat((prefix + "TRUCK_WATER_" + i), firetrucks.get(i).getCurrentWater());
			saveData.putFloat((prefix + "TRUCK_X_POS_" + i), firetrucks.get(i).getPosition().x);
			saveData.putFloat((prefix + "TRUCK_Y_POS_" + i), firetrucks.get(i).getPosition().y);
			saveData.putBoolean((prefix + "UNLIMITED_WATER_" + i), firetrucks.get(i).isUnlimitedWater());
			saveData.putBoolean((prefix + "DEFENCE_UP_" + i), firetrucks.get(i).getDefenceUp());
			System.out.println(firetrucks.get(i).isUnlimitedWater());
		}
		saveData.putBoolean((prefix + "FREEZE_ENEMIES"), freezeEnemies);
		saveData.putBoolean((prefix + "RAIN_DANCE"), rainDance);
		saveData.putFloat((prefix + "GAME_TIME"), gameTimer);
		saveData.putInteger((prefix + "DIFFICULTY"), difficultyChosen);
		saveData.flush();
	}

	/**
	 * Accesses the prefences file 'Kroy' and gets the values saved in the saveSlot (either 1, 2 or 3) and applies them
	 * as needed. - NP STUDIOS
	 */
	public void loadGame() {
		if(saveSlot != 0) {
			String prefix = "SLOT_" + saveSlot + "_";
			for(int i = 0; i < 6; i++){
				fortresses.get(i).setHealthPoints(saveData.getFloat(prefix + "FORTRESS_HEALTH_" + i, fortressStats[i][0]));
				if(fortresses.get(i).getHealthPoints() <= 0) fortresses.get(i).die();
				firetrucks.get(i).setHealthPoints(saveData.getFloat(prefix + "TRUCK_HEALTH_" + i, firetrucks.get(i).getMaxHealthPoints()));
				if (firetrucks.get(i).getHealthPoints() <= 0) {
					firetrucks.get(i).die();
				}
				firetrucks.get(i).setCurrentWater(saveData.getFloat(prefix + "TRUCK_WATER_" + i, truckStats[i][2]));
				firetrucks.get(i).setPosition(new Vector2(saveData.getFloat(prefix + "TRUCK_X_POS_" + i,
						spawnPosition.x - 135 + (i * 50)),
						saveData.getFloat(prefix + "TRUCK_Y_POS_" + i,spawnPosition.y)));
				firetrucks.get(i).setUnlimitedWater(saveData.getBoolean((prefix + "UNLIMITED_WATER_" + i)));
				firetrucks.get(i).setDefenceUp(saveData.getBoolean(prefix + "DEFENCE_UP_" + i));
				firetrucks.get(i).update();
			}
			gameTimer = saveData.getFloat(prefix + "GAME_TIME", 300);
			hud.setTimer(saveData.getFloat(prefix + "GAME_TIME", 300));
			if (saveData.getBoolean((prefix + "FREEZE_ENEMIES"))) {
				freezePatrols(true);
			}
			if (saveData.getBoolean(prefix + "RAIN_DANCE")) {
				rainDance();
			}
		}
	}

	// FORTRESS_COUNT_FIX_2 - START OF MODIFICATION  - NP STUDIOS - LUCY IVATT
	// Deleted unused setters for fortressCount
	// FORTRESS_COUNT_FIX_2 - END OF MODIFICATION  - NP STUDIOS

    // FORTRESS_IMPROVEMENT_2 - START OF MODIFICATION  - NP_STUDIOS - CASSIE_LILLYSTONE----

	/**
	 * Returns an ArrayList containing all the fortresses- NP STUDIOS
	 */

	public ArrayList<Fortress> getFortresses(){
	    return fortresses;
    } //Added a getter which returns a list of fortresses, required for making fortress health improve over time
    // FORTRESS_IMPROVEMENT_2 - END OF MODIFICATION  - NP_STUDIOS----

	public int getFortressesCount() {
		return fortressesCount;
	}
}
