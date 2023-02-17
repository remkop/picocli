package picocli.codegen.annotation.processing;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IAnnotatedElement;
import picocli.CommandLine.Model.ITypeInfo;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Model.UnmatchedArgsBinding;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Unmatched;
import picocli.codegen.util.JulLogFormatter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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
import static java.util.Collections.disjoint;
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
 * the annotation processor will assign an {@link AnnotatedElementHolder}
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
    private static final String COMMAND_DEFAULT_NAME = CommandSpec.DEFAULT_COMMAND_NAME;
    private static Logger logger = Logger.getLogger(AbstractCommandSpecProcessor.class.getName());
    private static boolean loadBundlesDuringAnnotationProcessing;

    /** The ProcessingEnvironment set by the {@link #init(ProcessingEnvironment)} method. */
    protected ProcessingEnvironment processingEnv;

    private static final String COMMAND_TYPE = Command.class.getName().replace('$', '.');

    static ConsoleHandler handler = new ConsoleHandler();

    protected AbstractCommandSpecProcessor() {
        if (Boolean.getBoolean("jul.format")) {
            for (Handler h : Logger.getLogger("picocli.annotation.processing").getHandlers()) {
                h.setFormatter(new JulLogFormatter());
            }
        }
//        if (System.getProperty("java.util.logging.config.file") == null) {
//            for (Handler h : Logger.getLogger("picocli.annotation.processing").getHandlers()) {
//                Logger.getLogger("picocli.annotation.processing").removeHandler(h);
//            }
//            handler.setFormatter(new JulLogFormatter());
//            handler.setLevel(Level.ALL);
//            Logger.getLogger("picocli.annotation.processing").addHandler(handler);
//            Logger.getLogger("picocli.annotation.processing").setLevel(Level.ALL);
//        }
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

    /**
     * Returns the max supported source version.
     * Returns {@link SourceVersion#latest()} by default,
     * subclasses may override or may use the {@link SupportedSourceVersion} annotation.
     * @return the max supported source version
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        SupportedSourceVersion ssv = this.getClass().getAnnotation(SupportedSourceVersion.class);
        SourceVersion sv = null;
        if (ssv == null) {
            return SourceVersion.latest();
        } else {
            return ssv.value();
        }
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }
    // inherit doc
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        logger.fine("entered process, processingOver=" + roundEnv.processingOver());

        try {
            return tryProcess(annotations, roundEnv);
        } catch (Exception e) {
            // Generators are supposed to do their own error handling, but let's be paranoid.
            // We don't allow exceptions of any kind to propagate to the compiler
            fatalError(stacktrace(e));
            return false;
        }
    }
    /**
     * During annotation processing, resource bundles may not be available on the
     * classpath and thereby cause failures.
     * For that reason, by default, resource bundles are not loaded during annotation processing.
     * This method allows for enabling loading of resource bundles during annotation processing.
     *
     * @since 4.8.0
     * @param loadBundles true if bundles should be loaded, false (default) if bundles should not be loaded
     */
    public static final void setLoadResourceBundles(boolean loadBundles) {
        loadBundlesDuringAnnotationProcessing = loadBundles;
    }

    private static String stacktrace(Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private boolean tryProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Model.Messages.setLoadBundles(loadBundlesDuringAnnotationProcessing);
        new AnnotationValidator(processingEnv).validateAnnotations(roundEnv);

        Context context = new Context();
        buildCommands(roundEnv, context);
        buildMixins(roundEnv, context);
        buildArgGroups(roundEnv, context);
        buildOptions(roundEnv, context);
        buildParameters(roundEnv, context);
        buildParentCommands(roundEnv, context);
        buildSpecs(roundEnv, context);
        buildUnmatched(roundEnv, context);

        context.connectModel(this);
        debugFoundAnnotations(annotations, roundEnv);

        return handleCommands(context.commands, annotations, roundEnv);
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

    private void buildCommands(RoundEnvironment roundEnv, Context context) {
        logger.fine("Building commands...");
        Set<? extends Element> explicitCommands = roundEnv.getElementsAnnotatedWith(Command.class);

        for (Element element : explicitCommands) {
            buildCommand(element, context, roundEnv);
        }
    }

    private CommandSpec buildCommand(Element element, final Context context, final RoundEnvironment roundEnv) {
        return buildCommand(true, element, context, roundEnv);
    }
    @SuppressWarnings("deprecation")
    private CommandSpec buildCommand(boolean reuseExisting, Element element, final Context context, final RoundEnvironment roundEnv) {
        debugElement(element, "@Command");

        CommandSpec result = null;
        if (reuseExisting) { // #1440 subcommands should create separate instances
            result = context.commands.get(element);
            if (result != null) {
                return result;
            }
        }
        result = CommandSpec.wrapWithoutInspection(element);
        result.interpolateVariables(false);
        context.commands.put(element, result);

        element.accept(new SimpleElementVisitor6<Void, CommandSpec>(){
            @Override
            public Void visitType(TypeElement e, CommandSpec commandSpec) {
                updateCommandSpecFromTypeElement(e, context, commandSpec, roundEnv);

                List<? extends Element> enclosedElements = e.getEnclosedElements();
                processEnclosedElements(context, roundEnv, enclosedElements);
                return null;
            }

            @Override
            public Void visitExecutable(ExecutableElement e, CommandSpec commandSpec) {
                updateCommandFromMethodElement(e, context, commandSpec, roundEnv);

                List<? extends Element> enclosedElements = e.getEnclosedElements();
                processEnclosedElements(context, roundEnv, enclosedElements);
                return null;
            }
        }, result);

        logger.fine(String.format("CommandSpec[name=%s] built for %s", result.name(), element));
        return result;
    }

    private void updateCommandSpecFromTypeElement(TypeElement typeElement,
                                                  Context context,
                                                  CommandSpec result,
                                                  RoundEnvironment roundEnv) {
        TypeElement superClass = superClassFor(typeElement);
        debugElement(superClass, "  super");
        result.withToString(typeElement.asType().toString());

        Stack<TypeElement> hierarchy = buildTypeHierarchy(typeElement);
        while (!hierarchy.isEmpty()) {
            typeElement = hierarchy.pop();
            updateCommandSpecFromCommandAnnotation(result, typeElement, context, roundEnv);
            context.registerCommandType(result, typeElement); // FIXME: unnecessary?
        }
    }

    private void updateCommandFromMethodElement(ExecutableElement method, Context context, CommandSpec result, RoundEnvironment roundEnv) {
        debugMethod(method);
        result.withToString(method.getEnclosingElement().asType().toString() + "." + method.getSimpleName());

        updateCommandSpecFromCommandAnnotation(result, method, context, roundEnv);
        result.setAddMethodSubcommands(false); // must reset: true by default in the @Command annotation

        // set command name to method name, unless @Command#name is set
        if (result.name().equals(COMMAND_DEFAULT_NAME)) {
            result.name(method.getSimpleName().toString());
        }

        // add this commandSpec as a subcommand to its parent
        if (isSubcommand(method, roundEnv)) {
            CommandSpec commandSpec = buildCommand(method.getEnclosingElement(), context, roundEnv);
            commandSpec.addSubcommand(result.name(), result);
        }
        buildOptionsAndPositionalsFromMethodParameters(method, result, context);
    }

    private boolean isSubcommand(ExecutableElement method, RoundEnvironment roundEnv) {
        Element typeElement = method.getEnclosingElement();
        Command cmd = typeElement.getAnnotation(Command.class);

        if (cmd != null) {
            return cmd.addMethodSubcommands();
        }

        List<? extends Element> elements = typeElement.getEnclosedElements();

        // The class is a Command if it has any fields or methods annotated with the below:
        return !disjoint(roundEnv.getElementsAnnotatedWith(Option.class), elements)
                || !disjoint(roundEnv.getElementsAnnotatedWith(Parameters.class), elements)
                || !disjoint(roundEnv.getElementsAnnotatedWith(Mixin.class), elements)
                || !disjoint(roundEnv.getElementsAnnotatedWith(ArgGroup.class), elements)
                || !disjoint(roundEnv.getElementsAnnotatedWith(Unmatched.class), elements)
                || !disjoint(roundEnv.getElementsAnnotatedWith(Spec.class), elements);
    }

    private Stack<TypeElement> buildTypeHierarchy(TypeElement typeElement) {
        Stack<TypeElement> hierarchy = new Stack<TypeElement>();
        int count = 0;
        while (typeElement != null && count++ < 20) {
            logger.fine("Adding to type hierarchy: " + typeElement);
            hierarchy.add(typeElement);
            typeElement = superClassFor(typeElement);
        }
        return hierarchy;
    }

    private void updateCommandSpecFromCommandAnnotation(CommandSpec result,
                                                        Element element,
                                                        Context context,
                                                        RoundEnvironment roundEnv) {
        Command cmd = element.getAnnotation(Command.class);
        if (cmd != null) {
            updateCommandAttributes(result, cmd);

            List<CommandSpec> subcommands = findSubcommands(element.getAnnotationMirrors(), context, roundEnv);
            for (CommandSpec sub : subcommands) {
                result.addSubcommand(sub.name(), sub);
            }
            if (cmd.mixinStandardHelpOptions()) {
                context.commandsRequestingStandardHelpOptions.add(result);
            }
        }
    }

    private void updateCommandAttributes(CommandSpec result, Command cmd) {
        // null factory to prevent
        // javax.lang.model.type.MirroredTypeException: Attempt to access Class object for TypeMirror picocli.CommandLine.NoVersionProvider
        result.updateCommandAttributes(cmd, null);
        VersionProviderMetaData.initVersionProvider(result, cmd);
        DefaultValueProviderMetaData.initDefaultValueProvider(result, cmd);
    }

    private List<CommandSpec> findSubcommands(List<? extends AnnotationMirror> annotationMirrors,
                                              Context context,
                                              RoundEnvironment roundEnv) {
        List<CommandSpec> result = new ArrayList<CommandSpec>();
        for (AnnotationMirror am : annotationMirrors) {
            if (am.getAnnotationType().toString().equals(COMMAND_TYPE)) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
                    if ("subcommands".equals(entry.getKey().getSimpleName().toString())) {
                        AnnotationValue list = entry.getValue();

                        @SuppressWarnings("unchecked")
                        List<AnnotationValue> typeMirrors = (List<AnnotationValue>) list.getValue();
                        registerSubcommands(typeMirrors, result, context, roundEnv);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private void registerSubcommands(List<AnnotationValue> typeMirrors,
                                     List<CommandSpec> result,
                                     Context context,
                                     RoundEnvironment roundEnv) {

        for (AnnotationValue typeMirror : typeMirrors) {
            Element subcommandElement = processingEnv.getElementUtils().getTypeElement(
                    typeMirror.getValue().toString().replace('$', '.'));
            logger.fine("Processing subcommand: " + subcommandElement);

            if (isValidSubcommandHasNameAttribute(subcommandElement)) {
                CommandSpec commandSpec = buildCommand(false, subcommandElement, context, roundEnv);
                result.add(commandSpec);
            }
        }
    }

    private void processEnclosedElements(Context context, RoundEnvironment roundEnv, List<? extends Element> enclosedElements) {
        for (Element enclosed : enclosedElements) {
            if (enclosed.getAnnotation(Command.class) != null) {
                buildCommand(enclosed, context, roundEnv);
            }
            if (enclosed.getAnnotation(ArgGroup.class) != null) {
                buildArgGroup(enclosed, context, roundEnv);
            }
            if (enclosed.getAnnotation(Mixin.class) != null) {
                buildMixin(enclosed, roundEnv, context);
            }
            if (enclosed.getAnnotation(Option.class) != null) {
                buildOption(enclosed, context);
            }
            if (enclosed.getAnnotation(Parameters.class) != null) {
                buildParameter(enclosed, context);
            }
            if (enclosed.getAnnotation(Unmatched.class) != null) {
                buildUnmatched(enclosed, context);
            }
            if (enclosed.getAnnotation(Spec.class) != null) {
                buildSpec(enclosed, context);
            }
            if (enclosed.getAnnotation(ParentCommand.class) != null) {
                buildParentCommand(enclosed, context);
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

    private void buildMixins(RoundEnvironment roundEnv, Context context) {
        logger.fine("Building mixins...");
        Set<? extends Element> explicitMixins = roundEnv.getElementsAnnotatedWith(Mixin.class);
        for (Element element : explicitMixins) {
            buildMixin(element, roundEnv, context);
        }
    }

    private void buildMixin(Element element, RoundEnvironment roundEnv, Context context) {
        debugElement(element, "@Mixin");
        if (element.asType().getKind() != TypeKind.DECLARED) {
            error(element, "@Mixin must have a declared type, not %s", element.asType());
            return;
        }
        TypeElement type = (TypeElement) ((DeclaredType) element.asType()).asElement();
        CommandSpec mixin = buildCommand(type, context, roundEnv);

        logger.fine("Built mixin: " + mixin + " from " + element);
        if (EnumSet.of(ElementKind.FIELD, ElementKind.PARAMETER).contains(element.getKind())) {
            VariableElement variableElement = (VariableElement) element;
            MixinInfo mixinInfo = new MixinInfo(variableElement, mixin);

            CommandSpec mixee = buildCommand(mixinInfo.enclosingElement(), context, roundEnv);
            Set<MixinInfo> mixinInfos = context.mixinInfoMap.get(mixee);
            if (mixinInfos == null) {
                mixinInfos = new HashSet<MixinInfo>(2);
                context.mixinInfoMap.put(mixee, mixinInfos);
            }
            mixinInfos.add(mixinInfo);
            logger.fine("Mixin name=" + mixinInfo.mixinName() + ", target command=" + mixee.userObject());
        }
    }

    private void buildArgGroups(RoundEnvironment roundEnv, Context context) {
        logger.fine("Building argGroups...");
        Set<? extends Element> explicitArgGroups = roundEnv.getElementsAnnotatedWith(ArgGroup.class);
        for (Element element : explicitArgGroups) {
            buildArgGroup(element, context, roundEnv);
        }
    }

    private void buildArgGroup(Element element, Context context, RoundEnvironment roundEnv) {
        debugElement(element, "@ArgGroup");
        TypeMirror elementType = element.asType();
        if (elementType.getKind() != TypeKind.DECLARED && elementType.getKind() != TypeKind.ARRAY) {
            error(element, "@ArgGroup must have a declared or array type, not %s", elementType);
            return;
        }
        @SuppressWarnings("deprecation") // SimpleElementVisitor6 is deprecated in Java 9
        ArgGroupSpec.Builder builder = element.accept(new SimpleElementVisitor6<ArgGroupSpec.Builder, Void>(null) {
            @Override public ArgGroupSpec.Builder visitVariable(VariableElement e, Void aVoid) {
                return ArgGroupSpec.builder(new TypedMember(e, -1));
            }
            @Override public ArgGroupSpec.Builder visitExecutable(ExecutableElement e, Void aVoid) {
                return ArgGroupSpec.builder(new TypedMember(e, AbstractCommandSpecProcessor.this));
            }
        }, null);
        if (builder == null) {
            error(element, "Only methods or variables can be annotated with @ArgGroup, not %s", element);
        } else {
            builder.updateArgGroupAttributes(element.getAnnotation(ArgGroup.class));
            context.argGroupElements.put(element, builder);

            DeclaredType declaredType = (elementType.getKind() == TypeKind.ARRAY)
                    ? (DeclaredType) ((ArrayType) elementType).getComponentType()
                    : (DeclaredType) elementType;

            TypeElement typeElement = (TypeElement) declaredType.asElement();
            processEnclosedElements(context, roundEnv, typeElement.getEnclosedElements());
        }
    }

    private void buildOptions(RoundEnvironment roundEnv, Context context) {
        logger.fine("Building options...");
        Set<? extends Element> explicitOptions = roundEnv.getElementsAnnotatedWith(Option.class);
        for (Element element : explicitOptions) {
            buildOption(element, context);
        }
    }

    private void buildOption(Element element, Context context) {
        if (context.options.containsKey(element)) {
            return;
        }
        TypedMember typedMember = extractTypedMember(element, "@Option");
        if (typedMember != null) {
            OptionSpec.Builder builder = OptionSpec.builder(typedMember, context.factory);
            builder.completionCandidates(CompletionCandidatesMetaData.extract(element));
            builder.converters(TypeConverterMetaData.extract(element));
            builder.parameterConsumer(ParameterConsumerMetaData.extract(element));
            context.options.put(element, builder);
        }
    }

    private void buildParameters(RoundEnvironment roundEnv, Context context) {
        logger.fine("Building parameters...");
        Set<? extends Element> explicitParameters = roundEnv.getElementsAnnotatedWith(Parameters.class);
        for (Element element : explicitParameters) {
            buildParameter(element, context);
        }
    }

    private void buildParameter(Element element, Context context) {
        if (context.parameters.containsKey(element)) {
            return;
        }
        TypedMember typedMember = extractTypedMember(element, "@Parameters");
        if (typedMember != null) {
            PositionalParamSpec.Builder builder = PositionalParamSpec.builder(typedMember, context.factory);
            builder.completionCandidates(CompletionCandidatesMetaData.extract(element));
            builder.converters(TypeConverterMetaData.extract(element));
            builder.parameterConsumer(ParameterConsumerMetaData.extract(element));
            context.parameters.put(element, builder);
        }
    }

    private TypedMember extractTypedMember(Element element, String annotation) {
        debugElement(element, annotation);
        if (element.getKind() == ElementKind.FIELD) { // || element.getKind() == ElementKind.PARAMETER) {
            return new TypedMember((VariableElement) element, -1);
        } else if (element.getKind() == ElementKind.METHOD) {
            return new TypedMember((ExecutableElement) element, AbstractCommandSpecProcessor.this);
        }
        error(element, "Can only process %s annotations on fields, " +
                "methods and @Command-annotated method parameters, not on %s", annotation, element.getKind());
        return null;
    }

    private void buildOptionsAndPositionalsFromMethodParameters(ExecutableElement method,
                                                                CommandSpec result,
                                                                Context context) {
        List<? extends VariableElement> params = method.getParameters();
        int position = -1;
        for (VariableElement variable : params) {
            boolean isOption     = variable.getAnnotation(Option.class) != null;
            boolean isPositional = variable.getAnnotation(Parameters.class) != null;
            boolean isMixin      = variable.getAnnotation(Mixin.class) != null;
            boolean isArgGroup   = variable.getAnnotation(ArgGroup.class) != null;

            if (isOption && isPositional) {
                error(variable, "Method %s parameter %s should not have both @Option and @Parameters annotation", method.getSimpleName(), variable.getSimpleName());
            } else if ((isOption || isPositional) && isMixin) {
                error(variable, "Method %s parameter %s should not have a @Mixin annotation as well as an @Option or @Parameters annotation", method.getSimpleName(), variable.getSimpleName());
            } else if ((isOption || isPositional || isMixin) && isArgGroup) {
                error(variable, "Method %s parameter %s should not have a @ArgGroup annotation as well as an @Option, @Parameters or @Mixin annotation", method.getSimpleName(), variable.getSimpleName());
            }
            if (isOption) {
                TypedMember typedMember = new TypedMember(variable, -1);
                OptionSpec.Builder builder = OptionSpec.builder(typedMember, context.factory);

                builder.completionCandidates(CompletionCandidatesMetaData.extract(variable));
                builder.parameterConsumer(ParameterConsumerMetaData.extract(variable));
                builder.converters(TypeConverterMetaData.extract(variable));
                context.options.put(variable, builder);
            } else if (isArgGroup) {
                TypedMember typedMember = new TypedMember(variable, -1);
                ArgGroupSpec.Builder builder = ArgGroupSpec.builder(typedMember);
                builder.updateArgGroupAttributes(variable.getAnnotation(ArgGroup.class));
                context.argGroupElements.put(variable, builder);

            } else if (!isMixin) { // params without any annotation are also positional
                position++;
                TypedMember typedMember = new TypedMember(variable, position);
                PositionalParamSpec.Builder builder = PositionalParamSpec.builder(typedMember, context.factory);
                builder.completionCandidates(CompletionCandidatesMetaData.extract(variable));
                builder.parameterConsumer(ParameterConsumerMetaData.extract(variable));
                builder.converters(TypeConverterMetaData.extract(variable));
                context.parameters.put(variable, builder);
            }
        }
    }

    /**
     * Obtains the super type element for a given type element.
     *
     * @param element The type element
     * @return The super type element or null if none exists
     */
    private static TypeElement superClassFor(TypeElement element) {
        TypeMirror superclass = element.getSuperclass();
        if (superclass.getKind() == TypeKind.NONE) {
            return null;
        }
        logger.finest(format("Superclass of %s is %s (of kind %s)", element, superclass, superclass.getKind()));
        DeclaredType kind = (DeclaredType) superclass;
        return (TypeElement) kind.asElement();
    }

    private void buildUnmatched(RoundEnvironment roundEnv, Context context) {
        logger.fine("Building unmatched...");
        Set<? extends Element> explicitUnmatched = roundEnv.getElementsAnnotatedWith(Unmatched.class);
        for (Element element : explicitUnmatched) {
            buildUnmatched(element, context);
        }
    }

    private void buildUnmatched(Element element, Context context) {
        debugElement(element, "@Unmatched");
        IAnnotatedElement specElement = buildTypedMember(element);
        if (specElement == null) {
            error(element, "Only methods or variables can be annotated with @Unmatched, not %s", element);
        } else {
            context.unmatchedElements.put(element, specElement);
        }
    }

    private void buildSpecs(RoundEnvironment roundEnv, Context context) {
        logger.fine("Building specs...");
        Set<? extends Element> explicitSpecs = roundEnv.getElementsAnnotatedWith(Spec.class);
        for (Element element : explicitSpecs) {
            buildSpec(element, context);
        }
    }

    private void buildSpec(Element element, Context context) {
        debugElement(element, "@Spec");
        IAnnotatedElement specElement = buildTypedMember(element);
        if (specElement == null) {
            error(element, "Only methods or variables can be annotated with @Spec, not %s", element);
        } else {
            context.specElements.put(element, specElement);
        }
    }

    private void buildParentCommands(RoundEnvironment roundEnv, Context context) {
        logger.fine("Building parentCommands...");
        Set<? extends Element> explicitParentCommands = roundEnv.getElementsAnnotatedWith(ParentCommand.class);
        for (Element element : explicitParentCommands) {
            buildParentCommand(element, context);
        }
    }

    private void buildParentCommand(Element element, Context context) {
        debugElement(element, "@ParentCommand");
        IAnnotatedElement parentCommandElement = buildTypedMember(element);
        if (parentCommandElement == null) {
            error(element, "Only methods or variables can be annotated with @ParentCommand, not %s", element);
        } else {
            context.parentCommandElements.put(element, parentCommandElement);
        }
    }

    @SuppressWarnings("deprecation") // SimpleElementVisitor6 is deprecated in Java 9
    private IAnnotatedElement buildTypedMember(Element element) {
        return element.accept(new SimpleElementVisitor6<TypedMember, Void>(null) {
            @Override
            public TypedMember visitVariable(VariableElement e, Void aVoid) {
                return new TypedMember(e, -1);
            }

            @Override
            public TypedMember visitExecutable(ExecutableElement e, Void aVoid) {
                return new TypedMember(e, AbstractCommandSpecProcessor.this);
            }
        }, null);
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
        if (element == null) { return; }
        logElementDetails(element, s);
    }

    private void logElementDetails(Element element, String s) {
        logger.finest(format(s + ": kind=%s, cls=%s, simpleName=%s, type=%s, typeKind=%s, enclosed=%s, enclosing=%s",
                element.getKind(), element.getClass().getName(), element.getSimpleName(), element.asType(),
                element.asType().getKind(), element.getEnclosedElements(), element.getEnclosingElement()));
        TypeMirror typeMirror = element.asType();
        if (element.getKind() == ENUM) {
            for (Element enclosed : element.getEnclosedElements()) {
                logElementDetails(enclosed, s + "  ");
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
                logElementDetails(declaredType.asElement(), indent + "  --> ");
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

    private void debugFoundAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        logger.fine("Found annotations: " + annotations);
        //processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Found annotations: " + annotations);
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            //processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, annotatedElements + " is annotated with " + annotation);
            logger.finest(annotatedElements + " is annotated with " + annotation);
            // …
        }
    }

    /**
     * Prints a compile-time NOTE message.
     * @param msg the info message
     */
    protected void logInfo(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, getClass().getName() + " " + msg);
    }

    /**
     * Prints a compile-time error message for the specified element.
     * @param element the problematic element
     * @param msg the error message with optional format specifiers
     * @param args the arguments to use to call {@code String.format} on the error message
     */
    protected void error(Element element, String msg, Object... args) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                format(msg, args),
                element);
    }

    /**
     * Prints a compile-time warning message for the specified element.
     * @param element the problematic element, may be {@code null}
     * @param msg the warning message with optional format specifiers
     * @param args the arguments to use to call {@code String.format} on the warning message
     */
    protected void warn(Element element, String msg, Object... args) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                format(msg, args),
                element);
    }

    /**
     * Prints a compile-time error message prefixed with "FATAL ERROR".
     * @param msg the error message with optional format specifiers
     */
    protected void fatalError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
    }

    static class Context {
        IFactory factory = null; //new NullFactory();
        Map<Element, CommandSpec> commands = new LinkedHashMap<Element, CommandSpec>();
        Map<TypeMirror, List<CommandSpec>> commandTypes = new LinkedHashMap<TypeMirror, List<CommandSpec>>();
        Map<Element, OptionSpec.Builder> options = new LinkedHashMap<Element, OptionSpec.Builder>();
        Map<Element, PositionalParamSpec.Builder> parameters = new LinkedHashMap<Element, PositionalParamSpec.Builder>();
        Map<Element, ArgGroupSpec.Builder> argGroupElements = new LinkedHashMap<Element, ArgGroupSpec.Builder>();
        Map<CommandSpec, Set<MixinInfo>> mixinInfoMap = new IdentityHashMap<CommandSpec, Set<MixinInfo>>();
        Map<Element, IAnnotatedElement> parentCommandElements = new LinkedHashMap<Element, IAnnotatedElement>();
        Map<Element, IAnnotatedElement> specElements = new LinkedHashMap<Element, IAnnotatedElement>();
        Map<Element, IAnnotatedElement> unmatchedElements = new LinkedHashMap<Element, IAnnotatedElement>();
        Set<CommandSpec> commandsRequestingStandardHelpOptions = new LinkedHashSet<CommandSpec>();

        private void connectModel(AbstractCommandSpecProcessor proc) {
            logger.fine("---------------------------");
            logger.fine("Known commands...");
            for (Map.Entry<Element, CommandSpec> cmd : commands.entrySet()) {
                logger.fine(String.format("%s has CommandSpec[name=%s]", cmd.getKey(), cmd.getValue().name()));
            }
            logger.fine("Known mixins...");
            for (Map.Entry<CommandSpec, Set<MixinInfo>> mixinEntry : mixinInfoMap.entrySet()) {
                logger.fine(String.format("mixins for %s:", mixinEntry.getKey().userObject()));
                for (MixinInfo mixinInfo : mixinEntry.getValue()) {
                    logger.fine(String.format("mixin name=%s userObj=%s:", mixinInfo.mixinName(), mixinInfo.mixin().userObject()));
                }
            }

            for (Map.Entry<Element, OptionSpec.Builder> option : options.entrySet()) {
                ArgGroupSpec.Builder group = argGroupElements.get(option.getKey().getEnclosingElement());
                if (group != null) {
                    logger.fine("Building OptionSpec for " + option + " in arg group " + group);
                    group.addArg(option.getValue().build());
                } else {
                    CommandSpec commandSpec = getOrCreateCommandSpecForArg(option.getKey(), commands);
                    logger.fine("Building OptionSpec for " + option + " in spec " + commandSpec);
                    commandSpec.addOption(option.getValue().build());
                }
            }
            for (Map.Entry<Element, PositionalParamSpec.Builder> parameter : parameters.entrySet()) {
                ArgGroupSpec.Builder group = argGroupElements.get(parameter.getKey().getEnclosingElement());
                if (group != null) {
                    logger.fine("Building PositionalParamSpec for " + parameter + " in arg group " + group);
                    group.addArg(parameter.getValue().build());
                } else {
                    CommandSpec commandSpec = getOrCreateCommandSpecForArg(parameter.getKey(), commands);
                    logger.fine("Building PositionalParamSpec for " + parameter);
                    commandSpec.addPositional(parameter.getValue().build());
                }
            }
            if (connectArgGroups(proc)) {
                return;
            }

            // @Spec
            for (Map.Entry<Element, IAnnotatedElement> entry : specElements.entrySet()) {
                CommandSpec commandSpec1 = commands.get(entry.getKey().getEnclosingElement());
                if (commandSpec1 != null) {
                    logger.fine("Adding " + entry + " to commandSpec " + commandSpec1);
                    commandSpec1.addSpecElement(entry.getValue());
                } else {
                    Element enclosingElement = entry.getKey().getEnclosingElement();
                    if (enclosingElement.getKind() == ElementKind.CLASS || enclosingElement.getKind() == ENUM) {
                        TypeMirror typeMirror = enclosingElement.asType();
                        TypeElement typeElement = (TypeElement) ((DeclaredType) typeMirror).asElement();
                        List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
                        boolean valid = false;
                        for (TypeMirror interf : interfaces) {
                            if (interf.toString().equals("picocli.CommandLine.IVersionProvider")) {
                                valid = true;
                            }
                        }
                        if (!valid) {
                            proc.error(entry.getKey(), "@Spec must be enclosed in a @Command, or in a class that implements IVersionProvider but was %s: %s", entry.getKey().getEnclosingElement(), entry.getKey().getEnclosingElement().getSimpleName());
                        }
                    } else {
                        proc.error(entry.getKey(), "@Spec must be enclosed in a @Command, but was %s: %s", entry.getKey().getEnclosingElement(), entry.getKey().getEnclosingElement().getSimpleName());
                    }
                }
            }
            for (Map.Entry<Element, IAnnotatedElement> entry : parentCommandElements.entrySet()) {
                CommandSpec commandSpec1 = commands.get(entry.getKey().getEnclosingElement());
                if (commandSpec1 != null) {
                    logger.fine("Adding " + entry + " to commandSpec " + commandSpec1);
                    commandSpec1.addParentCommandElement(entry.getValue());
                } else {
                    proc.error(entry.getKey(), "@ParentCommand must be enclosed in a @Command, but was %s: %s", entry.getKey().getEnclosingElement(), entry.getKey().getEnclosingElement().getSimpleName());
                }
            }
            for (Map.Entry<Element, IAnnotatedElement> entry : unmatchedElements.entrySet()) {
                CommandSpec commandSpec1 = commands.get(entry.getKey().getEnclosingElement());
                if (commandSpec1 != null) {
                    logger.fine("Adding " + entry + " to commandSpec " + commandSpec1);
                    IAnnotatedElement annotatedElement = entry.getValue();
                    if (annotatedElement.getTypeInfo().isArray() || annotatedElement.getTypeInfo().isCollection()) {
                        UnmatchedArgsBinding unmatchedArgsBinding = annotatedElement.getTypeInfo().isArray()
                                ? UnmatchedArgsBinding.forStringArrayConsumer(annotatedElement.setter())
                                : UnmatchedArgsBinding.forStringCollectionSupplier(annotatedElement.getter());
                        commandSpec1.addUnmatchedArgsBinding(unmatchedArgsBinding);
                    } else {
                        proc.error(entry.getKey(), "@Unmatched must be of type String[] or List<String> but was: %s", annotatedElement.getTypeInfo().getClassName());
                    }
                } else {
                    proc.error(entry.getKey(), "@Unmatched must be enclosed in a @Command, but was %s: %s", entry.getKey().getEnclosingElement(), entry.getKey().getEnclosingElement().getSimpleName());
                }
            }
            for (Map.Entry<CommandSpec, Set<MixinInfo>> mixinEntry : mixinInfoMap.entrySet()) {
                CommandSpec mixee = mixinEntry.getKey();
                for (MixinInfo mixinInfo : mixinEntry.getValue()) {
                    logger.fine(String.format("Adding mixin name=%s to %s", mixinInfo.mixinName(), mixee.name()));
                    mixee.addMixin(mixinInfo.mixinName(), mixinInfo.mixin(), mixinInfo.annotatedElement());
                }
            }

            //#377 Standard help options should be added last
            for (CommandSpec commandSpec : commandsRequestingStandardHelpOptions) {
                commandSpec.mixinStandardHelpOptions(true);
            }
        }

        private boolean connectArgGroups(AbstractCommandSpecProcessor proc) {
            // first, loop over all @ArgGroup-annotated elements and
            // populate the associated builder with @Options and @Parameters
            // (but no sub-groups yet)
            Map<Element, TypeElement> argGroupElementsToType = new LinkedHashMap<Element, TypeElement>();
            Map<TypeElement, TypeElement> groupTypeToParentGroupType = new LinkedHashMap<TypeElement, TypeElement>();
            Map<TypeElement, ArgGroupSpec.Builder> argGroupsByType = new LinkedHashMap<TypeElement, ArgGroupSpec.Builder>();
            for (Map.Entry<Element, ArgGroupSpec.Builder> entry : argGroupElements.entrySet()) {
                Element argGroupElement = entry.getKey(); // field, method or parameter
                ArgGroupSpec.Builder builder = entry.getValue();
                //logger.severe(argGroupElement.toString());
                //proc.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "ArgGroup", argGroupElement);

                Types typeUtils = proc.processingEnv.getTypeUtils();

                // get the type or return type of the @ArgGroup-annotated field, method or parameter
                TypeMirror typeMirror = argGroupElement.asType();
                if (typeMirror.getKind() == TypeKind.EXECUTABLE) {
                    // for @ArgGroup-annotated methods, use the method return type
                    typeMirror = ((ExecutableType) typeMirror).getReturnType();
                }
                if (typeMirror.getKind() != TypeKind.DECLARED && typeMirror.getKind() != TypeKind.ARRAY) {
                    proc.error(entry.getKey(), "The type of an @ArgGroup-annotated element '%s' must be a declared class, a collection or an array, but was %s", argGroupElement.getSimpleName(), typeMirror);
                    return true;
                }
                CompileTimeTypeInfo typeInfo = new CompileTimeTypeInfo(typeMirror);
                TypeElement typeElement = (TypeElement) typeUtils.asElement(typeInfo.auxTypeMirrors.get(0));

                CommandSpec argsHolder = commands.get(typeElement);
                if (argsHolder != null) {
                    // The command class that holds this group has a @Command annotation
                    // or @Option or @Parameters-annotated elements.
                    // (TODO: it may have a @Mixin annotation: should mixins be done before groups?)
                    for (OptionSpec option : argsHolder.options()) {
                        builder.addArg(option);
                    }
                    for (PositionalParamSpec positional : argsHolder.positionalParameters()) {
                        builder.addArg(positional);
                    }
                }
                argGroupsByType.put(typeElement, builder);
                argGroupElementsToType.put(argGroupElement, typeElement);
                Element enclosingElement = argGroupElement.getEnclosingElement();
                if (enclosingElement.getKind() == ElementKind.CLASS || enclosingElement.getKind() == ElementKind.INTERFACE) {
                    TypeElement enclosingType = (TypeElement) typeUtils.asElement(enclosingElement.asType());
                    groupTypeToParentGroupType.put(typeElement, enclosingType);
                }
            }

            Element[] lookup = new Element[argGroupElements.size()];
            Graph graph = new Graph(argGroupElements.size());
            int i = 0;
            Map<TypeElement, Integer> typeToIndex = new LinkedHashMap<TypeElement, Integer>();
            for (Map.Entry<Element, ArgGroupSpec.Builder> entry : argGroupElements.entrySet()) {
                Element argGroupElement = entry.getKey(); // field, method or parameter
                lookup[i] = argGroupElement;
                typeToIndex.put(argGroupElementsToType.get(argGroupElement), i++);
            }
            for (Map.Entry<TypeElement, Integer> entry : typeToIndex.entrySet()) {
                TypeElement type = entry.getKey();
                Integer parentIndex = typeToIndex.get(groupTypeToParentGroupType.get(type));
                if (parentIndex != null) {
                    graph.addEdge(typeToIndex.get(type), parentIndex);
                }
            }
            Stack<Integer> sortedGroups = graph.topologicalSort();
            logger.fine(argGroupElements.toString());
            while (!sortedGroups.isEmpty()) {
                Element argGroupElement = lookup[sortedGroups.pop()];
                ArgGroupSpec.Builder argGroupBuilder = argGroupElements.get(argGroupElement);
                logger.log(Level.FINE, "args=%s, typeInfo=%s", new Object[]{argGroupBuilder.args(), argGroupBuilder.typeInfo()});
                ArgGroupSpec group = argGroupBuilder.build();

                CommandSpec commandSpec = getOrCreateCommandSpecForArg(argGroupElement, commands);
                logger.fine("Building ArgGroupSpec for " + argGroupElement + " in command " + commandSpec);
                commandSpec.addArgGroup(group);

                Types typeUtils = proc.processingEnv.getTypeUtils();
                TypeElement parentGroupElement = (TypeElement) typeUtils.asElement(argGroupElement.getEnclosingElement().asType());
                ArgGroupSpec.Builder parentGroup = argGroupsByType.get(parentGroupElement);
                if (parentGroup != null) {
                    // there may be multiple commands/subcommands with this parent arg group
                    for (Map.Entry<Element, ArgGroupSpec.Builder> entry : argGroupElements.entrySet()) {
                        TypeMirror entryTypeMirror = entry.getKey().asType();
                        if (entryTypeMirror.getKind() == TypeKind.DECLARED || entryTypeMirror.getKind() == TypeKind.ARRAY) {
                            CompileTimeTypeInfo typeInfo = new CompileTimeTypeInfo(entryTypeMirror);
                            TypeElement elementType = (TypeElement) typeUtils.asElement(typeInfo.auxTypeMirrors.get(0));
                            if (elementType != null && elementType.toString().equals(parentGroupElement.toString())) {
                                entry.getValue().addSubgroup(group);
                            }
                        }
                    }
                }
            }
            return false;
        }

        private static CommandSpec getOrCreateCommandSpecForArg(Element argElement,
                                                                Map<Element, CommandSpec> commands) {
            Element key = argElement.getEnclosingElement();
            CommandSpec commandSpec = commands.get(key);
            if (commandSpec == null) {
                logger.fine("Element " + argElement + " is enclosed by " + key + " which does not have a @Command annotation");
                commandSpec = CommandSpec.forAnnotatedObjectLenient(key);
                commandSpec.interpolateVariables(false);
                commands.put(key, commandSpec);
            }
            return commandSpec;
        }

        private void registerCommandType(CommandSpec result, TypeElement typeElement) {
            List<CommandSpec> forSubclass = commandTypes.get(typeElement.asType());
            if (forSubclass == null) {
                forSubclass = new ArrayList<CommandSpec>();
                commandTypes.put(typeElement.asType(), forSubclass);
            }
            forSubclass.add(result);
        }
    }

    /**
     * Helper class for <a href="https://en.wikipedia.org/wiki/Topological_sorting">topologically sorting</a> ArgGroups.
     */
    static class Graph {
        private int vertexCount;   // No. of vertices
        private List<Integer>[] adjacencyList; // Adjacency List

        //Constructor
        @SuppressWarnings("unchecked")
        Graph(int vertexCount) {
            this.vertexCount = vertexCount;
            adjacencyList = new LinkedList[vertexCount];
            for (int i = 0; i < vertexCount; ++i) {
                adjacencyList[i] = new LinkedList();
            }
        }
        /**
         * subgroup is a dependency for group
         * @param subgroup
         * @param group
         */
        void addEdge(int subgroup, int group) {
            adjacencyList[subgroup].add(group);
        }

        // Function to add an edge into the graph
        //void addEdge(int v,int w) { adj[v].add(w); }

        // A recursive function used by topologicalSort
        void topologicalSortUtil(int v, boolean visited[], Stack<Integer> stack) {
            // Mark the current node as visited.
            visited[v] = true;
            Integer i;

            // Recur for all the vertices adjacent to this
            // vertex
            Iterator<Integer> it = adjacencyList[v].iterator();
            while (it.hasNext()) {
                i = it.next();
                if (!visited[i]) {
                    topologicalSortUtil(i, visited, stack);
                }
            }

            // Push current vertex to stack which stores result
            stack.push(v);
        }

        // The function to do Topological Sort. It uses
        // recursive topologicalSortUtil()
        Stack<Integer> topologicalSort() {
            Stack<Integer> stack = new Stack<Integer>();

            // Mark all the vertices as not visited
            boolean visited[] = new boolean[vertexCount];

            // Call the recursive helper function to store
            // Topological Sort starting from all vertices
            // one by one
            for (int i = 0; i < vertexCount; i++) {
                if (!visited[i]) {
                    topologicalSortUtil(i, visited, stack);
                }
            }
            return stack;
        }
    }

    static class NullFactory implements IFactory {
        @Override
        public <K> K create(Class<K> cls) throws Exception {
            return null;
        }
    }

}
