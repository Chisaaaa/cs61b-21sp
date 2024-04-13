package game2048;

import java.util.Random;

import ucb.util.CommandArgs;
/*
Tile
This class represents numbered tiles on the board. If a variable of type Tile is null, it’s treated as an empty tile on the board. You will not need to create any of these objects, though you will need have an understanding of them since you will be using them in the Model class. The only method of this class you’ll need to use is .value() which returns the value of the given tile. For example if Tile t corresponds to a tile with the value 8, then t.value() will return 8.

Side
The Side class is a special type of class called an Enum. An enum is similar has restricted functionality. Specifically, enums may take on only one of a finite set of values. In this case, we have a value for each of the 4 sides: NORTH, SOUTH, EAST, and WEST. You will not need to use any of the methods of this class nor manipulate the instance variables.

Enums can be assigned with syntax like Side s = Side.NORTH. Note that rather than using the new keyword, we simply set the Side value equal to one of the four values. Similarly if we have a function like public static void printSide(Side s), we can call this function as follows: printSide(Side.NORTH), which will pass the value NORTH to the function.

Model
This class represents the entire state of the game. A Model object represents a game of 2048. It has instance variables for the state of the board (i.e. where all the Tile objects are, what the score is, etc) as well as a variety of methods. One of the challenges when you get to the fourth final task of this project (writing the tilt method) will be to figure out which of these methods and instance variables are useful.

Board
This class represents the board of tiles itself. It has three methods that you’ll use: setViewingPerspective, tile, move. Optionally, for experimentation, you can use getRandomNonNullTile.


Your assignment
Your job for this project is to modify and complete the Model class, specifically the emptySpaceExists, maxTileExists, atLeastOneMoveExists and tilt methods. Everything else has been implemented for you. We recommend completing them in this order. The first two are relatively straightforward. The third (atLeastOneMoveExists) is harder, and the final method tilt will probably be quite difficult. We anticipate that tilt will take you 3 to 10 hours to complete. The first three methods will handle the game over conditions, and the final method tilt will modify the board after key-presses from the user. You can read the very short body of the checkGameOver method to get an idea of how your methods will be used to check if the game is over.

 */


/** The main class for the 2048 game.
 *  @author P. N. Hilfinger
 */
public class Main {

    /** Number of squares on the side of a board. */
    static final int BOARD_SIZE = 4;
    /** Probability of choosing 2 as random tile (as opposed to 4). */
    static final double TILE2_PROBABILITY = 0.9;

    /** The main program.  ARGS may contain the options --seed=NUM,
     *  (random seed); --log (record moves and random tiles
     *  selected.). */
    public static void main(String... args) {
        CommandArgs options =
            new CommandArgs("--seed=(\\d+) --log=(.+)",
                            args);
        if (!options.ok()) {
            System.err.println("Usage: java game2048.Main [ --seed=NUM ] "
                               + "[ --log=LOG_FILE ]");
            System.exit(1);
        }

        Random gen = new Random();
        if (options.contains("--seed")) {
            gen.setSeed(options.getLong("--seed"));
        }

        Model model = new Model(BOARD_SIZE);

        GUI gui;

        gui = new GUI("2048 61B", model);
        gui.display(true);

        InputSource inp;

        inp = new GUISource(gui, gen, TILE2_PROBABILITY,
                            options.getFirst("--log"));

        Game game = new Game(model, inp);

        try {
            while (game.playing()) {
                game.playGame();
            }
        } catch (IllegalStateException excp) {
            System.err.printf("Internal error: %s%n", excp.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }

}
