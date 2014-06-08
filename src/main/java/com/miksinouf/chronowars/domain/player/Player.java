package com.miksinouf.chronowars.domain.player;

import com.miksinouf.chronowars.domain.Board;
import com.miksinouf.chronowars.domain.token.IllegalMoveException;
import com.miksinouf.chronowars.domain.token.Move;

public class Player {

    private final String pseudo;
    private final Color color;
    private Integer timeBonus;
    private Integer score;
    private final Tokens tokens;
    private final Board board;

    public Player(String pseudo, Color color, Integer numberOfTokens, Board board) {
        this.pseudo = pseudo;
        this.color = color;
        this.board = board;
        this.timeBonus = 0;
        this.score = 0;
        this.tokens = new Tokens(board.size(), numberOfTokens);
    }

    /**
     *
     * @param x x
     * @param y y
     * @return is state legal
     */
    public boolean set(Integer x, Integer y) {
        try {
            tokens.addToken(x,y);
        } catch (TooManyTokensException | IllegalMoveException e) {
            return false;
        }

        return true;
    }
    
    public boolean move(Integer oldX, Integer oldY, Integer newX, Integer newY) {
        try {
            tokens.moveToken(oldX, oldY, Move.DOWN);
        } catch (IllegalMoveException e) {
            return false;
        }

        return true;
    }



}