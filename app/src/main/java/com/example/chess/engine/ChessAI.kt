package com.example.chess.engine

import com.example.chess.model.AIDifficulty
import com.example.chess.model.Move
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import kotlin.random.Random

object ChessAI {

    // Piece values in centipawns
    private const val PAWN_VAL = 100
    private const val KNIGHT_VAL = 320
    private const val BISHOP_VAL = 330
    private const val ROOK_VAL = 500
    private const val QUEEN_VAL = 900
    private const val KING_VAL = 20000

    // Piece-Square Tables (White perspective: rank 0 is 1st rank, rank 7 is 8th rank)
    // Table is [rank 0..7][file 0..7]
    private val PAWN_PST = arrayOf(
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(5, 10, 10, -20, -20, 10, 10, 5),
        intArrayOf(5, -5, -10, 0, 0, -10, -5, 5),
        intArrayOf(0, 0, 0, 20, 20, 0, 0, 0),
        intArrayOf(5, 5, 10, 25, 25, 10, 5, 5),
        intArrayOf(10, 10, 20, 30, 30, 20, 10, 10),
        intArrayOf(50, 50, 50, 50, 50, 50, 50, 50),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
    )

    private val KNIGHT_PST = arrayOf(
        intArrayOf(-50, -40, -30, -30, -30, -30, -40, -50),
        intArrayOf(-40, -20, 0, 5, 5, 0, -20, -40),
        intArrayOf(-30, 5, 10, 15, 15, 10, 5, -30),
        intArrayOf(-30, 0, 15, 20, 20, 15, 0, -30),
        intArrayOf(-30, 5, 15, 20, 20, 15, 5, -30),
        intArrayOf(-30, 0, 10, 15, 15, 10, 0, -30),
        intArrayOf(-40, -20, 0, 0, 0, 0, -20, -40),
        intArrayOf(-50, -40, -30, -30, -30, -30, -40, -50)
    )

    private val BISHOP_PST = arrayOf(
        intArrayOf(-20, -10, -10, -10, -10, -10, -10, -20),
        intArrayOf(-10, 5, 0, 0, 0, 0, 5, -10),
        intArrayOf(-10, 10, 10, 10, 10, 10, 10, -10),
        intArrayOf(-10, 0, 10, 10, 10, 10, 0, -10),
        intArrayOf(-10, 5, 5, 10, 10, 5, 5, -10),
        intArrayOf(-10, 0, 5, 10, 10, 5, 0, -10),
        intArrayOf(-10, 0, 0, 0, 0, 0, 0, -10),
        intArrayOf(-20, -10, -10, -10, -10, -10, -10, -20)
    )

    private val ROOK_PST = arrayOf(
        intArrayOf(0, 0, 0, 5, 5, 0, 0, 0),
        intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
        intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
        intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
        intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
        intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
        intArrayOf(5, 10, 10, 10, 10, 10, 10, 5),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
    )

    private val QUEEN_PST = arrayOf(
        intArrayOf(-20, -10, -10, -5, -5, -10, -10, -20),
        intArrayOf(-10, 0, 0, 0, 0, 0, 0, -10),
        intArrayOf(-10, 0, 5, 5, 5, 5, 0, -10),
        intArrayOf(-5, 0, 5, 5, 5, 5, 0, -5),
        intArrayOf(0, 0, 5, 5, 5, 5, 0, -5),
        intArrayOf(-10, 5, 5, 5, 5, 5, 0, -10),
        intArrayOf(-10, 0, 5, 0, 0, 0, 0, -10),
        intArrayOf(-20, -10, -10, -5, -5, -10, -10, -20)
    )

    private val KING_OPENING_PST = arrayOf(
        intArrayOf(20, 30, 10, 0, 0, 10, 30, 20),
        intArrayOf(20, 20, 0, 0, 0, 0, 20, 20),
        intArrayOf(-10, -20, -20, -20, -20, -20, -20, -10),
        intArrayOf(-20, -30, -30, -40, -40, -30, -30, -20),
        intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30),
        intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30),
        intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30),
        intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30)
    )

    // Opening book lines (UCI strings)
    private val OPENING_BOOK = mapOf(
        "" to listOf("e2e4", "d2d4", "g1f3", "c2c4"),
        // After 1. e4
        "e2e4" to listOf("e7e5", "c7c5", "e7e6", "c7c6"),
        // 1. e4 e5
        "e2e4 e7e5" to listOf("g1f3", "f1c4", "b1c3"),
        // 1. e4 e5 2. Nf3
        "e2e4 e7e5 g1f3" to listOf("b8c6", "g8f6"),
        // 1. e4 e5 2. Nf3 Nc6
        "e2e4 e7e5 g1f3 b8c6" to listOf("f1b5", "f1c4", "d2d4"),
        // 1. e4 c5 (Sicilian)
        "e2e4 c7c5" to listOf("g1f3", "b1c3", "c2c3"),
        // 1. d4
        "d2d4" to listOf("d7d5", "g8f6", "e7e6"),
        // 1. d4 d5
        "d2d4 d7d5" to listOf("c2c4", "g1f3", "c1f4"),
        // 1. d4 d5 2. c4 (Queen's Gambit)
        "d2d4 d7d5 c2c4" to listOf("e7e6", "c7c6", "d5c4")
    )

    fun getBestMove(board: ChessBoard, difficulty: AIDifficulty): Move? {
        val legalMoves = board.generateLegalMoves(board.turn)
        if (legalMoves.isEmpty()) return null
        if (legalMoves.size == 1) return legalMoves.first()

        // 1. Check opening book for master/advanced
        if (difficulty == AIDifficulty.MASTER || difficulty == AIDifficulty.ADVANCED) {
            val uciHistory = board.completedMoves.joinToString(" ") { it.toUci() }
            val bookResponses = OPENING_BOOK[uciHistory]
            if (bookResponses != null && bookResponses.isNotEmpty()) {
                val chosenUci = bookResponses.random()
                val matchedMove = legalMoves.find { it.toUci() == chosenUci }
                if (matchedMove != null) return matchedMove
            }
        }

        // 2. Novice mode: 35% random move, else depth 1
        if (difficulty == AIDifficulty.NOVICE) {
            if (Random.nextFloat() < 0.35f) {
                return legalMoves.random()
            }
        }

        // 3. Beginner mode: 15% random blunder, else depth 2
        if (difficulty == AIDifficulty.BEGINNER) {
            if (Random.nextFloat() < 0.15f) {
                val nonBlunder = legalMoves.shuffled().take(3).random()
                return nonBlunder
            }
        }

        val depth = difficulty.depth
        val isMaximizing = board.turn == PieceColor.WHITE

        var bestMove: Move? = null
        var bestScore = if (isMaximizing) -1_000_000 else 1_000_000

        // Order moves for high alpha-beta pruning efficiency
        val orderedMoves = orderMoves(board, legalMoves)

        for (move in orderedMoves) {
            val simBoard = board.clone()
            simBoard.makeMove(move)

            val score = minimax(
                board = simBoard,
                depth = depth - 1,
                alpha = -1_000_000,
                beta = 1_000_000,
                isMaximizing = !isMaximizing
            )

            if (isMaximizing) {
                if (score > bestScore) {
                    bestScore = score
                    bestMove = move
                }
            } else {
                if (score < bestScore) {
                    bestScore = score
                    bestMove = move
                }
            }
        }

        return bestMove ?: legalMoves.first()
    }

    private fun minimax(
        board: ChessBoard,
        depth: Int,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean
    ): Int {
        val state = board.getGameState()
        if (state.isOver) {
            return when (state) {
                com.example.chess.model.GameState.CHECKMATE -> {
                    // Checkmated side loses heavily
                    if (isMaximizing) -50000 - depth else 50000 + depth
                }
                else -> 0 // Stalemate or draw
            }
        }

        if (depth <= 0) {
            return quiescence(board, alpha, beta, isMaximizing, 2)
        }

        var curAlpha = alpha
        var curBeta = beta
        val legalMoves = board.generateLegalMoves(board.turn)
        val orderedMoves = orderMoves(board, legalMoves)

        if (isMaximizing) {
            var maxEval = -1_000_000
            for (move in orderedMoves) {
                val clone = board.clone()
                clone.makeMove(move)
                val eval = minimax(clone, depth - 1, curAlpha, curBeta, false)
                maxEval = maxOf(maxEval, eval)
                curAlpha = maxOf(curAlpha, eval)
                if (curBeta <= curAlpha) break // Beta cutoff
            }
            return maxEval
        } else {
            var minEval = 1_000_000
            for (move in orderedMoves) {
                val clone = board.clone()
                clone.makeMove(move)
                val eval = minimax(clone, depth - 1, curAlpha, curBeta, true)
                minEval = minOf(minEval, eval)
                curBeta = minOf(curBeta, eval)
                if (curBeta <= curAlpha) break // Alpha cutoff
            }
            return minEval
        }
    }

    // Quiescence search to avoid the horizon effect on tactical captures
    private fun quiescence(
        board: ChessBoard,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean,
        limitCaptures: Int
    ): Int {
        val standPat = evaluatePosition(board)

        if (limitCaptures <= 0) return standPat

        var curAlpha = alpha
        var curBeta = beta

        if (isMaximizing) {
            if (standPat >= curBeta) return curBeta
            if (standPat > curAlpha) curAlpha = standPat

            val captures = board.generateLegalMoves(board.turn).filter { it.capturedPiece != null }
            for (move in captures) {
                val clone = board.clone()
                clone.makeMove(move)
                val score = quiescence(clone, curAlpha, curBeta, false, limitCaptures - 1)
                if (score >= curBeta) return curBeta
                if (score > curAlpha) curAlpha = score
            }
            return curAlpha
        } else {
            if (standPat <= curAlpha) return curAlpha
            if (standPat < curBeta) curBeta = standPat

            val captures = board.generateLegalMoves(board.turn).filter { it.capturedPiece != null }
            for (move in captures) {
                val clone = board.clone()
                clone.makeMove(move)
                val score = quiescence(clone, curAlpha, curBeta, true, limitCaptures - 1)
                if (score <= curAlpha) return curAlpha
                if (score < curBeta) curBeta = score
            }
            return curBeta
        }
    }

    private fun orderMoves(board: ChessBoard, moves: List<Move>): List<Move> {
        return moves.sortedByDescending { move ->
            var score = 0
            // MVV-LVA (Most Valuable Victim - Least Valuable Attacker)
            if (move.capturedPiece != null) {
                val victimVal = getPieceValue(move.capturedPiece.type)
                val attacker = board.getPiece(move.from)
                val attackerVal = if (attacker != null) getPieceValue(attacker.type) else 0
                score += 1000 + (victimVal - attackerVal / 10)
            }
            // Promotion priority
            if (move.promotion != null) {
                score += 900
            }
            // Castling bonus
            if (move.isCastling) {
                score += 60
            }
            score
        }
    }

    fun evaluateBoardInCentipawns(board: ChessBoard): Int {
        return evaluatePosition(board)
    }

    // Centipawns positive = White advantage, negative = Black advantage
    private fun evaluatePosition(board: ChessBoard): Int {
        var score = 0

        for (f in 0..7) {
            for (r in 0..7) {
                val piece = board.getPiece(f, r) ?: continue
                val isWhite = piece.color == PieceColor.WHITE
                val value = getPieceValue(piece.type)
                val pst = getPstBonus(piece.type, f, r, isWhite)

                val pieceScore = value + pst
                if (isWhite) {
                    score += pieceScore
                } else {
                    score -= pieceScore
                }
            }
        }

        // Mobility count bonus
        val whiteMobility = board.generatePseudoLegalMoves(PieceColor.WHITE).size
        val blackMobility = board.generatePseudoLegalMoves(PieceColor.BLACK).size
        score += (whiteMobility - blackMobility) * 2

        // Check bonus/penalty
        if (board.isKingInCheck(PieceColor.BLACK)) score += 35
        if (board.isKingInCheck(PieceColor.WHITE)) score -= 35

        return score
    }

    private fun getPieceValue(type: PieceType): Int = when (type) {
        PieceType.PAWN -> PAWN_VAL
        PieceType.KNIGHT -> KNIGHT_VAL
        PieceType.BISHOP -> BISHOP_VAL
        PieceType.ROOK -> ROOK_VAL
        PieceType.QUEEN -> QUEEN_VAL
        PieceType.KING -> KING_VAL
    }

    private fun getPstBonus(type: PieceType, file: Int, rank: Int, isWhite: Boolean): Int {
        val tableRank = if (isWhite) rank else (7 - rank)
        val tableFile = file

        return when (type) {
            PieceType.PAWN -> PAWN_PST[tableRank][tableFile]
            PieceType.KNIGHT -> KNIGHT_PST[tableRank][tableFile]
            PieceType.BISHOP -> BISHOP_PST[tableRank][tableFile]
            PieceType.ROOK -> ROOK_PST[tableRank][tableFile]
            PieceType.QUEEN -> QUEEN_PST[tableRank][tableFile]
            PieceType.KING -> KING_OPENING_PST[tableRank][tableFile]
        }
    }

    fun getHint(board: ChessBoard): Pair<Move, String>? {
        val move = getBestMove(board, AIDifficulty.MASTER) ?: return null
        val piece = board.getPiece(move.from)
        val pieceName = piece?.type?.displayName ?: "Piece"

        val explanation = when {
            move.isCastling -> "Castle your king to safety and connect rooks."
            move.promotion != null -> "Promote pawn into Queen for decisive material gain."
            move.capturedPiece != null -> "Capture opponent's ${move.capturedPiece.type.displayName.lowercase()} at ${move.to.toAlgebraic()}."
            piece?.type == PieceType.PAWN -> "Advance pawn to ${move.to.toAlgebraic()} to fight for central control."
            piece?.type == PieceType.KNIGHT -> "Maneuver knight to active outpost at ${move.to.toAlgebraic()}."
            piece?.type == PieceType.BISHOP -> "Develop bishop along open diagonal to ${move.to.toAlgebraic()}."
            piece?.type == PieceType.ROOK -> "Control rank/file with rook at ${move.to.toAlgebraic()}."
            piece?.type == PieceType.QUEEN -> "Position queen aggressively at ${move.to.toAlgebraic()}."
            else -> "Play $pieceName to ${move.to.toAlgebraic()} to improve your position."
        }

        return Pair(move, explanation)
    }
}
