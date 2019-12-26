package com.dicycat.kroy.misc;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.dicycat.kroy.GameObject;
import com.dicycat.kroy.screens.GameScreen;

public class WaterStream extends GameObject{

	public WaterStream(Vector2 spawnPos) {
		super(spawnPos, new Texture("Blue.png"), new Vector2(1,1));
	}
	
	public void setRange(float x){
		sprite.setScale(x,2);
	}

	@Override
	public void Update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Render(SpriteBatch batch) {
		super.Render(batch);
		
	}

}