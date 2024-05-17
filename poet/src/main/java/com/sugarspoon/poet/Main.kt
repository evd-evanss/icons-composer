package com.sugarspoon.poet

import com.sugarspoon.poet.IconsInfo.Companion.root
import java.nio.file.Paths

fun main() {
    val iconsInfo = IconsInfo(
        path = "https://github.com/evd-evanss/icons-path/raw/main/Outline.zip",
        pathToDrawable = Paths.get("$root/icons/src/main/res/drawable/"),
        nameToClass = "IconsOutline"
    )
    IconsPoet().apply {
        generate(iconsInfo)
    }
}