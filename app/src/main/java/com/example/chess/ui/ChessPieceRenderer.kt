package com.example.chess.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType

@Composable
fun ChessPiece(
    piece: Piece,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp)
        ) {
            val isWhite = piece.color == PieceColor.WHITE

            val fillBrush = if (isWhite) {
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFFE2E8F0))
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A))
                )
            }

            val strokeColor = if (isWhite) Color(0xFF1E293B) else Color(0xFFE2E8F0)
            val detailColor = if (isWhite) Color(0xFF64748B) else Color(0xFF94A3B8)
            val strokeWidth = (size.width * 0.038f).coerceIn(1.5f, 4.5f)

            when (piece.type) {
                PieceType.PAWN -> drawPawn(fillBrush, strokeColor, detailColor, strokeWidth, isWhite)
                PieceType.KNIGHT -> drawKnight(fillBrush, strokeColor, detailColor, strokeWidth, isWhite)
                PieceType.BISHOP -> drawBishop(fillBrush, strokeColor, detailColor, strokeWidth, isWhite)
                PieceType.ROOK -> drawRook(fillBrush, strokeColor, detailColor, strokeWidth, isWhite)
                PieceType.QUEEN -> drawQueen(fillBrush, strokeColor, detailColor, strokeWidth, isWhite)
                PieceType.KING -> drawKing(fillBrush, strokeColor, detailColor, strokeWidth, isWhite)
            }
        }
    }
}

private fun DrawScope.drawPawn(
    fill: Brush,
    stroke: Color,
    detail: Color,
    strokeWidth: Float,
    isWhite: Boolean
) {
    val w = size.width
    val h = size.height

    // Base pedestal
    val basePath = Path().apply {
        moveTo(w * 0.20f, h * 0.88f)
        lineTo(w * 0.80f, h * 0.88f)
        lineTo(w * 0.75f, h * 0.78f)
        lineTo(w * 0.25f, h * 0.78f)
        close()
    }
    drawPath(basePath, fill)
    drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Body
    val bodyPath = Path().apply {
        moveTo(w * 0.32f, h * 0.78f)
        cubicTo(w * 0.36f, h * 0.60f, w * 0.38f, h * 0.50f, w * 0.40f, h * 0.45f)
        lineTo(w * 0.60f, h * 0.45f)
        cubicTo(w * 0.62f, h * 0.50f, w * 0.64f, h * 0.60f, w * 0.68f, h * 0.78f)
        close()
    }
    drawPath(bodyPath, fill)
    drawPath(bodyPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Collar ring
    drawRoundRect(
        brush = fill,
        topLeft = Offset(w * 0.34f, h * 0.43f),
        size = Size(w * 0.32f, h * 0.07f),
        cornerRadius = CornerRadius(h * 0.035f)
    )
    drawRoundRect(
        color = stroke,
        topLeft = Offset(w * 0.34f, h * 0.43f),
        size = Size(w * 0.32f, h * 0.07f),
        cornerRadius = CornerRadius(h * 0.035f),
        style = Stroke(strokeWidth)
    )

    // Pawn head circle
    val headRadius = w * 0.16f
    val headCenter = Offset(w * 0.50f, h * 0.28f)
    drawCircle(fill, radius = headRadius, center = headCenter)
    drawCircle(stroke, radius = headRadius, center = headCenter, style = Stroke(strokeWidth))

    // Specular highlight
    if (isWhite) {
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = headRadius * 0.35f,
            center = Offset(w * 0.45f, h * 0.23f)
        )
    }
}

private fun DrawScope.drawKnight(
    fill: Brush,
    stroke: Color,
    detail: Color,
    strokeWidth: Float,
    isWhite: Boolean
) {
    val w = size.width
    val h = size.height

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.18f, h * 0.88f)
        lineTo(w * 0.82f, h * 0.88f)
        lineTo(w * 0.76f, h * 0.80f)
        lineTo(w * 0.24f, h * 0.80f)
        close()
    }
    drawPath(basePath, fill)
    drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Horse head profile
    val knightPath = Path().apply {
        moveTo(w * 0.24f, h * 0.80f)
        // Chest curve forward
        cubicTo(w * 0.16f, h * 0.65f, w * 0.18f, h * 0.48f, w * 0.25f, h * 0.42f)
        // Muzzle and jaw
        lineTo(w * 0.18f, h * 0.36f)
        lineTo(w * 0.24f, h * 0.26f)
        lineTo(w * 0.38f, h * 0.25f)
        // Ear
        lineTo(w * 0.44f, h * 0.12f)
        lineTo(w * 0.54f, h * 0.22f)
        // Mane curves down back
        cubicTo(w * 0.64f, h * 0.25f, w * 0.76f, h * 0.34f, w * 0.78f, h * 0.50f)
        cubicTo(w * 0.80f, h * 0.65f, w * 0.75f, h * 0.74f, w * 0.76f, h * 0.80f)
        close()
    }
    drawPath(knightPath, fill)
    drawPath(knightPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Eye
    drawCircle(
        color = detail,
        radius = w * 0.035f,
        center = Offset(w * 0.36f, h * 0.32f)
    )

    // Mane detailing notches
    val manePath = Path().apply {
        moveTo(w * 0.56f, h * 0.32f)
        lineTo(w * 0.66f, h * 0.36f)
        moveTo(w * 0.58f, h * 0.44f)
        lineTo(w * 0.70f, h * 0.48f)
        moveTo(w * 0.58f, h * 0.56f)
        lineTo(w * 0.71f, h * 0.60f)
    }
    drawPath(manePath, detail, style = Stroke(strokeWidth * 0.8f, cap = StrokeCap.Round))
}

private fun DrawScope.drawBishop(
    fill: Brush,
    stroke: Color,
    detail: Color,
    strokeWidth: Float,
    isWhite: Boolean
) {
    val w = size.width
    val h = size.height

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.20f, h * 0.88f)
        lineTo(w * 0.80f, h * 0.88f)
        lineTo(w * 0.74f, h * 0.80f)
        lineTo(w * 0.26f, h * 0.80f)
        close()
    }
    drawPath(basePath, fill)
    drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Mitre body
    val bodyPath = Path().apply {
        moveTo(w * 0.32f, h * 0.80f)
        cubicTo(w * 0.30f, h * 0.60f, w * 0.24f, h * 0.45f, w * 0.40f, h * 0.22f)
        lineTo(w * 0.50f, h * 0.16f)
        lineTo(w * 0.60f, h * 0.22f)
        cubicTo(w * 0.76f, h * 0.45f, w * 0.70f, h * 0.60f, w * 0.68f, h * 0.80f)
        close()
    }
    drawPath(bodyPath, fill)
    drawPath(bodyPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Bishop cut / slash
    val slashPath = Path().apply {
        moveTo(w * 0.42f, h * 0.32f)
        lineTo(w * 0.56f, h * 0.44f)
    }
    drawPath(slashPath, stroke, style = Stroke(strokeWidth * 1.1f, cap = StrokeCap.Round))

    // Cross or finial ball on top
    drawCircle(fill, radius = w * 0.055f, center = Offset(w * 0.50f, h * 0.13f))
    drawCircle(stroke, radius = w * 0.055f, center = Offset(w * 0.50f, h * 0.13f), style = Stroke(strokeWidth))
}

private fun DrawScope.drawRook(
    fill: Brush,
    stroke: Color,
    detail: Color,
    strokeWidth: Float,
    isWhite: Boolean
) {
    val w = size.width
    val h = size.height

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.18f, h * 0.88f)
        lineTo(w * 0.82f, h * 0.88f)
        lineTo(w * 0.76f, h * 0.80f)
        lineTo(w * 0.24f, h * 0.80f)
        close()
    }
    drawPath(basePath, fill)
    drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Castle Tower Body
    val towerPath = Path().apply {
        moveTo(w * 0.28f, h * 0.80f)
        lineTo(w * 0.32f, h * 0.42f)
        lineTo(w * 0.24f, h * 0.40f)
        // Battlements (3 crenellations)
        lineTo(w * 0.24f, h * 0.22f)
        lineTo(w * 0.36f, h * 0.22f)
        lineTo(w * 0.36f, h * 0.28f)
        lineTo(w * 0.44f, h * 0.28f)
        lineTo(w * 0.44f, h * 0.22f)
        lineTo(w * 0.56f, h * 0.22f)
        lineTo(w * 0.56f, h * 0.28f)
        lineTo(w * 0.64f, h * 0.28f)
        lineTo(w * 0.64f, h * 0.22f)
        lineTo(w * 0.76f, h * 0.22f)
        lineTo(w * 0.76f, h * 0.40f)
        lineTo(w * 0.68f, h * 0.42f)
        lineTo(w * 0.72f, h * 0.80f)
        close()
    }
    drawPath(towerPath, fill)
    drawPath(towerPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Horizontal ridge line below battlements
    drawLine(
        color = detail,
        start = Offset(w * 0.28f, h * 0.40f),
        end = Offset(w * 0.72f, h * 0.40f),
        strokeWidth = strokeWidth * 0.9f
    )
}

private fun DrawScope.drawQueen(
    fill: Brush,
    stroke: Color,
    detail: Color,
    strokeWidth: Float,
    isWhite: Boolean
) {
    val w = size.width
    val h = size.height

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.16f, h * 0.88f)
        lineTo(w * 0.84f, h * 0.88f)
        lineTo(w * 0.76f, h * 0.80f)
        lineTo(w * 0.24f, h * 0.80f)
        close()
    }
    drawPath(basePath, fill)
    drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Coronet Crown Body (flared with 5 points)
    val queenPath = Path().apply {
        moveTo(w * 0.28f, h * 0.80f)
        lineTo(w * 0.30f, h * 0.58f)
        // Point 1 (leftmost)
        lineTo(w * 0.16f, h * 0.32f)
        // Valley 1
        lineTo(w * 0.30f, h * 0.45f)
        // Point 2
        lineTo(w * 0.34f, h * 0.26f)
        // Valley 2
        lineTo(w * 0.42f, h * 0.42f)
        // Point 3 (center peak)
        lineTo(w * 0.50f, h * 0.20f)
        // Valley 3
        lineTo(w * 0.58f, h * 0.42f)
        // Point 4
        lineTo(w * 0.66f, h * 0.26f)
        // Valley 4
        lineTo(w * 0.70f, h * 0.45f)
        // Point 5 (rightmost)
        lineTo(w * 0.84f, h * 0.32f)
        lineTo(w * 0.70f, h * 0.58f)
        lineTo(w * 0.72f, h * 0.80f)
        close()
    }
    drawPath(queenPath, fill)
    drawPath(queenPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Crown jewels (balls on tips)
    val tipCenters = listOf(
        Offset(w * 0.16f, h * 0.30f),
        Offset(w * 0.34f, h * 0.24f),
        Offset(w * 0.50f, h * 0.18f),
        Offset(w * 0.66f, h * 0.24f),
        Offset(w * 0.84f, h * 0.30f)
    )
    for (center in tipCenters) {
        drawCircle(fill, radius = w * 0.038f, center = center)
        drawCircle(stroke, radius = w * 0.038f, center = center, style = Stroke(strokeWidth * 0.85f))
    }

    // Waist line
    drawLine(
        color = detail,
        start = Offset(w * 0.30f, h * 0.60f),
        end = Offset(w * 0.70f, h * 0.60f),
        strokeWidth = strokeWidth * 0.8f
    )
}

private fun DrawScope.drawKing(
    fill: Brush,
    stroke: Color,
    detail: Color,
    strokeWidth: Float,
    isWhite: Boolean
) {
    val w = size.width
    val h = size.height

    // Base
    val basePath = Path().apply {
        moveTo(w * 0.16f, h * 0.88f)
        lineTo(w * 0.84f, h * 0.88f)
        lineTo(w * 0.76f, h * 0.80f)
        lineTo(w * 0.24f, h * 0.80f)
        close()
    }
    drawPath(basePath, fill)
    drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Imperial robe and body
    val kingBody = Path().apply {
        moveTo(w * 0.28f, h * 0.80f)
        cubicTo(w * 0.22f, h * 0.62f, w * 0.24f, h * 0.44f, w * 0.32f, h * 0.32f)
        lineTo(w * 0.68f, h * 0.32f)
        cubicTo(w * 0.76f, h * 0.44f, w * 0.78f, h * 0.62f, w * 0.72f, h * 0.80f)
        close()
    }
    drawPath(kingBody, fill)
    drawPath(kingBody, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Arched dome crown
    val crownPath = Path().apply {
        moveTo(w * 0.30f, h * 0.32f)
        cubicTo(w * 0.36f, h * 0.22f, w * 0.64f, h * 0.22f, w * 0.70f, h * 0.32f)
        close()
    }
    drawPath(crownPath, fill)
    drawPath(crownPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

    // Imperial Cross on top (Royal Cross Pattée)
    // Vertical beam
    drawLine(
        color = stroke,
        start = Offset(w * 0.50f, h * 0.24f),
        end = Offset(w * 0.50f, h * 0.10f),
        strokeWidth = strokeWidth * 1.5f,
        cap = StrokeCap.Square
    )
    // Horizontal beam
    drawLine(
        color = stroke,
        start = Offset(w * 0.41f, h * 0.16f),
        end = Offset(w * 0.59f, h * 0.16f),
        strokeWidth = strokeWidth * 1.5f,
        cap = StrokeCap.Square
    )

    // Inner highlight for cross on white king
    if (isWhite) {
        drawLine(
            color = Color.White,
            start = Offset(w * 0.50f, h * 0.22f),
            end = Offset(w * 0.50f, h * 0.12f),
            strokeWidth = strokeWidth * 0.7f
        )
        drawLine(
            color = Color.White,
            start = Offset(w * 0.43f, h * 0.16f),
            end = Offset(w * 0.57f, h * 0.16f),
            strokeWidth = strokeWidth * 0.7f
        )
    }
}
