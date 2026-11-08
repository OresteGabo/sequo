package dev.orestegabo.sequo

import dev.orestegabo.sequo.feature.settings.AppLanguage
import dev.orestegabo.sequo.feature.settings.appText
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.StringResource
import kotlin.test.Test
import kotlin.test.assertTrue

class AppTextCoverageTest {
    @OptIn(InternalResourceApi::class)
    @Test fun everyDeclaredStringRendersInBothSelectableLanguages() {
        val relative = "src/commonMain/composeResources/values/strings.xml"
        val source = listOf(File(relative), File("shared/$relative")).first { it.isFile }
        val factory = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        }
        val strings = factory.newDocumentBuilder().parse(source).getElementsByTagName("string")
        for (index in 0 until strings.length) {
            val name = strings.item(index).attributes.getNamedItem("name").nodeValue
            val resource = StringResource("coverage:$name", name, emptySet())
            for (language in AppLanguage.entries) {
                assertTrue(appText(resource, language).isNotBlank(), "$name must render in $language")
            }
        }
    }
}
