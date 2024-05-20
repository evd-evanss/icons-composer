package com.cora.poet

import java.nio.file.Path
import java.nio.file.Paths

data class IconsInfo(
    val path: String,
    val pathToGenerateKt: Path = getKtPath(),
    val pathToDrawable: Path,
    val resourceName: String = "ArcoDrawable",
    val paintersName: String = "ArcoPainter",
) {

    companion object {
        fun getKtPath() = Paths.get(
            Paths.get("").toAbsolutePath().toFile().absolutePath,
            "icons/src/main/java"
        )
        val root = Paths.get("").toAbsolutePath().toFile().absolutePath
    }
}