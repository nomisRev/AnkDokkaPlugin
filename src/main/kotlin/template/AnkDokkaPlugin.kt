package template

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.doc.Br
import org.jetbrains.dokka.model.doc.CodeBlock
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.transformers.documentation.PreMergeDocumentableTransformer

class AnkDokkaPlugin : DokkaPlugin() {
    val dokkaBasePlugin by lazy { plugin<DokkaBase>() }
    val ank by extending {
        dokkaBasePlugin.preMergeDocumentableTransformer with AnkCompiler
    }
}

object AnkCompiler : PreMergeDocumentableTransformer {

    // Could we optimise with `suspend` and running in parallel?
    override fun invoke(modules: List<DModule>): List<DModule> =
        modules.also {
            Engine.compileCode(modules.allSnippets(), emptyList())
        }
}

private fun CodeBlock.asStringOrNull(): String? =
    buildString {
        children.forEach { tag ->
            when (tag) {
                is Text -> append(tag.body)
                Br -> append("\n")
                else -> return null
            }
        }
    }

private fun List<DModule>.allSnippets(): List<Snippet> =
    flatMap { module ->
        module.packages.flatMap { `package` ->
            `package`.children.flatMap { documentable ->
                documentable.documentation.values.flatMap { node ->
                    node.children.flatMap { tagWrapper ->
                        tagWrapper.children.mapNotNull { docTag ->
                            (docTag as? CodeBlock)?.let { code ->
                                code.params["lang"]?.let { fence ->
                                    fenceRegexStart.matchEntire(fence)?.let { match ->
                                        docTag.asStringOrNull()?.let { rawCode ->
                                            val lang = match.groupValues[1].trim()
                                            Snippet(fence, lang, rawCode, null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }