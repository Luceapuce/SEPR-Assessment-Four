package com.dicycat.kroy.bullets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

/**
 * Stores patterns of bullets to fire
 * 
 * @author Riju De
 *
 */
public class Pattern {
	private Bullet[][] bullets;	//Bullets to fire
	private float waitTime;		//Time between bullets
	private Boolean aim;		//Should the bullets be targeted towards the player
	private float cooldown; 		//Time to wait after firing pattern
	private int offset;
	private int xtra;

	
	/**
	 * Create a static directional pattern, fires in a single defined direction
	 * @param degree Direction to shoot
	 * @param speed Speed of the bullets
	 * @param range Distance each bullet travels
	 * @param timeBetweenShots Time before the next shot
	 * @param patternLength How many shots in the pattern
	 * @param multi How many bullets per shot (spread)
	 * @param cooldown Time after pattern to wait before firing the next pattern
	 * @param damage Amount of damage to be dealt
	 */

	// UNIQUE_FORTRESS_HEALTH_DAMAGE_4 - START OF MODIFICATION  - NPSTUDIOS - CASSIE_LILLYSTONE ---
	//For all Pattern constructors, added fortressDamage as a parameter and as a parameter for the instantiation of the bullets
	// Also set aim to true for the first pattern as this would just fire straight down and not target the user
	public Pattern(int degree, int speed, int range, float timeBetweenShots, int patternLength, int multi, float cooldown,
				   Texture bulletTexture, float damage) {
		aim = true;
		waitTime = timeBetweenShots;
		bullets = new Bullet[patternLength][multi];
		this.cooldown = cooldown; 
		offset = (multi - (multi % 2)) / 2;
		xtra = (1-(multi % 2)) * 5;
		degree = 90 - degree;	//Convert normal bearings (0 is up, clockwise) to LIBGDX Vector2 degrees (0 is right, anti-clockwise)

		Vector2 direction;
		for (int i = 0; i < patternLength; i++) {
			for (int j = 0; j < multi; j++) {
				direction = new Vector2(1, 1);
				direction.setAngle(degree + ((j - offset) * 10) + xtra);
				bullets[i][j] = new Bullet(Vector2.Zero, direction, speed, range, bulletTexture, damage); //Create bullet
			}
		}
	}
	
	/**
	 * Create an aimed pattern
	 * @param speed Speed of the bullets
	 * @param range Distance each bullet travels
	 * @param timeBetweenShots Time before the next shot
	 * @param patternLength How many shots in the pattern
	 * @param multi How many bullets per shot (spread)
	 * @param cooldown Time after pattern to wait before firing the next pattern
	 * @param fortressDamage Amount of damage to be dealt by fortress
	 */
	public Pattern(int speed, int range, float timeBetweenShots, int patternLength, int multi, float cooldown,
				   Texture bulletTexture, float fortressDamage) {
		this.aim = true;
		waitTime = timeBetweenShots;
		bullets = new Bullet[patternLength][multi];
		this.cooldown = cooldown;
		offset = (multi - (multi % 2)) / 2;
		xtra = (1-(multi % 2)) * 5;

		Vector2 direction = Vector2.Zero;
		for (int i = 0; i < patternLength; i++) {
			for (int j = 0; j < multi; j++) {
				bullets[i][j] = new Bullet(Vector2.Zero, direction, speed, range, bulletTexture, fortressDamage); //Create bullet
			}
		}
	}

	/**
	 * Create a spiral pattern
	 * @param clockwise Should the spiral spin clockwise?
	 * @param startAngle Starting direction of the spiral
	 * @param rotations How many full rotations to perform
	 * @param speed Speed of the bullets
	 * @param range Distance each bullet travels
	 * @param timeBetweenShots Time before the next shot
	 * @param multi How many bullets per shot (spread)
	 * @param cooldown Time after pattern to wait before firing the next pattern
	 * @param fortressDamage Amount of damage to be dealt by fortress
	 */
	public Pattern(Boolean clockwise, int startAngle, int rotations, int speed, int range, float timeBetweenShots,
				   int multi, float cooldown, Texture bulletTexture, float fortressDamage) {
		aim = false;
		waitTime = timeBetweenShots;
		int patternLength = rotations * 36;
		bullets = new Bullet[patternLength][multi];
		this.cooldown = cooldown;
		offset = (multi - (multi % 2)) / 2;
		xtra = (1-(multi % 2)) * 5;

		int degree;	//Convert normal bearings (0 is up, clockwise) to LIBGDX Vector2 degrees (0 is right, anti-clockwise)

		Vector2 direction = Vector2.Zero;
		for (int i = 0; i < patternLength; i++) {
			degree = (clockwise) ? 10 : -10;
			degree = 90 - (i*degree) + startAngle;
			for (int j = 0; j < multi; j++) {
				direction = new Vector2(1, 1);
				direction.setAngle(degree + ((j - offset) * 10) + xtra);
				bullets[i][j] = new Bullet(Vector2.Zero, direction, speed, range, bulletTexture, fortressDamage); //Create bullet
			}
		}
	}

	// UNIQUE_FORTRESS_HEALTH_DAMAGE_4 - END OF MODIFICATION  - NPSTUDIOS ---

	/**
	 * @param set The set of bullets to fire
	 * @return Set of bullets to fire
	 */
	public Bullet[] bulletSet(int set) {
		return bullets[set];
	}

	/**
	 * @param set The set of bullets to fire
	 * @param aimDir The direction the bullets should fire
	 * @return Set of aimed bullets to fire
	 */
	public Bullet[] aimedSet(int set, Vector2 aimDir) {
		Vector2 direction;
		for (int i = 0; i < bullets[set].length; i++) {
			direction = new Vector2(1, 1);
			direction.setAngle(aimDir.angle() + ((i - offset) * 10) + xtra);
			bullets[set][i].changeDirection(direction);
		}
		return bullets[set];
	}

	//Getters
	public Boolean getAim() { return aim; }	
	public Bullet[][] getBullets(){return bullets;}
	public float getWaitTime(){return waitTime;}
	public float getCooldown(){return cooldown;}
}
