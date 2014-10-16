package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class MyGdxGame extends ApplicationAdapter implements ApplicationListener {

    // Met deze class kunnen we android/ios-specific code aanroepen
    private final NativeCode nativeCode;

    SpriteBatch batch;
    Texture img;

    Stage stage;
    Skin skin;
    TextButton button;

    BitmapFont font;
    String message = "...";

    int screenWidth, screenHeight;


    public MyGdxGame(NativeCode nativeCode) {
        this.nativeCode = nativeCode;
    }

	@Override
	public void create () {
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

		batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");

        // Set up
        stage = new Stage();
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        font = new BitmapFont(Gdx.files.internal("data/default.fnt"));

        button = new TextButton("Ga terug", skin);
        button.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("touch down");
                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("touch up");
                nativeCode.openLobby();
            }
        });
        button.setPosition(screenWidth / 2, screenHeight / 2);
        button.setHeight(150);
        button.setWidth(300);
        stage.addActor(button);

        // Set up the event listeners
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(gestureDetector);
        inputMultiplexer.addProcessor(inputProcessor);

        Gdx.input.setInputProcessor(inputMultiplexer);

        // doeIets() voert de code uit afhankelijk van het platform (Voor ons Android natuurlijk)
        nativeCode.doeIets();
	}

    @Override
    public void render () {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(img, 0, 0); // test image
        font.draw(batch, message, 20, screenHeight - 20); // debug message
        batch.end();

        stage.draw();
    }


    /**
     * The GestureListener can signal whether it consumed the event or wants it to be passed on
     * to the next InputProcessor by returning either true or false respectively from its methods.
     * @see <a href="https://github.com/libgdx/libgdx/wiki/Gesture-detection">link</a>
     */
    GestureDetector gestureDetector = new GestureDetector(new GestureListener() {

        private float lastDistance;
        private float lastInitialDistance;

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            lastDistance = 0;
            System.out.println("touchdown " + x + " " + y);
            return false;
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            message = "Tap performed, finger" + Integer.toString(button);
            return false;
        }

        @Override
        public boolean longPress(float x, float y) {
            message = "Long press performed";
            return false;
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            message = "Fling performed, velocity:" + Float.toString(velocityX) +
                      "," + Float.toString(velocityY);
            System.out.println("fling " + velocityX + " " + velocityY);
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            return false;
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            return false;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {

            if(initialDistance != lastInitialDistance) {
                // The starting point of the drag.
                lastDistance = initialDistance;
                lastInitialDistance = initialDistance;
            }
            else {
                // The current drag
                float distanceMoved = distance - lastDistance;
                message = "Zoom performed, initial Distance:" + Float.toString(initialDistance) +
                        " Distance: " + Float.toString(distance) +
                        " - distanceMoved: " + distanceMoved;


                button.setWidth(Math.max(40.0f, button.getWidth() + distanceMoved));
                button.setHeight(Math.max(20.0f, button.getHeight() + (distanceMoved / 2)));

                lastDistance = distance;
            }
            return false;
        }

        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
            message = "Pinch performed";
            return false;
        }
    });

    static int pointerCount = 0;
    InputProcessor inputProcessor = new InputProcessor() {
        @Override
        public boolean keyDown(int keycode) {
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if(pointer == 0) {
                lastX = screenX;
                lastY = screenY;
            }

            pointerCount++;
            Gdx.app.log("Pointer", "Pointer count: " + pointerCount);
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            pointerCount--;
            return false;
        }

        int lastX, lastY;
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {

            if(pointerCount == 1 && pointer == 0) {
                int movedX = screenX - lastX;
                int movedY = lastY - screenY;
               // Gdx.app.log("touch", "Dragged: " + screenX + " " + screenY + " pointer " + pointer + "  new xy: " + newX + "," + newY);

                button.setPosition(button.getX() + movedX, button.getY() + movedY);

                lastX = screenX;
                lastY = screenY;
            }
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }

        @Override
        public boolean scrolled(int amount) {
            return false;
        }
    };




    @Override
    public void dispose () {
        if(stage != null) stage.dispose();
        if(skin != null) skin.dispose();
    }
}
