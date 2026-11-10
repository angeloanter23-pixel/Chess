package com.example.chess.model

enum class PieceColor {
    WHITE,
    BLACK;

    fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE
}

enum class PieceType(val symbol: String, val value: Int, val displayName: String) {
    PAWN("P", 1, "Pawn"),
    KNIGHT("N", 3, "Knight"),
    BISHOP("B", 3, "Bishop"),
    ROOK("R", 5, "Rook"),
    QUEEN("Q", 9, "Queen"),
    KING("K", 100, "King");

    fun getUnicode(color: PieceColor): String = when (color) {
        PieceColor.WHITE -> when (this) {
            PAWN -> "♙"
            KNIGHT -> "♘"
            BISHOP -> "♗"
            ROOK -> "♖"
            QUEEN -> "♕"
            KING -> "♔"
        }
        PieceColor.BLACK -> when (this) {
            PAWN -> "♟"
            KNIGHT -> "♞"
            BISHOP -> "♝"
            ROOK -> "♜"
            QUEEN -> "♛"
            KING -> "♚"
        }
    }
}

data class Piece(
    val type: PieceType,
    val color: PieceColor
)

data class Square(
    val file: Int, // 0..7 (a..h)
    val rank: Int  // 0..7 (1..8)
) {
    init {
        require(file in 0..7 && rank in 0..7) { "Square out of bounds: file=$file, rank=$rank" }
    }

    val fileChar: Char get() = ('a' + file)
    val rankChar: Char get() = ('1' + rank)

    fun toAlgebraic(): String = "$fileChar$rankChar"

    companion object {
        fun fromAlgebraic(algebraic: String): Square? {
            if (algebraic.length != 2) return null
            val file = algebraic[0].lowercaseChar() - 'a'
            val rank = algebraic[1] - '1'
            if (file !in 0..7 || rank !in 0..7) return null
            return Square(file, rank)
        }

        fun isValid(file: Int, rank: Int): Boolean = file in 0..7 && rank in 0..7
    }
}

data class Move(
    val from: Square,
    val to: Square,
    val promotion: PieceType? = null,
    val isCastling: Boolean = false,
    val isEnPassant: Boolean = false,
    val capturedPiece: Piece? = null,
    var san: String = ""
) {
    fun toUci(): String {
        val promo = promotion?.symbol?.lowercase() ?: ""
        return "${from.toAlgebraic()}${to.toAlgebraic()}$promo"
    }
}

enum class GameState(val isOver: Boolean, val message: String) {
    ONGOING(false, "Game in progress"),
    CHECKMATE(true, "Checkmate!"),
    STALEMATE(true, "Draw by Stalemate"),
    DRAW_INSUFFICIENT_MATERIAL(true, "Draw by Insufficient Material"),
    DRAW_50_MOVES(true, "Draw by 50-Move Rule"),
    DRAW_REPETITION(true, "Draw by 3-Fold Repetition"),
    TIMEOUT_WHITE_WINS(true, "White wins on time"),
    TIMEOUT_BLACK_WINS(true, "Black wins on time")
}

enum class GameMode(val displayName: String) {
    VS_AI("vs Computer"),
    PASS_AND_PLAY("Pass & Play"),
    PUZZLES("Puzzles"),
    ANALYSIS("Analysis Board")
}

enum class AIDifficulty(val displayName: String, val elo: Int, val depth: Int, val description: String) {
    NOVICE("Novice", 400, 1, "Casual bot that occasionally blunders"),
    BEGINNER("Beginner", 800, 2, "Plays solid basics with tactical gaps"),
    INTERMEDIATE("Club Player", 1200, 3, "Plans moves ahead with good piece coordination"),
    ADVANCED("Expert", 1500, 4, "Strong tactical awareness and positional defense"),
    MASTER("Grandmaster", 1850, 4, "Deep calculation, positional play & opening mastery")
}

enum class BoardTheme(
    val displayName: String,
    val lightSquareHex: Long,
    val darkSquareHex: Long,
    val highlightLastHex: Long = 0x88F7EC59,
    val selectedSquareHex: Long = 0x9952B788,
    val hintSquareHex: Long = 0x9938B2AC
) {
    EMERALD(
        displayName = "Emerald",
        lightSquareHex = 0xFFEBECD0,
        darkSquareHex = 0xFF739552
    ),
    WOOD(
        displayName = "Tournament Wood",
        lightSquareHex = 0xFFF0D9B5,
        darkSquareHex = 0xFFB58863
    ),
    MIDNIGHT(
        displayName = "Midnight Obsidian",
        lightSquareHex = 0xFF4A5568,
        darkSquareHex = 0xFF1A202C
    ),
    OCEAN(
        displayName = "Nordic Ocean",
        lightSquareHex = 0xFFD2E5EE,
        darkSquareHex = 0xFF4B7399
    )
}

enum class TimeControl(val displayName: String, val seconds: Int) {
    UNLIMITED("Untimed", 0),
    BULLET_1("1 min", 60),
    BLITZ_3("3 min", 180),
    BLITZ_5("5 min", 300),
    RAPID_10("10 min", 600)
}
