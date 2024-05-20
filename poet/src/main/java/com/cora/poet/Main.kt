package com.cora.poet

import com.cora.poet.IconsInfo.Companion.root
import java.nio.file.Paths

fun main() {
    val coraIcons = "https://drive.usercontent.google.com/u/0/uc?id=19UDVbhqAiBriU3eejihDVvEWUtvjvLWb&export=download"
    val iconsInfo = IconsInfo(
        path = coraIcons,
        pathToDrawable = Paths.get("$root/icons/src/main/res/drawable/"),
    )
    IconsPoet().apply { generate(iconsInfo) }
}