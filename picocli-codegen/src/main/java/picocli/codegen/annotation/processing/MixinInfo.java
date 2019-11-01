package picocli.codegen.annotation.processing;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.IAnnotatedElement;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.EnumSet;
import java.util.List;

/**
 * Holds information on the `@Mixin`-annotated program element.
 */
class MixinInfo {
    private final String mixinName;
    private final IAnnotatedElement annotatedElement;
    private final VariableElement element;
    private final CommandSpec mixin;

    public MixinInfo(VariableElement element, CommandSpec mixin) {
        this.element = element;
        this.mixin = mixin;

        String name = element.getAnnotation(CommandLine.Mixin.class).name();
        if (name.length() == 0) {
            name = element.getSimpleName().toString();
        }
        this.mixinName = name;
        Element targetType = element.getEnclosingElement();
        int position = -1;
        if (EnumSet.of(ElementKind.METHOD, ElementKind.CONSTRUCTOR).contains(targetType.getKind())) {
            List<? extends VariableElement> parameters = ((ExecutableElement) targetType).getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                if (parameters.get(i).getSimpleName().contentEquals(element.getSimpleName())) {
                    position = i;
                    break;
                }
            }
        }
        annotatedElement = new TypedMember(element, position);
    }

    public Element enclosingElement() {
        return element.getEnclosingElement();
    }

    public String mixinName() {
        return mixinName;
    }

    public CommandSpec mixin() {
        return mixin;
    }

    public IAnnotatedElement annotatedElement() {
        return annotatedElement;
    }

    public int hashCode() {
        int result = 17;
        result += result * 37 + mixinName.hashCode();
        result += result * 37 + element.getSimpleName().hashCode();
        result += result * 37 + mixin.userObject().toString().hashCode();
        return result;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof MixinInfo)) {
            return false;
        }
        MixinInfo other = (MixinInfo) object;
        if (!mixinName.equals(other.mixinName)) {
            return false;
        }
        if (!element.getSimpleName().equals(other.element.getSimpleName())) {
            return false;
        }
        if (!mixin.userObject().toString().equals(other.mixin.userObject().toString())) {
            return false;
        }
        return true;
    }
}
