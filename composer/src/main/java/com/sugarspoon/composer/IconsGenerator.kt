package com.sugarspoon.composer

import com.android.ide.common.vectordrawable.Svg2Vector
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipInputStream
import kotlin.io.path.createDirectories

class IconsGenerator {

    fun process(iconsInfo: IconsInfo) {
        removeOldIcons(iconsInfo)
        val icons = downloadAndUnzipIcons(iconsInfo)
        generateInterface(icons, iconsInfo.tag)
        generateIconClass(icons, iconsInfo)
    }

    private fun removeOldIcons(iconsInfo: IconsInfo) {
        iconsInfo.pathToGenerateKt.toFile().listFiles()
            ?.filter { it.name != "${iconsInfo.nameToClass}.kt" }
            ?.forEach { it.delete() }

        iconsInfo.pathToDrawable.toFile().listFiles()
            ?.filter { it.name.startsWith("ic_") }
            ?.forEach { it.delete() }
    }

    private fun downloadAndUnzipIcons(iconsInfo: IconsInfo): List<Pair<String, String>> {

        val icons = mutableListOf<Pair<String, String>>()
        val inputStream = URL(iconsInfo.path).openConnection().getInputStream().buffered()

        iconsInfo.pathToDrawable.createDirectories()

        ZipInputStream(inputStream).use { zipInputStream ->
            while (true) {
                val entry = zipInputStream.nextEntry ?: break
                if (entry.isDirectory) {
                    zipInputStream.closeEntry()
                    continue
                }
                if (entry.name.contains("._")){
                    zipInputStream.closeEntry()
                    continue
                }
                if (entry.name.contains(".DS_Store") or entry.name.contains("ZWNJbag_2")){
                    zipInputStream.closeEntry()
                    continue
                }

                println("icon raw = ${entry.name}")
                val svgFileName = "ic_" + entry.name
                    .filterNot { it.isWhitespace() }
                    .replace("\u200C", "")
                    .split("/").last()
                    .replace("-", "_")
                    .lowercase()

                println("icon svg = $svgFileName")
                val outFileSvg = File(iconsInfo.pathToDrawable.toFile(), svgFileName)

                val xmlFileName = svgFileName
                    .removeSuffix(".svg")
                    .replace('.', '_') + ICONS_SUFFIX

                println("icon xml = $xmlFileName")
                val outFileXml = File(iconsInfo.pathToDrawable.toFile(), xmlFileName)

                outFileSvg.outputStream().use { outputStream ->
                    zipInputStream.copyTo(outputStream)
                }

                zipInputStream.closeEntry()

                outFileXml.outputStream().buffered().use { outputStream ->
                    Svg2Vector.parseSvgToXml(outFileSvg, outputStream)
                }

                outFileSvg.delete()

                val lines = outFileXml.readLines()

                val contents = lines.joinToString("\n") + "\n"

                Files.write(outFileXml.toPath(), contents.toByteArray())

                val resourceName = xmlFileName.removeSuffix(".xml")

                val iconName = resourceName
                    .removePrefix("ic_")
                    .split("_")
                    .joinToString("") {
                        it.replaceFirstChar { c -> c.uppercase() }
                    }

                println("icon kt = $iconName")
                icons.add(Pair(iconName, resourceName))
            }
        }
        return icons
    }

    private fun generateInterface(
        icons: List<Pair<String, String>>,
        tag: String,
    ) {
        val iconsCommonsInterface = TypeSpec.interfaceBuilder(ICONS_INTERFACE_NAME)
        val list = ClassName("kotlin.collections", "List")
        val names = ClassName(
            PACKAGE_FOR_GEN_INTERFACE_CORE,
            "IconsModel"
        )
        val listOfNames = list.parameterizedBy(names)

        iconsCommonsInterface
            .addFunction(
                FunSpec.builder("getAll")
                    .addModifiers(KModifier.ABSTRACT)
                    .returns(listOfNames).build()
            )


        icons.sortedBy { it.first }.forEach { (iconName) ->
            val property = PropertySpec
                .builder(iconName, ClassName("kotlin", "Int"))
                .build()

            iconsCommonsInterface.addProperty(property)
        }
        val file = FileSpec.builder(PACKAGE_FOR_GEN_INTERFACE_CORE, ICONS_INTERFACE_NAME)
            .addType(iconsCommonsInterface.build())
            .indent("    ")
            .build()

        file.writeTo(
            Paths.get(
                Paths.get("").toAbsolutePath().toFile().absolutePath,
                "icons/src/main/java"
            )
        )
    }

    private fun generateIconClass(
        illustrations: List<Pair<String, String>>,
        iconsInfo: IconsInfo,
    ) {
        val kotlinArrayList = ClassName("kotlin.collections", "ArrayList")
        val kotlinList = ClassName("kotlin.collections", "List")
        val kotlinInt = ClassName("kotlin", "Int")
        val assetModel = ClassName(
            PACKAGE_FOR_GEN_INTERFACE_CORE,
            "IconsModel"
        )
        val illustrationAssetsInterface = ClassName(
            PACKAGE_FOR_GEN_INTERFACE_CORE,
            ICONS_INTERFACE_NAME
        )
        val listOfAssetModel = kotlinList.parameterizedBy(assetModel)
        val arrayListOfModels = kotlinArrayList.parameterizedBy(assetModel)


        val illustrationBrandClass = TypeSpec
            .classBuilder(iconsInfo.nameToClass)
            .addModifiers(KModifier.INTERNAL)
            .addSuperinterface(illustrationAssetsInterface)

        val funGetIllustrations = FunSpec
            .builder("getAll")
            .addModifiers(KModifier.OVERRIDE)
            .returns(listOfAssetModel)
            .addStatement("val assets = %T()", arrayListOfModels)


        illustrations.sortedBy { it.first }.forEach { (iconName, iconResource) ->
            val property = PropertySpec
                .builder(iconName, kotlinInt)
                .addModifiers(KModifier.OVERRIDE)
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement(
                            "return %L",
                            "R.drawable.$iconResource"
                        )
                        .build()
                )
                .build()

            funGetIllustrations.addCode(
                CodeBlock.of(
                    "assets.add(" +
                            "IconsModel(icon = $iconName, name = \"${iconName}\")" +
                            ")" + "\n"
                )
            )

            illustrationBrandClass.addProperty(property)
        }
        funGetIllustrations.addStatement("return assets")
        illustrationBrandClass.addFunction(funGetIllustrations.build())

        val file = FileSpec.builder(PACKAGE_FOR_GEN_INTERFACE_CORE, iconsInfo.nameToClass)
            .addImport(R_PACKAGE_NAME, "R")
            .addType(illustrationBrandClass.build())
            .indent("    ")
            .build()

        file.writeTo(iconsInfo.pathToGenerateKt)
    }

    companion object {
        private const val PACKAGE_FOR_GEN_INTERFACE_CORE = "com.sugarspoon.icons.generated"
        private const val R_PACKAGE_NAME = "com.sugarspoon.icons"
        private const val ICONS_SUFFIX = ".xml"
        private const val ICONS_INTERFACE_NAME = "Assets"
    }
}