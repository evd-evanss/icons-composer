package com.cora.poet

import com.android.ide.common.vectordrawable.Svg2Vector
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipInputStream
import kotlin.io.path.createDirectories

class IconsGenerator {

    fun generate(foldersInfo: FoldersInfo) {
        deleteOldIcons(foldersInfo)
        val icons = downloadAndUnzipIcons(foldersInfo)

        generateDrawableInterface(icons)
        generateDrawableClass(icons, foldersInfo)

        generatePainterInterface(icons)
        generatePaintersClass(icons, foldersInfo)
    }

    private fun deleteOldIcons(foldersInfo: FoldersInfo) {
        foldersInfo.pathToGenerateKt.toFile().listFiles()
            ?.filter { it.name != "${foldersInfo.resourceName}.kt" }
            ?.forEach { it.delete() }

        foldersInfo.pathToDrawable.toFile().listFiles()
            ?.filter { it.name.startsWith("ic_") }
            ?.forEach { it.delete() }
    }

    private fun downloadAndUnzipIcons(foldersInfo: FoldersInfo): List<Pair<String, String>> {
        println("Baixando ícones...")
        val icons = mutableListOf<Pair<String, String>>()
        val inputStream = URL(foldersInfo.zipUrl).openConnection().getInputStream().buffered()

        foldersInfo.pathToDrawable.createDirectories()

        ZipInputStream(inputStream).use { zipInputStream ->
            println("Abrindo zip...")
            while (true) {
                val entry = zipInputStream.nextEntry ?: break
                if (entry.isDirectory) {
                    zipInputStream.closeEntry()
                    continue
                }
                if (entry.name.contains("._")) {
                    zipInputStream.closeEntry()
                    continue
                }
                if (entry.name.contains(".DS_Store") or entry.name.contains("ZWNJbag_2")) {
                    zipInputStream.closeEntry()
                    continue
                }

                println("icon raw = ${entry.name}")

                val svgFileName = createSvgName(entry.name)
                println("icon svg = $svgFileName")

                val xmlFileName = createXmlName(svgFileName)
                println("icon xml = $xmlFileName")

                val fileSvg = File(foldersInfo.pathToDrawable.toFile(), svgFileName)
                val fileXml = File(foldersInfo.pathToDrawable.toFile(), xmlFileName)

                fileSvg.outputStream().use { outputStream ->
                    zipInputStream.copyTo(outputStream)
                }

                zipInputStream.closeEntry()

                fileXml.outputStream().buffered().use { outputStream ->
                    Svg2Vector.parseSvgToXml(fileSvg.toPath(), outputStream)
                }

                fileSvg.delete()

                val resourceName = xmlFileName.removeSuffix(".xml")

                val iconNameKotlin = resourceName
                    .removePrefix("ic_")
                    .split("_")
                    .joinToString("") {
                        it.replaceFirstChar { c -> c.uppercase() }
                    }

                println("icon kt = $iconNameKotlin\n")
                icons.add(Pair(iconNameKotlin, resourceName))
            }
        }
        return icons
    }

    private fun generateDrawableInterface(icons: List<Pair<String, String>>) {
        val list = ClassName("kotlin.collections", "List")
        val iconsModel = ClassName(PACKAGE_ICON_MODEL, "IconModel")
        val iconsModelList = list.parameterizedBy(iconsModel)

        val typeSpecInterface = TypeSpec.interfaceBuilder(ICONS_INTERFACE_NAME)

        typeSpecInterface.addFunction(
            FunSpec.builder("getAll")
                .addModifiers(KModifier.ABSTRACT)
                .returns(iconsModelList).build()
        )

        icons.sortedBy { it.first }.forEach { (iconName) ->
            val property = PropertySpec
                .builder(iconName, ClassName("kotlin", "Int"))
                .addAnnotation(AnnotationSpec.builder(ClassName(
                    "androidx.annotation",
                    "DrawableRes"
                )).useSiteTarget(AnnotationSpec.UseSiteTarget.GET).build())
                .build()

            typeSpecInterface.addProperty(property)
        }
        val file = FileSpec.builder(GENERATED_PACKAGE, ICONS_INTERFACE_NAME)
            .addType(typeSpecInterface.build())
//            .indent("    ")
            .build()

        file.writeTo(
            Paths.get(
                Paths.get("").toAbsolutePath().toFile().absolutePath,
                "icons/src/main/kotlin"
            )
        )
    }

    private fun generatePainterInterface(icons: List<Pair<String, String>>) {
        val typeSpecInterface = TypeSpec.interfaceBuilder("Painters")
        val list = ClassName("kotlin.collections", "List")
        val painter = ClassName("androidx.compose.ui.graphics.painter", "Painter")
        val composable = ClassName("androidx.compose.runtime", "Composable")
        val composableAnnotation = AnnotationSpec.builder(composable).useSiteTarget(AnnotationSpec.UseSiteTarget.GET).build()
        val iconsModel = ClassName(PACKAGE_ICON_MODEL, "IconModel")
        val iconsModelList = list.parameterizedBy(iconsModel)

        typeSpecInterface.addFunction(
            FunSpec.builder("getAll")
                .addModifiers(KModifier.ABSTRACT)
                .returns(iconsModelList).build()
        )

        icons.sortedBy { it.first }.forEach { (iconName) ->
            val property = PropertySpec
                .builder(iconName, painter)
                .addAnnotation(composableAnnotation)
                .build()

            typeSpecInterface.addProperty(property)
        }
        val file = FileSpec.builder(GENERATED_PACKAGE, "Painters")
            .addType(typeSpecInterface.build())
            .build()

        file.writeTo(
            Paths.get(
                Paths.get("").toAbsolutePath().toFile().absolutePath,
                "icons/src/main/kotlin"
            )
        )
    }

    private fun generateDrawableClass(icons: List<Pair<String, String>>, foldersInfo: FoldersInfo, ) {
        val kotlinArrayList = ClassName("kotlin.collections", "ArrayList")
        val kotlinList = ClassName("kotlin.collections", "List")
        val kotlinInt = ClassName("kotlin", "Int")
        val assetModel = ClassName(
            PACKAGE_ICON_MODEL,
            "IconModel"
        )
        val illustrationAssetsInterface = ClassName(
            GENERATED_PACKAGE,
            ICONS_INTERFACE_NAME
        )
        val listOfAssetModel = kotlinList.parameterizedBy(assetModel)
        val arrayListOfModels = kotlinArrayList.parameterizedBy(assetModel)

        val iconsOutlineClass = TypeSpec
            .objectBuilder(foldersInfo.resourceName)
            .addModifiers(KModifier.PUBLIC)
            .addSuperinterface(illustrationAssetsInterface)

        val funGetIllustrations = FunSpec
            .builder("getAll")
            .addModifiers(KModifier.OVERRIDE)
            .returns(listOfAssetModel)
            .addStatement("val assets = %T()", arrayListOfModels)


        icons.sortedBy { it.first }.forEach { (iconName, iconResource) ->
            val property = PropertySpec
                .builder(iconName, kotlinInt)
                .addAnnotation(AnnotationSpec.builder(ClassName(
                     "androidx.annotation",
                     "DrawableRes"
                )).useSiteTarget(AnnotationSpec.UseSiteTarget.GET).build())
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
                            "IconModel(icon = $iconName, name = \"${iconName}\")" +
                            ")" + "\n"
                )
            )

            iconsOutlineClass.addProperty(property)
        }
        funGetIllustrations.addStatement("return assets")
        iconsOutlineClass.addFunction(funGetIllustrations.build())

        val file = FileSpec.builder(GENERATED_PACKAGE, foldersInfo.resourceName)
            .addImport(R_PACKAGE_NAME, "R")
            .addType(iconsOutlineClass.build())
//            .indent("    ")
            .build()

        file.writeTo(foldersInfo.pathToGenerateKt)
    }

    private fun generatePaintersClass(icons: List<Pair<String, String>>, foldersInfo: FoldersInfo, ) {
        val kotlinArrayList = ClassName("kotlin.collections", "ArrayList")
        val kotlinList = ClassName("kotlin.collections", "List")
        val painter = ClassName("androidx.compose.ui.graphics.painter", "Painter")
        val composable = ClassName("androidx.compose.runtime", "Composable")
        val composableAnnotation = AnnotationSpec.builder(composable).build()
        val assetModel = ClassName(
            PACKAGE_ICON_MODEL,
            "IconModel"
        )
        val illustrationAssetsInterface = ClassName(
            GENERATED_PACKAGE,
            "Painters"
        )
        val listOfAssetModel = kotlinList.parameterizedBy(assetModel)
        val arrayListOfModels = kotlinArrayList.parameterizedBy(assetModel)

        val iconsOutlineClass = TypeSpec
            .objectBuilder(foldersInfo.paintersName)
            .addModifiers(KModifier.PUBLIC)
            .addSuperinterface(illustrationAssetsInterface)

        val funGetIllustrations = FunSpec
            .builder("getAll")
            .addModifiers(KModifier.OVERRIDE)
            .returns(listOfAssetModel)
            .addStatement("val assets = %T()", arrayListOfModels)

        icons.sortedBy { it.first }.forEach { (iconName, iconResource) ->
            val property = PropertySpec.builder(iconName, painter)
                .addModifiers(KModifier.OVERRIDE)
                .getter(
                    FunSpec.getterBuilder()
                        .addAnnotation(composableAnnotation)
                        .addStatement(
                            "return %M(%L)",
                            MemberName("androidx.compose.ui.res", "painterResource"),
                            "R.drawable.$iconResource"
                        )
                        .build(),
                )
                .build()

            funGetIllustrations.addCode(
                CodeBlock.of(
                    "assets.add(" +
                            "IconModel(icon = R.drawable.$iconResource, name = \"${iconName}\")" +
                            ")" + "\n"
                )
            )

            iconsOutlineClass.addProperty(property)
        }
        funGetIllustrations.addStatement("return assets")
        iconsOutlineClass.addFunction(funGetIllustrations.build())

        val file = FileSpec.builder(GENERATED_PACKAGE, foldersInfo.paintersName)
            .addImport(R_PACKAGE_NAME, "R")
            .addType(iconsOutlineClass.build())
            .indent("    ")
            .build()

        file.writeTo(foldersInfo.pathToGenerateKt)
    }

    private fun createSvgName(entryName: String) = entryName
        .filterNot { it.isWhitespace() }
        .replace("\u200C", "")
        .split("/").last()
        .replace("-", "_")
        .lowercase()

    private fun createXmlName(svgFileName: String) = svgFileName
        .replace(".ic_", "ic_")
        .replace("ic__", "ic_")
        .removeSuffix(".svg") + ICONS_SUFFIX

    companion object {
        private const val GENERATED_PACKAGE = "com.cora.icons.generated"
        private const val PACKAGE_ICON_MODEL = "com.cora.icons.model"
        private const val R_PACKAGE_NAME = "com.cora.icons"
        private const val ICONS_SUFFIX = ".xml"
        private const val ICONS_INTERFACE_NAME = "Assets"
    }
}