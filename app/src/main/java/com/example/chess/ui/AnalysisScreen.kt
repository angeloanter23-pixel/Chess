package com.example.chess.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.GameMode
import com.example.chess.model.PieceColor
import com.example.chess.model.TimeControl
import com.example.chess.viewmodel.ChessViewModel
import java.util.Locale

@Composable
fun AnalysisScreen(
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.gameMode != GameMode.ANALYSIS) {
            viewModel.startNewGame(mode = GameMode.ANALYSIS, timeControl = TimeControl.UNLIMITED)
        }
    }

    val evalScore = uiState.evaluationCentipawns / 100.0
    val evalFormatted = if (evalScore >= 0) String.format(Locale.US, "+%.2f", evalScore) else String.format(Locale.US, "%.2f", evalScore)
    val evalVerdict = when {
        evalScore > 3.0 -> "White has a decisive advantage"
        evalScore > 1.0 -> "White is noticeably better"
        evalScore > 0.3 -> "White has a slight edge"
        evalScore < -3.0 -> "Black has a decisive advantage"
        evalScore < -1.0 -> "Black is noticeably better"
        evalScore < -0.3 -> "Black has a slight edge"
        else -> "Even position"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F141C))
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .widthIn(max = 520.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Evaluation Summary Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181F2A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Engine Analysis",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = evalVerdict,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Surface(
                    color = if (evalScore >= 0) Color(0xFF0F3E2E) else Color(0xFF3B1D28),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (evalScore >= 0) Color(0xFF10B981) else Color(0xFFF43F5E)
                    )
                ) {
                    Text(
                        text = evalFormatted,
                        color = if (evalScore >= 0) Color(0xFF34D399) else Color(0xFFFB7185),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Main Board with Evaluation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EvaluationBar(
                centipawns = uiState.evaluationCentipawns,
                isCheckmate = false,
                winningColor = null,
                modifier = Modifier.height(340.dp)
            )

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

        // Active Turn Display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Turn: ${if (uiState.currentTurn == PieceColor.WHITE) "White to move ♔" else "Black to move ♚"}",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            Text(
                text = "Total Moves: ${uiState.moveHistory.size}",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }

        // Hint / Best Move Explanation Card
        AnimatedVisibility(
            visible = uiState.hintExplanation != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Engine suggestion",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = " ${uiState.hintExplanation}",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }

        // Move History Notation
        if (uiState.moveHistory.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                uiState.moveHistory.chunked(2).forEachIndexed { index, pair ->
                    Surface(
                        color = Color(0xFF1E232A),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${index + 1}. ${pair[0].san}${if (pair.size > 1) " " + pair[1].san else ""}",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // Action Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.undoMove() },
                enabled = uiState.moveHistory.isNotEmpty(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Undo, contentDescription = "Undo", modifier = Modifier.size(16.dp))
                Text(text = " Undo", fontSize = 12.sp)
            }

            Button(
                onClick = { viewModel.requestHint() },
                modifier = Modifier.weight(1.2f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = "Best Move", tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                Text(text = " Best Move", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { viewModel.flipBoard() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = "Flip", modifier = Modifier.size(16.dp))
                Text(text = " Flip", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    viewModel.startNewGame(mode = GameMode.ANALYSIS, timeControl = TimeControl.UNLIMITED)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                Text(text = " Reset", color = Color(0xFFEF4444), fontSize = 12.sp)
            }
        }
    }

    if (uiState.showPromotionDialog) {
        PawnPromotionDialog(
            color = uiState.currentTurn,
            onPieceSelected = { chosen -> viewModel.onPromotionPieceChosen(chosen) }
        )
    }
}
