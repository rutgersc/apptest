package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class GameLauncher extends Game {
    private OrthographicCamera camera;
    private final NativeCode nativeCode;

    public SpriteBatch batch;
    public BitmapFont font;

    public GameLauncher(NativeCode nativeCode) {
        this.nativeCode = nativeCode;
    }

	@Override
	public void create () {
        batch = new SpriteBatch();
        //Use LibGDX's default Arial font.
        font = new BitmapFont();
        this.setScreen(new StartBucketGame(this));
	}

    public void render() {
        super.render(); //important!
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
