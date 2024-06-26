package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author TODO: YOUR NAME HERE
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;

        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.

        //thought:新写一个函数返回该tile上还有多少个空位置，从第二行开始遍历board使用该函数，先判断能否合并，能合并就先合并。

        this.board.setViewingPerspective(side);

        for (int i = 0; i < this.board.size(); i += 1){
            int flag = 1;
            //每列有一个flag，用于rule 2，flag为-1则表示前一个数经过一次合并，1则表示未经过合并

            for (int j = this.board.size() - 2; j >= 0; j -= 1){
                if (this.board.tile(i, j) != null){
                    //不为空格才能继续
                    int pre_num = get_pre_nonempty(this.board, i, j);
                    int now_num = this.board.tile(i, j).value();
                    Tile t = this.board.tile(i, j);

                    if (pre_num == 1){
                        //如果该格上面全为空
                        this.board.move(i, this.board.size() - 1, t);
                        changed = true;
                    }
                    else {
                        //如果该格上面不全为空
                        int empty_pos = empty_position_num(this.board, i, j);

                        if(pre_num == now_num && flag == 1){
                            //需要合并
                            this.board.move(i, empty_pos, t);
                            this.score += 2 * now_num;
                            flag = -1;
                            changed = true;
                        } else if (empty_pos == j + 1) {
                            //不需要合并，不需要移动
                        } else {
                            //不需要合并且需要移动
                            this.board.move(i, empty_pos - 1, t);
                            flag = 1;
                            changed = true;
                        }
                    }
                }
            }
        }

        this.board.setViewingPerspective(Side.NORTH);

        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }


    public int get_pre_nonempty(Board b, int now_col, int now_row){
        int pre_nonempty = 1;
        for (int i = now_row + 1; i <= b.size() - 1; i += 1){
            if (b.tile(now_col, i) != null) {
                pre_nonempty = b.tile(now_col, i).value();
                break;
            }
        }
        return pre_nonempty;
    }

    public int empty_position_num(Board b, int now_col, int now_row){
        //返回该格的前一个不空的格的行(row)值，如果该格的上面全空，则返回-1
        int empty_position = -1;
        for (int i = now_row + 1; i <= b.size() - 1; i += 1){
            if (b.tile(now_col, i) != null) {
                empty_position = i;
                break;
            }
        }
        return empty_position;
    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        // TODO: Fill in this function.
        for (int i = 0; i < b.size(); i += 1){
            for (int j = 0; j < b.size(); j += 1){
                if (b.tile(i, j) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        // TODO: Fill in this function.
        for (int i = 0; i < b.size(); i += 1){
            for (int j = 0; j < b.size(); j += 1){
                if (b.tile(i, j) == null) {
                    continue;
                }
                if (b.tile(i, j).value() == MAX_PIECE){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.
        if (emptySpaceExists(b)) return true;
        for (int i = 0; i < b.size(); i += 1){
            for (int j = 0; j < b.size(); j += 1){
                if (check_up(b, i, j)) return true;
                if (check_down(b, i, j)) return true;
                if (check_left(b, i, j)) return true;
                if (check_right(b, i, j)) return true;
            }
        }
        return false;
    }

    public static boolean check_up(Board b, int now_col, int now_row) {
        if (now_row == 0) return false;
        if (b.tile(now_col, now_row) == null || b.tile(now_col, now_row - 1) == null) return false;
        else if (b.tile(now_col, now_row).value() == b.tile(now_col, now_row - 1).value()) return true;
        else return false;
    }

    public static boolean check_down(Board b, int now_col, int now_row) {
        if (now_row == b.size() - 1) return false;
        if (b.tile(now_col, now_row) == null || b.tile(now_col, now_row + 1) == null) return false;
        else if (b.tile(now_col, now_row).value() == b.tile(now_col, now_row + 1).value()) return true;
        else return false;
    }

    public static boolean check_left(Board b, int now_col, int now_row) {
        if (now_col == 0) return false;
        if (b.tile(now_col, now_row) == null || b.tile(now_col - 1, now_row) == null) return false;
        else if (b.tile(now_col, now_row).value() == b.tile(now_col - 1, now_row).value()) return true;
        else return false;
    }

    public static boolean check_right(Board b, int now_col, int now_row) {
        if (now_col == b.size() - 1) return false;
        if (b.tile(now_col, now_row) == null || b.tile(now_col + 1, now_row) == null) return false;
        else if (b.tile(now_col, now_row).value() == b.tile(now_col + 1, now_row).value()) return true;
        else return false;
    }

    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
