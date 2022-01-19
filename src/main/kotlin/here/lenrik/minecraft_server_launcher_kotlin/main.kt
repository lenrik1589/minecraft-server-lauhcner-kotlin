package here.lenrik.minecraft_server_launcher_kotlin

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import java.time.Instant


fun literal(name: String) = LiteralArgumentBuilder.literal<Source>(name)!!
fun <ArgType> argument(name: String, type: ArgumentType<ArgType>) = RequiredArgumentBuilder.argument<Source, ArgType>(name, type)!!

class Source {
	var fabric: Boolean? = null
}

fun main(args: Array<String>) {
	val dispatcher = CommandDispatcher<Source>()
	val createLiteral = "create" {
		//@formatter:off
		val version = try { StringArgumentType.getString(it, "version") } catch(iae: IllegalArgumentException) { null }
		val name = try { StringArgumentType.getString(it, "name") } catch(iae: IllegalArgumentException) { null }
		//@formatter:on
		!"Create config $name, on version $version!"
		1
	}.build()
	dispatcher += "config"().then(createLiteral)
	dispatcher += "config"().then(
		"create"().then(
			"--version"().then(
				argument(
					"version", string()
				) to createLiteral
			)
		).then(
			"--fabric"().redirect(createLiteral) { it.source.fabric = true; it.source }
		).then(
			argument("name", string()) to createLiteral
		)
	).then(
		"list" { !"List configs!"; 1 }.build()
	)/*.redirect(listConfigsNode)*/
	dispatcher += "install" { !"Install!"; 1 }
	dispatcher += "run" { !"Run!"; 1 }


	with(" ".join(args.toList())) {
		dispatcher.parse(this, Source()).also { result ->
			if(result.exceptions.isNotEmpty() || result.context.nodes.isEmpty()) {
				" ".join(result.exceptions.values.map(Exception::toString))
				dispatcher.getAllUsage(if(result.context.nodes.isEmpty()) dispatcher.root else result.context.nodes.keys.last(), result.context.source, false).onEach { !it }
			} else {
				!this.substring(0, result.reader.cursor)
				!"${result.exceptions}, ${result.context.command}"
				dispatcher.execute(result)
			}
		}
	}
}

infix fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.to(
	target: CommandNode<S>
) = this.redirect(target)!!

operator fun <T> CommandDispatcher<T>.plus(literal: LiteralArgumentBuilder<T>): LiteralCommandNode<T> {
	return this.register(literal)
}

operator fun <T> CommandDispatcher<T>.plusAssign(literal: LiteralArgumentBuilder<T>) {
	this.plus(literal)
}

operator fun String.invoke() = literal(this)

operator fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.invoke(command: Command<S>) = this.executes(command)!!

operator fun String.invoke(command: Command<Source>) = this()(command)!!

private fun String.join(other: Collection<String>): String {
	return other.joinToString(this)
}

private operator fun String.not() = println("[${Instant.now()}](${Thread.currentThread().name}) $this")
