package picocli.codegen.annotation.processing;

import picocli.CommandLine;
import picocli.codegen.util.Assert;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;

class TypedMember implements CommandLine.Model.IAnnotatedElement, CommandLine.Model.IExtensible {
    final Element element;
    final String name;
    final CommandLine.Model.ITypeInfo typeInfo;
    final boolean hasInitialValue;
    final int position;
    private CommandLine.Model.IGetter getter;
    private CommandLine.Model.ISetter setter;

    static boolean isAnnotated(Element e) {
        return e.getAnnotation(CommandLine.Option.class) != null
            || e.getAnnotation(CommandLine.Parameters.class) != null
            || e.getAnnotation(CommandLine.ArgGroup.class) != null
            || e.getAnnotation(CommandLine.Unmatched.class) != null
            || e.getAnnotation(CommandLine.Mixin.class) != null
            || e.getAnnotation(CommandLine.Spec.class) != null
            || e.getAnnotation(CommandLine.ParentCommand.class) != null;
    }
    TypedMember(VariableElement variable, int position) {
        element = Assert.notNull(variable, "field");
        name = variable.getSimpleName().toString();
        hasInitialValue = variable.getConstantValue() != null;
        typeInfo = new CompileTimeTypeInfo(variable.asType());
        this.position = position;
        getter = new AnnotatedElementHolder(element);
        setter = (AnnotatedElementHolder) getter;
    }

    TypedMember(ExecutableElement method, AbstractCommandSpecProcessor processor) {
        element = Assert.notNull(method, "method");
        name = propertyName(method.getSimpleName().toString());
        position = -1;
        List<? extends TypeMirror> parameterTypes = ((ExecutableType) method.asType()).getParameterTypes();
        boolean isGetter = parameterTypes.isEmpty() && method.getReturnType().getKind() != TypeKind.VOID;
        boolean isSetter = !parameterTypes.isEmpty();
        if (isSetter == isGetter) {
            processor.error(method, "Only getter or setter methods can be annotated with @Option, but %s is neither.",
                    method.getSimpleName());
            //throw new CommandLine.InitializationException("Invalid method, must be either getter or setter: " + method);
            isGetter = true; // soldier on... :-(
        }
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
        getter = new AnnotatedElementHolder(element);
        setter = (AnnotatedElementHolder) getter;
    }

    public Object userObject()      { return element; }
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) { return getAnnotation(annotationClass) != null; }
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) { return element.getAnnotation(annotationClass); }
    public String getName()         { return name; }
    public boolean isArgSpec()      { return isOption() || isParameter() || isMethodParameter(); }
    public boolean isOption()       { return isAnnotationPresent(CommandLine.Option.class); }
    public boolean isParameter()    { return isAnnotationPresent(CommandLine.Parameters.class); }
    public boolean isArgGroup()     { return isAnnotationPresent(CommandLine.ArgGroup.class); }
    public boolean isMixin()        { return isAnnotationPresent(CommandLine.Mixin.class); }
    public boolean isUnmatched()    { return isAnnotationPresent(CommandLine.Unmatched.class); }
    public boolean isSpec()         { return isAnnotationPresent(CommandLine.Spec.class); }
    public boolean isParentCommand(){ return isAnnotationPresent(CommandLine.ParentCommand.class); }
    public boolean isMultiValue()   { return getTypeInfo().isMultiValue(); }
    public boolean isInteractive()  { return (isOption() && getAnnotation(CommandLine.Option.class).interactive()) || (isParameter() && getAnnotation(CommandLine.Parameters.class).interactive()); }
    public CommandLine.Model.ITypeInfo getTypeInfo()  { return typeInfo; }
    public CommandLine.Model.IGetter getter() { return getter; }
    public CommandLine.Model.ISetter setter() { return setter; }
    public String toString() { return element.toString(); }
    public String getToString() {
        if (isMixin()) { return abbreviate("mixin from member " + toGenericString()); }
        return (String.valueOf(element.getKind()).toLowerCase(Locale.ENGLISH) + " ") + abbreviate(toGenericString());
    }
    String toGenericString() { return element.asType().toString() + " " + element.getEnclosingElement() + "." + element.getSimpleName(); }
    public boolean hasInitialValue()    { return hasInitialValue; }
    public boolean isMethodParameter()  { return position >= 0; }
    public int getMethodParamPosition() { return position; }

    @Override
    public CommandLine.Model.IScope scope() {
        return null; // FIXME
    }

    public String getMixinName() {
        String annotationName = getAnnotation(CommandLine.Mixin.class).name();
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

    public <T> T getExtension(Class<T> cls) {
        return null;
    }
}
