
data class Person(val age: Int, val name: String)

/**
 * This function is documented, and Ank will check that it complies
 *
 * ```kotlin:ank
 * listOf(1, 2, 3).map { it + 1 }
 * ```
 */
fun example(): Unit = Unit

/**
 * This function is documented, and Ank can correctly handle the exception
 *
 * ```kotlin:ank:fail
 * throw RuntimeException("MOTHERFUCKING SNAKES ON A PLANE")
 * ```
 */
fun exampleException(): Unit = Unit

/**
 * This function is documented, and Ank can correctly handle the exception
 *
 * ```kotlin:ank:replace
 * fun hello(name: String) = "Hello $name!"
 *
 * hello("Λrrow")
 * ```
 */
fun exampleReplace(): Unit = Unit

/**
 * This function is documented, and I have access to my classpath from docs
 * Since it's silent, it doesn't add `// Either.Right(1)` as output.
 *
 * ```kotlin:ank:silent
 * import arrow.core.Either
 *
 * Either.Right(1)
 * ```
 */
fun exampleDependency(): Unit = Unit
