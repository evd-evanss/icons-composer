package com.cora.poet

import java.nio.file.Path
import java.nio.file.Paths

data class FoldersInfo(
    val zipUrl: String,
    val pathToGenerateKt: Path = Paths.get(
        Paths.get("").toAbsolutePath().toFile().absolutePath,
        "icons/src/main/kotlin"
    ),
    val pathToDrawable: Path = Paths.get("$root/icons/src/main/res/drawable/"),
    val resourceName: String = "ArcoDrawable",
    val paintersName: String = "ArcoPainter",
) {

    companion object {
        private val root: String = Paths.get("").toAbsolutePath().toFile().absolutePath
    }
}