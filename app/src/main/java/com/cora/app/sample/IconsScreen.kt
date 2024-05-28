package com.cora.app.sample

import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.cora.design.compose.defaultHorizontalPadding
import br.com.cora.design.compose.theme.ArcoTheme
import br.com.cora.design.compose.theme.color.tokens.DefaultColorTokens
import br.com.cora.design.compose.theme.dimensions.Spacing
import br.com.cora.design.compose.ui.InputText
import br.com.cora.design.compose.ui.RoundIconFrame
import br.com.cora.design.compose.ui.RoundIconFrameBackground
import br.com.cora.design.compose.ui.Tag
import br.com.cora.design.compose.ui.TagType
import br.com.cora.design.compose.ui.Text
import br.com.cora.design.compose.ui.preview.ArcoPreview
import com.cora.icons.extensions.filterBy
import com.cora.icons.extensions.toPainter
import com.cora.icons.generated.ArcoDrawable
import com.cora.icons.generated.ArcoPainter

@Composable
fun ColumnScope.IconsScreen() {
    var query by remember { mutableStateOf("") }
    val icons = ArcoDrawable.getAll().filterBy(query)
    val listState = rememberLazyListState()

    ArcoDrawable.ArrowLeft
//    Icon(painter = , contentDescription = )

    InputText(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.base)
            .defaultHorizontalPadding(),
        value = query,
        onValueChange = { query = it },
        label = "Procurar em ${icons.size} ícones",
        leadingIcon = ArcoDrawable.Search
    )

    LazyColumn(
        state = listState,
        modifier = Modifier.shadow(elevation = .5.dp)
    ) {
        items(
            count = icons.size,
            key = { icons[it].name }
        ) {
            val iconModel = icons[it]
            Row(
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .shadow(elevation = .2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconFrame(
                    roundIconFrameBackground = RoundIconFrameBackground.BLUE,
                    iconTint = DefaultColorTokens.Gray1,
                    iconSize = 24.dp,
                    icon = iconModel.icon,
                    modifier = Modifier
                        .defaultHorizontalPadding()
                        .size(48.dp)
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = iconModel.name,
                    style = ArcoTheme.typography.body1BoldGrayTextStyle,
                    textAlign = TextAlign.Start
                )
                Tag(
                    modifier = Modifier.defaultHorizontalPadding(),
                    text = (it + 1).toString(),
                    type = TagType.Secondary
                )
            }
        }
    }
}

@Preview
@Composable
fun IconsScreenPreview() = ArcoPreview {
    Column {
        IconsScreen()
    }
}