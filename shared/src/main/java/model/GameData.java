package model;
import chess.ChessGame;

public record GameData (int gameID, String whitePlayer, String blackPlayer, String gameName, ChessGame game) {}
