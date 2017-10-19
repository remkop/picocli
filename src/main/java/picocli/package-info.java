/**
 * Provides the main {@link picocli.CommandLine CommandLine} class for parsing command line arguments;
 * the {@link picocli.AutoComplete} class has been moved to the {@link picocli} package,
 * but to preserve binary compatibility this package contains a thin delegate class that forwards to the new package.
 * <p>
 * The {@link picocli.CommandLine CommandLine} class is a one-file framework for creating Java command line
 * applications with almost zero code.
 *
 * {@code CommandLine} is in a single file, so it can be included in source form.
 * This lets users run picocli-based applications without requiring picocli as an external dependency.
 * </p>
 */
package picocli;
