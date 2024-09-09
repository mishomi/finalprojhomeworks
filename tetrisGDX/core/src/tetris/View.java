package tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.awt.*;

import static com.badlogic.gdx.graphics.Color.*;

public class View {
	private SpriteBatch batch = new SpriteBatch();
	private BitmapFont font = new BitmapFont();
	static final int BOX_SIZE = 30;
	static final int ORIGIN = 50;
	private Graphics graphics;
	ShapeRenderer shapeRenderer = new ShapeRenderer();

	public View(Graphics graphics) {
		this.graphics = graphics;
	}

	public void draw(TetrisModel model) {
		drawData(new DrawDataParameter(model.state.field, 0, 0, true));
		drawData(new DrawDataParameter(model.state.figure, model.state.position.y(), model.state.position.x(), false));
		drawScore(model.state.score); // Draw the score
	}
	private void drawData(DrawDataParameter parameterObject) {

		for (int r = 0; r < parameterObject.fs.length; r++) {
			for (int c = 0; c < parameterObject.fs[r].length; c++) {
				if (!parameterObject.drawBackground && parameterObject.fs[r][c] == 0)
					continue;
				drawBox(r + parameterObject.row,c + parameterObject.col,parameterObject.fs[r][c]);
			}
		}
	}

	private void drawBox(int row, int col, int value) {
		graphics.drawBoxAt(ORIGIN + col * BOX_SIZE, ORIGIN + row * BOX_SIZE, BOX_SIZE, value);
	}
	private void drawScore(int score) {
		shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(BLACK);
		shapeRenderer.rect(5, 10, 100, 25);
		shapeRenderer.end();

		batch.begin();
		font.setColor(WHITE);
		font.draw(batch, "Score: " + score, 20, 35);
		batch.end();
	}


	public void dispose() {
		shapeRenderer.dispose();
		batch.dispose();
		font.dispose();
	}
}
