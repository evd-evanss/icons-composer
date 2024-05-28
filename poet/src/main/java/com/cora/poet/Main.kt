package com.cora.poet

fun main() {
    val foldersInfo = FoldersInfo(zipUrl = "https://drive.usercontent.google.com/u/0/uc?id=19UDVbhqAiBriU3eejihDVvEWUtvjvLWb&export=download")
    IconsGenerator().apply { generate(foldersInfo) }
}