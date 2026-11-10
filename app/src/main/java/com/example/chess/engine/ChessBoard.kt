package com.example.chess.engine

import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Square

class ChessBoard {

    private val grid: Array<Array<Piece?>> = Array(8) { Array(8) { null } }

    var turn: PieceColor = PieceColor.WHITE
        private set

    var enPassantTarget: Square? = null
        private set

    var whiteKingMoved: Boolean = false
        private set
    var whiteKingsideRookMoved: Boolean = false
        private set
    var whiteQueensideRookMoved: Boolean = false
        private set

    var blackKingMoved: Boolean = false
        private set
    var blackKingsideRookMoved: Boolean = false
        private set
    var blackQueensideRookMoved: Boolean = false
        private set

    var halfmoveClock: Int = 0
        private set
    var fullmoveNumber: Int = 1
        private set

    private val positionHistory: MutableList<String> = mutableListOf()
    private val moveHistoryStack: MutableList<BoardSnapshot> = mutableListOf()
    val completedMoves: MutableList<Move> = mutableListOf()

    private data class BoardSnapshot(
        val gridCopy: Array<Array<Piece?>>,
        val turn: PieceColor,
        val enPassantTarget: Square?,
        val whiteKingMoved: Boolean,
        val whiteKingsideRookMoved: Boolean,
        val whiteQueensideRookMoved: Boolean,
        val blackKingMoved: Boolean,
        val blackKingsideRookMoved: Boolean,
        val blackQueensideRookMoved: Boolean,
        val halfmoveClock: Int,
        val fullmoveNumber: Int,
        val lastMove: Move?
    )

    init {
        setupStandardPosition()
    }

    fun reset() {
        setupStandardPosition()
    }

    fun setupStandardPosition() {
        for (f in 0..7) {
            for (r in 0..7) {
                grid[f][r] = null
            }
        }

        // White pieces (rank 0 = '1', rank 1 = '2')
        grid[0][0] = Piece(PieceType.ROOK, PieceColor.WHITE)
        grid[1][0] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
        grid[2][0] = Piece(PieceType.BISHOP, PieceColor.WHITE)
        grid[3][0] = Piece(PieceType.QUEEN, PieceColor.WHITE)
        grid[4][0] = Piece(PieceType.KING, PieceColor.WHITE)
        grid[5][0] = Piece(PieceType.BISHOP, PieceColor.WHITE)
        grid[6][0] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
        grid[7][0] = Piece(PieceType.ROOK, PieceColor.WHITE)
        for (f in 0..7) {
            grid[f][1] = Piece(PieceType.PAWN, PieceColor.WHITE)
        }

        // Black pieces (rank 7 = '8', rank 6 = '7')
        grid[0][7] = Piece(PieceType.ROOK, PieceColor.BLACK)
        grid[1][7] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
        grid[2][7] = Piece(PieceType.BISHOP, PieceColor.BLACK)
        grid[3][7] = Piece(PieceType.QUEEN, PieceColor.BLACK)
        grid[4][7] = Piece(PieceType.KING, PieceColor.BLACK)
        grid[5][7] = Piece(PieceType.BISHOP, PieceColor.BLACK)
        grid[6][7] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
        grid[7][7] = Piece(PieceType.ROOK, PieceColor.BLACK)
        for (f in 0..7) {
            grid[f][6] = Piece(PieceType.PAWN, PieceColor.BLACK)
        }

        turn = PieceColor.WHITE
        enPassantTarget = null
        whiteKingMoved = false
        whiteKingsideRookMoved = false
        whiteQueensideRookMoved = false
        blackKingMoved = false
        blackKingsideRookMoved = false
        blackQueensideRookMoved = false
        halfmoveClock = 0
        fullmoveNumber = 1
        positionHistory.clear()
        moveHistoryStack.clear()
        completedMoves.clear()
        recordPosition()
    }

    fun getPiece(square: Square): Piece? = grid[square.file][square.rank]
    fun getPiece(file: Int, rank: Int): Piece? = if (Square.isValid(file, rank)) grid[file][rank] else null

    fun setPiece(square: Square, piece: Piece?) {
        grid[square.file][square.rank] = piece
    }

    fun copyBoard(): Array<Array<Piece?>> {
        return Array(8) { f ->
            Array(8) { r ->
                grid[f][r]
            }
        }
    }

    fun clone(): ChessBoard {
        val clone = ChessBoard()
        for (f in 0..7) {
            for (r in 0..7) {
                clone.grid[f][r] = this.grid[f][r]
            }
        }
        clone.turn = this.turn
        clone.enPassantTarget = this.enPassantTarget
        clone.whiteKingMoved = this.whiteKingMoved
        clone.whiteKingsideRookMoved = this.whiteKingsideRookMoved
        clone.whiteQueensideRookMoved = this.whiteQueensideRookMoved
        clone.blackKingMoved = this.blackKingMoved
        clone.blackKingsideRookMoved = this.blackKingsideRookMoved
        clone.blackQueensideRookMoved = this.blackQueensideRookMoved
        clone.halfmoveClock = this.halfmoveClock
        clone.fullmoveNumber = this.fullmoveNumber
        clone.positionHistory.addAll(this.positionHistory)
        return clone
    }

    fun isSquareAttacked(square: Square, byColor: PieceColor): Boolean {
        // Pawn attacks
        val pawnPushes = if (byColor == PieceColor.WHITE) -1 else 1
        val pawnAttackRank = square.rank + pawnPushes
        if (pawnAttackRank in 0..7) {
            if (square.file > 0) {
                val p = grid[square.file - 1][pawnAttackRank]
                if (p?.color == byColor && p.type == PieceType.PAWN) return true
            }
            if (square.file < 7) {
                val p = grid[square.file + 1][pawnAttackRank]
                if (p?.color == byColor && p.type == PieceType.PAWN) return true
            }
        }

        // Knight attacks
        val knightOffsets = arrayOf(
            Pair(1, 2), Pair(2, 1), Pair(2, -1), Pair(1, -2),
            Pair(-1, -2), Pair(-2, -1), Pair(-2, 1), Pair(-1, 2)
        )
        for (off in knightOffsets) {
            val f = square.file + off.first
            val r = square.rank + off.second
            if (Square.isValid(f, r)) {
                val p = grid[f][r]
                if (p?.color == byColor && p.type == PieceType.KNIGHT) return true
            }
        }

        // Diagonal attacks (Bishop, Queen)
        val diagDirs = arrayOf(Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1))
        for (dir in diagDirs) {
            var f = square.file + dir.first
            var r = square.rank + dir.second
            while (Square.isValid(f, r)) {
                val p = grid[f][r]
                if (p != null) {
                    if (p.color == byColor && (p.type == PieceType.BISHOP || p.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                f += dir.first
                r += dir.second
            }
        }

        // Orthogonal attacks (Rook, Queen)
        val orthoDirs = arrayOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))
        for (dir in orthoDirs) {
            var f = square.file + dir.first
            var r = square.rank + dir.second
            while (Square.isValid(f, r)) {
                val p = grid[f][r]
                if (p != null) {
                    if (p.color == byColor && (p.type == PieceType.ROOK || p.type == PieceType.QUEEN)) {
                        return true
                    }
                    break
                }
                f += dir.first
                r += dir.second
            }
        }

        // King adjacent attacks
        for (df in -1..1) {
            for (dr in -1..1) {
                if (df == 0 && dr == 0) continue
                val f = square.file + df
                val r = square.rank + dr
                if (Square.isValid(f, r)) {
                    val p = grid[f][r]
                    if (p?.color == byColor && p.type == PieceType.KING) return true
                }
            }
        }

        return false
    }

    fun findKingSquare(color: PieceColor): Square? {
        for (f in 0..7) {
            for (r in 0..7) {
                val p = grid[f][r]
                if (p != null && p.type == PieceType.KING && p.color == color) {
                    return Square(f, r)
                }
            }
        }
        return null
    }

    fun isKingInCheck(color: PieceColor): Boolean {
        val kingSq = findKingSquare(color) ?: return false
        return isSquareAttacked(kingSq, color.opposite())
    }

    fun generateLegalMoves(forColor: PieceColor = turn): List<Move> {
        val pseudoMoves = generatePseudoLegalMoves(forColor)
        val legalMoves = mutableListOf<Move>()

        for (m in pseudoMoves) {
            if (isMoveLegal(m, forColor)) {
                legalMoves.add(m)
            }
        }

        // Compute SAN for all legal moves to enable rich notation and disambiguation
        assignSanNotations(legalMoves, forColor)

        return legalMoves
    }

    private fun isMoveLegal(move: Move, forColor: PieceColor): Boolean {
        // Special check for castling: King cannot castle out of, through, or into check
        if (move.isCastling) {
            if (isKingInCheck(forColor)) return false
            val rank = if (forColor == PieceColor.WHITE) 0 else 7
            val step = if (move.to.file > move.from.file) 1 else -1
            // Intermediate square check
            val midSquare = Square(move.from.file + step, rank)
            if (isSquareAttacked(midSquare, forColor.opposite())) return false
            if (isSquareAttacked(move.to, forColor.opposite())) return false
        }

        // Apply move temporarily
        val fromPiece = grid[move.from.file][move.from.rank] ?: return false
        val toPiece = grid[move.to.file][move.to.rank]
        var enPassantCaptured: Piece? = null

        grid[move.from.file][move.from.rank] = null

        if (move.isEnPassant) {
            val capturedPawnRank = if (forColor == PieceColor.WHITE) move.to.rank - 1 else move.to.rank + 1
            enPassantCaptured = grid[move.to.file][capturedPawnRank]
            grid[move.to.file][capturedPawnRank] = null
        }

        if (move.promotion != null) {
            grid[move.to.file][move.to.rank] = Piece(move.promotion, forColor)
        } else {
            grid[move.to.file][move.to.rank] = fromPiece
        }

        val inCheck = isKingInCheck(forColor)

        // Undo temporary move
        grid[move.from.file][move.from.rank] = fromPiece
        grid[move.to.file][move.to.rank] = toPiece
        if (move.isEnPassant) {
            val capturedPawnRank = if (forColor == PieceColor.WHITE) move.to.rank - 1 else move.to.rank + 1
            grid[move.to.file][capturedPawnRank] = enPassantCaptured
        }

        return !inCheck
    }

    fun generatePseudoLegalMoves(forColor: PieceColor): List<Move> {
        val moves = mutableListOf<Move>()
        for (f in 0..7) {
            for (r in 0..7) {
                val piece = grid[f][r] ?: continue
                if (piece.color != forColor) continue
                val from = Square(f, r)

                when (piece.type) {
                    PieceType.PAWN -> generatePawnMoves(from, piece, moves)
                    PieceType.KNIGHT -> generateKnightMoves(from, piece, moves)
                    PieceType.BISHOP -> generateSlidingMoves(from, piece, arrayOf(Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1)), moves)
                    PieceType.ROOK -> generateSlidingMoves(from, piece, arrayOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1)), moves)
                    PieceType.QUEEN -> generateSlidingMoves(from, piece, arrayOf(
                        Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1),
                        Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1)
                    ), moves)
                    PieceType.KING -> generateKingMoves(from, piece, moves)
                }
            }
        }
        return moves
    }

    private fun generatePawnMoves(from: Square, piece: Piece, out: MutableList<Move>) {
        val forward = if (piece.color == PieceColor.WHITE) 1 else -1
        val startRank = if (piece.color == PieceColor.WHITE) 1 else 6
        val promoRank = if (piece.color == PieceColor.WHITE) 7 else 0

        // Single push
        val oneRank = from.rank + forward
        if (oneRank in 0..7 && grid[from.file][oneRank] == null) {
            val to = Square(from.file, oneRank)
            if (oneRank == promoRank) {
                // Promotions
                out.add(Move(from, to, promotion = PieceType.QUEEN))
                out.add(Move(from, to, promotion = PieceType.ROOK))
                out.add(Move(from, to, promotion = PieceType.BISHOP))
                out.add(Move(from, to, promotion = PieceType.KNIGHT))
            } else {
                out.add(Move(from, to))
                // Double push from start rank
                val twoRank = from.rank + forward * 2
                if (from.rank == startRank && grid[from.file][twoRank] == null) {
                    out.add(Move(from, Square(from.file, twoRank)))
                }
            }
        }

        // Captures
        val captureFiles = intArrayOf(from.file - 1, from.file + 1)
        for (cf in captureFiles) {
            if (cf in 0..7 && oneRank in 0..7) {
                val target = grid[cf][oneRank]
                val to = Square(cf, oneRank)
                if (target != null && target.color != piece.color) {
                    if (oneRank == promoRank) {
                        out.add(Move(from, to, promotion = PieceType.QUEEN, capturedPiece = target))
                        out.add(Move(from, to, promotion = PieceType.ROOK, capturedPiece = target))
                        out.add(Move(from, to, promotion = PieceType.BISHOP, capturedPiece = target))
                        out.add(Move(from, to, promotion = PieceType.KNIGHT, capturedPiece = target))
                    } else {
                        out.add(Move(from, to, capturedPiece = target))
                    }
                } else if (enPassantTarget != null && to == enPassantTarget) {
                    // En passant
                    val targetPawn = grid[cf][from.rank]
                    out.add(Move(from, to, isEnPassant = true, capturedPiece = targetPawn))
                }
            }
        }
    }

    private fun generateKnightMoves(from: Square, piece: Piece, out: MutableList<Move>) {
        val offsets = arrayOf(
            Pair(1, 2), Pair(2, 1), Pair(2, -1), Pair(1, -2),
            Pair(-1, -2), Pair(-2, -1), Pair(-2, 1), Pair(-1, 2)
        )
        for (off in offsets) {
            val f = from.file + off.first
            val r = from.rank + off.second
            if (Square.isValid(f, r)) {
                val destPiece = grid[f][r]
                if (destPiece == null || destPiece.color != piece.color) {
                    out.add(Move(from, Square(f, r), capturedPiece = destPiece))
                }
            }
        }
    }

    private fun generateSlidingMoves(from: Square, piece: Piece, directions: Array<Pair<Int, Int>>, out: MutableList<Move>) {
        for (dir in directions) {
            var f = from.file + dir.first
            var r = from.rank + dir.second
            while (Square.isValid(f, r)) {
                val destPiece = grid[f][r]
                if (destPiece == null) {
                    out.add(Move(from, Square(f, r)))
                } else {
                    if (destPiece.color != piece.color) {
                        out.add(Move(from, Square(f, r), capturedPiece = destPiece))
                    }
                    break
                }
                f += dir.first
                r += dir.second
            }
        }
    }

    private fun generateKingMoves(from: Square, piece: Piece, out: MutableList<Move>) {
        for (df in -1..1) {
            for (dr in -1..1) {
                if (df == 0 && dr == 0) continue
                val f = from.file + df
                val r = from.rank + dr
                if (Square.isValid(f, r)) {
                    val destPiece = grid[f][r]
                    if (destPiece == null || destPiece.color != piece.color) {
                        out.add(Move(from, Square(f, r), capturedPiece = destPiece))
                    }
                }
            }
        }

        // Castling moves
        if (piece.color == PieceColor.WHITE && !whiteKingMoved && from == Square(4, 0)) {
            // Kingside (O-O) -> e1 to g1
            if (!whiteKingsideRookMoved && grid[7][0]?.type == PieceType.ROOK && grid[7][0]?.color == PieceColor.WHITE) {
                if (grid[5][0] == null && grid[6][0] == null) {
                    out.add(Move(from, Square(6, 0), isCastling = true))
                }
            }
            // Queenside (O-O-O) -> e1 to c1
            if (!whiteQueensideRookMoved && grid[0][0]?.type == PieceType.ROOK && grid[0][0]?.color == PieceColor.WHITE) {
                if (grid[1][0] == null && grid[2][0] == null && grid[3][0] == null) {
                    out.add(Move(from, Square(2, 0), isCastling = true))
                }
            }
        } else if (piece.color == PieceColor.BLACK && !blackKingMoved && from == Square(4, 7)) {
            // Kingside (O-O) -> e8 to g8
            if (!blackKingsideRookMoved && grid[7][7]?.type == PieceType.ROOK && grid[7][7]?.color == PieceColor.BLACK) {
                if (grid[5][7] == null && grid[6][7] == null) {
                    out.add(Move(from, Square(6, 7), isCastling = true))
                }
            }
            // Queenside (O-O-O) -> e8 to c8
            if (!blackQueensideRookMoved && grid[0][7]?.type == PieceType.ROOK && grid[0][7]?.color == PieceColor.BLACK) {
                if (grid[1][7] == null && grid[2][7] == null && grid[3][7] == null) {
                    out.add(Move(from, Square(2, 7), isCastling = true))
                }
            }
        }
    }

    fun makeMove(move: Move): Boolean {
        val movingPiece = grid[move.from.file][move.from.rank] ?: return false
        if (movingPiece.color != turn) return false

        // Take snapshot before mutating for undo support
        moveHistoryStack.add(
            BoardSnapshot(
                gridCopy = copyBoard(),
                turn = turn,
                enPassantTarget = enPassantTarget,
                whiteKingMoved = whiteKingMoved,
                whiteKingsideRookMoved = whiteKingsideRookMoved,
                whiteQueensideRookMoved = whiteQueensideRookMoved,
                blackKingMoved = blackKingMoved,
                blackKingsideRookMoved = blackKingsideRookMoved,
                blackQueensideRookMoved = blackQueensideRookMoved,
                halfmoveClock = halfmoveClock,
                fullmoveNumber = fullmoveNumber,
                lastMove = completedMoves.lastOrNull()
            )
        )

        val isCapture = move.capturedPiece != null || grid[move.to.file][move.to.rank] != null || move.isEnPassant
        val isPawnMove = movingPiece.type == PieceType.PAWN

        // Move piece on grid
        grid[move.from.file][move.from.rank] = null

        // Handle En Passant capture
        if (move.isEnPassant) {
            val capRank = if (turn == PieceColor.WHITE) move.to.rank - 1 else move.to.rank + 1
            grid[move.to.file][capRank] = null
        }

        // Handle Castling rook move
        if (move.isCastling) {
            val rank = move.from.rank
            if (move.to.file == 6) { // Kingside
                val rook = grid[7][rank]
                grid[7][rank] = null
                grid[5][rank] = rook
            } else if (move.to.file == 2) { // Queenside
                val rook = grid[0][rank]
                grid[0][rank] = null
                grid[3][rank] = rook
            }
        }

        // Place piece or promoted piece
        if (move.promotion != null) {
            grid[move.to.file][move.to.rank] = Piece(move.promotion, turn)
        } else {
            grid[move.to.file][move.to.rank] = movingPiece
        }

        // Update castling rights
        if (movingPiece.type == PieceType.KING) {
            if (turn == PieceColor.WHITE) whiteKingMoved = true else blackKingMoved = true
        }
        if (movingPiece.type == PieceType.ROOK) {
            if (turn == PieceColor.WHITE) {
                if (move.from == Square(0, 0)) whiteQueensideRookMoved = true
                if (move.from == Square(7, 0)) whiteKingsideRookMoved = true
            } else {
                if (move.from == Square(0, 7)) blackQueensideRookMoved = true
                if (move.from == Square(7, 7)) blackKingsideRookMoved = true
            }
        }
        // If rook was captured on original square
        if (move.to == Square(0, 0)) whiteQueensideRookMoved = true
        if (move.to == Square(7, 0)) whiteKingsideRookMoved = true
        if (move.to == Square(0, 7)) blackQueensideRookMoved = true
        if (move.to == Square(7, 7)) blackKingsideRookMoved = true

        // Update En Passant target
        if (isPawnMove && kotlin.math.abs(move.to.rank - move.from.rank) == 2) {
            val epRank = (move.to.rank + move.from.rank) / 2
            enPassantTarget = Square(move.from.file, epRank)
        } else {
            enPassantTarget = null
        }

        // Update 50-move clock
        if (isCapture || isPawnMove) {
            halfmoveClock = 0
        } else {
            halfmoveClock++
        }

        // Update full move count
        if (turn == PieceColor.BLACK) {
            fullmoveNumber++
        }

        turn = turn.opposite()
        completedMoves.add(move)
        recordPosition()

        return true
    }

    fun undoMove(): Move? {
        if (moveHistoryStack.isEmpty()) return null
        val snapshot = moveHistoryStack.removeAt(moveHistoryStack.size - 1)

        for (f in 0..7) {
            for (r in 0..7) {
                grid[f][r] = snapshot.gridCopy[f][r]
            }
        }

        turn = snapshot.turn
        enPassantTarget = snapshot.enPassantTarget
        whiteKingMoved = snapshot.whiteKingMoved
        whiteKingsideRookMoved = snapshot.whiteKingsideRookMoved
        whiteQueensideRookMoved = snapshot.whiteQueensideRookMoved
        blackKingMoved = snapshot.blackKingMoved
        blackKingsideRookMoved = snapshot.blackKingsideRookMoved
        blackQueensideRookMoved = snapshot.blackQueensideRookMoved
        halfmoveClock = snapshot.halfmoveClock
        fullmoveNumber = snapshot.fullmoveNumber

        if (positionHistory.isNotEmpty()) {
            positionHistory.removeAt(positionHistory.size - 1)
        }

        val lastMove = if (completedMoves.isNotEmpty()) {
            completedMoves.removeAt(completedMoves.size - 1)
        } else null

        return lastMove
    }

    fun getGameState(): GameState {
        val legalMoves = generateLegalMoves(turn)
        val inCheck = isKingInCheck(turn)

        if (legalMoves.isEmpty()) {
            return if (inCheck) GameState.CHECKMATE else GameState.STALEMATE
        }

        if (halfmoveClock >= 100) {
            return GameState.DRAW_50_MOVES
        }

        if (isThreefoldRepetition()) {
            return GameState.DRAW_REPETITION
        }

        if (hasInsufficientMaterial()) {
            return GameState.DRAW_INSUFFICIENT_MATERIAL
        }

        return GameState.ONGOING
    }

    private fun recordPosition() {
        positionHistory.add(getBoardKey())
    }

    private fun getBoardKey(): String {
        val sb = StringBuilder()
        for (r in 7 downTo 0) {
            for (f in 0..7) {
                val p = grid[f][r]
                sb.append(p?.type?.symbol ?: ".")
                sb.append(p?.color?.name?.first() ?: ".")
            }
        }
        sb.append(turn.name.first())
        sb.append(if (whiteKingMoved) "0" else "1")
        sb.append(if (whiteKingsideRookMoved) "0" else "1")
        sb.append(if (whiteQueensideRookMoved) "0" else "1")
        sb.append(if (blackKingMoved) "0" else "1")
        sb.append(if (blackKingsideRookMoved) "0" else "1")
        sb.append(if (blackQueensideRookMoved) "0" else "1")
        sb.append(enPassantTarget?.toAlgebraic() ?: "-")
        return sb.toString()
    }

    private fun isThreefoldRepetition(): Boolean {
        if (positionHistory.isEmpty()) return false
        val current = positionHistory.last()
        var count = 0
        for (pos in positionHistory) {
            if (pos == current) {
                count++
                if (count >= 3) return true
            }
        }
        return false
    }

    fun hasInsufficientMaterial(): Boolean {
        val whitePieces = mutableListOf<PieceType>()
        val blackPieces = mutableListOf<PieceType>()

        for (f in 0..7) {
            for (r in 0..7) {
                val p = grid[f][r] ?: continue
                if (p.type == PieceType.KING) continue
                if (p.color == PieceColor.WHITE) whitePieces.add(p.type)
                else blackPieces.add(p.type)
            }
        }

        // K vs K
        if (whitePieces.isEmpty() && blackPieces.isEmpty()) return true

        // K+B vs K or K+N vs K
        if (whitePieces.isEmpty() && blackPieces.size == 1 &&
            (blackPieces[0] == PieceType.BISHOP || blackPieces[0] == PieceType.KNIGHT)
        ) return true
        if (blackPieces.isEmpty() && whitePieces.size == 1 &&
            (whitePieces[0] == PieceType.BISHOP || whitePieces[0] == PieceType.KNIGHT)
        ) return true

        return false
    }

    fun getCapturedPieces(): Pair<List<Piece>, List<Piece>> {
        // Pieces captured by White (meaning Black pieces missing from board)
        // Pieces captured by Black (meaning White pieces missing from board)
        val initialWhite = mutableListOf(
            PieceType.PAWN, PieceType.PAWN, PieceType.PAWN, PieceType.PAWN,
            PieceType.PAWN, PieceType.PAWN, PieceType.PAWN, PieceType.PAWN,
            PieceType.KNIGHT, PieceType.KNIGHT,
            PieceType.BISHOP, PieceType.BISHOP,
            PieceType.ROOK, PieceType.ROOK,
            PieceType.QUEEN
        )
        val initialBlack = mutableListOf(
            PieceType.PAWN, PieceType.PAWN, PieceType.PAWN, PieceType.PAWN,
            PieceType.PAWN, PieceType.PAWN, PieceType.PAWN, PieceType.PAWN,
            PieceType.KNIGHT, PieceType.KNIGHT,
            PieceType.BISHOP, PieceType.BISHOP,
            PieceType.ROOK, PieceType.ROOK,
            PieceType.QUEEN
        )

        for (f in 0..7) {
            for (r in 0..7) {
                val p = grid[f][r] ?: continue
                if (p.type == PieceType.KING) continue
                if (p.color == PieceColor.WHITE) {
                    initialWhite.remove(p.type)
                } else {
                    initialBlack.remove(p.type)
                }
            }
        }

        val capturedBlackPieces = initialBlack.map { Piece(it, PieceColor.BLACK) }
        val capturedWhitePieces = initialWhite.map { Piece(it, PieceColor.WHITE) }

        return Pair(capturedBlackPieces, capturedWhitePieces)
    }

    private fun assignSanNotations(moves: List<Move>, forColor: PieceColor) {
        for (m in moves) {
            val movingPiece = grid[m.from.file][m.from.rank] ?: continue

            val san = when {
                m.isCastling -> {
                    if (m.to.file == 6) "O-O" else "O-O-O"
                }
                movingPiece.type == PieceType.PAWN -> {
                    if (m.capturedPiece != null || m.isEnPassant) {
                        val promo = if (m.promotion != null) "=${m.promotion.symbol}" else ""
                        "${m.from.fileChar}x${m.to.toAlgebraic()}$promo"
                    } else {
                        val promo = if (m.promotion != null) "=${m.promotion.symbol}" else ""
                        "${m.to.toAlgebraic()}$promo"
                    }
                }
                else -> {
                    // Non-pawn pieces
                    val symbol = movingPiece.type.symbol
                    val capture = if (m.capturedPiece != null) "x" else ""

                    // Check for disambiguation
                    val ambiguousMoves = moves.filter {
                        it.to == m.to && it.from != m.from &&
                                grid[it.from.file][it.from.rank]?.type == movingPiece.type &&
                                grid[it.from.file][it.from.rank]?.color == forColor
                    }

                    val disambiguation = when {
                        ambiguousMoves.isEmpty() -> ""
                        ambiguousMoves.none { it.from.file == m.from.file } -> "${m.from.fileChar}"
                        ambiguousMoves.none { it.from.rank == m.from.rank } -> "${m.from.rankChar}"
                        else -> m.from.toAlgebraic()
                    }

                    "$symbol$disambiguation$capture${m.to.toAlgebraic()}"
                }
            }
            m.san = san
        }
    }

    fun loadFen(fen: String) {
        val parts = fen.trim().split(" ")
        if (parts.isEmpty()) return

        for (f in 0..7) {
            for (r in 0..7) {
                grid[f][r] = null
            }
        }

        val ranks = parts[0].split("/")
        if (ranks.size != 8) return

        for (rIdx in 0..7) {
            val rank = 7 - rIdx
            var file = 0
            val rowStr = ranks[rIdx]
            for (ch in rowStr) {
                if (ch.isDigit()) {
                    file += ch.toString().toInt()
                } else {
                    val color = if (ch.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK
                    val type = when (ch.lowercaseChar()) {
                        'p' -> PieceType.PAWN
                        'n' -> PieceType.KNIGHT
                        'b' -> PieceType.BISHOP
                        'r' -> PieceType.ROOK
                        'q' -> PieceType.QUEEN
                        'k' -> PieceType.KING
                        else -> PieceType.PAWN
                    }
                    if (file in 0..7) {
                        grid[file][rank] = Piece(type, color)
                    }
                    file++
                }
            }
        }

        if (parts.size > 1) {
            turn = if (parts[1] == "b") PieceColor.BLACK else PieceColor.WHITE
        }

        if (parts.size > 2) {
            val castling = parts[2]
            whiteKingsideRookMoved = !castling.contains("K")
            whiteQueensideRookMoved = !castling.contains("Q")
            blackKingsideRookMoved = !castling.contains("k")
            blackQueensideRookMoved = !castling.contains("q")
            whiteKingMoved = !castling.contains("K") && !castling.contains("Q")
            blackKingMoved = !castling.contains("k") && !castling.contains("q")
        }

        if (parts.size > 3) {
            val ep = parts[3]
            enPassantTarget = if (ep != "-") Square.fromAlgebraic(ep) else null
        }

        if (parts.size > 4) {
            halfmoveClock = parts[4].toIntOrNull() ?: 0
        }

        if (parts.size > 5) {
            fullmoveNumber = parts[5].toIntOrNull() ?: 1
        }

        positionHistory.clear()
        moveHistoryStack.clear()
        completedMoves.clear()
        recordPosition()
    }

    fun exportFen(): String {
        val sb = StringBuilder()
        for (r in 7 downTo 0) {
            var emptyCount = 0
            for (f in 0..7) {
                val p = grid[f][r]
                if (p == null) {
                    emptyCount++
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount)
                        emptyCount = 0
                    }
                    val ch = when (p.type) {
                        PieceType.PAWN -> 'p'
                        PieceType.KNIGHT -> 'n'
                        PieceType.BISHOP -> 'b'
                        PieceType.ROOK -> 'r'
                        PieceType.QUEEN -> 'q'
                        PieceType.KING -> 'k'
                    }
                    sb.append(if (p.color == PieceColor.WHITE) ch.uppercaseChar() else ch)
                }
            }
            if (emptyCount > 0) sb.append(emptyCount)
            if (r > 0) sb.append('/')
        }

        sb.append(if (turn == PieceColor.WHITE) " w " else " b ")

        var castle = ""
        if (!whiteKingMoved) {
            if (!whiteKingsideRookMoved) castle += "K"
            if (!whiteQueensideRookMoved) castle += "Q"
        }
        if (!blackKingMoved) {
            if (!blackKingsideRookMoved) castle += "k"
            if (!blackQueensideRookMoved) castle += "q"
        }
        if (castle.isEmpty()) castle = "-"
        sb.append(castle).append(" ")

        sb.append(enPassantTarget?.toAlgebraic() ?: "-").append(" ")
        sb.append(halfmoveClock).append(" ")
        sb.append(fullmoveNumber)

        return sb.toString()
    }
}
