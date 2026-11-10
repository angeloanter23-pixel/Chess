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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.GameMode
import com.example.chess.model.PieceColor
import com.example.chess.puzzles.PuzzleRepository
import com.example.chess.viewmodel.ChessViewModel
import com.example.chess.viewmodel.PuzzleStatus

@Composable
fun PuzzlesScreen(
    viewModel: ChessViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val puzzles = PuzzleRepository.puzzles
    val currentPuzzle = uiState.currentPuzzle ?: puzzles.first()

    LaunchedEffect(Unit) {
        if (uiState.currentPuzzle == null || uiState.gameMode != GameMode.PUZZLES) {
            viewModel.loadPuzzle(puzzles.first())
        }
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
        // Puzzle Selection Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            puzzles.forEachIndexed { index, puzzle ->
                val isSolved = uiState.solvedPuzzleIds.contains(puzzle.id)
                val isSelected = currentPuzzle.id == puzzle.id

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.loadPuzzle(puzzle) },
                    leadingIcon = {
                        if (isSolved) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Solved",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    label = { Text("#${puzzle.id} ${puzzle.title}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF38BDF8),
                        selectedLabelColor = Color(0xFF0F172A),
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.testTag("puzzle_chip_${puzzle.id}")
                )
            }
        }

        // Current Puzzle Header Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181F2A)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentPuzzle.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentPuzzle.theme} • ${currentPuzzle.difficulty}",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }

                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Text(
                            text = "${currentPuzzle.rating} ELO",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // Status Feedback Banner
        val bannerBg = when (uiState.puzzleStatus) {
            PuzzleStatus.SOLVED -> Color(0xFF064E3B)
            PuzzleStatus.FAILED -> Color(0xFF7F1D1D)
            PuzzleStatus.IN_PROGRESS -> Color(0xFF1E293B)
        }
        val bannerTextColor = when (uiState.puzzleStatus) {
            PuzzleStatus.SOLVED -> Color(0xFF34D399)
            PuzzleStatus.FAILED -> Color(0xFFF87171)
            PuzzleStatus.IN_PROGRESS -> Color(0xFF94A3B8)
        }

        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = bannerBg),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.puzzleFeedback,
                    color = bannerTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                if (uiState.puzzleStatus == PuzzleStatus.SOLVED) {
                    Button(
                        onClick = { viewModel.nextPuzzle() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("next_puzzle_btn")
                    ) {
                        Text(text = "Next", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Interactive Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            ChessBoardView(
                board = uiState.boardGrid,
                selectedSquare = uiState.selectedSquare,
                legalDestinations = uiState.legalDestinations,
                lastMove = uiState.lastMove,
                inCheckSquare = uiState.inCheckSquare,
                hintMove = uiState.hintMove,
                isFlipped = currentPuzzle.playerColor == PieceColor.BLACK,
                theme = uiState.boardTheme,
                showCoordinates = uiState.showCoordinates,
                onSquareClicked = { square -> viewModel.onSquareSelected(square) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Puzzle Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.retryCurrentPuzzle() },
                modifier = Modifier
                    .weight(1f)
                    .testTag("retry_puzzle_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(16.dp)
                )
                Text(text = " Retry", fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = { viewModel.requestHint() },
                modifier = Modifier
                    .weight(1f)
                    .testTag("puzzle_hint_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Hint",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(16.dp)
                )
                Text(text = " Hint", fontSize = 13.sp)
            }

            Button(
                onClick = { viewModel.nextPuzzle() },
                modifier = Modifier
                    .weight(1f)
                    .testTag("skip_next_puzzle_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
            ) {
                Text(text = "Next", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next Puzzle",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
