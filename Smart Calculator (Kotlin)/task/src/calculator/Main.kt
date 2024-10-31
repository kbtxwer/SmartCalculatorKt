package calculator

import java.math.BigInteger
import java.util.*

fun main() {
    Calculator().run()
}

class Calculator {
    private val variables: MutableMap<String, BigInteger> = mutableMapOf()

    fun run() {
        var terminate = false
        while (!terminate) {
            val line = readln().trim()
            try {
                when {
                    line.isEmpty() -> {}
                    line.isCommand() -> terminate = line.command()
                    line.isAssignment() -> line.assignment()
                    else -> line.expression()
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    private fun String.command(): Boolean {
        when (this) {
            "/help" -> println("The program adds and subtracts numbers")
            "/exit" -> println("Bye!").run { return true }
            else -> throw Exception("Unknown command")
        }

        return false
    }

    private fun String.assignment() {
        val terms = split("\\s*=\\s*".toRegex())
        if (!terms[0].isValidIdentifier()) throw Exception("Invalid identifier")
        else if (terms.size != 2 || !terms[1].isValidTerm()) throw Exception("Invalid assignment")
        else variables[terms[0]] = terms[1].getValue()
    }

    private fun String.expression() {
        if (isBlank()) throw Exception("Invalid expression")
        val result = Stack<BigInteger>()
        // 33 + 20 + 11 + 49 - 32 - 9 + 1 - 80 + 4
//        println(parsePostfixExp())
       parsePostfixExp().forEach {
            when(it) {
                is BigInteger -> {
                    result.push(it)
                }
                is String -> {
                    throw Exception("Invalid expression")
                }
                else -> {
                    val op = it as ((BigInteger, BigInteger)-> BigInteger)
                    val r = result.pop()
                    val l = result.pop()
                    result.push(op(l, r))
                }
            }
        }
        println(result.pop())
    }

    private fun String.parsePostfixExp(): Queue<Any> {
        val regTerm = "(([a-zA-Z]+)|([+-]?\\d+))"
        // format the expression by adding additional space to ensure the split operation goes well
        val exp = this.replace(regTerm.toRegex(), " $0 ")
            .replace("[()]".toRegex(), " $0 ")
        // then we can convert the expression from infix to postfix
        val precedence = mapOf(
            '+' to 1, '-' to 1, '*' to 2, '/' to 2,
            ::add to 1, ::subtract to 1, ::multiply to 2, ::divide to 2, "(" to 0, ")" to 0
        )

        val opStack = Stack<Any>()
        val postFixQueue = LinkedList<Any>()

        exp.split("\\s+".toRegex()).forEach {
            when {
                it.isBlank() -> {}
                it.isValidTerm() -> {
                    postFixQueue.add(it.getValue()) // value
                }
                it.isOperator() -> {
                    val op = it.parseOperator()
                    while (opStack.isNotEmpty() && precedence[op]!! <= precedence[opStack.peek()]!!) {
                        postFixQueue.add(opStack.pop()) // operator
                    }
                    opStack.push(op)
                }
                it == "(" -> {
                    opStack.push(it) // string
                }
                it == ")" -> {
                    while (opStack.isNotEmpty() && opStack.peek() != "(") {
                        postFixQueue.add(opStack.pop()) // value or operator
                    }
                    if (opStack.isNotEmpty() && opStack.peek() == "(") {
                        opStack.pop()
                    } else {
                        throw Exception("Invalid expression")
                    }
                }
                else -> throw Exception("Invalid expression")
            }
        }

        while (opStack.isNotEmpty()) {
            postFixQueue.add(opStack.pop())
        }

        return postFixQueue
    }

    private fun String.parseOperator(): (BigInteger, BigInteger) -> BigInteger {
        var operator = ' '
        this.forEach { op ->
            when (operator) {
                '+' -> {
                    if (op == '-') {
                        operator = '-'
                    } else if (op != '+') {
                        operator = '_'
                    }
                }

                '-' -> {
                    if (op == '-') {
                        operator = '+'
                    } else if (op != '+') {
                        operator = '_'
                    }
                }

                ' ' -> operator = op
                else -> operator = '_'
            }
        }
        return when (operator) {
            '+' -> ::add
            '-' -> ::subtract
            '*' -> ::multiply
            '/' -> ::divide
            else -> throw Exception("Invalid expression")
        }
    }

    fun String.getValue() =
        variables.getOrElse(this.trim()) { this.toBigIntegerOrNull() ?: throw Exception("Unknown variable") }

    private fun String.isValidIdentifier() = "[a-zA-Z]+".toRegex().matches(this)
    private fun String.isValidTerm() = isValidIdentifier() || toBigIntegerOrNull() != null
    private fun String.isOperator() = "([+\\-*/])+".toRegex().matches(this)
    private fun String.isAssignment() = contains("=")
    private fun String.isCommand() = startsWith("/")
    private fun String.isValidExpression(): Boolean {
        val validTerm = "(([a-zA-Z]+)|([+-]?\\d+))"
        val validOperator = "([+\\-*/()])+"
        return "($validTerm)(\\s+$validOperator\\s+$validTerm)*".toRegex().matches(this)
    }

    private fun add(x: BigInteger, y: BigInteger): BigInteger = x + y
    private fun subtract(x: BigInteger, y: BigInteger): BigInteger = x - y
    private fun multiply(x: BigInteger, y: BigInteger): BigInteger = x * y
    private fun divide(x: BigInteger, y: BigInteger): BigInteger = x / y
}