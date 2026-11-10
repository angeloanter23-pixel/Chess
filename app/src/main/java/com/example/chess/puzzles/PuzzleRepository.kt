package com.example.chess.puzzles

import com.example.chess.model.PieceColor

data class ChessPuzzle(
    val id: Int,
    val title: String,
    val theme: String,
    val difficulty: String,
    val rating: Int,
    val fen: String,
    val playerColor: PieceColor,
    val description: String,
    val solutionUci: List<String>,
    val explanation: String
)

object PuzzleRepository {

    val puzzles: List<ChessPuzzle> = listOf(
        ChessPuzzle(
            id = 1,
            title = "Back Rank Mate",
            theme = "Mate in 1",
            difficulty = "Beginner",
            rating = 750,
            fen = "6k1/5ppp/8/8/8/8/4QPPP/6K1 w - - 0 1",
            playerColor = PieceColor.WHITE,
            description = "White to move and checkmate the trapped king.",
            solutionUci = listOf("e2e8"),
            explanation = "The black king is trapped behind its own pawns. Queen to e8 delivers an inescapable back rank checkmate!"
        ),
        ChessPuzzle(
            id = 2,
            title = "Knight Fork",
            theme = "Tactical Fork",
            difficulty = "Beginner",
            rating = 850,
            fen = "r1b1k2r/pppp1ppp/8/4q3/1b2N3/8/PPP2PPP/R1BQKB1R w KQkq - 0 1",
            playerColor = PieceColor.WHITE,
            description = "White to move: break the pin safely.",
            solutionUci = listOf("c1d2"),
            explanation = "Bd2 blocks the pin and neutralizes Black's threat on the king!"
        ),
        ChessPuzzle(
            id = 3,
            title = "Smothered Mate",
            theme = "Knight Smother",
            difficulty = "Intermediate",
            rating = 1200,
            fen = "6rk/7p/7N/8/8/8/8/7K w - - 0 1",
            playerColor = PieceColor.WHITE,
            description = "White to move: deliver checkmate with the knight.",
            solutionUci = listOf("h6f7"),
            explanation = "Nf7# checkmate! The black king is completely smothered and blocked by its own pieces."
        ),
        ChessPuzzle(
            id = 4,
            title = "Scholar's Decisive Blow",
            theme = "Mate in 1",
            difficulty = "Beginner",
            rating = 600,
            fen = "r1bqkb1r/pppp1ppp/2n5/4p3/2B1n3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 0 1",
            playerColor = PieceColor.WHITE,
            description = "White to move: find the killer blow against f7.",
            solutionUci = listOf("f3f7"),
            explanation = "Qxf7# checkmate! Protected by the bishop on c4, the queen delivers instant checkmate against the vulnerable f7 square."
        ),
        ChessPuzzle(
            id = 5,
            title = "Anastasia's Mate",
            theme = "Checkmate Net",
            difficulty = "Advanced",
            rating = 1450,
            fen = "7k/5Npp/8/8/8/8/1R4PP/6K1 w - - 0 1",
            playerColor = PieceColor.WHITE,
            description = "White to move and finish off the cornered king.",
            solutionUci = listOf("b2b8"),
            explanation = "Rb8# checkmate! The rook pins the king along the 8th rank while the knight locks down escape squares."
        ),
        ChessPuzzle(
            id = 6,
            title = "Queen Skewer",
            theme = "Skewer Tactic",
            difficulty = "Intermediate",
            rating = 1100,
            fen = "4q3/8/4k3/8/8/8/8/R5K1 w - - 0 1",
            playerColor = PieceColor.WHITE,
            description = "White to move: slide rook across to set up the decisive skewer.",
            solutionUci = listOf("a1e1"),
            explanation = "Re1+ skewers the king! When the king steps away, White captures the undefended black queen on e8."
        ),
        ChessPuzzle(
            id = 7,
            title = "Queen Snatch",
            theme = "Hanging Piece",
            difficulty = "Intermediate",
            rating = 1050,
            fen = "r4rk1/ppp2ppp/8/4P3/1b1q4/8/PP3PPP/R1BQR1K1 w - - 0 1",
            playerColor = PieceColor.WHITE,
            description = "White to move: capture the undefended piece.",
            solutionUci = listOf("d1d4"),
            explanation = "Qxd4 wins the black queen cleanly!"
        ),
        ChessPuzzle(
            id = 8,
            title = "Arabian Mate",
            theme = "Rook and Knight Duo",
            difficulty = "Intermediate",
            rating = 1300,
            fen = "7k/R7/5N2/8/8/8/8/6K1 w - - 0 1",
            playerColor = PieceColor.WHITE,
            description = "White to move and deliver the legendary Arabian Checkmate.",
            solutionUci = listOf("a7h7"),
            explanation = "Rh7# checkmate! The knight on f6 covers g8 and protects the rook on h7. The king cannot escape!"
        )
    )
}
