package com.dicycat.kroy.bullets;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.dicycat.kroy.entities.Entity;
import com.dicycat.kroy.screens.GameScreen;

public class BulletDispenser {

	List<Pattern> patterns;	//Stores all patterns
	Pattern firingPattern; 	//Current pattern firing
	Entity owner;			//Entity the bulletDispnser is attached to
	
	int currentPattern;		//Current pattern to fire
	int currentBullet;		//Current bullet to fire
	float patternTime;		//Time between firing patterns
	float patternTimer;		//Time since last pattern
	float bulletTimer;		//Time since last bullet
	
	public BulletDispenser(Entity creator) 		//Constructor
	{
		owner = creator;
		patterns = new ArrayList<Pattern>();
		currentPattern = 0;
		bulletTimer = 0;
		patternTimer = 0;
	}
	
	public void AddPattern(Pattern pattern) {	//Add a pattern to the bullet dispensers arsenal
		patterns.add(pattern);
		if (patterns.size() == 1) {	//If only pattern, set as firing pattern
			firingPattern = patterns.get(0);
			patternTime = firingPattern.Cooldown();
		}
	}
	
	public Bullet[] Update(Boolean fire) {		//Called every frame
		if (patterns.size() == 0) {	//No patterns -> no checks required
			return null;
		}
		patternTimer += Gdx.graphics.getDeltaTime();	//Increment timers by time passed
		bulletTimer += Gdx.graphics.getDeltaTime();
		
		//If should be firing, find any bullets that should be fired this frame
		//Then reset and timers and increment bullet/pattern as needed
		if (fire && patternTimer >= patternTime) {		
			if (bulletTimer >= firingPattern.WaitTime()) {
				bulletTimer = 0;
				Bullet[] toFire;	//Stores bullets to be fired
				if (firingPattern.Aim()) {
					Vector2 targetDirection = new Vector2(GameScreen.mainGameScreen.GetPlayer().GetCentre().x - owner.GetCentre().x, GameScreen.mainGameScreen.GetPlayer().GetCentre().y - owner.GetCentre().y); //Aim from entity to player
					
					toFire = firingPattern.AimedSet(currentBullet, targetDirection);
				}else {
					toFire = firingPattern.BulletSet(currentBullet);
				}
				currentBullet++;
				if (currentBullet >= firingPattern.Bullets().length) {
					currentPattern++;
					currentBullet = 0;
					patternTime = firingPattern.Cooldown();
					patternTimer = 0;
					if (currentPattern >= patterns.size()) {
						currentPattern = 0;
					}
					firingPattern = patterns.get(currentPattern);
				}
				return toFire;
			}
		}
		
		return null;	//Not firing/no bullets
	}
}

