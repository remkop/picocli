package picocli.examples.jsr380.beanvalidation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionStrategy;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

import java.util.Set;

class ValidatingExecutionStrategy implements IExecutionStrategy {
    public int execute(ParseResult parseResult) {
        validate(parseResult.commandSpec());
        return new CommandLine.RunLast().execute(parseResult); // default execution strategy
    }

    void validate(CommandSpec spec) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Object>> violations = validator.validate(spec.userObject());
        if (!violations.isEmpty()) {
            String errorMsg = "";
            for (ConstraintViolation<?> violation : violations) {
                errorMsg += "ERROR: " + violation.getMessage() + "\n";
            }
            throw new ParameterException(spec.commandLine(), errorMsg);
        }
    }
}
