package com.example.chess.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.audio.SoundManager
import com.example.chess.engine.ChessAI
import com.example.chess.engine.ChessBoard
import com.example.chess.model.AIDifficulty
import com.example.chess.model.BoardTheme
import com.example.chess.model.GameMode
import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import com.example.chess.model.TimeControl
import com.example.chess.puzzles.ChessPuzzle
import com.example.chess.puzzles.PuzzleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class PuzzleStatus {
    IN_PROGRESS,
    SOLVED,
    FAILED
}

data class ChessUiState(
    val boardGrid: Array<Array<Piece?>> = Array(8) { Array(8) { null } },
    val currentTurn: PieceColor = PieceColor.WHITE,
    val selectedSquare: Square? = null,
    val legalDestinations: List<Square> = emptyList(),
    val lastMove: Move? = null,
    val inCheckSquare: Square? = null,
    val gameState: GameState = GameState.ONGOING,
    val gameMode: GameMode = GameMode.VS_AI,
    val aiDifficulty: AIDifficulty = AIDifficulty.INTERMEDIATE,
    val playerColor: PieceColor = PieceColor.WHITE,
    val isBoardFlipped: Boolean = false,
    val isAiThinking: Boolean = false,
    val evaluationCentipawns: Int = 0,
    val hintMove: Move? = null,
    val hintExplanation: String? = null,
    val pendingPromotionMove: Move? = null,
    val showPromotionDialog: Boolean = false,
    val showGameOverDialog: Boolean = false,
    val boardTheme: BoardTheme = BoardTheme.EMERALD,
    val showCoordinates: Boolean = true,
    val timeControl: TimeControl = TimeControl.BLITZ_5,
    val whiteTimeRemainingMs: Long = 300_000L,
    val blackTimeRemainingMs: Long = 300_000L,
    val capturedByWhite: List<Piece> = emptyList(),
    val capturedByBlack: List<Piece> = emptyList(),
    val whiteAdvantage: Int = 0,
    val blackAdvantage: Int = 0,
    val moveHistory: List<Move> = emptyList(),
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    // Puzzles
    val currentPuzzle: ChessPuzzle? = null,
    val puzzleStatus: PuzzleStatus = PuzzleStatus.IN_PROGRESS,
    val puzzleFeedback: String = "",
    val solvedPuzzleIds: Set<Int> = emptySet()
)

class ChessViewModel(application: Application) : AndroidViewModel(application) {

    private val chessBoard = ChessBoard()
    val soundManager = SoundManager(application)

    private val _uiState = MutableStateFlow(ChessUiState())
    val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var aiJob: Job? = null

    init {
        startNewGame(GameMode.VS_AI, TimeControl.BLITZ_5, AIDifficulty.INTERMEDIATE, PieceColor.WHITE)
    }

    fun startNewGame(
        mode: GameMode = _uiState.value.gameMode,
        timeControl: TimeControl = _uiState.value.timeControl,
        difficulty: AIDifficulty = _uiState.value.aiDifficulty,
        playerColor: PieceColor = _uiState.value.playerColor
    ) {
        timerJob?.cancel()
        aiJob?.cancel()

        chessBoard.reset()

        val timeMs = timeControl.seconds * 1000L

        _uiState.update { current ->
            current.copy(
                boardGrid = chessBoard.copyBoard(),
                currentTurn = chessBoard.turn,
                selectedSquare = null,
                legalDestinations = emptyList(),
                lastMove = null,
                inCheckSquare = null,
                gameState = GameState.ONGOING,
                gameMode = mode,
                aiDifficulty = difficulty,
                playerColor = playerColor,
                isBoardFlipped = (playerColor == PieceColor.BLACK && mode == GameMode.VS_AI),
                isAiThinking = false,
                evaluationCentipawns = 0,
                hintMove = null,
                hintExplanation = null,
                pendingPromotionMove = null,
                showPromotionDialog = false,
                showGameOverDialog = false,
                timeControl = timeControl,
                whiteTimeRemainingMs = timeMs,
                blackTimeRemainingMs = timeMs,
                capturedByWhite = emptyList(),
                capturedByBlack = emptyList(),
                whiteAdvantage = 0,
                blackAdvantage = 0,
                moveHistory = emptyList()
            )
        }

        updateBoardState()

        if (timeControl != TimeControl.UNLIMITED && mode != GameMode.PUZZLES && mode != GameMode.ANALYSIS) {
            startTimer()
        }

        // If player chose Black in vs AI, let AI make opening move!
        if (mode == GameMode.VS_AI && playerColor == PieceColor.BLACK) {
            triggerAiMove()
        }
    }

    fun onSquareSelected(square: Square) {
        val state = _uiState.value
        if (state.gameState.isOver || state.isAiThinking) return

        // In VS_AI mode, ignore taps during AI turn
        if (state.gameMode == GameMode.VS_AI && state.currentTurn != state.playerColor) {
            return
        }

        val clickedPiece = chessBoard.getPiece(square)

        // Case 1: An existing square is already selected
        if (state.selectedSquare != null) {
            val from = state.selectedSquare

            // If clicking the same square, deselect
            if (from == square) {
                _uiState.update { it.copy(selectedSquare = null, legalDestinations = emptyList()) }
                return
            }

            // If clicking another piece of own color, switch selection
            if (clickedPiece != null && clickedPiece.color == state.currentTurn) {
                val legalMoves = chessBoard.generateLegalMoves(state.currentTurn).filter { it.from == square }
                _uiState.update {
                    it.copy(
                        selectedSquare = square,
                        legalDestinations = legalMoves.map { m -> m.to }
                    )
                }
                return
            }

            // Attempt to move from -> square
            val matchingMoves = chessBoard.generateLegalMoves(state.currentTurn).filter {
                it.from == from && it.to == square
            }

            if (matchingMoves.isNotEmpty()) {
                // Check if move requires pawn promotion
                val promotionMove = matchingMoves.firstOrNull { it.promotion != null }
                if (promotionMove != null) {
                    // Open promotion dialog
                    _uiState.update {
                        it.copy(
                            pendingPromotionMove = Move(from, square),
                            showPromotionDialog = true
                        )
                    }
                    return
                }

                // Regular move execution
                executeMove(matchingMoves.first())
                return
            }

            // Invalid destination: deselect
            _uiState.update { it.copy(selectedSquare = null, legalDestinations = emptyList()) }
        } else {
            // Case 2: No square selected yet
            if (clickedPiece != null && clickedPiece.color == state.currentTurn) {
                val legalMoves = chessBoard.generateLegalMoves(state.currentTurn).filter { it.from == square }
                _uiState.update {
                    it.copy(
                        selectedSquare = square,
                        legalDestinations = legalMoves.map { m -> m.to }
                    )
                }
            }
        }
    }

    fun onPromotionPieceChosen(promotedType: PieceType) {
        val pending = _uiState.value.pendingPromotionMove ?: return
        val legalMoves = chessBoard.generateLegalMoves(_uiState.value.currentTurn)
        val fullMove = legalMoves.find {
            it.from == pending.from && it.to == pending.to && it.promotion == promotedType
        } ?: Move(pending.from, pending.to, promotion = promotedType)

        _uiState.update { it.copy(showPromotionDialog = false, pendingPromotionMove = null) }
        executeMove(fullMove)
    }

    private fun executeMove(move: Move) {
        val isCapture = move.capturedPiece != null || chessBoard.getPiece(move.to) != null || move.isEnPassant
        val success = chessBoard.makeMove(move)
        if (!success) return

        // Audio & Haptic feedback
        if (chessBoard.isKingInCheck(chessBoard.turn)) {
            soundManager.playCheck()
        } else if (isCapture) {
            soundManager.playCapture()
        } else {
            soundManager.playMove()
        }

        // In Puzzle mode, validate solution
        if (_uiState.value.gameMode == GameMode.PUZZLES) {
            handlePuzzleMove(move)
            updateBoardState(lastMove = move)
            return
        }

        updateBoardState(lastMove = move)

        // Check if game is over
        val state = chessBoard.getGameState()
        if (state.isOver) {
            timerJob?.cancel()
            if (state == GameState.CHECKMATE) {
                val winner = chessBoard.turn.opposite()
                if (_uiState.value.gameMode == GameMode.VS_AI) {
                    if (winner == _uiState.value.playerColor) soundManager.playVictory() else soundManager.playDefeat()
                } else {
                    soundManager.playVictory()
                }
            }
            _uiState.update { it.copy(gameState = state, showGameOverDialog = true) }
            return
        }

        // Trigger AI move if in VS_AI mode
        if (_uiState.value.gameMode == GameMode.VS_AI && chessBoard.turn != _uiState.value.playerColor) {
            triggerAiMove()
        }
    }

    private fun triggerAiMove() {
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _uiState.update { it.copy(isAiThinking = true) }

            // Simulated think delay between 400ms and 900ms for realistic game feel
            val thinkDelay = (450..850).random().toLong()
            delay(thinkDelay)

            val bestMove = withContext(Dispatchers.Default) {
                ChessAI.getBestMove(chessBoard, _uiState.value.aiDifficulty)
            }

            _uiState.update { it.copy(isAiThinking = false) }

            if (bestMove != null && isActive) {
                executeMove(bestMove)
            }
        }
    }

    private fun updateBoardState(lastMove: Move? = chessBoard.completedMoves.lastOrNull()) {
        val captured = chessBoard.getCapturedPieces()
        val whiteValue = captured.first.sumOf { it.type.value }
        val blackValue = captured.second.sumOf { it.type.value }
        val diff = whiteValue - blackValue

        val inCheckSq = if (chessBoard.isKingInCheck(chessBoard.turn)) {
            chessBoard.findKingSquare(chessBoard.turn)
        } else null

        val eval = ChessAI.evaluateBoardInCentipawns(chessBoard)

        _uiState.update { current ->
            current.copy(
                boardGrid = chessBoard.copyBoard(),
                currentTurn = chessBoard.turn,
                selectedSquare = null,
                legalDestinations = emptyList(),
                lastMove = lastMove,
                inCheckSquare = inCheckSq,
                gameState = chessBoard.getGameState(),
                evaluationCentipawns = eval,
                hintMove = null,
                hintExplanation = null,
                capturedByWhite = captured.first,
                capturedByBlack = captured.second,
                whiteAdvantage = if (diff > 0) diff else 0,
                blackAdvantage = if (diff < 0) -diff else 0,
                moveHistory = chessBoard.completedMoves.toList()
            )
        }
    }

    fun undoMove() {
        if (_uiState.value.gameState.isOver || _uiState.value.isAiThinking) return
        if (chessBoard.completedMoves.isEmpty()) return

        // In VS_AI mode, undo both AI move and user move so it returns to user's turn
        if (_uiState.value.gameMode == GameMode.VS_AI) {
            if (chessBoard.turn != _uiState.value.playerColor) {
                // If AI hasn't moved yet, undo 1 move
                chessBoard.undoMove()
            } else {
                // Undo both AI and Player move
                chessBoard.undoMove()
                chessBoard.undoMove()
            }
        } else {
            chessBoard.undoMove()
        }

        soundManager.playMove()
        updateBoardState()
    }

    fun requestHint() {
        if (_uiState.value.gameState.isOver || _uiState.value.isAiThinking) return
        val hint = ChessAI.getHint(chessBoard) ?: return

        _uiState.update {
            it.copy(
                hintMove = hint.first,
                hintExplanation = hint.second
            )
        }
    }

    fun flipBoard() {
        _uiState.update { it.copy(isBoardFlipped = !it.isBoardFlipped) }
    }

    fun setBoardTheme(theme: BoardTheme) {
        _uiState.update { it.copy(boardTheme = theme) }
    }

    fun toggleCoordinates() {
        _uiState.update { it.copy(showCoordinates = !it.showCoordinates) }
    }

    fun toggleSound() {
        val newState = !_uiState.value.soundEnabled
        soundManager.isSoundEnabled = newState
        _uiState.update { it.copy(soundEnabled = newState) }
    }

    fun toggleHaptics() {
        val newState = !_uiState.value.hapticsEnabled
        soundManager.isHapticsEnabled = newState
        _uiState.update { it.copy(hapticsEnabled = newState) }
    }

    fun setAiDifficulty(difficulty: AIDifficulty) {
        _uiState.update { it.copy(aiDifficulty = difficulty) }
    }

    fun dismissGameOverDialog() {
        _uiState.update { it.copy(showGameOverDialog = false) }
    }

    // Timer management
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(100)
                if (_uiState.value.gameState.isOver) break

                val turn = chessBoard.turn
                if (turn == PieceColor.WHITE) {
                    val remaining = _uiState.value.whiteTimeRemainingMs - 100
                    if (remaining <= 0) {
                        _uiState.update {
                            it.copy(
                                whiteTimeRemainingMs = 0,
                                gameState = GameState.TIMEOUT_BLACK_WINS,
                                showGameOverDialog = true
                            )
                        }
                        soundManager.playDefeat()
                        break
                    } else {
                        _uiState.update { it.copy(whiteTimeRemainingMs = remaining) }
                    }
                } else {
                    val remaining = _uiState.value.blackTimeRemainingMs - 100
                    if (remaining <= 0) {
                        _uiState.update {
                            it.copy(
                                blackTimeRemainingMs = 0,
                                gameState = GameState.TIMEOUT_WHITE_WINS,
                                showGameOverDialog = true
                            )
                        }
                        soundManager.playVictory()
                        break
                    } else {
                        _uiState.update { it.copy(blackTimeRemainingMs = remaining) }
                    }
                }
            }
        }
    }

    // Puzzles logic
    fun loadPuzzle(puzzle: ChessPuzzle) {
        timerJob?.cancel()
        aiJob?.cancel()

        chessBoard.loadFen(puzzle.fen)

        _uiState.update {
            it.copy(
                boardGrid = chessBoard.copyBoard(),
                currentTurn = chessBoard.turn,
                selectedSquare = null,
                legalDestinations = emptyList(),
                lastMove = null,
                inCheckSquare = null,
                gameState = GameState.ONGOING,
                gameMode = GameMode.PUZZLES,
                currentPuzzle = puzzle,
                puzzleStatus = PuzzleStatus.IN_PROGRESS,
                puzzleFeedback = puzzle.description,
                isBoardFlipped = puzzle.playerColor == PieceColor.BLACK,
                isAiThinking = false,
                hintMove = null,
                hintExplanation = null,
                moveHistory = emptyList()
            )
        }

        updateBoardState()
    }

    private fun handlePuzzleMove(move: Move) {
        val puzzle = _uiState.value.currentPuzzle ?: return
        val playedUci = move.toUci()
        val expectedUci = puzzle.solutionUci.firstOrNull()

        if (playedUci == expectedUci) {
            // Puzzle solved!
            soundManager.playVictory()
            _uiState.update {
                it.copy(
                    puzzleStatus = PuzzleStatus.SOLVED,
                    puzzleFeedback = "Correct! " + puzzle.explanation,
                    solvedPuzzleIds = it.solvedPuzzleIds + puzzle.id
                )
            }
        } else {
            // Wrong move
            soundManager.playDefeat()
            _uiState.update {
                it.copy(
                    puzzleStatus = PuzzleStatus.FAILED,
                    puzzleFeedback = "That move is not optimal. Try again!"
                )
            }
        }
    }

    fun retryCurrentPuzzle() {
        val puzzle = _uiState.value.currentPuzzle ?: return
        loadPuzzle(puzzle)
    }

    fun nextPuzzle() {
        val puzzles = PuzzleRepository.puzzles
        val currentIndex = puzzles.indexOfFirst { it.id == _uiState.value.currentPuzzle?.id }
        val nextIndex = (currentIndex + 1) % puzzles.size
        loadPuzzle(puzzles[nextIndex])
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        aiJob?.cancel()
        soundManager.release()
    }
}
