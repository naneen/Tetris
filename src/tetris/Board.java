// Board.java
package tetris;

/**
 * CS108 Tetris Board. Represents a Tetris board -- essentially a 2-d grid of
 * booleans. Supports tetris pieces and row clearing. Has an "undo" feature that
 * allows clients to add and remove pieces efficiently. Does not do any drawing
 * or have any idea of pixels. Instead, just represents the abstract 2-d board.
 */
public class Board {
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private boolean[][] grid, backup;
	private boolean DEBUG = true;
	boolean committed;
	private int[] widths;
	private int[] heights;

	// Here a few trivial methods are provided:

	/**
	 * Creates an empty board of the given width and height measured in blocks.
	 */
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];
		backup = new boolean[width][height];
		committed = true;

		// YOUR CODE HERE
		this.widths = new int[width];
		this.heights = new int[height];

	}

	/**
	 * Returns the width of the board in blocks.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns the height of the board in blocks.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns the max column height present in the board. For an empty board
	 * this is 0.
	 */
	public int getMaxHeight() {
		// YOUR CODE HERE
		int max = 0;
		for (int i = 0; i < this.width; i++) {
			if (getColumnHeight(i) > max) {
				max = getColumnHeight(i);
			}
		}
		return max;
	}

	/**
	 * Checks the board for internal consistency -- used for debugging.
	 */
	public void sanityCheck() {
		if (DEBUG) {
			// YOUR CODE HERE
		}
	}

	/**
	 * Given a piece and an x, returns the y value where the piece would come to
	 * rest if it were dropped straight down at that x.
	 * 
	 * <p>
	 * Implementation: use the skirt and the col heights to compute this fast --
	 * O(skirt length).
	 */
	public int dropHeight(Piece piece, int x) {
		int[] skirt = piece.getSkirt();
		int maxH = this.getColumnHeight(x);
		for( int i=0; i<skirt.length; i++ ){
			if( maxH<this.getColumnHeight(i+x)  - skirt[i]){
				maxH = this.getColumnHeight(i+x) - skirt[i];
			}
		}
		return maxH;
	}

	/**
	 * Returns the height of the given column -- i.e. the y value of the highest
	 * block + 1. The height is 0 if the column contains no blocks.
	 */
	public int getColumnHeight(int x) {
		for (int i = this.height - 1; i >= 0; i--) {
			if (this.grid[x][i]) {
				return i + 1;
			}
		}
		return 0;
	}

	/**
	 * Returns the number of filled blocks in the given row.
	 */
	public int getRowWidth(int y) {
		int countW = 0;
		for (int i = 0; i < this.width; i++) {
			if (grid[i][y]) {
				countW++;
			}
		}
		return countW;
	}

	/**
	 * Returns true if the given block is filled in the board. Blocks outside of
	 * the valid width/height area always return true.
	 */
	public boolean getGrid(int x, int y) {
		return grid[x][y]; // YOUR CODE HERE
	}

	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;

	/**
	 * Attempts to add the body of a piece to the board. Copies the piece blocks
	 * into the board grid. Returns PLACE_OK for a regular placement, or
	 * PLACE_ROW_FILLED for a regular placement that causes at least one row to
	 * be filled.
	 * 
	 * <p>
	 * Error cases: A placement may fail in two ways. First, if part of the
	 * piece may falls out of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 * Or the placement may collide with existing blocks in the grid in which
	 * case PLACE_BAD is returned. In both error cases, the board may be left in
	 * an invalid state. The client can use undo(), to recover the valid,
	 * pre-place state.
	 */
	public int place(Piece piece, int x, int y) {
		this.backUp();
		
		// flag !committed problem
		if (!committed)
			throw new RuntimeException("place commit problem");

		int result = PLACE_OK;
		TPoint[] body = piece.getBody();

		// YOUR CODE HERE
		if (y < 0 || (y+piece.getHeight()) > height || x < 0 || (x+piece.getWidth()) > width) {
			return PLACE_OUT_BOUNDS;
		} else if (this.isGridTrue(body, x, y)) {
			return PLACE_BAD;
		}

		this.fillGrid(body, x, y);
		return result;
	}
	
	private void backUp(){
		for( int x=0; x<this.width; x++ ){
			for( int y=0; y<this.height; y++ ){
				this.backup[x][y] = this.grid[x][y];
			}
		}
	}

	private boolean isGridTrue(TPoint[] body, int bX, int bY) {
		boolean bool = false;
		for (int i = 0; i < body.length; i++) {
			int x = body[i].x + bX;
			int y = body[i].y + bY;
			if ( grid[x][y] ) {
				bool = true;
				break;
			}
		}
		return bool;
	}

	private void fillGrid(TPoint[] body, int bX, int bY) {
		for (int i = 0; i < body.length; i++) {
			int x = body[i].x + bX;
			int y = body[i].y + bY;
			grid[x][y] = true;
		}
	}

	/**
	 * Deletes rows that are filled all the way across, moving things above
	 * down. Returns the number of rows cleared.
	 */
	public int clearRows() {
		int rowsCleared = 0;
		// YOUR CODE HERE
		for (int y = 0; y < this.height; y++) {
			int count = 0;
			for (int x = 0; x < this.width; x++) {
				if (grid[x][y]) {
					count++;
				}
			}
			if (count == width) {
				rowsCleared++;
				for (int xx = 0; xx < this.width; xx++) {
					grid[xx][y] = false;
				}
				this.moveGridDown(y);
				y--;
			}
		}
		sanityCheck();
		return rowsCleared;
	}

	private void moveGridDown(int minY) {
		int maxY = this.getMaxHeight();
		for (int x = 0; x < this.width; x++) {
			for (int y = minY; y < maxY; y++) {
				this.grid[x][y] = this.grid[x][y + 1];
				this.grid[x][y + 1] = false;
			}
		}
	}

	/**
	 * Reverts the board to its state before up to one place and one
	 * clearRows(); If the conditions for undo() are not met, such as calling
	 * undo() twice in a row, then the second undo() does nothing. See the
	 * overview docs.
	 */
	public void undo() {
		// YOUR CODE HERE
		committed = true;
		for( int x=0; x<this.width; x++ ){
			for( int y=0; y<this.height; y++ ){
				this.grid[x][y] = this.backup[x][y];
			}
		}
	}

	/**
	 * Puts the board in the committed state.
	 */
	public void commit() {
		committed = true;
	}

	/*
	 * Renders the board state as a big String, suitable for printing. This is
	 * the sort of print-obj-state utility that can help see complex state
	 * change over time. (provided debugging utility)
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height - 1; y >= 0; y--) {
			buff.append('|');
			for (int x = 0; x < width; x++) {
				if (getGrid(x, y))
					buff.append('+');
				else
					buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x = 0; x < width + 2; x++)
			buff.append('-');
		return (buff.toString());
	}
}
