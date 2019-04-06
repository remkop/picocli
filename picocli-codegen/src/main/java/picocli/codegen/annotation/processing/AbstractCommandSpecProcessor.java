package picocli.codegen.annotation.processing;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IAnnotatedElement;
import picocli.CommandLine.Model.ITypeInfo;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Unmatched;
import picocli.codegen.annotation.processing.internal.CompletionCandidatesMetaData;
import picocli.codegen.annotation.processing.internal.DefaultValueProviderMetaData;
import picocli.codegen.annotation.processing.internal.GetterSetterMetaData;
import picocli.codegen.annotation.processing.internal.TypeConverterMetaData;
import picocli.codegen.annotation.processing.internal.VersionProviderMetaData;
import picocli.codegen.util.Assert;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import static javax.lang.model.element.ElementKind.ENUM;

/**
 * Abstract annotation processor for {@code @picocli.*} annotations that produces a set of
 * {@link CommandSpec} objects built from the annotated source code.
 * <p>
 * Subclasses should override the {@link #handleCommands(Map, Set, RoundEnvironment)}
 * method to do something useful with these {@code CommandSpec} objects,
 * like generating source code, documentation or configuration files.
 * </p><p>
 * Note that due to the limitations of annotation processing and the compiler API,
 * annotation attributes of type {@code Class} are {@linkplain Element#getAnnotation(Class) not available}
 * as {@code Class} values at compile time, but only as {@code TypeMirror} values.
 * Picocli 4.0 introduces a new {@link ITypeInfo} interface that provides {@code ArgSpec}
 * type metadata that can be used both at compile time and at runtime.
 * </p><p>
 * Similarly, {@code ArgSpec} objects constructed by the annotation processor will have a
 * {@link picocli.CommandLine.Model.IGetter} and {@link picocli.CommandLine.Model.ISetter}
 * implementation that is different from the one used at runtime and cannot be invoked directly:
 * the annotation processor will assign an {@link GetterSetterMetaData}
 * implementation that gives subclass annotation processors access to the annotated element.
 * </p><p>
 * {@code CommandSpec} objects constructed by the annotation processor will have an
 * {@link VersionProviderMetaData} version provider and a {@link DefaultValueProviderMetaData}
 * default value provider, which gives subclass annotation processors access to the
 * {@code TypeMirror} of the version provider and default value provider specified in the
 * annotation.
 * </p>
 * @since 4.0
 */
public abstract class AbstractCommandSpecProcessor extends AbstractProcessor {
    private static final String COMMAND_DEFAULT_NAME = "<main class>";
    private static Logger logger = Logger.getLogger(AbstractCommandSpecProcessor.class.getName());

    /** The ProcessingEnvironment set by the {@link #init(ProcessingEnvironment)} method. */
    protected ProcessingEnvironment processingEnv;

    private static final String COMMAND_TYPE = Command.class.getName().replace('$', '.');

    static ConsoleHandler handler = new ConsoleHandler();

    {
        for (Handler h : Logger.getLogger("picocli.annotation.processing").getHandlers()) {
            Logger.getLogger("picocli.annotation.processing").removeHandler(h);
        }
        handler.setFormatter(new JulLogFormatter());
        handler.setLevel(Level.ALL);
        Logger.getLogger("picocli.annotation.processing").addHandler(handler);
        Logger.getLogger("picocli.annotation.processing").setLevel(Level.ALL);
    }

    /**
     * Returns the annotation types supported by the super class, and adds
     * {@code "picocli.*"} if necessary.
     * Subclasses can omit the {@code @SupportedAnnotationTypes("picocli.*")} annotation,
     * but add other annotations if desired.
     *
     * @return the set of supported annotation types, with at least {@code "picocli.*"}
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> result = super.getSupportedAnnotationTypes();
        if (!result.contains("picocli.*")) {
            result = new TreeSet<String>(result);
            result.add("picocli.*");
        }
        return result;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    // inherit doc
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        logger.info("Entered process, processingOver=" + roundEnv.processingOver());

        IFactory factory = null; //new NullFactory();
        Map<Element, CommandSpec> commands = new LinkedHashMap<Element, CommandSpec>();
        Map<TypeMirror, List<CommandSpec>> commandTypes = new LinkedHashMap<TypeMirror, List<CommandSpec>>();
        Map<Element, OptionSpec.Builder> options = new LinkedHashMap<Element, OptionSpec.Builder>();
        Map<Element, PositionalParamSpec.Builder> parameters = new LinkedHashMap<Element, PositionalParamSpec.Builder>();
        List<MixinInfo> mixinInfoList = new ArrayList<MixinInfo>();
        Map<Element, IAnnotatedElement> parentCommands = new LinkedHashMap<Element, IAnnotatedElement>();
        Map<Element, IAnnotatedElement> specs = new LinkedHashMap<Element, IAnnotatedElement>();
        Map<Element, IAnnotatedElement> unmatched = new LinkedHashMap<Element, IAnnotatedElement>();

        logger.fine("Building commands...");
        buildCommands(roundEnv, factory, commands, commandTypes, options, parameters);

        logger.fine("Building mixins...");
        buildMixins(roundEnv, factory, commands, mixinInfoList, commandTypes, options, parameters);

        logger.fine("Building options...");
        buildOptions(roundEnv, factory, options);

        logger.fine("Building parameters...");
        buildParameters(roundEnv, factory, parameters);

        logger.fine("Building parentCommands...");
        buildParentCommands(roundEnv, factory, parentCommands);

        logger.fine("Building specs...");
        buildSpecs(roundEnv, factory, specs);

        logger.fine("Building unmatched...");
        buildUnmatched(roundEnv, factory, unmatched);

        logger.fine("---------------------------");
        logger.fine("Known commands...");
        for (Map.Entry<Element, CommandSpec> cmd : commands.entrySet()) {
            logger.fine(String.format("%s has CommandSpec[name=%s]", cmd.getKey(), cmd.getValue().name()));
        }
        logger.fine("Known mixins...");
        for (MixinInfo mixinInfo : mixinInfoList) {
            logger.fine(String.format("%s is mixin for %s", mixinInfo.mixin.userObject(), mixinInfo.mixee.userObject()));
        }

        for (Map.Entry<Element, OptionSpec.Builder> option : options.entrySet()) {
            CommandSpec commandSpec = getOrCreateCommandSpecForArg(option, commands);
            logger.fine("Building OptionSpec for " + option + " in spec " + commandSpec);
            commandSpec.addOption(option.getValue().build());
        }
        for (Map.Entry<Element, PositionalParamSpec.Builder> parameter : parameters.entrySet()) {
            CommandSpec commandSpec = getOrCreateCommandSpecForArg(parameter, commands);
            logger.fine("Building PositionalParamSpec for " + parameter);
            commandSpec.addPositional(parameter.getValue().build());
        }
        for (MixinInfo mixinInfo : mixinInfoList) {
            mixinInfo.addMixin();
        }

        logger.fine("Found annotations: " + annotations);
        //processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Found annotations: " + annotations);
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            //processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, annotatedElements + " is annotated with " + annotation);
            logger.finest(annotatedElements + " is annotated with " + annotation);
            // …
        }

        validateNoAnnotationsOnInterfaceField(roundEnv);
        validateInvalidCombination(roundEnv, Mixin.class, Option.class);
        validateInvalidCombination(roundEnv, Mixin.class, Parameters.class);
        validateInvalidCombination(roundEnv, Mixin.class, Unmatched.class);
        validateInvalidCombination(roundEnv, Mixin.class, Spec.class);
        validateInvalidCombination(roundEnv, Unmatched.class, Option.class);
        validateInvalidCombination(roundEnv, Unmatched.class, Parameters.class);
        validateInvalidCombination(roundEnv, Spec.class, Option.class);
        validateInvalidCombination(roundEnv, Spec.class, Parameters.class);
        validateInvalidCombination(roundEnv, Spec.class, Unmatched.class);
        validateInvalidCombination(roundEnv, Option.class, Parameters.class);

        // TODO
        //validateSpecFieldTypeIsCommandSpec(roundEnv);
        //validateOptionOrParametersIsNotFinalPrimitiveOrFinalString(roundEnv);
        //validateUnmatchedFieldTypeIsStringArrayOrListOfString(roundEnv);

        return handleCommands(commands, annotations, roundEnv);
    }

    private void validateNoAnnotationsOnInterfaceField(RoundEnvironment roundEnv) {
        validateNoAnnotationsOnInterfaceField(roundEnv.getElementsAnnotatedWith(Option.class));
        validateNoAnnotationsOnInterfaceField(roundEnv.getElementsAnnotatedWith(Parameters.class));
        validateNoAnnotationsOnInterfaceField(roundEnv.getElementsAnnotatedWith(Mixin.class));
        validateNoAnnotationsOnInterfaceField(roundEnv.getElementsAnnotatedWith(ParentCommand.class));
        validateNoAnnotationsOnInterfaceField(roundEnv.getElementsAnnotatedWith(Spec.class));
        validateNoAnnotationsOnInterfaceField(roundEnv.getElementsAnnotatedWith(Unmatched.class));
    }

    private void validateNoAnnotationsOnInterfaceField(Set<? extends Element> all) {
        for (Element element : all) {
            if (element.getKind() == ElementKind.FIELD &&
                    element.getEnclosingElement().getKind() == ElementKind.INTERFACE) {
                error(element, "Invalid picocli annotation on interface field %s.%s",
                        element.getEnclosingElement().toString(), element.getSimpleName());
            }
        }
    }

    private <T1 extends Annotation, T2 extends Annotation> void validateInvalidCombination(
            RoundEnvironment roundEnv, Class<T1> c1, Class<T2> c2) {
        for (Element element : roundEnv.getElementsAnnotatedWith(c1)) {
            if (element.getAnnotation(c2) != null) {
                error(element, "%s cannot have both @%s and @%s annotations",
                        element, c1.getCanonicalName(), c2.getCanonicalName());
            }
        }
    }

    /**
     * Subclasses must implement this method and do something with the
     * {@code CommandSpec} command model objects that were found during compilation.
     *
     * @param commands a map of annotated elements to their associated {@code CommandSpec}.
     *                 Note that the key set may contain classes that do not have a {@code @Command}
     *                 annotation but were added to the map because the class has fields
     *                 annotated with {@code Option} or {@code @Parameters}.
     * @param annotations the annotation types requested to be processed
     * @param roundEnv environment for information about the current and prior round
     * @return whether or not the set of annotation types are claimed by this processor.
     *           If {@code true} is returned, the annotation types are claimed and subsequent
     *           processors will not be asked to process them; if {@code false} is returned,
     *           the annotation types are unclaimed and subsequent processors may be asked
     *           to process them. A processor may always return the same boolean value or
     *           may vary the result based on chosen criteria.
     */
    protected abstract boolean handleCommands(Map<Element, CommandSpec> commands,
                                              Set<? extends TypeElement> annotations,
                                              RoundEnvironment roundEnv);

    private CommandSpec getOrCreateCommandSpecForArg(Map.Entry<Element, ?> argElement,
                                                     Map<Element, CommandSpec> commands) {
        Element key = argElement.getKey().getEnclosingElement();
        CommandSpec commandSpec = commands.get(key);
        if (commandSpec == null) {
            logger.fine("Element " + argElement.getKey() + " is enclosed by " + key + " which does not have a @Command annotation");
            commandSpec = CommandSpec.forAnnotatedObjectLenient(key);
            commands.put(key, commandSpec);
        }
        return commandSpec;
    }

    private void buildUnmatched(RoundEnvironment roundEnv, IFactory factory, Map<Element, IAnnotatedElement> unmatched) {
        Set<? extends Element> explicitUnmatched = roundEnv.getElementsAnnotatedWith(Unmatched.class);
        for (Element element : explicitUnmatched) {
            debugElement(element, "@Unmatched");
            if (element.getKind() == ElementKind.FIELD) {

            } else if (element.getKind() == ElementKind.METHOD) {

            } else if (element.getKind() == ElementKind.PARAMETER) {

            }
        }
    }

    private void buildSpecs(RoundEnvironment roundEnv, IFactory factory, Map<Element, IAnnotatedElement> specs) {
        Set<? extends Element> explicitSpecs = roundEnv.getElementsAnnotatedWith(Spec.class);
        for (Element element : explicitSpecs) {
            debugElement(element, "@Spec");
            if (element.getKind() == ElementKind.FIELD) {

            } else if (element.getKind() == ElementKind.METHOD) {

            } else if (element.getKind() == ElementKind.PARAMETER) {

            }
        }
    }

    private void buildMixins(RoundEnvironment roundEnv,
                             IFactory factory,
                             Map<Element, CommandSpec> mixinsDeclared,
                             List<MixinInfo> mixinInfoList,
                             Map<TypeMirror, List<CommandSpec>> commandTypes,
                             Map<Element, OptionSpec.Builder> options,
                             Map<Element, PositionalParamSpec.Builder> parameters) {
        Set<? extends Element> explicitMixins = roundEnv.getElementsAnnotatedWith(Mixin.class);
        for (Element element : explicitMixins) {
            if (element.asType().getKind() != TypeKind.DECLARED) {
                error(element, "@Mixin must have a declared type, not %s", element.asType());
                continue;
            }
            TypeElement type = (TypeElement) ((DeclaredType) element.asType()).asElement();
            CommandSpec mixin = buildCommand(type, factory, mixinsDeclared, commandTypes, options, parameters);

            logger.fine("Built mixin: " + mixin + " from " + element);
            if (EnumSet.of(ElementKind.FIELD, ElementKind.PARAMETER).contains(element.getKind())) {
                VariableElement variableElement = (VariableElement) element;
                String name = element.getAnnotation(Mixin.class).name();
                if (name.length() == 0) {
                    name = variableElement.getSimpleName().toString();
                }
                Element targetType = element.getEnclosingElement();
                CommandSpec mixee = buildCommand(targetType, factory, mixinsDeclared, commandTypes, options, parameters);
                mixinInfoList.add(new MixinInfo(mixee, name, mixin));
                logger.fine("Mixin name=" + name + ", target command=" + mixee.userObject());
            }
        }
    }

    private void buildParentCommands(RoundEnvironment roundEnv, IFactory factory, Map<Element, IAnnotatedElement> parentCommands) {
        Set<? extends Element> explicitParentCommands = roundEnv.getElementsAnnotatedWith(ParentCommand.class);
        for (Element element : explicitParentCommands) {
            debugElement(element, "@ParentCommand");
            if (element.getKind() == ElementKind.FIELD) {

            } else if (element.getKind() == ElementKind.METHOD) {

            } else if (element.getKind() == ElementKind.PARAMETER) {

            }
        }
    }

    private void buildOptions(RoundEnvironment roundEnv,
                              IFactory factory,
                              Map<Element, OptionSpec.Builder> options) {
        Set<? extends Element> explicitOptions = roundEnv.getElementsAnnotatedWith(Option.class);
        for (Element element : explicitOptions) {
            if (options.containsKey(element)) { continue; }
            TypedMember typedMember = extractTypedMember(element, "@Option");
            if (typedMember != null) {
                OptionSpec.Builder builder = OptionSpec.builder(typedMember, factory);
                builder.completionCandidates(extractCompletionCandidates(element, element.getAnnotationMirrors()));
                builder.converters(extractConverters(element, element.getAnnotationMirrors()));
                options.put(element, builder);
            }
        }
    }

    private void buildParameters(RoundEnvironment roundEnv,
                                 IFactory factory,
                                 Map<Element, PositionalParamSpec.Builder> parameters) {
        Set<? extends Element> explicitParameters = roundEnv.getElementsAnnotatedWith(Parameters.class);
        for (Element element : explicitParameters) {
            if (parameters.containsKey(element)) { continue; }
            TypedMember typedMember = extractTypedMember(element, "@Parameters");
            if (typedMember != null) {
                PositionalParamSpec.Builder builder = PositionalParamSpec.builder(typedMember, factory);
                builder.completionCandidates(extractCompletionCandidates(element, element.getAnnotationMirrors()));
                builder.converters(extractConverters(element, element.getAnnotationMirrors()));
                parameters.put(element, builder);
            }
        }
    }

    private TypedMember extractTypedMember(Element element, String annotation) {
        debugElement(element, annotation);
        if (element.getKind() == ElementKind.FIELD) { // || element.getKind() == ElementKind.PARAMETER) {
            return new TypedMember((VariableElement) element, -1);
        } else if (element.getKind() == ElementKind.METHOD) {
            return new TypedMember((ExecutableElement) element);
        }
        error(element, "Cannot only process %s annotations on fields, " +
                "methods and method parameters, not on %s", annotation, element.getKind());
        return null;
    }

    private void buildCommands(RoundEnvironment roundEnv,
                               IFactory factory,
                               Map<Element, CommandSpec> commands,
                               Map<TypeMirror, List<CommandSpec>> commandTypes,
                               Map<Element, OptionSpec.Builder> options,
                               Map<Element, PositionalParamSpec.Builder> parameters) {
        Set<? extends Element> explicitCommands = roundEnv.getElementsAnnotatedWith(Command.class);
        for (Element element : explicitCommands) {
            buildCommand(element, factory, commands, commandTypes, options, parameters);
        }
    }

    private CommandSpec buildCommand(Element element,
                                     IFactory factory,
                                     Map<Element, CommandSpec> commands,
                                     Map<TypeMirror, List<CommandSpec>> commandTypes,
                                     Map<Element, OptionSpec.Builder> options,
                                     Map<Element, PositionalParamSpec.Builder> parameters) {
        String commandClassName = element.asType().toString();
        debugElement(element, "@Command");

        CommandSpec result = commands.get(element);
        if (result != null) {
            return result;
        }
        result = CommandSpec.wrapWithoutInspection(element);
        result.withToString(commandClassName);
        commands.put(element, result);

        boolean hasCommandAnnotation = false;
        boolean mixinStandardHelpOptions = false;
        if (element.getKind() == ElementKind.CLASS) {
            TypeElement superClass = superClassFor((TypeElement) element);
            debugElement(superClass, "  super");

            TypeElement typeElement = (TypeElement) element;
            Stack<TypeElement> hierarchy = new Stack<TypeElement>();
            int count = 0;
            while (typeElement != null && count++ < 20) {
                logger.fine("Adding to type hierarchy: " + typeElement);
                hierarchy.add(typeElement);
                typeElement = superClassFor(typeElement);
            }
            while (!hierarchy.isEmpty()) {
                typeElement = hierarchy.pop();
                Command cmd = typeElement.getAnnotation(Command.class);
                if (cmd != null) {
                    updateCommandAttributes(result, cmd);

                    List<CommandSpec> subcommands = findSubcommands(typeElement.getAnnotationMirrors(),
                            factory, commands, commandTypes, options, parameters);
                    for (CommandSpec sub : subcommands) {
                        result.addSubcommand(sub.name(), sub);
                    }
                    hasCommandAnnotation = true;
                }
                List<CommandSpec> forSubclass = commandTypes.get(typeElement.asType());
                if (forSubclass == null) {
                    forSubclass = new ArrayList<CommandSpec>();
                    commandTypes.put(typeElement.asType(), forSubclass);
                }
                forSubclass.add(result);
                //hasCommandAnnotation |= initFromAnnotatedFields(instance, typeElement, result, factory); // TODO
                if (cmd != null) {
                    mixinStandardHelpOptions |= cmd.mixinStandardHelpOptions();
                }
            }
            result.mixinStandardHelpOptions(mixinStandardHelpOptions); //#377 Standard help options should be added last
        } else if (element.getKind() == ElementKind.METHOD) {
            ExecutableElement method = (ExecutableElement) element;
            debugMethod(method);

            Command cmd = method.getAnnotation(Command.class);
            updateCommandAttributes(result, cmd);
            result.setAddMethodSubcommands(false);
            result.withToString(commandClassName + "." + method.getSimpleName());

            // set command name to method name, unless @Command#name is set
            if (result.name().equals(COMMAND_DEFAULT_NAME)) {
                result.name(method.getSimpleName().toString());
            }

            Element cls = method.getEnclosingElement();
            if (cls.getAnnotation(Command.class) != null && cls.getAnnotation(Command.class).addMethodSubcommands()) {
                CommandSpec commandSpec = buildCommand(cls,
                        factory, commands, commandTypes, options, parameters);
                commandSpec.addSubcommand(result.name(), result);
            }
            hasCommandAnnotation = true;
            result.mixinStandardHelpOptions(method.getAnnotation(Command.class).mixinStandardHelpOptions());
            buildOptionsAndPositionalsFromMethodParameters(method, result, factory, options, parameters);
        }
        //result.updateArgSpecMessages(); // TODO resource bundle

        // TODO run validation logic
//            if (annotationsAreMandatory) { validateCommandSpec(result, hasCommandAnnotation, commandClassName); }
//            result.withToString(commandClassName).validate();
        logger.fine(String.format("CommandSpec[name=%s] built for %s", result.name(), element));
        return result;
    }

    private void updateCommandAttributes(CommandSpec result, Command cmd) {
        // null factory to prevent
        // javax.lang.model.type.MirroredTypeException: Attempt to access Class object for TypeMirror picocli.CommandLine.NoVersionProvider
        result.updateCommandAttributes(cmd, null);
        try {
            cmd.versionProvider();
        } catch (MirroredTypeException ex) {
            VersionProviderMetaData provider = new VersionProviderMetaData(ex.getTypeMirror());
            if (!provider.isDefault()) {
                result.versionProvider(provider);
            }
        }
        try {
            cmd.defaultValueProvider();
        } catch (MirroredTypeException ex) {
            DefaultValueProviderMetaData provider = new DefaultValueProviderMetaData(ex.getTypeMirror());
            if (!provider.isDefault()) {
                result.defaultValueProvider(provider);
            }
        }
    }

    private List<CommandSpec> findSubcommands(List<? extends AnnotationMirror> annotationMirrors,
                                              IFactory factory,
                                              Map<Element, CommandSpec> commands,
                                              Map<TypeMirror, List<CommandSpec>> commandTypes,
                                              Map<Element, OptionSpec.Builder> options,
                                              Map<Element, PositionalParamSpec.Builder> parameters) {
        List<CommandSpec> result = new ArrayList<CommandSpec>();
        for (AnnotationMirror am : annotationMirrors) {
            if (am.getAnnotationType().toString().equals(COMMAND_TYPE)) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                    if ("subcommands".equals(entry.getKey().getSimpleName().toString())) {
                        AnnotationValue list = entry.getValue();

                        @SuppressWarnings("unchecked")
                        List<AnnotationValue> typeMirrors = (List<AnnotationValue>) list.getValue();
                        registerSubcommands(typeMirrors, result, factory, commands, commandTypes, options, parameters);
                        break;
                    }
                }
            }
        }

        return result;
    }

    private void registerSubcommands(List<AnnotationValue> typeMirrors,
                                     List<CommandSpec> result,
                                     IFactory factory,
                                     Map<Element, CommandSpec> commands,
                                     Map<TypeMirror, List<CommandSpec>> commandTypes,
                                     Map<Element, OptionSpec.Builder> options,
                                     Map<Element, PositionalParamSpec.Builder> parameters) {

        for (AnnotationValue typeMirror : typeMirrors) {
            Element subcommandElement = processingEnv.getElementUtils().getTypeElement(
                    typeMirror.getValue().toString().replace('$', '.'));
            logger.fine("Processing subcommand: " + subcommandElement);

            if (isValidSubcommandHasNameAttribute(subcommandElement)) {
                CommandSpec commandSpec = buildCommand(subcommandElement,
                        factory, commands, commandTypes, options, parameters);
                result.add(commandSpec);
            }
        }
    }

    private boolean isValidSubcommandHasNameAttribute(Element subcommandElement) {
        Command annotation = subcommandElement.getAnnotation(Command.class);
        if (annotation == null) {
            error(subcommandElement, "Subcommand is missing @Command annotation with a name attribute");
            return false;
        } else if (COMMAND_DEFAULT_NAME.equals(annotation.name())) {
            error(subcommandElement, "Subcommand @Command annotation should have a name attribute");
            return false;
        }
        return true;
    }

    private void buildOptionsAndPositionalsFromMethodParameters(ExecutableElement method,
                                                                CommandSpec result,
                                                                IFactory factory,
                                                                Map<Element, OptionSpec.Builder> options,
                                                                Map<Element, PositionalParamSpec.Builder> parameters) {
        List<? extends VariableElement> params = method.getParameters();
        int position = -1;
        for (VariableElement variable : params) {
            boolean isOption = variable.getAnnotation(Option.class) != null;
            boolean isPositional = variable.getAnnotation(Parameters.class) != null;
            boolean isMixin = variable.getAnnotation(Mixin.class) != null;

            if (isOption && isPositional) {
                error(variable, "Method %s parameter %s should not have both @Option and @Parameters annotation", method.getSimpleName(), variable.getSimpleName());
            } else if ((isOption || isPositional) && isMixin) {
                error(variable, "Method %s parameter %s should not have a @Mixin annotation as well as an @Option or @Parameters annotation", method.getSimpleName(), variable.getSimpleName());
            }
            if (isOption) {
                TypedMember typedMember = new TypedMember(variable, -1);
                OptionSpec.Builder builder = OptionSpec.builder(typedMember, factory);

                builder.completionCandidates(extractCompletionCandidates(variable, variable.getAnnotationMirrors()));
                builder.converters(extractConverters(variable, variable.getAnnotationMirrors()));
                options.put(variable, builder);
            } else if (!isMixin) {
                position++;
                TypedMember typedMember = new TypedMember(variable, position);
                PositionalParamSpec.Builder builder = PositionalParamSpec.builder(typedMember, factory);
                builder.completionCandidates(extractCompletionCandidates(variable, variable.getAnnotationMirrors()));
                builder.converters(extractConverters(variable, variable.getAnnotationMirrors()));
                parameters.put(variable, builder);
            }
        }
    }

    private Iterable<String> extractCompletionCandidates(Element element, List<? extends AnnotationMirror> annotationMirrors) {
        for (AnnotationMirror mirror : annotationMirrors) {
            DeclaredType annotationType = mirror.getAnnotationType();
            if (isOption(annotationType) || isParameter(annotationType)) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
                for (ExecutableElement attribute : elementValues.keySet()) {
                    if ("completionCandidates".equals(attribute.getSimpleName().toString())) {
                        AnnotationValue typeMirror = elementValues.get(attribute);
                        return new CompletionCandidatesMetaData((TypeMirror) typeMirror);
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private TypeConverterMetaData[] extractConverters(Element element, List<? extends AnnotationMirror> annotationMirrors) {
        for (AnnotationMirror mirror : annotationMirrors) {
            DeclaredType annotationType = mirror.getAnnotationType();
            if (isOption(annotationType) || isParameter(annotationType)) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
                for (ExecutableElement attribute : elementValues.keySet()) {
                    if ("converter".equals(attribute.getSimpleName().toString())) {
                        AnnotationValue list = elementValues.get(attribute);
                        List<AnnotationValue> typeMirrors = (List<AnnotationValue>) list.getValue();
                        List<TypeConverterMetaData> result = new ArrayList<TypeConverterMetaData>();
                        for (AnnotationValue annotationValue : typeMirrors) {
                            result.add(new TypeConverterMetaData((TypeMirror) annotationValue));
                        }
                        return result.toArray(new TypeConverterMetaData[0]);
                    }
                }
            }
        }
        return new TypeConverterMetaData[0];
    }

    private boolean isOption(DeclaredType annotationType) {
        return Option.class.getName().equals(annotationType.toString());
    }

    private boolean isParameter(DeclaredType annotationType) {
        return Parameters.class.getName().equals(annotationType.toString());
    }

    private void debugMethod(ExecutableElement method) {
        logger.finest(format("  method: simpleName=%s, asType=%s, varargs=%s, returnType=%s, enclosingElement=%s, params=%s, typeParams=%s",
                method.getSimpleName(), method.asType(), method.isVarArgs(), method.getReturnType(), method.getEnclosingElement(), method.getParameters(), method.getTypeParameters()));
        for (VariableElement variable : method.getParameters()) {
            logger.finest(format("    variable: name=%s, annotationMirrors=%s, @Option=%s, @Parameters=%s",
                    variable.getSimpleName(), variable.getAnnotationMirrors(), variable.getAnnotation(
                            Option.class), variable.getAnnotation(Parameters.class)));
        }
    }

    private void debugElement(Element element, String s) {
        logger.finest(format(s + ": kind=%s, cls=%s, simpleName=%s, type=%s, typeKind=%s, enclosed=%s, enclosing=%s",
                element.getKind(), element.getClass().getName(), element.getSimpleName(), element.asType(),
                element.asType().getKind(), element.getEnclosedElements(), element.getEnclosingElement()));
        TypeMirror typeMirror = element.asType();
        if (element.getKind() == ENUM) {
            for (Element enclosed : element.getEnclosedElements()) {
                debugElement(enclosed, s + "  ");
            }
        } else {
            debugType(typeMirror, element, s + "  ");
        }
    }

    private void debugType(TypeMirror typeMirror, Element element, String indent) {
        if (indent.length() > 20) { return; }
        if (typeMirror.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) typeMirror;
            logger.finest(format("%stype=%s, asElement=%s, (elementKind=%s, elementClass=%s), typeArgs=%s, enclosing=%s",
                    indent, declaredType,
                    declaredType.asElement(), declaredType.asElement().getKind(), declaredType.asElement().getClass(),
                    declaredType.getTypeArguments(),
                    declaredType.getEnclosingType()));
            for (TypeMirror tm : declaredType.getTypeArguments()) {
                if (!tm.equals(typeMirror)) {
                    debugType(tm, element, indent + "  ");
                }
            }
            if (declaredType.asElement().getKind() == ENUM && !element.equals(declaredType.asElement())) {
                debugElement(declaredType.asElement(), indent + "  --> ");
            }
        } else if (typeMirror.getKind() == TypeKind.EXECUTABLE) {
            ExecutableType type = (ExecutableType) typeMirror;
            logger.finest(format("%stype=%s, typeArgs=%s, paramTypes=%s, returnType=%s",
                    indent, type, type.getTypeVariables(),
                    type.getParameterTypes(), type.getReturnType()));
            for (TypeMirror tm : type.getParameterTypes()) {
                if (!tm.equals(typeMirror)) {
                    debugType(tm, element, indent + "  ");
                }
            }
        } else {
            logger.finest(format("%s%s %s is of kind=%s", indent, typeMirror, element.getSimpleName(), typeMirror.getKind()));
        }
    }

    private static class MixinInfo {
        private final CommandSpec mixee;
        private final String name;
        private final CommandSpec mixin;

        MixinInfo(CommandSpec mixee, String name, CommandSpec mixin) {
            this.mixee = mixee;
            this.name = name;
            this.mixin = mixin;
        }

        void addMixin() {
            logger.fine(String.format("Adding mixin %s to %s", mixin.name(), mixee.name()));
            mixee.addMixin(name, mixin);
        }
    }

    static class CompileTimeTypeInfo implements ITypeInfo {
        private static Logger logger = Logger.getLogger(CompileTimeTypeInfo.class.getName());
        private static final EnumSet<TypeKind> PRIMITIVES = EnumSet.of(TypeKind.BYTE, TypeKind.BOOLEAN, TypeKind.CHAR,
                TypeKind.DOUBLE, TypeKind.FLOAT, TypeKind.INT, TypeKind.LONG, TypeKind.SHORT);

        final TypeMirror typeMirror;
        final List<? extends TypeMirror> auxTypeMirrors;
        final List<String> actualGenericTypeArguments;
        final TypeElement typeElement;
        final boolean isCollection;
        final boolean isMap;

        public CompileTimeTypeInfo(TypeMirror asType) {
            typeMirror = asType;

            // for non-multi-value types, the auxiliary type is a single-value list with the type
            List<? extends TypeMirror> aux = Arrays.asList(typeMirror);
            TypeElement tempTypeElement = null;
            boolean collection = false;
            boolean map = false;

            if (typeMirror.getKind() == TypeKind.DECLARED) {
                logger.finest("CompileTimeTypeInfo DECLARED typeMirror " + typeMirror);
                Element element = ((DeclaredType) typeMirror).asElement();
                if (element.getKind().isClass() || element.getKind().isInterface()) {
                    tempTypeElement = (TypeElement) element;
                    logger.finest("element is class or interface " + tempTypeElement);
                    map = find("java.util.Map", tempTypeElement);
                    collection = !map && find("java.util.Collection", tempTypeElement);
                }
                aux = ((DeclaredType) typeMirror).getTypeArguments();
                actualGenericTypeArguments = new ArrayList<String>();
                for (TypeMirror typeMirror : aux) {
                    actualGenericTypeArguments.add(typeMirror.toString());
                }
                logger.finest("aux (type args): " + aux);
                if (aux.isEmpty()) {
                    if (map || collection) {
                        aux = Arrays.asList(createStringTypeMirror(), createStringTypeMirror());
                        logger.finest("fixed aux (for multi type): " + aux);
                    } else {
                        aux = Arrays.asList(typeMirror);
                        logger.finest("fixed aux (for single type): " + aux);
                    }
                }
            } else if (typeMirror.getKind() == TypeKind.ARRAY) {
                aux = Arrays.asList(((ArrayType) typeMirror).getComponentType());
                actualGenericTypeArguments = Arrays.asList(aux.get(0).toString());
            } else {
                actualGenericTypeArguments = Collections.emptyList();
            }
            auxTypeMirrors = aux;
            typeElement = tempTypeElement;
            isCollection = collection;
            isMap = map;
        }

        private TypeMirror createStringTypeMirror() {
            TypeElement element = typeElement;
            while (element.getSuperclass().getKind() != TypeKind.NONE) {
                logger.finest("finding toString in " + element);

                element = (TypeElement) ((DeclaredType) element.getSuperclass()).asElement();
            }
            for (Element enclosed : typeElement.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.METHOD) {
                    ExecutableElement method = (ExecutableElement) enclosed;
                    if (method.getSimpleName().contentEquals("toString")) {
                        return method.getReturnType();
                    }
                }
            }
            throw new IllegalStateException("Cannot find toString method in Object");
        }
        private static boolean find(String interfaceName, TypeElement typeElement) {
            return find(interfaceName, typeElement, new HashSet<Element>());
        }
        private static boolean find(String interfaceName, TypeElement typeElement, Set<Element> visited) {
            if (visited.contains(typeElement)) { return false; }
            visited.add(typeElement);
            //logger.finest("trying to find " + interfaceName + " in " + typeElement);

            if (typeElement.getQualifiedName().contentEquals(interfaceName)) {
                return true;
            }
            for (TypeMirror implemented : typeElement.getInterfaces()) {
                if (find(interfaceName, (TypeElement) ((DeclaredType) implemented).asElement())) {
                    return true;
                }
            }
            while (typeElement.getSuperclass().getKind() != TypeKind.NONE) {
                typeElement = (TypeElement) ((DeclaredType) typeElement.getSuperclass()).asElement();
                if (find(interfaceName, typeElement)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<ITypeInfo> getAuxiliaryTypeInfos() {
            // for non-multi-value types, the auxiliary type is a single-value list with the type
            if (!isMultiValue()) {
                logger.fine("getAuxiliaryTypeInfos (non-multi) returning new list with this");
                return Arrays.<ITypeInfo>asList(this);
            }

            List<ITypeInfo> result = new ArrayList<ITypeInfo>();
            for (TypeMirror typeMirror : auxTypeMirrors) {
                result.add(new CompileTimeTypeInfo(typeMirror));
            }
            logger.fine("getAuxiliaryTypeInfos (multi) returning list " + result);
            return result;
        }

        @Override
        public List<String> getActualGenericTypeArguments() {
            return actualGenericTypeArguments;
        }

        @Override
        public boolean isBoolean() {
            TypeMirror type = auxTypeMirrors.get(0);
            return type.getKind() == TypeKind.BOOLEAN || "java.lang.Boolean".equals(type.toString());
        }

        @Override
        public boolean isMultiValue() {
            return isArray() || isCollection() || isMap();
        }

        @Override
        public boolean isArray() {
            return typeMirror.getKind() == TypeKind.ARRAY;
        }

        @Override
        public boolean isCollection() {
            return isCollection;
        }

        @Override
        public boolean isMap() {
            return isMap;
        }

        @Override
        public boolean isEnum() {
            TypeMirror type = auxTypeMirrors.get(0);
            return type.getKind() == TypeKind.DECLARED &&
                    ((DeclaredType) type).asElement().getKind() == ElementKind.ENUM;
        }

        @Override
        public List<String> getEnumConstantNames() {
            if (!isEnum()) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<String>();
            TypeMirror type = auxTypeMirrors.get(0);
            List<? extends Element> enclosed = ((DeclaredType) type).asElement().getEnclosedElements();
            for (Element element : enclosed) {
                if (element.getKind() == ElementKind.ENUM_CONSTANT) {
                    result.add(element.toString());
                }
            }
            return result;
        }

        @Override
        public String getClassName() {
            return typeElement == null ? typeMirror.toString() : typeElement.getQualifiedName().toString();
        }

        @Override
        public String getClassSimpleName() {
            return typeElement == null ? typeMirror.toString() : typeElement.getSimpleName().toString();
        }

        @Override
        public Class<?> getType() {
            return null;
        }

        @Override
        public Class<?>[] getAuxiliaryTypes() {
            return new Class[0];
        }

        @Override
        public String toString() {
            return String.format("CompileTimeTypeInfo(%s, aux=%s, collection=%s, map=%s)",
                    typeMirror, Arrays.toString(auxTypeMirrors.toArray()), isCollection, isMap);
        }
    }

    static class TypedMember implements IAnnotatedElement {
        final Element element;
        final String name;
        final ITypeInfo typeInfo;
        final boolean hasInitialValue;
        final int position;
        private CommandLine.Model.IGetter getter;
        private CommandLine.Model.ISetter setter;

        static boolean isAnnotated(Element e) {
            return false
                    || e.getAnnotation(Option.class) == null
                    || e.getAnnotation(Parameters.class) == null
                    || e.getAnnotation(ArgGroup.class) == null
                    || e.getAnnotation(Unmatched.class) == null
                    || e.getAnnotation(Mixin.class) == null
                    || e.getAnnotation(Spec.class) == null
                    || e.getAnnotation(ParentCommand.class) == null;
        }
        TypedMember(VariableElement variable, int position) {
            element = Assert.notNull(variable, "field");
            name = variable.getSimpleName().toString();
            hasInitialValue = variable.getConstantValue() != null;
            typeInfo = new CompileTimeTypeInfo(variable.asType());
            this.position = position;
            getter = new GetterSetterMetaData(element);
            setter = (GetterSetterMetaData) getter;
        }

        private TypedMember(ExecutableElement method) {
            element = Assert.notNull(method, "method");
            name = propertyName(method.getSimpleName().toString());
            position = -1;
            List<? extends TypeMirror> parameterTypes = ((ExecutableType) method.asType()).getParameterTypes();
            boolean isGetter = parameterTypes.isEmpty() && method.getReturnType().getKind() != TypeKind.VOID;
            boolean isSetter = !parameterTypes.isEmpty();
            if (isSetter == isGetter) { throw new CommandLine.InitializationException("Invalid method, must be either getter or setter: " + method); }
            if (isGetter) {
                hasInitialValue = true; // TODO
                typeInfo = new CompileTimeTypeInfo(method.getReturnType());
                //if (Proxy.isProxyClass(scope.getClass())) {
                //    CommandLine.Model.PicocliInvocationHandler handler = (CommandLine.Model.PicocliInvocationHandler) Proxy.getInvocationHandler(scope);
                //    CommandLine.Model.PicocliInvocationHandler.ProxyBinding binding = handler.new ProxyBinding(method);
                //    getter = binding; setter = binding;
                //    initializeInitialValue(method);
                //} else {
                //    //throw new IllegalArgumentException("Getter method but not a proxy: " + scope + ": " + method);
                //    CommandLine.Model.MethodBinding binding = new CommandLine.Model.MethodBinding(scope, method);
                //    getter = binding; setter = binding;
                //}
            } else {
                hasInitialValue = false;
                typeInfo = new CompileTimeTypeInfo(parameterTypes.get(0));
                //CommandLine.Model.MethodBinding binding = new CommandLine.Model.MethodBinding(scope, method);
                //getter = binding; setter = binding;
            }
            getter = new GetterSetterMetaData(element);
            setter = (GetterSetterMetaData) getter;
        }

        public Object userObject()      { return element; }
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) { return getAnnotation(annotationClass) != null; }
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) { return element.getAnnotation(annotationClass); }
        public String getName()         { return name; }
        public boolean isArgSpec()      { return isOption() || isParameter() || isMethodParameter(); }
        public boolean isOption()       { return isAnnotationPresent(Option.class); }
        public boolean isParameter()    { return isAnnotationPresent(Parameters.class); }
        public boolean isArgGroup()     { return isAnnotationPresent(ArgGroup.class); }
        public boolean isMixin()        { return isAnnotationPresent(Mixin.class); }
        public boolean isUnmatched()    { return isAnnotationPresent(Unmatched.class); }
        public boolean isInjectSpec()   { return isAnnotationPresent(Spec.class); }
        public boolean isMultiValue()   { return getTypeInfo().isMultiValue(); }
        public boolean isInteractive()  { return (isOption() && getAnnotation(Option.class).interactive()) || (isParameter() && getAnnotation(Parameters.class).interactive()); }
        public ITypeInfo getTypeInfo()  { return typeInfo; }
        public CommandLine.Model.IGetter getter()         { return getter; }
        public CommandLine.Model.ISetter setter()         { return setter; }
        public String toString() { return element.toString(); }
        public String getToString() {
            if (isMixin()) { return abbreviate("mixin from member " + toGenericString()); }
            return (element.getKind() + " ") + abbreviate(toGenericString());
        }
        String toGenericString() { return element.asType().toString() + element.getEnclosingElement() + "." + element.getSimpleName(); }
        public boolean hasInitialValue()    { return hasInitialValue; }
        public boolean isMethodParameter()  { return position >= 0; }
        public int getMethodParamPosition() { return position; }

        @Override
        public CommandLine.Model.IScope scope() {
            return null; // FIXME
        }

        public String getMixinName() {
            String annotationName = getAnnotation(Mixin.class).name();
            return empty(annotationName) ? getName() : annotationName;
        }

        static String propertyName(String methodName) {
            if (methodName.length() > 3 && (methodName.startsWith("get") || methodName.startsWith("set"))) { return decapitalize(methodName.substring(3)); }
            return decapitalize(methodName);
        }

        private static String decapitalize(String name) {
            if (name == null || name.length() == 0) { return name; }
            char[] chars = name.toCharArray();
            chars[0] = Character.toLowerCase(chars[0]);
            return new String(chars);
        }

        static String abbreviate(String text) {
            return text.replace("private ", "")
                    .replace("protected ", "")
                    .replace("public ", "")
                    .replace("java.lang.", "");
        }
        static boolean empty(String str) { return str == null || str.trim().length() == 0; }
    }

    /**
     * Obtains the super type element for a given type element.
     *
     * @param element The type element
     * @return The super type element or null if none exists
     */
    static TypeElement superClassFor(TypeElement element) {
        TypeMirror superclass = element.getSuperclass();
        if (superclass.getKind() == TypeKind.NONE) {
            return null;
        }
        logger.finest(format("Superclass of %s is %s (of kind %s)", element, superclass, superclass.getKind()));
        DeclaredType kind = (DeclaredType) superclass;
        return (TypeElement) kind.asElement();
    }

    void error(Element e, String msg, Object... args) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                format(msg, args),
                e);
    }

    static class NullFactory implements IFactory {
        @Override
        public <K> K create(Class<K> cls) throws Exception {
            return null;
        }
    }

}
