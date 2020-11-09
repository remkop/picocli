/*
   Copyright 2017 Remko Popma

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package picocli.groovy;

import java.util.List;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import picocli.CommandLine;

/**
 * Ensures that Groovy scripts annotated with {@link PicocliScript} are transformed into a class that
 * extends {@link PicocliBaseScript}.
 * This class performs the same transformations as {@link org.codehaus.groovy.transform.BaseScriptASTTransformation},
 * and in addition moves {@link picocli.CommandLine.Command} annotations to the generated script class.
 * The {@code @Command} annotation must be on the same {@code import} or local variable as the {@code PicocliScript}
 * annotation.
 *
 * @author Remko Popma
 * @since 2.0
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class PicocliScriptASTTransformation extends AbstractASTTransformation {

    @SuppressWarnings("deprecation")
    private static final Class<PicocliScript> MY_CLASS1 = PicocliScript.class;
    private static final Class<PicocliScript2> MY_CLASS2 = PicocliScript2.class;
    private static final Class<CommandLine.Command> COMMAND_CLASS = CommandLine.Command.class;
    @SuppressWarnings("deprecation")
    private static final Class<PicocliBaseScript> BASE_SCRIPT1_CLASS = PicocliBaseScript.class;
    private static final Class<PicocliBaseScript2> BASE_SCRIPT2_CLASS = PicocliBaseScript2.class;
    private static final ClassNode MY_TYPE1 = ClassHelper.make(MY_CLASS1);
    private static final ClassNode MY_TYPE2 = ClassHelper.make(MY_CLASS2);
    private static final ClassNode COMMAND_TYPE = ClassHelper.make(COMMAND_CLASS);
    private static final ClassNode BASE_SCRIPT1_TYPE = ClassHelper.make(BASE_SCRIPT1_CLASS);
    private static final ClassNode BASE_SCRIPT2_TYPE = ClassHelper.make(BASE_SCRIPT2_CLASS);
    private static final String MY_TYPE_NAME1 = "@" + MY_TYPE1.getNameWithoutPackage();
    private static final String MY_TYPE_NAME2 = "@" + MY_TYPE2.getNameWithoutPackage();
    private static final Parameter[] CONTEXT_CTOR_PARAMETERS = {new Parameter(ClassHelper.BINDING_TYPE, "context")};

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE1.equals(node.getClassNode()) && !MY_TYPE2.equals(node.getClassNode())) return;

        if (parent instanceof DeclarationExpression) {
            changeBaseScriptTypeFromDeclaration(source, (DeclarationExpression) parent, node);
        } else if (parent instanceof ImportNode || parent instanceof PackageNode) {
            changeBaseScriptTypeFromPackageOrImport(source, parent, node);
        } else if (parent instanceof ClassNode) {
            changeBaseScriptTypeFromClass(source, (ClassNode) parent, node);
        }
    }

    private static ClassNode scriptType(final AnnotationNode node) {
        if (MY_TYPE1.equals(node.getClassNode())) {
            return BASE_SCRIPT1_TYPE;
        }
        if (MY_TYPE2.equals(node.getClassNode())) {
            return BASE_SCRIPT2_TYPE;
        }
        throw new IllegalStateException("Expected annotation @PicocliScript or @PicocliScript2, but was " + node.getClassNode());
    }

    private static String annotationTypeName(final AnnotationNode node) {
        if (MY_TYPE1.equals(node.getClassNode())) {
            return MY_TYPE_NAME1;
        }
        if (MY_TYPE2.equals(node.getClassNode())) {
            return MY_TYPE_NAME2;
        }
        throw new IllegalStateException("Expected annotation @PicocliScript or @PicocliScript2, but was " + node.getClassNode());
    }

    private void changeBaseScriptTypeFromPackageOrImport(final SourceUnit source, final AnnotatedNode parent, final AnnotationNode node) {
        Expression value = node.getMember("value");
        ClassNode scriptType;
        if (value == null) {
            scriptType = scriptType(node);
        } else {
            if (!(value instanceof ClassExpression)) {
                addError("Annotation " + annotationTypeName(node) + " member 'value' should be a class literal.", value);
                return;
            }
            scriptType = value.getType();
        }
        List<ClassNode> classes = source.getAST().getClasses();
        for (ClassNode classNode : classes) {
            if (classNode.isScriptBody()) {
                changeBaseScriptType(source, parent, classNode, scriptType, node);
            }
        }
    }

    private void changeBaseScriptTypeFromClass(final SourceUnit source, final ClassNode parent, final AnnotationNode node) {
        changeBaseScriptType(source, parent, parent, parent.getSuperClass(), node);
    }

    private void changeBaseScriptTypeFromDeclaration(final SourceUnit source, final DeclarationExpression de, final AnnotationNode node) {
        if (de.isMultipleAssignmentDeclaration()) {
            addError("Annotation " + annotationTypeName(node) + " not supported with multiple assignment notation.", de);
            return;
        }

        ClassNode cNode = de.getDeclaringClass();
        ClassNode baseScriptType = de.getVariableExpression().getType().getPlainNodeReference();
        if (baseScriptType.isScript()) {
            if (!(de.getRightExpression() instanceof EmptyExpression)) {
                addError("Annotation " + annotationTypeName(node) + " not supported with variable assignment.", de);
                return;
            }
            de.setRightExpression(new VariableExpression("this"));
        } else {
            baseScriptType = scriptType(node);
        }
        Expression value = node.getMember("value");
        if (value != null) {
            addError("Annotation " + annotationTypeName(node) + " cannot have member 'value' if used on a declaration.", value);
            return;
        }


        changeBaseScriptType(source, de, cNode, baseScriptType, node);
    }

    private void changeBaseScriptType(final SourceUnit source, final AnnotatedNode parent, final ClassNode cNode, final ClassNode baseScriptType, final AnnotationNode node) {
        if (!cNode.isScriptBody()) {
            addError("Annotation " + annotationTypeName(node) + " can only be used within a Script.", parent);
            return;
        }

        if (!baseScriptType.isScript()) {
            addError("Declared type " + baseScriptType + " does not extend groovy.lang.Script class!", parent);
            return;
        }

        List<AnnotationNode> annotations = parent.getAnnotations(COMMAND_TYPE);
        if (cNode.getAnnotations(COMMAND_TYPE).isEmpty()) { // #388 prevent "Duplicate annotation for class" AnnotationFormatError
            cNode.addAnnotations(annotations);
        }
        cNode.setSuperClass(baseScriptType);


        // Method in base script that will contain the script body code.
        MethodNode runScriptMethod = ClassHelper.findSAM(baseScriptType);

        // If they want to use a name other than than "run", then make the change.
        if (isCustomScriptBodyMethod(runScriptMethod)) {
            MethodNode defaultMethod = cNode.getDeclaredMethod("run", Parameter.EMPTY_ARRAY);
            // GROOVY-6706: Sometimes an NPE is thrown here.
            // The reason is that our transform is getting called more than once sometimes.
            if (defaultMethod != null) {
                cNode.removeMethod(defaultMethod);
                MethodNode methodNode = new MethodNode(runScriptMethod.getName(), runScriptMethod.getModifiers() & ~ACC_ABSTRACT
                        , runScriptMethod.getReturnType(), runScriptMethod.getParameters(), runScriptMethod.getExceptions()
                        , defaultMethod.getCode());
                // The AST node metadata has the flag that indicates that this method is a script body.
                // It may also be carrying data for other AST transforms.
                methodNode.copyNodeMetaData(defaultMethod);
                cNode.addMethod(methodNode);
            }
        }

        // If the new script base class does not have a contextual constructor (g.l.Binding), then we won't either.
        // We have to do things this way (and rely on just default constructors) because the logic that generates
        // the constructors for our script class have already run.
        if (cNode.getSuperClass().getDeclaredConstructor(CONTEXT_CTOR_PARAMETERS) == null) {
            ConstructorNode orphanedConstructor = cNode.getDeclaredConstructor(CONTEXT_CTOR_PARAMETERS);
            cNode.removeConstructor(orphanedConstructor);
        }
    }

    private boolean isCustomScriptBodyMethod(MethodNode node) {
        return node != null
                && !(node.getDeclaringClass().equals(ClassHelper.SCRIPT_TYPE)
                && "run".equals(node.getName())
                && node.getParameters().length == 0);
    }
}
