/**
 * 2D Grid bomb-finding game by Freeman.
 * Recommendations:
 *      Ideally, do not skip the tutorial.
 *      You *can* use [DEBUG] to get rid of delays, but that detracts from the experience.
 * 
 * Features:
 *      Basic tutorial
 *      2D grid graphics with different tile types
 *      Player can move around in the grid
 *      BOMB explosion animation after losing
 *      Simple DEBUG mode (accessed by using [DEBUG] at *most* action prompts)
 *      Random BOMB generation
 *      Ability to choose starting position on grid
 *      Ability to detect distance from bomb using the [PING] action
 *      Multiple possible actions (e.g. [YES], [Y] does the same thing)
 *      Whitespace removal (e.g. [PING ] is treated like [PING])
 */
import java.util.Scanner;
import java.util.Arrays;
public class Game {
    public static Scanner scan = new Scanner(System.in);
    public static final int BOARD_COLUMNS = 9; // Units for possible x positions
    public static final int BOARD_ROWS = 12; // Units for possible y positions
    
    public static final String TILE_STRONG = "___";
    public static final String TILE_WEAK = "...";
    public static final String TILE_EMPTY = "   ";
    
    
    public static String[][] board;
    public static int[] playerPos, bombPos, actPos;
    
    public static int direction; // 0 for right, 1 for up, 2, for left, 3 for down (► ▲ ◄ ▼)
    public static long startTime, endTime;
    public static double distanceFromBomb;
    public static int movesLeft, pingsLeft;

    public static boolean debugMode;
    
    public static void main(String[] args) {
        debugMode = false;
        playTutorial();
        do {
            playIntro();
            game();
        } while (playAgain());
        println("Game has finished.");
    }

    // Main game.
    public static void game() {
        playerPos = new int[2];
        bombPos = new int[2];
        actPos = new int[2];
        
        direction = 0; // Start facing left.
        movesLeft = (BOARD_COLUMNS*BOARD_ROWS)/3;
        pingsLeft = 5;
        // ---------------------------------------------------------------
        // Game initialization.
        println("<------------------POINT INSERTION------------------>");
        print("You can choose where you want to begin your search");
        println("...", 500);
        playerPos = getInitialPos();
        do { // Generate bomb position.
            bombPos[0] = (int) (Math.random()*(BOARD_COLUMNS-1));
            bombPos[1] = (int) (Math.random()*(BOARD_ROWS-1));
        } while (Arrays.equals(playerPos, bombPos)); // Prevent bomb and player start at same position.
        board = createBoard(BOARD_ROWS, BOARD_COLUMNS, playerPos, bombPos);
        println("Starting game...");
        startTime = System.currentTimeMillis();
        // ---------------------------------------------------------------
        // Gameplay loop.
        while (!bombFound() && movesLeft > 0) {
            clearScreen();
            distanceFromBomb = Math.hypot(bombPos[0]-playerPos[0], bombPos[1]-playerPos[1]);
            System.out.printf("Current Pos: (%d, %d)", playerPos[0], playerPos[1]);
            System.out.printf("\t\tMoves left: %d" + ((movesLeft <= 5) ? " /!\\" : "") + "%n", movesLeft);
            
            if (debugMode) {
                println(String.format("Distance: %.2f%n", distanceFromBomb));
                println("bombPos: " + Arrays.toString(bombPos));
                println("playerPos:" + Arrays.toString(playerPos));
                println("actPos: " + Arrays.toString(actPos));
            }

            renderBoard();
            actPos = getAction();
            doAction();
            movesLeft--;
        }
        // ---------------------------------------------------------------
        // End screen.
        clearScreen();
        renderBoard();
        endTime = System.currentTimeMillis();
        if (bombFound()) { // player found bomb victory
            println("After hours and hours of searching...");
            delay(1000);
            println("You have found the bomb, and defused it successfully.");
            delay(1000);
            println("You, who have risked your life for others...");
            delay(1000);
            println("Shall be forever remembered in history.");
        }
        else { // player failed to find bomb
            println("Oh man, this isn't good...");
            delay(1000);
            for (int i = 5; i > 0; i--) {
                println(String.format("%d SECOND" + ((i == 1) ? "" : "S") + " UNTIL DETONATION.", i));
                delay(1000);
            }
            println("B O O M", 500);
            destroyBoard(5);
            println("...", 1000);
            println("Y-u sti-l t--re?");
            delay(1000);
            println("It see-s l-ke you w-re too la-e, -e ca- f--l it ha-pen--g...");
            delay(1000);
            println("T-e exp--si-n... it's go--g -- b- a di-a-ter...");
            delay(2000);
            println("---------------------------------------------------------");
            println("Don't worry...");
            delay(1000);
            println("Being there in the first place makes you a hero enough.");
            delay(1000);
            println("But it's not like heroes always win, right?");
            delay(1000);
            println("Yet many still look up to them; a beacon of hope.");
            delay(3000);
            println("This isn't the end.");
            if ((int)(Math.random()*5) == 0) {
                println("By the way, [DEBUG] is a valid action... try it sometime!");
                delay(1000);
            }
            println("...", 1000);
        }
        System.out.println();
        delay(2000);
        showStats(startTime, endTime, movesLeft);
        delay(2000);
    }

    // Gets user's starting position. Returns the resulting position.
    public static int[] getInitialPos() {
        int playerX, playerY;
        // Get initial player X position.
        println(String.format("On what COLUMN (x) do you want to begin on? [0 to %d]", BOARD_COLUMNS-1));
        while (true) {
            System.out.print(">>> ");
            try {
                playerX = scan.nextInt();
                if (playerX < 0) {
                    println("Defaulted to 0.");
                    playerX = 0;
                }
                else if (playerX > BOARD_COLUMNS-1) {
                    println(String.format("Defaulted to %d.", BOARD_COLUMNS-1));
                    playerX = BOARD_COLUMNS-1;
                }
                break; // X pos obtained, exit while loop.
            }
            catch (Exception e) {
                println("Something went wrong...");
                println("Please enter an Integer.");
                scan.nextLine();
            }
        }
        // Get initial player Y position.
        println(String.format("On what ROW (y) do you want to begin on? [0 to %d]", BOARD_ROWS-1));
        while (true) {
            System.out.print(">>> ");
            try {
                playerY = scan.nextInt();
                if (playerY < 0) {
                    println("Defaulted to 0.");
                    playerY = 0;
                }
                else if (playerY > BOARD_ROWS-1) {
                    println(String.format("Defaulted to %d.", BOARD_ROWS-1));
                    playerY = BOARD_ROWS-1;
                }
                break; // Y pos obtained, exit while loop.
            }
            catch (Exception e) {
                println("Something went wrong...");
                println("Please enter an Integer.");
                scan.nextLine();
            }
        }
        scan.nextLine(); // flush scanner.
        return new int[]{playerX, playerY};
    }
    
    // Runs the tutorial.
    public static void playTutorial() {
        println("/!\\ Warning: Tutorial only runs once per run. /!\\");
        println("Skip tutorial? [Y/N]");
        System.out.print(">>> ");
        try {
            String input = scan.nextLine().toUpperCase().replaceAll("\\s", "");
            switch (input) {
                case ("YES"):
                case ("Y"):
                case ("SKIP"):
                    println("Skipped.\n\n\n");
                    return;
                case ("NO"):
                case ("N"):
                    break;
                case ("DEBUG"):
                case ("DEBUGMODE"):
                    debugMode = true;
                    println("DEBUG mode enabled, skipping TUTORIAL.\n\n\n");
                    return;
                default:
                    println("Seems like a NO to me.\n\n\n");
            }
        } catch (Exception e) { // Empty string, continue INTRO
            println("Seems like a NO to me.\n\n\n");
        }
        println("This game is a 2D-grid exploration game.");
        delay(1000);
        println("There will be a BOMB within the grid. Your OBJECTIVE is to find it.");
        delay(1000);
        println("Your character will be indicated by an arrow character (e.g. ►).");
        delay(1000);
        println("Within the grid, there will be three main tile types:");
        delay(500);
        println("    STRONG (___): Requires two moves to break. Becomes WEAK when hit.");
        delay(500);
        println("    WEAK   (...): Requires one move to break. Becomes EMPTY when broken.");
        delay(500);
        println("    EMPTY  (   ): Can be moved into.");
        delay(1000);
        println("When the tile containing the BOMB is broken, it becomes exposed ( @ ).");
        delay(1000);
        println("Move into the BOMB to defuse it and win the game.");
        delay(1000);
        println("Above the grid, there will be a counter showing your current moves left.");
        delay(1000);
        println("If that counter reaches 0, and you haven't found the bomb, you lose.");
        delay(1000);
        println("You can use the action [PING] to get the your current distance from the BOMB.");
        delay(1000);
        println("Be careful, though, as PINGs are limited.");
        delay(1000);
        println("You begin with 5 PINGs and cannot gain more in one game session.");
        delay(1000);
        println("Good luck, and have fun playing this game!\n\n\n");
    }
    
    // Game introduction.
    public static void playIntro() {
        if (debugMode) {
            println("DEBUG mode is currrently enabled, skipping INTRO.\n\n\n");
            return;
        }
        clearScreen();
        println("Skip introduction? [Y/N]");
        System.out.print(">>> ");
        try {
            String input = scan.nextLine().toUpperCase().replaceAll("\\s", "");
            switch (input) {
                case ("YES"):
                case ("Y"):
                case ("SKIP"):
                    println("Skipped.\n\n\n");
                    return; // Instantly end INTRO
                case ("NO"):
                case ("N"):
                    break;
                case ("DEBUG"):
                case ("DEBUGMODE"):
                    debugMode = true;
                    println("DEBUG mode enabled, skipping INTRO.\n\n\n");
                    return; // Enable DEBUG mode and end INTRO
                default:
                    println("Seems like a NO to me.");
            }
        } catch (Exception e) { // Empty string, continue INTRO
            println("Seems like a NO to me.");
        }
        println("Starting...\n\n\n", 100);
        println("Something's happening.");
        delay(1000);
        println("People are panicking, and screams echo through the air.");
        delay(1000);
        println("What the heck is going on???");
        delay(1000);
        println("It seems that someone planted a BOMB underground...");
        delay(1000);
        println("A BOMB so powerful, it will blow up the entire CITY!");
        delay(1000);
        println("Fortunately, a response team was quickly formed to find the BOMB...");
        delay(1000);
        println("And YOU were selected as the most capable person to go underground.");
        delay(1000);
        println("Good luck, go find that BOMB and save everyone!");
        delay(1000);
        println("\n\n\n\n\n");
    }

    // Check if the bomb is found, where the player and bomb are in the same tile. Returns true if yes, false if no.
    public static boolean bombFound() {
        return Arrays.equals(playerPos, bombPos);
    }

    // Shows time taken and moves left.
    public static void showStats(long startTime, long endTime, int movesLeft) {
        println(String.format("Time taken: %.2f seconds.", (endTime-startTime)/1000d));
        delay(2000);
        if (bombFound()) println(String.format("Moves left: %d.%n", movesLeft));
        else println("BOMB has exploded...");
    }

    // Generates the game board. Returns the resulting board.
    public static String[][] createBoard(int columns, int rows, int[] player, int[] bomb) {
        String[][] board = new String[columns][rows];
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                // If current index is player position, force empty tile.
                if (col == player[0] && row == player[1]) board[row][col] = TILE_EMPTY;
                // If current index is bomb position, force non-empty tile.
                else if (col == bomb[0] && row == bomb[1]) {
                    switch((int)Math.random()*2) {
                        case (0):
                            board[row][col] = TILE_STRONG;
                            break;
                        case (1):
                            board[row][col] = TILE_WEAK;
                            break;
                    }
                }
                // Generate random tile.
                else {
                    switch((int)(Math.random()*3)){
                        case (0):
                            board[row][col] = TILE_STRONG;
                            break;
                        case (1):
                            board[row][col] = TILE_WEAK;
                            break;
                        case (2):
                            board[row][col] = TILE_EMPTY;
                            break;
                    }
                }
            }
        }
        return board;
    }

    // Outputs the current board on the screen.
    public static void renderBoard() {
        // Column (X) indicators
        System.out.print("      ");
        for (int col = 0; col < board[0].length; col++) System.out.printf("%3d   ", col);
        System.out.println();
        System.out.println();
        
        for (int row = 0; row < board.length; row++) {
            System.out.printf("%-3d   ", row); // Row (Y) indicators
            for (int col = 0; col < board[row].length; col++) {
                if (col == playerPos[0] && row == playerPos[1]) {
                    char playerChar;
                    switch (direction) {
                        case (0):
                            playerChar = '►';
                            break;
                        case (1):
                            playerChar = '▲';
                            break;
                        case (2):
                            playerChar = '◄';
                            break;
                        case (3):
                            playerChar = '▼';
                            break;
                        default:
                            playerChar = '?';
                    }
                    System.out.printf("%c%c%c", board[row][col].charAt(0), playerChar, board[row][col].charAt(2)); // Render player if current index is player position
                }
                else if (col == bombPos[0] && row == bombPos[1] && board[row][col].equals(TILE_EMPTY)) System.out.print(" @ "); // Bomb is exposed.
                else System.out.print(board[row][col]); // Render tile at index
                System.out.print("   "); // Buffer between tiles
            }
            System.out.println();
            System.out.println();
        }
    }
    
    // Animation of the bomb destroying the board, from top row to bottom row. [cyclesPerRow] is how many frames each row gets to draw.
    public static void destroyBoard(int cyclesPerRow) {
        for (int ROW = 0; ROW <= board.length; ROW++) { // Destroyed rows, 0 to ROW
            for (int cycle = 0; cycle < cyclesPerRow; cycle++) {
                for (int row = 0; row < ROW; row++) {
                    for (int col = 0; col < board[row].length; col++) {
                        board[row][col] = genRandomChar(3);
                    }
                }
                clearScreen();
                if (debugMode) System.out.printf("ROW: %d // Cycle: %d%n", ROW, cycle);
                renderBoard();
                delay(100);
            }
        }
    }
    
    // Get and execute an valid action from the user. Returns the tile the player wants to act on in 0th and 1st index.
    // Also returns the remaining pings left in the 2nd index of the array, since I can't return two different things.
    public static int[] getAction() {
        String input;
        System.out.println("Do something. [{MOVEMENT}, HELP, PING]");
        do {
            System.out.print(">>> ");
            input = scan.nextLine().toUpperCase().replaceAll("\\s", "");
            switch(input) {
                case ("NORTH"):
                case ("UP"):
                case ("W"):
                    if (playerPos[1] == 0) {
                        println("Can't move up anymore...");
                        break;
                    }
                    direction = 1;
                    return new int[]{playerPos[0], playerPos[1]-1};
                case ("SOUTH"):
                case ("DOWN"):
                case ("S"):
                    if (playerPos[1] == BOARD_ROWS-1) {
                        println("Can't move DOWN anymore...");
                        break;
                    }
                    direction = 3;
                    return new int[]{playerPos[0], playerPos[1]+1};
                case ("WEST"):
                case ("LEFT"):
                case ("A"):
                    if (playerPos[0] == 0) {
                        println("Can't move RIGHT anymore...");
                        break;
                    }
                    direction = 2;
                    return new int[]{playerPos[0]-1, playerPos[1]};
                case ("EAST"):
                case ("RIGHT"):
                case ("D"):
                    if (playerPos[0] == BOARD_COLUMNS-1) {
                        println("Can't move RIGHT anymore...");
                        break;
                    }
                    direction = 0;
                    return new int[]{playerPos[0]+1, playerPos[1]};
                case ("PING"):
                    if (pingsLeft <=0) {
                        println("Out of PINGS.");
                        break;
                    }
                    println(String.format("%d PINGs left.", --pingsLeft));
                    for (int i = 0; i < 3; i++) {
                        println("PINGing...", 100);
                        delay(500);
                    }
                    println(String.format("PING success. The bomb is %.2f units away. What now?", distanceFromBomb));
                    break;
                case ("HELP"):
                    println("{MOVEMENT}: Controls your character's position.");
                    println("\t[UP, NORTH, W]: Moves player UP.");
                    println("\t[DOWN, SOUTH, S]: Moves player DOWN.");
                    println("\t[LEFT, WEST, A]: Moves player LEFT.");
                    println("\t[RIGHT, EAST, D]: Moves player RIGHT.");
                    println("[PING]: Gets the current distance from bomb.");
                    println(String.format("\tCurrent PINGs left: %d.", pingsLeft));
                    println("Your OBJECTIVE is to find the bomb before you run out of moves.");
                    break;
                case ("DEBUG"):
                case ("DEBUGMODE"):
                    debugMode = !debugMode;
                    println(String.format("DEBUG mode %s.", (debugMode) ? "enabled" : "disabled"));
                    break;
                default:
                    println("Unrecognized action... Try HELP for a list of actions.");
            }
        } while (true);
    }

    // Applies the action to the board. Returns the resulting player position.
    public static void doAction() {
        switch(board[actPos[1]][actPos[0]]) {
            case ("___"): // Strong tile --> empty tile.
                if (debugMode) println("STRONG");
                board[actPos[1]][actPos[0]] = TILE_WEAK;
                break;
            case ("..."): // Weak tile --> empty tile.
                if (debugMode) println("WEAK");
                board[actPos[1]][actPos[0]] = TILE_EMPTY;
                break;
            case ("   "): // Empty tile --> move to position.
                if (debugMode) println("EMPTY");
                System.arraycopy(actPos, 0, playerPos, 0, 2);
                break;
            default:
                if (debugMode) println("DEFAULT");
                System.arraycopy(actPos, 0, playerPos, 0, 2); // If something goes very wrong just let the user phase through wall. I don't care anymore.
        }
    }

    // Ask if the player wants to play again. Returns true if yes, false if no.
    public static boolean playAgain() {
        clearScreen();
        println("Do you want to play again? [Y/N]");
        System.out.print(">>> ");
        try {
            String input = scan.nextLine().toUpperCase().replaceAll("\\s", "");
            switch (input) {
                case ("YES"):
                case ("Y"):
                    return true;
                case ("NO"):
                case ("N"):
                    return false;
                default:
                    println("Seems like a NO to me.");
                    return false;
            }
        } catch (Exception e) { // Empty string.
            println("Seems like a NO to me.");
            return false;
        }
    }
    
    // ----------------UTILITY METHODS----------------
    // Clears the screen.
    private static void clearScreen() {
        if (!debugMode) System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
    }
    
    // Delay thread for [ms] milliseconds.
    private static void delay(int ms) {
        try {
            if (!debugMode) Thread.sleep(ms);
        } catch (Exception e) { }
    }

    // Modified System.out.println(). Delays for 10ms between characters.
    private static void println(String str) {
        for (int i = 0; i < str.length(); i++) {
            System.out.print(str.charAt(i));
            delay(10);
        }
        System.out.println();
    }

    // Modified System.out.println(). [msDelay] is the delay between characters in milliseconds. 
    private static void println(String str, int msDelay) {
        for (int i = 0; i < str.length(); i++) {
            System.out.print(str.charAt(i));
           delay(msDelay);
        }
        System.out.println();
    }

    // Modified System.out.print(). Delays for 10ms between character. No newline.
    private static void print(String str) {
        for (int i = 0; i < str.length(); i++) {
            System.out.print(str.charAt(i));
            delay(10);
        }
    }

    // Modified System.out.print(). [msDelay] is the delay between characters in milliseconds. No newline.
    private static void print(String str, int msDelay) {
        for (int i = 0; i < str.length(); i++) {
            System.out.print(str.charAt(i));
            delay(msDelay);
        }
    }
    
    // Generates [amount] kinda random characters as a string
    private static String genRandomChar(int amount) {
        String characters = "!@#$%^&*()-=+[]{};:\'\"<>/?\\";
        String random = "";
        for (int i = 0; i < amount; i++) random += characters.charAt((int) (Math.random()*characters.length()));
        return random;
    }
}
