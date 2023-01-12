package picocli.examples.kotlin.jsr380.beanvalidation

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import picocli.CommandLine
import picocli.CommandLine.IExecutionStrategy
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.ParseResult

class ValidatingExecutionStrategy : IExecutionStrategy {
    override fun execute(parseResult : ParseResult) : Int {
        validate(parseResult.commandSpec())
        return CommandLine.RunLast().execute(parseResult) // default execution strategy
    }

    private fun validate(spec : CommandSpec) {
        val validator: Validator = Validation.buildDefaultValidatorFactory().validator
        val violations: Set<ConstraintViolation<Any>> = validator.validate(spec.userObject())
        if (violations.isNotEmpty()) {
            var errorMsg = ""
            for (violation in violations) {
                errorMsg += "ERROR: ${violation.message}\n"
            }
            throw CommandLine.ParameterException(spec.commandLine(), errorMsg)
        }
    }
}
