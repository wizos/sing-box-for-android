package io.mo.viaport.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
@Stable
fun toVectorPainter(tint: Color, imageVector: ImageVector): VectorPainter {
    return rememberVectorPainter(
        defaultWidth = imageVector.defaultWidth,
        defaultHeight = imageVector.defaultHeight,
        viewportWidth = imageVector.viewportWidth,
        viewportHeight = imageVector.viewportHeight,
        name = imageVector.name,
        tintColor = tint,
        tintBlendMode = imageVector.tintBlendMode,
        autoMirror = imageVector.autoMirror,
        content = { _, _ -> RenderVectorGroup(group = imageVector.root) }
    )
}
@Composable
fun MiniCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit){
    // Box(modifier = Modifier.then(modifier).padding(5.dp),
    //     contentAlignment = Alignment.Center
    //     ) {
    // }
    // val cardShape = RoundedCornerShape(size = 32.dp)
    Card(
        modifier = Modifier.then(modifier),
        content = content
    )
}
