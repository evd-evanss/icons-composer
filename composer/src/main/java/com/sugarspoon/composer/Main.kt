package com.sugarspoon.composer

import com.sugarspoon.composer.IconsInfo.Companion.root
import com.sugarspoon.composer.IconsPath.filled
import java.nio.file.Paths

fun main() {
    val iconsInfo = IconsInfo(
        path = filled,
        pathToDrawable = Paths.get("$root/icons/src/main/res/drawable/"),
        tag = "filled",
        nameToClass = "IconsOutline"
    )
    IconsGenerator().process(iconsInfo)
}