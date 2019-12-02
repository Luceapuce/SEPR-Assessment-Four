package com.dicycat.kroy.screens;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dicycat.kroy.GameObject;
import com.dicycat.kroy.GameTextures;
import com.dicycat.kroy.Kroy;
import com.dicycat.kroy.debug.DebugCircle;
import com.dicycat.kroy.debug.DebugDraw;
import com.dicycat.kroy.debug.DebugLine;
import com.dicycat.kroy.debug.DebugRect;
import com.dicycat.kroy.entities.FireTruck;
import com.dicycat.kroy.entities.UFO;
import com.dicycat.kroy.scenes.HUD;

public class GameScreen implements Screen{
	public static GameScreen mainGameScreen;
	public GameTextures textures;
	
	Boolean showDebug = true;
	
	Kroy game;
	private OrthographicCamera gamecam;	//m 	//follows along what the port displays
	private Viewport gameport; 	//m
	private HUD hud;	//m
	
	FireTruck player; //Reference to the player
	List<GameObject> gameObjects;	//List of active game objects
	List<GameObject> toAdd;
	List<DebugDraw> debugObjects; //List of debug items
	
	public float gameTimer; //Timer to destroy station
	
	public GameScreen(Kroy _game) {
		game = _game;
		gamecam = new OrthographicCamera();    //m
		gameport = new ScreenViewport(gamecam);	//m //Mic:could also use StretchViewPort to make the screen stretch instead of adapt
		hud = new HUD(game.batch);						//or FitPort to make it fit into a specific width/height ratio
		textures = new GameTextures();
		gameTimer = 60 * 15; //Set timer to 15 minutes
		if (mainGameScreen == null) {
			mainGameScreen = this;
		}
		else {
			System.err.println("Duplicate GameScreens");
		}
	}
	
	@Override
	public void show() {	//Screen first shown
		toAdd = new ArrayList<GameObject>();
		gameObjects = new ArrayList<GameObject>();
		debugObjects = new ArrayList<DebugDraw>();
		player = new FireTruck(new Vector2(-50, -50));
		gameObjects.add(player);	//Player	//Mic:modified from (100, 100) to (0, 0)
		gameObjects.add(new UFO(new Vector2(0, 200)));	//UFO	//Mic:modified from (480,580) to (0, 200)
		//gameObjects.add(new Bullet(this, new Vector2(10, 10), new Vector2(1,5), 50, 500));	//Bullet
		
	}

	@Override
	public void render(float delta) {		//Called every frame
		Gdx.gl.glClearColor(.47f, .66f, .29f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
		hud.stage.draw();
		
		game.batch.setProjectionMatrix(gamecam.combined);	//Mic:only renders the part of the map where the camera is
		game.batch.begin(); // Game loop Start

		gameTimer -= delta;
		if (gameTimer <= 0) {
			//Destroy station
			System.err.println("Timer!");	//Temp test
		}
		
		UpdateLoop();	//Update all game objects
		
		game.batch.end();
		
		System.out.println("Render calls:" + game.batch.renderCalls + " | FPS:" + Gdx.graphics.getFramesPerSecond());
		
		if (showDebug) {
			DrawDebug(); //Draw all debug items as they have to be drawn outside the batch
		}
	}
	
	//region Game Logic
	private void UpdateLoop() {
		List<GameObject> toRemove = new ArrayList<GameObject>();
		for (GameObject gObject : gameObjects) {	//Go through every game object
			gObject.Update();//Update the game object
			if (gObject.CheckRemove()) {				//Check if game object is to be removed
				toRemove.add(gObject);					//Set it to be removed
			}else {
				gObject.Render(game.batch);
			}
		}
		for (GameObject rObject : toRemove) {	//Remove game objects set for removal
			gameObjects.remove(rObject);
		}
		for (GameObject aObject : toAdd) {		//Add game objects to be added
			gameObjects.add(aObject);
		}
		toAdd.clear();
	}
	
	public void AddGameObject(GameObject gameObject) {	//Add a game object next frame
		toAdd.add(gameObject);
	}
	
	public FireTruck GetPlayer() {
		return player;
	}
	
	private void DrawDebug() {		//Draws all debug objects for one frame
		for (DebugDraw dObject : debugObjects) {
			dObject.Draw(gamecam.combined);
		}
		debugObjects.clear();
	}
	
	public void DrawLine(Vector2 start, Vector2 end, int lineWidth, Color colour) {
		debugObjects.add(new DebugLine(start, end, lineWidth, colour));
	}
	
	public void DrawCircle(Vector2 position, float radius, int lineWidth, Color colour) {
		debugObjects.add(new DebugCircle(position, radius, lineWidth, colour));
	}

	
	public void DrawRect(Vector2 centre, Vector2 dimensions, int lineWidth, Color colour) {
		debugObjects.add(new DebugRect(centre, dimensions, lineWidth, colour));
	}

	@Override
	public void resize(int width, int height) {			
		gameport.update(width, height);				//m
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		game.batch.dispose();
	}

}
