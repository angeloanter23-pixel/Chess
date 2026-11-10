package com.example.chess.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.AIDifficulty
import com.example.chess.model.GameMode
import com.example.chess.model.GameState
import com.example.chess.model.PieceColor
import com.example.chess.model.TimeControl
import com.example.chess.viewmodel.ChessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessGameScreen(
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNewGameSheet by remember { mutableStateOf(false) }

    // Top player info
    val topPlayerIsWhite = if (uiState.isBoardFlipped) true else false
    val topPlayerColor = if (topPlayerIsWhite) PieceColor.WHITE else PieceColor.BLACK
    val bottomPlayerColor = topPlayerColor.opposite()

    val topPlayerName = when (uiState.gameMode) {
        GameMode.VS_AI -> if (uiState.playerColor == PieceColor.WHITE) "Chess Bot (${uiState.aiDifficulty.displayName})" else "You"
        GameMode.PASS_AND_PLAY -> if (topPlayerColor == PieceColor.WHITE) "White (Player 1)" else "Black (Player 2)"
        else -> "Opponent"
    }

    val bottomPlayerName = when (uiState.gameMode) {
        GameMode.VS_AI -> if (uiState.playerColor == PieceColor.WHITE) "You (White)" else "Chess Bot (${uiState.aiDifficulty.displayName})"
        GameMode.PASS_AND_PLAY -> if (bottomPlayerColor == PieceColor.WHITE) "White (Player 1)" else "Black (Player 2)"
        else -> "You"
    }

    val topCaptured = if (topPlayerColor == PieceColor.WHITE) uiState.capturedByWhite else uiState.capturedByBlack
    val bottomCaptured = if (bottomPlayerColor == PieceColor.WHITE) uiState.capturedByWhite else uiState.capturedByBlack
    val topAdvantage = if (topPlayerColor == PieceColor.WHITE) uiState.whiteAdvantage else uiState.blackAdvantage
    val bottomAdvantage = if (bottomPlayerColor == PieceColor.WHITE) uiState.whiteAdvantage else uiState.blackAdvantage

    val topTime = if (topPlayerColor == PieceColor.WHITE) uiState.whiteTimeRemainingMs else uiState.blackTimeRemainingMs
    val bottomTime = if (bottomPlayerColor == PieceColor.WHITE) uiState.whiteTimeRemainingMs else uiState.blackTimeRemainingMs

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F141C))
            .padding(horizontal = 12.dp)
            .widthIn(max = 520.dp)
    ) {
        // Top Player Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 2.dp)
        ) {
            CapturedPiecesRow(
                playerColor = topPlayerColor,
                playerName = topPlayerName,
                capturedPieces = topCaptured,
                materialAdvantage = topAdvantage,
                isCurrentTurn = uiState.currentTurn == topPlayerColor,
                modifier = Modifier.weight(1f)
            )

            if (uiState.timeControl != TimeControl.UNLIMITED) {
                ChessClock(
                    timeMillis = topTime,
                    isActive = uiState.currentTurn == topPlayerColor && !uiState.gameState.isOver,
                    color = topPlayerColor
                )
            }
        }

        // Chess Board with Evaluation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Evaluation bar
            EvaluationBar(
                centipawns = uiState.evaluationCentipawns,
                isCheckmate = uiState.gameState == GameState.CHECKMATE,
                winningColor = if (uiState.gameState == GameState.CHECKMATE) uiState.currentTurn.opposite() else null,
                modifier = Modifier.height(340.dp)
            )

            // Main Chess Board
            ChessBoardView(
                board = uiState.boardGrid,
                selectedSquare = uiState.selectedSquare,
                legalDestinations = uiState.legalDestinations,
                lastMove = uiState.lastMove,
                inCheckSquare = uiState.inCheckSquare,
                hintMove = uiState.hintMove,
                isFlipped = uiState.isBoardFlipped,
                theme = uiState.boardTheme,
                showCoordinates = uiState.showCoordinates,
                onSquareClicked = { square -> viewModel.onSquareSelected(square) },
                modifier = Modifier.weight(1f)
            )
        }

        // Bottom Player Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 4.dp)
        ) {
            CapturedPiecesRow(
                playerColor = bottomPlayerColor,
                playerName = bottomPlayerName,
                capturedPieces = bottomCaptured,
                materialAdvantage = bottomAdvantage,
                isCurrentTurn = uiState.currentTurn == bottomPlayerColor,
                modifier = Modifier.weight(1f)
            )

            if (uiState.timeControl != TimeControl.UNLIMITED) {
                ChessClock(
                    timeMillis = bottomTime,
                    isActive = uiState.currentTurn == bottomPlayerColor && !uiState.gameState.isOver,
                    color = bottomPlayerColor
                )
            }
        }

        // Status or Hint Banner
        AnimatedVisibility(
            visible = uiState.hintExplanation != null || uiState.isAiThinking || uiState.inCheckSquare != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    when {
                        uiState.hintExplanation != null -> {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Hint",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = " Hint: ${uiState.hintExplanation}",
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                        uiState.isAiThinking -> {
                            Text(
                                text = "⏳ Computer is thinking...",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        uiState.inCheckSquare != null -> {
                            Text(
                                text = "⚠️ Check! ${uiState.currentTurn.name.lowercase().replaceFirstChar { it.uppercase() }} King is under attack!",
                                color = Color(0xFFF87171),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Move History Notation Ribbon
        if (uiState.moveHistory.isNotEmpty()) {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Group moves into pairs (White, Black)
                val pairs = uiState.moveHistory.chunked(2)
                pairs.forEachIndexed { index, pair ->
                    val moveNum = index + 1
                    Surface(
                        color = Color(0xFF1E232A),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$moveNum.",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = " ${pair[0].san}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            if (pair.size > 1) {
                                Text(
                                    text = " ${pair[1].san}",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Game Control Buttons
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181F2A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // New Game
                IconButton(
                    onClick = { showNewGameSheet = true },
                    modifier = Modifier.testTag("new_game_btn")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Game",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(text = "New", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }

                // Undo
                IconButton(
                    onClick = { viewModel.undoMove() },
                    enabled = uiState.moveHistory.isNotEmpty() && !uiState.gameState.isOver,
                    modifier = Modifier.testTag("undo_btn")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo",
                            tint = if (uiState.moveHistory.isNotEmpty()) Color(0xFFF1F5F9) else Color(0xFF475569),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(text = "Undo", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }

                // Hint
                IconButton(
                    onClick = { viewModel.requestHint() },
                    enabled = !uiState.gameState.isOver,
                    modifier = Modifier.testTag("hint_btn")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Hint",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(text = "Hint", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }

                // Flip Board
                IconButton(
                    onClick = { viewModel.flipBoard() },
                    modifier = Modifier.testTag("flip_board_btn")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Flip Board",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(text = "Flip", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }

                // Rematch / Reset
                IconButton(
                    onClick = {
                        viewModel.startNewGame()
                    },
                    modifier = Modifier.testTag("reset_game_btn")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(text = "Restart", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }
            }
        }
    }

    // Pawn Promotion Dialog
    if (uiState.showPromotionDialog) {
        PawnPromotionDialog(
            color = uiState.currentTurn,
            onPieceSelected = { chosen -> viewModel.onPromotionPieceChosen(chosen) }
        )
    }

    // Game Over Dialog
    if (uiState.showGameOverDialog) {
        val title = when (uiState.gameState) {
            GameState.CHECKMATE -> "Checkmate! ${uiState.currentTurn.opposite().name.lowercase().replaceFirstChar { it.uppercase() }} Wins!"
            GameState.STALEMATE -> "Draw by Stalemate"
            GameState.DRAW_INSUFFICIENT_MATERIAL -> "Draw (Insufficient Material)"
            GameState.DRAW_50_MOVES -> "Draw (50-Move Rule)"
            GameState.DRAW_REPETITION -> "Draw (3-Fold Repetition)"
            GameState.TIMEOUT_WHITE_WINS -> "White Wins on Time!"
            GameState.TIMEOUT_BLACK_WINS -> "Black Wins on Time!"
            else -> "Game Finished"
        }

        GameOverDialog(
            title = title,
            subtitle = "Moves played: ${uiState.moveHistory.size}. Start a rematch or review the final position.",
            onRematch = {
                viewModel.dismissGameOverDialog()
                viewModel.startNewGame()
            },
            onDismiss = { viewModel.dismissGameOverDialog() }
        )
    }

    // New Game Configuration Bottom Sheet
    if (showNewGameSheet) {
        NewGameBottomSheet(
            currentMode = uiState.gameMode,
            currentDifficulty = uiState.aiDifficulty,
            currentTimeControl = uiState.timeControl,
            currentColor = uiState.playerColor,
            onStartGame = { mode, diff, tc, col ->
                viewModel.startNewGame(mode, tc, diff, col)
                showNewGameSheet = false
            },
            onDismiss = { showNewGameSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewGameBottomSheet(
    currentMode: GameMode,
    currentDifficulty: AIDifficulty,
    currentTimeControl: TimeControl,
    currentColor: PieceColor,
    onStartGame: (GameMode, AIDifficulty, TimeControl, PieceColor) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(currentMode) }
    var selectedDifficulty by remember { mutableStateOf(currentDifficulty) }
    var selectedTimeControl by remember { mutableStateOf(currentTimeControl) }
    var selectedColor by remember { mutableStateOf(currentColor) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF181F2A),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF475569)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Start New Game",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Selector
            Text(text = "Game Mode", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(GameMode.VS_AI, GameMode.PASS_AND_PLAY).forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { selectedMode = mode },
                        label = { Text(mode.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8),
                            selectedLabelColor = Color(0xFF0F172A),
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }

            // AI Difficulty (only if VS_AI)
            if (selectedMode == GameMode.VS_AI) {
                Text(text = "Bot Strength", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AIDifficulty.values().forEach { diff ->
                        FilterChip(
                            selected = selectedDifficulty == diff,
                            onClick = { selectedDifficulty = diff },
                            label = { Text("${diff.displayName} (${diff.elo})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color(0xFFCBD5E1)
                            )
                        )
                    }
                }

                // Choose Color
                Text(text = "Play As", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedColor == PieceColor.WHITE,
                        onClick = { selectedColor = PieceColor.WHITE },
                        label = { Text("White ♔") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFFCBD5E1)
                        )
                    )
                    FilterChip(
                        selected = selectedColor == PieceColor.BLACK,
                        onClick = { selectedColor = PieceColor.BLACK },
                        label = { Text("Black ♚") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF334155),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }

            // Time Controls
            Text(text = "Time Limit", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TimeControl.values().forEach { tc ->
                    FilterChip(
                        selected = selectedTimeControl == tc,
                        onClick = { selectedTimeControl = tc },
                        label = { Text(tc.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFF59E0B),
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }

            // Start Button
            Surface(
                color = Color(0xFF38BDF8),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onStartGame(selectedMode, selectedDifficulty, selectedTimeControl, selectedColor)
                    }
                    .testTag("start_game_confirm_btn")
            ) {
                Text(
                    text = "Play Game",
                    color = Color(0xFF0F172A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
