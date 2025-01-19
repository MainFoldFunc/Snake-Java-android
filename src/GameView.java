import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    // Game dimensions and variables
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 40;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
    static final int DELAY = 80;

    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];

    int bodyParts = 5;
    int applesEaten;
    int appleX;
    int appleY;

    char direction = 'R'; // R - right, L - left, U - up, D - down
    boolean running = false;

    Handler handler;
    Random random;
    SurfaceHolder surfaceHolder;
    Paint paint;

    public GameView(Context context) {
        super(context);
        random = new Random();
        surfaceHolder = getHolder();
        paint = new Paint();
        paint.setColor(Color.GREEN);
        handler = new Handler();

        this.setFocusable(true);
    }

    @Override
    public void run() {
        startGame();

        while (running) {
            long startTime = System.currentTimeMillis();

            move();
            checkApple();
            checkCollisions();

            // Draw the game
            if (surfaceHolder.getSurface().isValid()) {
                Canvas canvas = surfaceHolder.lockCanvas();
                draw(canvas);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }

            long endTime = System.currentTimeMillis();
            long deltaTime = endTime - startTime;
            if (deltaTime < DELAY) {
                try {
                    Thread.sleep(DELAY - deltaTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startGame() {
        newApple();
        running = true;
        new Thread(this).start();
    }

    public void draw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);  // Set background color to black
        if (running) {
            // Draw apple
            paint.setColor(Color.RED);
            canvas.drawRect(appleX, appleY, appleX + UNIT_SIZE, appleY + UNIT_SIZE, paint);

            // Draw snake
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    paint.setColor(Color.GREEN); // Head of the snake
                } else {
                    paint.setColor(new Color(33, 24, 122)); // Body of the snake
                }
                canvas.drawRect(x[i], y[i], x[i] + UNIT_SIZE, y[i] + UNIT_SIZE, paint);
            }

            // Display score
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            canvas.drawText("Score: " + applesEaten, SCREEN_WIDTH / 2 - 150, SCREEN_HEIGHT / 2 - 250, paint);

        } else {
            gameOver(canvas, applesEaten);
        }
    }

    public void gameOver(Canvas canvas, int applesEaten) {
        paint.setColor(Color.WHITE);
        paint.setTextSize(75);
        canvas.drawText("Game Over", SCREEN_WIDTH / 2 - 150, SCREEN_HEIGHT / 2, paint);

        paint.setTextSize(50);
        canvas.drawText("Score: " + applesEaten, SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2 + 75, paint);
    }

    public void newApple() {
        appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
        appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
    }

    public void move() {
        // Shift the body parts
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        // Move the head
        switch (direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;
        }
    }

    public void checkApple() {
        if (x[0] == appleX && y[0] == appleY) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    public void checkCollisions() {
        // Check if the head collides with the body
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false;
            }
        }

        // Check if the head touches the wall
        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

        // Stop the game if there was a collision
        if (!running) {
            handler.postDelayed(() -> {
                // Optionally restart the game or show a "Game Over" message
            }, 2000);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Handle touch input for direction control (optional)
        // e.g. swipe to change direction
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (direction != 'R') {
                    direction = 'L';
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (direction != 'L') {
                    direction = 'R';
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (direction != 'D') {
                    direction = 'U';
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (direction != 'U') {
                    direction = 'D';
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}

