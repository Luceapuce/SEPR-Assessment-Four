package com.dicycat.kroy;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dicycat.kroy.screens.GameScreen;
import com.dicycat.kroy.screens.MenuScreen;
import com.dicycat.kroy.screens.MinigameScreen;

/**
 * Main game class
 * 
 * @author Riju De
 * @author Sam Hutchings
 *
 */

public class Kroy extends Game {
	// GAME_RESIZE - START OF MODIFICATION  - NP STUDIOS - LUCY IVATT
	// Increased the games width and height values
	public static final int width = 1820;
	public static final int height = 980;
	// GAME_RESIZE - END OF MODIFICATION  - NP STUDIOS

	public static boolean debug = false;
	// GAME_RESIZE - END OF MODIFICATION  - NP STUDIOS
	
	public static GameScreen mainGameScreen;
	public static MenuScreen mainMenuScreen;
	public static MinigameScreen mainMinigameScreen;
	public static Preferences saveData;
	public SpriteBatch batch;
	
	@Override
	public void create () {
		// HIGHSCORE_1 - START OF MODIFICATION - NP STUDIOS - LUCY IVATT
		// Initializes the save file which the highscore will be accessed from.
		saveData = Gdx.app.getPreferences("Kroy");
		// HIGHSCORE_1 - END OF MODIFICATION - NP STUDIOS - LUCY IVATT
		batch = new SpriteBatch();
		mainMenuScreen = new MenuScreen(this);
		this.setScreen(mainMenuScreen);
	}

	@Override
	public void render () {
		// WARPING_FIX - START OF MODIFICATION  - NP STUDIOS - LUCY IVATT
		// The previous group limited the camera from reaching the edge of the map instead of fixing the warping issue.
		// Because of this, if you use the zoom out function when near the edge of the map the warping was still visible.
		// We added the two lines of code needed to clear the frame before the next frame is written which is what
		// was causing the issue.
		Gdx.gl.glClearColor( 0, 0, 0, 1 );
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );
		// WARPING_FIX - END OF MODIFICATION  - NP STUDIOS
		super.render();
	}
	
	@Override
	public void dispose () {}
	
	/**
	 * Call to generate a new GameScreen instance which runs a new game. Saveslot is not needed
	 * as this is created a new game and not loading it, therefore set to 0.
	 */
	public void newGame(int saveSlot, int difficultyChosen) {
		// DIFFICULTY_3 - START OF MODIFICATION - NP STUDIOS - BRUNO DAVIES
		mainGameScreen = new GameScreen(this,0, difficultyChosen, saveSlot);// Initialise new game
		// DIFFICULTY_3 - END OF MODIFICATION - NP STUDIOS - BRUNO DAVIES
		setScreen(mainGameScreen);// Display new game
	}

	// SAVING_1 - START OF MODIFICATION  - NP STUDIOS - LUCY IVATT
	// Added load game function
	/**
	 * Creates a new game instance and passes the saveslot number the game will need to load values from.  - NP STUDIOS
	 * @param saveSlot the slot to load the game from either 1, 2 or 3
	 */
	public void loadGame(int saveSlot) {
		mainGameScreen = new GameScreen(this, 0, saveData.getInteger("SLOT_" + saveSlot + "_" + "DIFFICULTY"), saveSlot);
		setScreen(mainGameScreen); // Display new game
	}
	// SAVING_1 - END OF MODIFICATION  - NP STUDIOS

	//MINIGAME_INTEGRATION_1 - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE
	public void newMinigame(boolean flag) {
		mainMinigameScreen = new MinigameScreen(this, flag);// Initialise new minigame
		setScreen(mainMinigameScreen);// Display new minigame
	}
	//MINIGAME_INTEGRATION_1 - END OF MODIFICATION - NPSTUDIOS

	/**
	 * Return back to the menu screen
	 */
	public void backToMenu() {
		mainMenuScreen.state = MenuScreen.MenuScreenState.MAINMENU; // sets menu screen back to the original state
		mainMenuScreen.setCurrentlyRunningGame(false); //Tells the screen not to block any button pushes which would initialise a new game again
		setScreen(mainMenuScreen); // displays the menu screen
	}

	//MINIGAME_INTEGRATION_2 - START OF MODIFICATION - NPSTUDIOS - BETHANY GILMORE
	public void backToGame(){
		mainGameScreen.setGameState(GameScreen.GameScreenState.RUN);
		setScreen(mainGameScreen);
	}
	//MINIGAME_INTEGRATION_2 - END OF MODIFICATION - NPSTUDIOS
	
	/**
	 * Centre of the screen width
	 * @return centre of the screen width
	 */
	public static int CentreWidth() {
		return width / 3;
	}

	// HIGHSCORE_2 - START OF MODIFICATION - NP STUDIOS - LUCY IVATT
	// Updated the setHighscore method to save the value to a 'Preferences' file which will persist between games, i.e
	// will save the highscore even when the game has been closed. Also updated the getHighScore method to access this
	// file.
	/**
	 * Saves the highscore to the games saved data file - updated by NP STUDIOS
	 * @param highScore The new high score
	 */
	public void setHighScore(Integer highScore) {
		if (highScore > saveData.getInteger("highscore", 0)) { // If the new score is bigger than the saved highscore
			saveData.putInteger("highscore", highScore); // replace the saved highscore with the newly achieves score
			saveData.flush(); } // updates the file
	}

	/**
	 * Get the current high score
	 * @return highScore
	 */
	public Integer getHighScore() {
		return saveData.getInteger("highscore", 0); // accesses high-score file and returns integer value
	}
	// HIGHSCORE_2 - END OF MODIFICATION - NP STUDIOS - LUCY IVATT
}
