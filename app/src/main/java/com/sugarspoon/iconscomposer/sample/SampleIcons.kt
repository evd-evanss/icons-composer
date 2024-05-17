package com.sugarspoon.iconscomposer.sample

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sugarspoon.icons.generated.IconsModel
import com.sugarspoon.icons.generated.SugarSpoonIcons
import java.util.Locale

@Composable
fun SampleIcons() {

    var query by remember { mutableStateOf("") }
    val icons = SugarSpoonIcons.Outline.getAll().filter {
        it.name.startsWith(query.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        })
                || it.name.contains(query.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        })
    }

    LazyColumn {
        item {
            TextField(
                value = query,
                onValueChange = {
                    query = it
                },
                placeholder = {
                    Text(text = "Buscar")
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = SugarSpoonIcons.Outline.Search),
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
        items(
            count = icons.size,
            key = {
                icons[it].name
            }
        ) {
            ItemIcon(iconsModel = icons[it])
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ItemIcon(iconsModel: IconsModel) {
    ListItem(
        icon = {
            Icon(
                painter = painterResource(id = iconsModel.icon),
                contentDescription = null
            )
        },
        text = {
            Text(text = iconsModel.name)
        }
    )
    Divider()
}