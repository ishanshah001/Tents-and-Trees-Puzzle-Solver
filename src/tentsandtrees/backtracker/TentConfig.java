package tentsandtrees.backtracker;

import java.io.*;
import java.util.*;

/**
 *  The full representation of a configuration in the TentsAndTrees puzzle.
 *  It can read an initial configuration from a file, and supports the
 *  Configuration methods necessary for the Backtracker solver.
 *
 *  @author RIT CS
 *  @author Ishan Shah
 */
public class TentConfig implements Configuration {
    // INPUT CONSTANTS
    /** An empty cell */
    public final static char EMPTY = '.';
    /** A cell occupied with grass */
    public final static char GRASS = '-';
    /** A cell occupied with a tent */
    public final static char TENT = '^';
    /** A cell occupied with a tree */
    public final static char TREE = '%';
    //GAME
    /** Holds the values for max no of tents the rows can hold */
    private int[] row;
    /** Holds the values for max no of tents the columns can hold */
    private int[] col;
    /** Holds the organisation of the board */
    private char[][] board;
    /** dimension of the board */
    private int DIM;
    /** cursor for row */
    private int rowc=0;
    /** cursor for column */
    private int colc=-1;
    // OUTPUT CONSTANTS
    /** A horizontal divider */
    public final static char HORI_DIVIDE = '-';
    /** A vertical divider */
    public final static char VERT_DIVIDE = '|';

    /**
     * Construct the initial configuration from an input file whose contents
     * are, for example:<br>
     * <tt><br>
     * 3        # square dimension of field<br>
     * 2 0 1    # row looking values, top to bottom<br>
     * 2 0 1    # column looking values, left to right<br>
     * . % .    # row 1, .=empty, %=tree<br>
     * % . .    # row 2<br>
     * . % .    # row 3<br>
     * </tt><br>
     * @param filename the name of the file to read from
     * @throws IOException if the file is not found or there are errors reading
     */
    public TentConfig(String filename) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        String line = in.readLine();
        DIM=Integer.parseInt(line);
        board = new char[DIM][DIM];
        row = new int[DIM];
        col = new int[DIM];
        // populate row look
        String[] fields = in.readLine().split("\\s+");
        for (int i = 0; i < DIM; ++i) {
            row[i] = Integer.parseInt(fields[i]);
        }
        fields = in.readLine().split("\\s+");
        for (int i = 0; i < DIM; ++i) {
            col[i] = Integer.parseInt(fields[i]);
        }

        for (int i = 0; i < DIM; ++i) {
            fields = in.readLine().split("\\s+");
            for (int j = 0; j < DIM; ++j) {
                this.board[i][j] = fields[j].charAt(0);
            }
        }

        in.close();
    }

    /**
     * Copy constructor.  Takes a config, other, and makes a full "deep" copy
     * of its instance data.
     * @param other the config to copy
     */
    private TentConfig(TentConfig other,char type) {
        this.rowc = other.rowc;
        this.colc= other.colc;;
        this.DIM= other.DIM;
        this.row= other.row;
        this.col=other.col;
        this.colc += 1;
        if (this.colc == DIM) {
            this.rowc += 1;
            this.colc = 0;
        }
        this.board = new char[DIM][DIM];
        for (int rowc=0; rowc<this.DIM; ++rowc) {
            System.arraycopy(other.board[rowc], 0, this.board[rowc], 0, this.DIM);
        }
        this.board[this.rowc][this.colc]= type;

    }


    /**
     * Used to get the piece at the next location
     * @param board board in its current state
     * @param cr row cursor
     * @param cc column cursor
     * @return the piece at the next location
     */
    private char nextPiece(char[][] board,int cr, int cc){
        cc += 1;
        if (cc == DIM) {
            cr += 1;
            cc = 0;
        }
        return board[cr][cc];
    }


    /**
     * Get the collection of successors from the current one.
     * @return All successors, valid and invalid
     */
    @Override
    public Collection<Configuration> getSuccessors() {
        List<Configuration> successors= new LinkedList<>();
        if(nextPiece(board,rowc,colc)==TREE){
            successors.add(new TentConfig(this, TREE));
        }
        else{
            int cr=rowc;
            int cc=colc;
            cc += 1;
            if (cc == DIM) {
                cr += 1;
                cc = 0;
            }
            if(check(cr,cc)){
                if (!(hasNeighbourTent(cr,cc,TENT)) && hasNeighbour(cr,cc,TREE)) {
                    successors.add(new TentConfig(this, TENT));
                }
            }
            successors.add(new TentConfig(this, GRASS));
        }
        return successors;
    }

    /**
     * Checks horizonatally, vertically and diagnoanlly for neighbour
     * @param x current row coordinate
     * @param y current column coordinate
     * @param type type of neighbour
     * @return
     */
    private boolean hasNeighbourTent(int x, int y,char type){
        for(int i=-1;i<=1;i++){
            for(int j=-1;j<=1;j++){
                if(validLoc(x+i,y+j)){
                    if(board[x+i][y+j]==type){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if co-ordinates are valid
     * @param x row coordinate
     * @param y column coordinate
     * @return true if coordinates are valid, else false
     */
    private boolean validLoc(int x, int y) {
        return (x>=0 && x<DIM && y>=0 && y<DIM);
    }


    /**
     * Checks if next loc would exceed no of tents according to row and column
     * @param x next location for row cursor
     * @param y next location for column cursor
     * @return true if tent can be added
     */
    private boolean check(int x, int y){
        int count=0;
        for(int j = 0;j<DIM;j++){
            if(board[x][j]==TENT){
                count++;
            }
        }
        if(count==row[x]){
            return false;
        }
        count=0;
        for(int i = 0;i<DIM;i++){
            if(board[i][y]==TENT){
                count++;
            }
        }
        if(count==col[y]){
            return false;
        }
        return true;
    }

    /**
     * Is the current configuration valid or not?
     * @return true if valid; false otherwise
     */
    @Override
    public boolean isValid() {
        boolean valid=true;
        int tempCount=0;

        //Checking row tent count matches does not exceed
        for (int i = 0; i < DIM; i++) {
            tempCount = 0;
            for (int j = 0; j < DIM; j++) {
                if (board[i][j] == TENT) {
                    tempCount++;
                }
            }
            if (tempCount > row[i]) {
                return false;
            }
        }
        //Checking column tent count does not exceed
        for (int i = 0; i < DIM; i++) {
            tempCount = 0;
            for (int j = 0; j < DIM; j++) {

                if (board[j][i] == TENT) {
                    tempCount++;
                }
            }
            if (tempCount > col[i]) {
                return false;
            }
        }

        if(rowc==DIM-1 && colc==DIM-1) {
            for (int i = 0; i < DIM; i++) {
                tempCount = 0;
                for (int j = 0; j < DIM; j++) {
                    if (board[i][j] == TENT) {
                        tempCount++;
                    }
                }
                if (tempCount != row[i]) {
                    return false;
                }
            }
            //Checking column tent count matches
            for (int i = 0; i < DIM; i++) {
                tempCount = 0;
                for (int j = 0; j < DIM; j++) {

                    if (board[j][i] == TENT) {
                        tempCount++;
                    }
                }
                if (tempCount != col[i]) {
                    return false;
                }
            }
        }

        //checks if tree doesn't have a neighbour after its next row has been populated
        if(rowc>=2 && colc==0) {
            for(int i=0;i<rowc-1;i++){
                for(int j=0;j<DIM;j++){
                    if (board[i][j] == TREE && !hasNeighbour(i,j,TENT)) {
                        return false;
                    }
                }
            }

        }
        return valid;
    }

    /**
     *  Checks if neighbour in all valid surrounding spots is of the given type
     * @param i row pointer
     * @param j column pointer
     * @param type type of neighbour
     * @return true if neighbour present, else false
     */
    private boolean hasNeighbour(int i, int j,char type){

        if(i==0 && j==0){
            if(board[i][j+1]==type || board[i+1][j]==type){
                return true;
            }
        }
        else if(i==0 && j==DIM-1){
            if(board[i+1][j]==type || board[i][j-1]==type){
                return true;
            }
        }
        else if(i==DIM-1 && j==0){
            if(board[i][j+1]==type || board[i-1][j]==type){
                return true;
            }
        }
        else if(i==DIM-1 && j==DIM-1){
            if(board[i][j-1]==type || board[i-1][j]==type){
                return true;
            }
        }
        else if(j==DIM-1){
            if(board[i][j-1]==type || board[i-1][j]==type || board[i+1][j]==type){
                return true;
            }
        }
        else if(j==0){
            if(board[i][j+1]==type || board[i-1][j]==type || board[i+1][j]==type){
                return true;
            }
        }
        else if(i==0){
            if(board[i][j+1]==type || board[i][j-1]==type || board[i+1][j]==type){
                return true;
            }
        }
        else if(i==DIM-1){
            if(board[i][j+1]==type || board[i][j-1]==type || board[i-1][j]==type){
                return true;
            }
        }
        else{
            if(board[i][j-1]==type || board[i][j+1]==type || board[i-1][j]==type || board[i+1][j]==type){
                return true;
            }
        }
        return false;
    }

    /**
     * Is the current configuration a goal?
     * @return true if goal; false otherwise
     */
    @Override
    public boolean isGoal() {
        return this.colc==DIM-1 && this.rowc==DIM-1;
    }

    /**
     * @return a string that represents the configuration.
     */
    @Override
    public String toString() {
        String config=" ";
        for(int i=0;i<DIM*2-1;i++){
            config+=HORI_DIVIDE;
        }
        config+="\n";
        for(int i=0;i<DIM;i++){
            config+=VERT_DIVIDE;
            for(int j=0;j<DIM;j++){
                config+=board[i][j]+" ";
            }
            config+=VERT_DIVIDE;
            config+=row[i] ;
            config+="\n";
        }
        config+=" ";
        for(int i=0;i<DIM*2-1;i++){
            config+=HORI_DIVIDE;
        }
        config+="\n";
        config+=" ";
        for(int i=0;i<DIM;i++){
            config+=col[i]+" ";
        }
        config+="\n";
        return config;
    }
}
