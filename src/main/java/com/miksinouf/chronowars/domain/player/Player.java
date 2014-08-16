package com.miksinouf.chronowars.domain.player;

import static com.miksinouf.chronowars.domain.board.MoveResultType.SUCCESS;

import com.miksinouf.chronowars.domain.board.*;

public class Player {

    public final String nickname;
    public final String identifier;
    private final Color color;
    private Integer score;
    private final Board board;
    private Player opponent;

    public Player(String nickname, Color color,
                  String identifier, Board board) {
        this.nickname = nickname;
        this.color = color;
        this.identifier = identifier;
        this.board = board;
        this.score = 0;
    }

    public Player(WaitingPlayer waitingPlayer, Board board, Color color) {
        this.nickname = waitingPlayer.nickname;
        this.color = color;
        this.identifier = waitingPlayer.identifier;
        this.board = board;
        this.score = 0;
    }

    /**
     * 
     * @param x
     *            x
     * @param y
     *            y
     * @return is state legal ?
     */
    public MoveResult set(Integer x, Integer y) throws IllegalMoveException, TooManyTokensException {
        checkPositionForPlayer(x, y);
        placeToken(x, y);
        return new MoveResult(SUCCESS, x, y);
    }

    public MoveResult move(Integer oldX, Integer oldY, Move move) throws IllegalMoveException {
        checkPositionForPlayer(oldX, oldY);
        MoveResult moveResult = board.moveToken(oldX, oldY, move);
        refreshPlayerScore(moveResult.position);

        return moveResult;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public Board getBoard() {
    	return this.board;
    }

	public Color getColor() {
		return color;
	}

	public Integer getScore() {
		return score;
	}

    private void placeToken(Integer x, Integer y) throws IllegalMoveException, TooManyTokensException {
        board.placeToken(x, y);
        refreshPlayerScore(new Position(x, y));
    }

    private void checkPositionForPlayer(Integer oldX, Integer oldY) throws IllegalMoveException {
        if (this.color != this.board.colorToPlay) {
            throw new IllegalMoveException(MoveResultType.BAD_PLAYER, oldX, oldY);
        }
    }

    private void refreshPlayerScore(Position p) {
        this.board
                .getShapes().stream()
                .filter(shape -> shape.tokens.contains(p))
                .forEach(shape -> this.board.maxShape = shape.getScore() > this.board.maxShape.getScore() ? shape : this.board.maxShape);

        this.score += this.board.maxShape.getScore();
    }
}
