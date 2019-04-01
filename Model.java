package com.javarush.task.task35.task3513;

import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    private Stack previousStates = new Stack();
    private Stack previousScores = new Stack();
    private boolean isSaveNeeded = true;
    int score;
    int maxTile;


    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public Model() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        resetGameTiles();
    }

    private void saveState(Tile[][] state) {
        Tile[][] arrayCopy = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                arrayCopy[i][j] = new Tile(state[i][j].value);
            }
        }
        previousStates.push(arrayCopy);
        previousScores.push(score);
        isSaveNeeded = false;
    }
    public void randomMove() {
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n) {
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                down();
                break;
        }
    }
    public void rollback() {
        if (!previousScores.empty() && !previousStates.empty()) {
            gameTiles = (Tile[][]) previousStates.pop();
            score = (int) previousScores.pop();
        }

    }
    private boolean compressTiles(Tile[] tiles) {
        boolean flag = false;
        for (int i = tiles.length - 1; i > 0; i--) {
            for(int j = 0 ; j < i ; j++){
                if( tiles[j].value == 0 ) {
                    if (tiles[j+1].value != 0) {
                        flag = true;
                    }
                    int tmp = tiles[j].value;
                    tiles[j].value = tiles[j+1].value;
                    tiles[j+1].value = tmp;
                }
            }
        }
        return flag;

    }
    private boolean mergeTiles(Tile[] tiles) {
/*
*/      boolean flag = false;
        for (int i = 0; i < tiles.length - 1; i++){
            if (tiles[i].value == tiles[i+1].value){
                if (tiles[i].value != 0) {
                    flag = true;
                }
                tiles[i].value = tiles[i].value * 2;
                tiles[i+1].value = 0;
                score += tiles[i].value;
            }
        }

        compressTiles(tiles);

        for(int i = 0; i < tiles.length; i++){
            if (tiles[i].value > maxTile){maxTile = tiles[i].value;}
        }
        return flag;

    }

    void resetGameTiles() {
        for(int i = 0; i < gameTiles.length; i++) {
            for(int j = 0; j < gameTiles[i].length; j++) gameTiles[i][j] = new Tile();
        }
        addTile();
        addTile();
        score = 0;
        maxTile = 0;
    }

    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if(emptyTiles.size() > 0)
            emptyTiles.get((int)(emptyTiles.size() * Math.random())).value = (Math.random() < 0.9 ? 2 : 4);
    }
    public boolean canMove() {
        for (int i = 0; i < gameTiles.length - 1; i++) {
            for (int j = 0; j < gameTiles.length - 1; j++) {
                if (gameTiles[i][j].value == 0 || gameTiles[i + 1][j].value == 0 || gameTiles[i][j + 1].value == 0) {
                    return true;
                }
                if (gameTiles[i][j].value == gameTiles[i][j+1].value || gameTiles[i][j].value == gameTiles[i + 1][j].value) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean move() {
        boolean triggerChanged = false;
        for (int i = 0; i < gameTiles.length; i++) {
            if (compressTiles(gameTiles[i])) {
                triggerChanged = true;
            }
            if (mergeTiles(gameTiles[i])) {
                triggerChanged = true;
            }
        }
        return triggerChanged;
    }

    void left() {
        if (isSaveNeeded) {
            saveState(gameTiles);
        }
        if (move()) {
            addTile();
            isSaveNeeded = true;
        }
    }


    void right() {
        saveState(gameTiles);
        rotate();
        rotate();
        if (move()) {
            addTile();
        }
        rotate();
        rotate();
    }

    void up() {
        saveState(gameTiles);
        rotate();
        rotate();
        rotate();
        if (move()) {
            addTile();
        }
        rotate();

    }

    void down() {
        saveState(gameTiles);
        rotate();
        if (move()) {
            addTile();
        }
        rotate();
        rotate();
        rotate();
    }

    void  rotate() {
        /*Поворачиваем массив против часовой стрелки */
        Tile[][] tempTile = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles.length; j++) {
                tempTile[i][FIELD_WIDTH - 1 - j] = gameTiles[j][i];
            }
        }
        gameTiles = tempTile.clone();
    }
    private List<Tile> getEmptyTiles() {
        List<Tile> list = new ArrayList<>();
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[i].length; j++) {
                if (gameTiles[i][j].isEmpty()) list.add(gameTiles[i][j]);
            }
        }
        return list;
    }

    public boolean hasBoardChanged() {
        Tile[][] lastBoard = (Tile[][])previousStates.peek();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (lastBoard[i][j].value != gameTiles[i][j].value) {
                    return true;
                }
            }
        }

        return false;
    }

    public MoveEfficiency getMoveEfficiency(Move move) {
        move.move();
        if (!hasBoardChanged()) {
            rollback();
            return new MoveEfficiency(-1, 0, move);
        }

        int emptyTilesCount = getEmptyTiles().size();

        MoveEfficiency moveEfficiency = new MoveEfficiency(emptyTilesCount, score, move);
        rollback();

        return moveEfficiency;
    }
    public void autoMove() {
        PriorityQueue<MoveEfficiency> queue = new PriorityQueue<>(4, Collections.reverseOrder());
        queue.add(getMoveEfficiency(this::left));
        queue.add(getMoveEfficiency(this::right));
        queue.add(getMoveEfficiency(this::up));
        queue.add(getMoveEfficiency(this::down));
        queue.peek().getMove().move();
    }

}
