package com.example

import com.example.chess.engine.ChessAI
import com.example.chess.engine.ChessBoard
import com.example.chess.model.AIDifficulty
import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun initialBoardHas20LegalMovesForWhite() {
        val board = ChessBoard()
        val legalMoves = board.generateLegalMoves(PieceColor.WHITE)
        assertEquals(20, legalMoves.size)
    }

    @Test
    fun foolsMateResultsInCheckmate() {
        val board = ChessBoard()
        board.makeMove(Move(Square(5, 1), Square(5, 2))) // 1. f3
        board.makeMove(Move(Square(4, 6), Square(4, 4))) // 1... e5
        board.makeMove(Move(Square(6, 1), Square(6, 3))) // 2. g4
        board.makeMove(Move(Square(3, 7), Square(7, 3))) // 2... Qh4#

        assertTrue(board.isKingInCheck(PieceColor.WHITE))
        val whiteMoves = board.generateLegalMoves(PieceColor.WHITE)
        assertTrue(whiteMoves.isEmpty())
        assertEquals(GameState.CHECKMATE, board.getGameState())
    }

    @Test
    fun enPassantExecution() {
        val board = ChessBoard()
        board.makeMove(Move(Square(4, 1), Square(4, 3))) // 1. e4
        board.makeMove(Move(Square(0, 6), Square(0, 5))) // 1... a6
        board.makeMove(Move(Square(4, 3), Square(4, 4))) // 2. e5
        board.makeMove(Move(Square(3, 6), Square(3, 4))) // 2... d5

        assertEquals(Square(3, 5), board.enPassantTarget)

        val legalMoves = board.generateLegalMoves(PieceColor.WHITE)
        val epMove = legalMoves.find { it.isEnPassant }
        assertNotNull(epMove)
        assertEquals(Square(3, 5), epMove?.to)

        board.makeMove(epMove!!)
        assertEquals(null, board.getPiece(Square(3, 4)))
        assertEquals(PieceType.PAWN, board.getPiece(Square(3, 5))?.type)
    }

    @Test
    fun chessAiReturnsValidLegalMove() {
        val board = ChessBoard()
        val bestMove = ChessAI.getBestMove(board, AIDifficulty.BEGINNER)
        assertNotNull(bestMove)
        val legalMoves = board.generateLegalMoves(PieceColor.WHITE)
        assertTrue(legalMoves.any { it.from == bestMove?.from && it.to == bestMove?.to })
    }
}
