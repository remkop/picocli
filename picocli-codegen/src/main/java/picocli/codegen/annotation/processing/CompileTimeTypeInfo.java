package picocli.codegen.annotation.processing;

import picocli.CommandLine;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor6;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

class CompileTimeTypeInfo implements CommandLine.Model.ITypeInfo {
    private static Logger logger = Logger.getLogger(CompileTimeTypeInfo.class.getName());
    private static final EnumSet<TypeKind> PRIMITIVES = EnumSet.of(
            TypeKind.BYTE, TypeKind.BOOLEAN, TypeKind.CHAR, TypeKind.DOUBLE,
            TypeKind.FLOAT, TypeKind.INT, TypeKind.LONG, TypeKind.SHORT);

    final TypeMirror typeMirror;
    final List<? extends TypeMirror> auxTypeMirrors;
    final List<String> actualGenericTypeArguments;
    final TypeElement typeElement;
    final boolean isCollection;
    final boolean isMap;

    final boolean isCharArray;

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
            isCharArray = false;
        } else if (typeMirror.getKind() == TypeKind.ARRAY) {
            aux = Arrays.asList(((ArrayType) typeMirror).getComponentType());
            actualGenericTypeArguments = Arrays.asList(aux.get(0).toString());
            isCharArray = "char".equals(aux.get(0).toString());
        } else {
            actualGenericTypeArguments = Collections.emptyList();
            isCharArray = false;
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
    public List<CommandLine.Model.ITypeInfo> getAuxiliaryTypeInfos() {
        // for non-multi-value types, the auxiliary type is a single-value list with the type
        if (!isMultiValue()) {
            logger.fine("getAuxiliaryTypeInfos (non-multi) returning new list with this");
            return Arrays.<CommandLine.Model.ITypeInfo>asList(this);
        }

        List<CommandLine.Model.ITypeInfo> result = new ArrayList<CommandLine.Model.ITypeInfo>();
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
        return isBooleanType(type);
    }

    static boolean isBooleanType(TypeMirror type) {
        return type.getKind() == TypeKind.BOOLEAN || "java.lang.Boolean".equals(type.toString()) || "java.util.Optional<java.lang.Boolean>".equals(type.toString());
    }

    @Override
    public boolean isOptional() {
        TypeMirror type = auxTypeMirrors.get(0);
        return "java.util.Optional".equals(type.toString());
    }

    @Override
    public boolean isMultiValue() {
        return isArray() || isCollection() || isMap();
    }

    @Override
    public boolean isArray() {
        return typeMirror.getKind() == TypeKind.ARRAY && !isCharArray;
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

    /**
     * Returns the fully qualified class name (not the canonical name, but with $-signs
     * for inner classes) of the typeElement.
     * If only a typeMirror is available, this method falls back to TypeMirror.toString,
     * which returns the canonical name.
     * @return the fully qualified class name of the type
     */
    @Override
    @SuppressWarnings("deprecation")
    public String getClassName() {
        if (typeElement == null) {
            return typeMirror.toString();
        }
        Element segment = typeElement;
        return segment.accept(new SimpleElementVisitor6<StringBuilder, StringBuilder>() {
            @Override
            public StringBuilder visitPackage(PackageElement e, StringBuilder sb) {
                String postfix = sb.length() > 0 && !e.isUnnamed() ? "." : "";
                sb.insert(0, e.getQualifiedName() + postfix);
                return sb;
            }

            @Override
            public StringBuilder visitType(TypeElement e, StringBuilder sb) {
                if (e.getNestingKind() == NestingKind.MEMBER) {
                    sb.insert(0, "$" + e.getSimpleName());
                } else {
                    sb.insert(0, e.getSimpleName());
                }
                return visit(e.getEnclosingElement(), sb);
            }
        }, new StringBuilder()).toString();
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
