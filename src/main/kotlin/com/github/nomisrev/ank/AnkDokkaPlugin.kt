package com.github.nomisrev.ank

import arrow.fx.coroutines.parTraverse
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.transformers.documentation.PreMergeDocumentableTransformer

class AnkDokkaPlugin : DokkaPlugin() {
  val dokkaBasePlugin by lazy { plugin<DokkaBase>() }
  val ank by extending {
    dokkaBasePlugin.preMergeDocumentableTransformer providing ::AnkCompiler
  }
}

/**
 * => Properly log errors through DokkaLogger
 */
private class AnkCompiler(private val ctx: DokkaContext) : PreMergeDocumentableTransformer {



  override fun invoke(modules: List<DModule>): List<DModule> = runBlocking(Dispatchers.Default) {
    ctx.logger.warn(colored(ANSI_PURPLE, "Λnk Dokka Plugin is running"))

    val urls: List<URL> = ctx.configuration.pluginsClasspath.map { it.toURI().toURL() } +
            ctx.configuration.sourceSets.flatMap { it.classpath.map { it.toURI().toURL() } }

    modules.parTraverse { module ->
      Engine.engine(
        urls.distinct().also {
          ctx.logger.error(colored(ANSI_RED, "Going to print classpath:"))
          ctx.logger.error(colored(ANSI_RED, it.joinToString(separator = "\n")))
        },
        ctx.logger
      ).use { engine ->
        val packages =
          module.packages.parTraverseCodeBlock(module) { module, `package`, documentable, node, wrapper, codeBlock ->
            Snippet(module, `package`, documentable, node, wrapper, codeBlock)?.let {
              engine.eval(it)
            }?.toCodeBlock()
          }

        module.copy(packages = packages)
      }

    }.also { Engine.testReport()?.let(::println) }
  }
}
