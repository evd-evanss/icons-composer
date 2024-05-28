package com.cora.icons.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import com.cora.icons.model.IconModel
import java.util.Locale

@Composable
fun Int.toPainter(): Painter = painterResource(this)

@Composable
fun Int.toImageVector() = ImageVector.vectorResource(this@toImageVector)

fun List<IconModel>.filterBy(query: String) = this.filter { model ->
    model.name.startsWith(
        query.replaceFirstChar { character ->
            if (character.isLowerCase()) character.titlecase(Locale.ROOT) else character.toString()
        }
    ) || model.name.contains(
        query.replaceFirstChar {character ->
            if (character.isLowerCase()) character.titlecase(Locale.ROOT) else character.toString()
        }
    )
}