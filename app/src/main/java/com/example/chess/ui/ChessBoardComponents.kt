package com.example.chess.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.BoardTheme
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import java.util.Locale

@Composable
fun ChessBoardView(
    board: Array<Array<Piece?>>,
    selectedSquare: Square?,
    legalDestinations: List<Square>,
    lastMove: Move?,
    inCheckSquare: Square?,
    hintMove: Move?,
    isFlipped: Boolean,
    theme: BoardTheme,
    showCoordinates: Boolean,
    onSquareClicked: (Square) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E232A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .aspectRatio(1f)
            .shadow(12.dp, RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF333B48), RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .testTag("chess_board")
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val squareWidth = maxWidth / 8
            val squareHeight = maxHeight / 8

            // 8x8 Grid
            Column(modifier = Modifier.fillMaxSize()) {
                val rankRange = if (isFlipped) 0..7 else 7 downTo 0
                val fileRange = if (isFlipped) 7 downTo 0 else 0..7

                for (r in rankRange) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        for (f in fileRange) {
                            val square = Square(f, r)
                            val isLightSquare = (f + r) % 2 != 0
                            val piece = board[f][r]

                            val isSelected = square == selectedSquare
                            val isLastMove = lastMove != null && (square == lastMove.from || square == lastMove.to)
                            val isInCheck = square == inCheckSquare
                            val isLegalDest = legalDestinations.contains(square)
                            val isHint = hintMove != null && (square == hintMove.from || square == hintMove.to)

                            val baseColor = if (isLightSquare) {
                                Color(theme.lightSquareHex)
                            } else {
                                Color(theme.darkSquareHex)
                            }

                            // Layered highlight colors
                            val squareColor = when {
                                isInCheck -> Color(0xCCEF4444) // Radiant check red
                                isSelected -> Color(theme.selectedSquareHex)
                                isHint -> Color(theme.hintSquareHex)
                                isLastMove -> Color(theme.highlightLastHex)
                                else -> baseColor
                            }

                            val coordColor = if (isLightSquare) {
                                Color(theme.darkSquareHex).copy(alpha = 0.85f)
                            } else {
                                Color(theme.lightSquareHex).copy(alpha = 0.85f)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(squareColor)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onSquareClicked(square) }
                                    .testTag("square_${square.toAlgebraic()}")
                            ) {
                                // Board Edge Coordinates
                                if (showCoordinates) {
                                    // Rank coordinate on left edge (file index 0 if not flipped, or 7 if flipped)
                                    val isLeftEdge = if (isFlipped) f == 7 else f == 0
                                    if (isLeftEdge) {
                                        Text(
                                            text = square.rankChar.toString(),
                                            color = coordColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(start = 2.dp, top = 1.dp)
                                        )
                                    }

                                    // File coordinate on bottom edge (rank index 0 if not flipped, or 7 if flipped)
                                    val isBottomEdge = if (isFlipped) r == 7 else r == 0
                                    if (isBottomEdge) {
                                        Text(
                                            text = square.fileChar.toString(),
                                            color = coordColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(end = 2.dp, bottom = 1.dp)
                                        )
                                    }
                                }

                                // Chess Piece
                                if (piece != null) {
                                    ChessPiece(
                                        piece = piece,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                // Move indicators
                                if (isLegalDest) {
                                    if (piece != null || (lastMove?.isEnPassant == true)) {
                                        // Capture target ring
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(3.dp)
                                                .border(
                                                    width = 3.dp,
                                                    color = Color(0x9922C55E),
                                                    shape = CircleShape
                                                )
                                        )
                                    } else {
                                        // Destination dot
                                        Box(
                                            modifier = Modifier
                                                .size(squareWidth * 0.32f)
                                                .align(Alignment.Center)
                                                .background(Color(0x7710B981), shape = CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EvaluationBar(
    centipawns: Int,
    isCheckmate: Boolean,
    winningColor: PieceColor?,
    modifier: Modifier = Modifier
) {
    // Normalizes -1000..+1000 centipawns to 0.05..0.95 white proportion
    val targetFraction = when {
        isCheckmate -> if (winningColor == PieceColor.WHITE) 0.98f else 0.02f
        else -> {
            val clamped = centipawns.coerceIn(-1000, 1000)
            0.5f + (clamped / 2000f) * 0.9f
        }
    }

    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction.coerceIn(0.04f, 0.96f),
        animationSpec = tween(durationMillis = 350),
        label = "eval_anim"
    )

    val scoreText = when {
        isCheckmate -> if (winningColor == PieceColor.WHITE) "+M" else "-M"
        else -> {
            val pawns = centipawns / 100.0
            if (pawns >= 0) String.format(Locale.US, "+%.1f", pawns) else String.format(Locale.US, "%.1f", pawns)
        }
    }

    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E232A)),
        modifier = modifier
            .width(22.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFF333B48), RoundedCornerShape(6.dp))
            .testTag("evaluation_bar")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Black portion (top)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(1f - animatedFraction)
                    .align(Alignment.TopCenter)
                    .background(Color(0xFF23272F))
            )

            // White portion (bottom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedFraction)
                    .align(Alignment.BottomCenter)
                    .background(Color(0xFFF8FAFC))
            )

            // Compact score badge in center
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(3.dp),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = scoreText,
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun CapturedPiecesRow(
    playerColor: PieceColor,
    playerName: String,
    capturedPieces: List<Piece>,
    materialAdvantage: Int,
    isCurrentTurn: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Active turn indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (isCurrentTurn) Color(0xFF10B981) else Color.Transparent,
                        shape = CircleShape
                    )
            )

            Text(
                text = " $playerName",
                color = if (isCurrentTurn) Color.White else Color(0xFF94A3B8),
                fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )

            if (materialAdvantage > 0) {
                Text(
                    text = " +$materialAdvantage",
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // Captured pieces row
        Row(
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val grouped = capturedPieces.groupBy { it.type }
            val displayOrder = listOf(
                PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP,
                PieceType.KNIGHT, PieceType.PAWN
            )

            for (type in displayOrder) {
                val list = grouped[type] ?: continue
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = type.getUnicode(if (playerColor == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE),
                        fontSize = 14.sp,
                        color = if (playerColor == PieceColor.WHITE) Color(0xFF94A3B8) else Color(0xFFE2E8F0)
                    )
                    if (list.size > 1) {
                        Text(
                            text = "${list.size}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChessClock(
    timeMillis: Long,
    isActive: Boolean,
    color: PieceColor,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (timeMillis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    val isLowTime = timeMillis in 1..29999
    val bgColor = when {
        isActive && isLowTime -> Color(0xFF7F1D1D)
        isActive -> Color(0xFF1E293B)
        else -> Color(0xFF0F172A)
    }

    val textColor = when {
        isLowTime -> Color(0xFFF87171)
        isActive -> Color.White
        else -> Color(0xFF94A3B8)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) Color(0xFF38BDF8) else Color(0xFF334155)
        ),
        modifier = modifier
    ) {
        Text(
            text = timeFormatted,
            color = textColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PawnPromotionDialog(
    color: PieceColor,
    onPieceSelected: (PieceType) -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Force user to choose promotion piece */ },
        title = {
            Text(
                text = "Promote Pawn",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val promotionChoices = listOf(
                    PieceType.QUEEN,
                    PieceType.ROOK,
                    PieceType.BISHOP,
                    PieceType.KNIGHT
                )

                for (type in promotionChoices) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .size(62.dp)
                            .clickable { onPieceSelected(type) }
                            .testTag("promote_${type.name.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ChessPiece(piece = Piece(type, color))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun GameOverDialog(
    title: String,
    subtitle: String,
    onRematch: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = onRematch,
                modifier = Modifier.testTag("rematch_button")
            ) {
                Text(
                    text = "New Game",
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Review Board", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF1E232A),
        shape = RoundedCornerShape(16.dp)
    )
}
