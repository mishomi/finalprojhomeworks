package tetris;

import java.util.ArrayList;
import java.util.List;

public class Model {
	static final int FigToDropForNextLevel = 33;
	static final int MaxLevel = 7;
	static final int Width = 7;
	static final int Depth = 15;

	public int level;
	public int removedCellsCounter;
	public int Score;
	public int DropScore;
	public Figure Fig;
	public int[][] newField;
	public int[][] oldField;
	public boolean noChanges = true;
	
	List<ModelListener> listeners = new ArrayList<>();

	public void dropFigure(Figure f) {
		int zz;
		if (f.y < Model.Depth - 2) {
			int oldX = Fig.x;
			int oldY = Fig.y;
			zz = Model.Depth;
			while (newField[f.x][zz] > 0)
				zz--;
			DropScore = (((level + 1) * (Model.Depth * 2 - f.y - zz) * 2) % 5) * 5;
			f.y = zz - 2;
			fireFigureMovedEvent(oldX, oldY);
			processFigureCantMoveDown();
		}
	}

	void checkIfFieldIsFull() {
		int i;
		for (i = 1; i <= Model.Width; i++) {
			if (newField[i][3] > 0) {
				fireGameOver();
				return;
			}
		}
		createNewFigure();
	}

	private void fireGameOver() {
		for (ModelListener modelListener : listeners) {
			modelListener.gameOver();
		}
	}

	public void initModel() {
		newField = new int[Model.Width + 2][Model.Depth + 2];
		oldField = new int[Model.Width + 2][Model.Depth + 2];
	}

	void packField() {
		for (int i = 1; i <= Model.Width; i++) {
			int n = Model.Depth;
			for (int j = Model.Depth; j > 0; j--) {
				if (oldField[i][j] > 0) {
					newField[i][n] = oldField[i][j];
					n--;
				}
			}
			;
			for (int k = n; k > 0; k--)
				newField[i][k] = 0;
		}
		fireFieldUpdated();
	}

	private void fireFieldUpdated() {
		for (ModelListener modelListener : listeners) {
			modelListener.fieldUpdated(newField);
		}
	}

	public void initMembers() {
		level = 0;
		Score = 0;
		removedCellsCounter = 0;
	}

	public void initMatrixes() {
		for (int i = 0; i < Model.Width + 1; i++) {
			for (int j = 0; j < Model.Depth + 1; j++) {
				newField[i][j] = 0;
				oldField[i][j] = 0;
			}
		}
	}

	void pasteFigure(Figure f) {
		int[] c = f.getCells();
		newField[f.x][f.y] = c[0];
		newField[f.x][f.y + 1] = c[1];
		newField[f.x][f.y + 2] = c[2];
		fireFieldUpdated();
	}

	boolean mayFigureSlideDown() {
		return (Fig.y < Model.Depth - 2) && (newField[Fig.x][Fig.y + 3] == 0);
	}

	final static int[][] SHIFTS = { { 0, -1, 0, 1 }, { -1, 0, 1, 0 }, { -1, -1, 1, 1 }, { 1, -1, -1, 1 }, };

	void testField() {
		copyField();
		for (int i = 1; i <= Model.Depth; i++) {
			for (int j = 1; j <= Model.Width; j++) {
				if (newField[j][i] > 0) {
					for (int[] s : SHIFTS) {
						if (!checkNeighbours(j + s[0], i + s[1], j + s[2], i + s[3], i, j))
							continue;
						for (ModelListener modelListener : listeners) {
							modelListener.foundNeighboursAt(j + s[0], i + s[1], j + s[2], i + s[3], i, j);
						}
					}
				}
			}
		}
	}

	private void copyField() {
		for (int i = 1; i <= Model.Depth; i++) {
			for (int j = 1; j <= Model.Width; j++) {
				oldField[j][i] = newField[j][i];
			}
		}
	}

	void trySlideDown() {
		if (mayFigureSlideDown()) {
			Fig.y++;
			fireFigureMovedEvent(Fig.x, Fig.y - 1);
		} else {
			processFigureCantMoveDown();
		}
	}

	public void addListener(ModelListener listener) {
		listeners.add(listener);
	}

	boolean areNeighbours(int a, int b, int c, int d, int i, int j) {
		return (newField[j][i] == newField[a][b]) && (newField[j][i] == newField[c][d]);
	}

	boolean checkNeighbours(int a, int b, int c, int d, int i, int j) {
		if (!areNeighbours(a, b, c, d, i, j))
			return false;
		oldField[a][b] = 0;
		oldField[j][i] = 0;
		oldField[c][d] = 0;
		noChanges = false;
		Score += (level + 1) * 10;
		removedCellsCounter++;
		return true;
	}

	void changeScore() {
		Score += DropScore;
		for (ModelListener modelListener : listeners) {
			modelListener.scoreHasChanged(Score);
		}

	}

	private void changeLevel(int change) {
		level += change;
		for (ModelListener modelListener : listeners) {
			modelListener.levelHasChanged(level);
		}

	}

	void descreaseLevel() {
		changeLevel(-1);
	}

	void increaseLevel() {
		changeLevel(1);
	}

	void removeSimilarNeighboringCells() {
		noChanges = true;
		testField();
		if (noChanges)
			return;
		packField();
		changeScore();
		if (removedCellsCounter < Model.FigToDropForNextLevel)
			return;
		removedCellsCounter = 0;
		if (level < Model.MaxLevel) {
			increaseLevel();
		}
	}

	void tryMoveLeft() {
		int oldX = Fig.x;
		int oldY = Fig.y;
		if ((Fig.x > 1) && (newField[Fig.x - 1][Fig.y + 2] == 0)) {
			Fig.x--;
			fireFigureMovedEvent(oldX, oldY);
	//			moveAndDrawFigure(oldX, oldY);
		}
	}

	void tryMoveRight() {
		int oldX = Fig.x;
		int oldY = Fig.y;
		if ((oldX < Model.Width) && (newField[oldX + 1][oldY + 2] == 0)) {
			Fig.x++;
			fireFigureMovedEvent(oldX, oldY);
		}
	}

	private void fireFigureMovedEvent(int oldX, int oldY) {
		for (ModelListener modelListener : listeners) {
			modelListener.figureMovedFrom(oldX, oldY);
		}
	}

	public void rotateDown() {
		Fig.rotateFigureCellsRight();
		fireUpdateFigure();
	}

	private void fireUpdateFigure() {
		for (ModelListener modelListener : listeners) {
			modelListener.figureUpdated(Fig);
		}
	}

	public void rotateUp() {
		Fig.rotateFigureCellsLeft();
		fireUpdateFigure();
	}

	void removeSimilarCellsRepeatedly() {
		do {
			removeSimilarNeighboringCells();
		} while (!noChanges);
	}

	public void createNewFigure() {
		Fig = new Figure();
		fireUpdateFigure();
	}

	void processFigureCantMoveDown() {
		DropScore = 0;
		pasteFigure(Fig);
		removeSimilarCellsRepeatedly();
		checkIfFieldIsFull();
	}


}