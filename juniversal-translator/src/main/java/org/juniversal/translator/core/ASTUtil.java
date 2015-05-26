/*
 * Copyright (c) 2012-2015, Microsoft Mobile
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.juniversal.translator.core;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.cplusplus.HierarchicalName;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ASTUtil {

    public static void parseJava(List<File> javaProjectDirectories) {

		/*
        ArrayList<File> files = new ArrayList<File>();
		for (File javaProjectDirectory : javaProjectDirectories)
			Util.getFilesRecursive(javaProjectDirectory, ".java", files);
	
		ArrayList<String> filePathsList = new ArrayList<String>();
		for (File file : files) {
			filePathsList.add(file.getPath());
		}

		String[] filePaths = filePathsList.toArray(new String[filePathsList.size()]);

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		parser.createASTs(filePaths, encodings, bindingKeys, requestor, monitor)

		String source = readFile(args[0]);
		parser.setSource(source.toCharArray());

		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null /xx* IProgressMonitor *xx/);

		TypeDeclaration typeDeclaration = ASTUtil.getFirstTypeDeclaration(compilationUnit);

		StringWriter writer = new StringWriter();
		CPPProfile profile = new CPPProfile();
		// profile.setTabStop(4);

		CPPWriter cppWriter = new CPPWriter(writer, profile);

		Context context = new Context((CompilationUnit) compilationUnit.getRoot(), source, 8, profile, cppWriter,
				OutputType.SOURCE_FILE);

		context.setPosition(typeDeclaration.getStartPosition());

		ASTWriters astWriters = new ASTWriters();

		try {
			context.setPosition(typeDeclaration.getStartPosition());
			skipSpaceAndComments();

			astWriters.writeNode(typeDeclaration, context);
		} catch (UserViewableException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (RuntimeException e) {
			if (e instanceof ContextPositionMismatchException)
				throw e;
			else
				throw new JUniversalException(e.getMessage() + "\nError occurred with context at position\n"
						+ context.getPositionDescription(context.getPosition()), e);
		}

		String cppOutput = writer.getBuffer().toString();

		System.out.println("Output:");
		System.out.println(cppOutput);
		*/

    }

    public static @Nullable AbstractTypeDeclaration getFirstTypeDeclaration(CompilationUnit compilationUnit) {
        return getFirst(compilationUnit.types());
    }

    public static @Nullable Statement getFirstStatement(Block block) {
        return getFirst(block.statements());
    }

    public static HierarchicalName typeBindingToHierarchicalName(ITypeBinding typeBinding) {
        @Nullable IPackageBinding packageBinding = typeBinding.getPackage();
        if (packageBinding == null)
            return new HierarchicalName(typeBinding.getName());
        else return new HierarchicalName(packageBinding.getNameComponents(), typeBinding.getName());
    }

    /**
     * Convert a name (which can be null, a SimpleName, or a QualifiedName) to a HierarchicalName (essentially a list of
     * strings for each component of the name).   Null maps to an empty list, a SimpleName to a single element list, and
     * a QualifiedName to a list element for each qualifier followed by the SimpleName identifier at the end.
     *
     * @param name name in question
     * @return name as a HierarchicalName
     */
    public static HierarchicalName toHierarchicalName(@Nullable Name name) {
        HierarchicalName hierarchicalName = new HierarchicalName();

        if (name != null)
            addNameToHierarchicalName(name, hierarchicalName);

        return hierarchicalName;
    }

    /**
     * Get the HierarchicalName associated with the main type defined in the CompilationUnit.   The HierarchicalName is
     * fully qualified, containing the package name followed by the type name.
     *
     * @param compilationUnit compilation unit
     * @return
     */
    public static HierarchicalName toHierarchicalName(CompilationUnit compilationUnit) {
        PackageDeclaration packageDeclaration = compilationUnit.getPackage();

        HierarchicalName packageHierarchicalName;
        if (packageDeclaration != null)
            packageHierarchicalName = toHierarchicalName(packageDeclaration.getName());
        else packageHierarchicalName = new HierarchicalName();

        return new HierarchicalName(packageHierarchicalName,
                getFirstTypeDeclaration(compilationUnit).getName().getIdentifier());
    }

    private static void addNameToHierarchicalName(Name name, HierarchicalName hierarchicalName) {
        if (name.isQualifiedName()) {
            QualifiedName qualifiedName = (QualifiedName) name;

            addNameToHierarchicalName(qualifiedName.getQualifier(), hierarchicalName);
            hierarchicalName.addComponent(qualifiedName.getName().getIdentifier());
        } else {
            SimpleName simpleName = (SimpleName) name;
            hierarchicalName.addComponent(simpleName.getIdentifier());
        }
    }

    /**
     * Return true is the specified modifier is an @Nullable annotation.   Currently we don't care about, nor even
     * support, fully qualified names for the @Nullable annotation.   Any non-qualified name that's @Nullable counts
     * here, but fully names currently never do currently.   The intention is that the programmer can pick between
     * several different @Nullable annotations, though JSimple code currently uses the IntelliJ one.
     *
     * @param extendedModifier modifier in question
     * @return true if modifier is an annotation for @Nullable, false otherwise
     */
    public static boolean isNullable(IExtendedModifier extendedModifier) {
        if (!(extendedModifier instanceof MarkerAnnotation))
            return false;

        Name typeName = ((MarkerAnnotation) extendedModifier).getTypeName();
        return typeName.isSimpleName() && ((SimpleName) typeName).getIdentifier().equals("Nullable");
    }

    /**
     * Return true is the specified modifier is an @Nullable annotation.   Currently we don't care about, nor even
     * support, fully qualified names for the @Nullable annotation.   Any non-qualified name that's @Nullable counts
     * here, but fully qualified names currently never do currently.   The intention is that the programmer can pick
     * between several different @Nullable annotations, though JSimple code currently uses the IntelliJ one.
     *
     * @param extendedModifier modifier in question
     * @return true if modifier is an annotation for @Nullable, false otherwise
     */
    public static boolean isFunctionalInterface(IExtendedModifier extendedModifier) {
        if (!(extendedModifier instanceof MarkerAnnotation))
            return false;

        Name typeName = ((MarkerAnnotation) extendedModifier).getTypeName();
        return typeName.isSimpleName() && ((SimpleName) typeName).getIdentifier().equals("FunctionalInterface");
    }

    /**
     * Return true is the specified modifier is "final".
     *
     * @param extendedModifier modifier in question
     * @return true if modifier is "final"
     */
    public static boolean isFinal(IExtendedModifier extendedModifier) {
        return extendedModifier instanceof Modifier && ((Modifier) extendedModifier).isFinal();
    }

    /**
     * Return true is the specified modifier is "final".
     *
     * @param extendedModifier modifier in question
     * @return true if modifier is "final"
     */
    public static boolean isAnnotation(IExtendedModifier extendedModifier) {
        return extendedModifier instanceof Annotation;
    }

    /**
     * Returns true if the list of extended modifiers (modifiers & annotations) includes "final".
     *
     * @param extendedModifiers extended modifiers to check
     * @return true if and only if list contains "final"
     */
    public static boolean containsFinal(List<?> extendedModifiers) {
        for (Object extendedModifierObject : extendedModifiers) {
            if (isFinal((IExtendedModifier) extendedModifierObject))
                return true;
        }
        return false;
    }

    /**
     * Returns true if the list of extended modifiers (modifiers & annotations) includes "static".
     *
     * @param extendedModifiers extended modifiers to check
     * @return true if and only if list contains "static"
     */
    public static boolean containsStatic(List extendedModifiers) {
        for (Object extendedModifierObject : extendedModifiers) {
            IExtendedModifier extendedModifier = (IExtendedModifier) extendedModifierObject;
            if (extendedModifier.isModifier() && ((Modifier) extendedModifier).isStatic())
                return true;
        }
        return false;
    }

    /**
     * Returns true if the list of extended modifiers (modifiers & annotations) includes "abstract".
     *
     * @param extendedModifiers extended modifiers to check
     * @return true if and only if list contains "static"
     */
    public static boolean containsAbstract(List<?> extendedModifiers) {
        for (Object extendedModifierObject : extendedModifiers) {
            IExtendedModifier extendedModifier = (IExtendedModifier) extendedModifierObject;
            if (extendedModifier.isModifier() && ((Modifier) extendedModifier).isAbstract())
                return true;
        }
        return false;
    }

    /**
     * For the given type reference, see if it uses type variable anywhere.   For example, T, ArrayList<T>, and
     * ArrayList<HashMap<String,T>> would all return true.
     *
     * This method is used for C++ templates to include the "typename" keyword where needed.
     *
     * @param type type reference
     * @return true if the type reference includes a type variable inside it
     */
    public static boolean typeReferenceContainsTypeVariable(Type type) {
        ITypeBinding typeBinding = type.resolveBinding();
        return typeBinding != null && typeReferenceContainsTypeVariable(typeBinding);
    }

    /**
     * For the given type reference, see if it uses type variable anywhere.   For example, T, ArrayList<T>, and
     * ArrayList<HashMap<String,T>> would all return true.   Note that the ITypeBinding should be for a reference
     * to a type, not the definition of the type.
     *
     * This method is used for C++ templates to include the "typename" keyword where needed.
     *
     * @param typeBinding type reference
     * @return true if the type reference includes a type variable inside it
     */
    public static boolean typeReferenceContainsTypeVariable(ITypeBinding typeBinding) {
        if (typeBinding.isTypeVariable())
            return true;

        if (typeBinding.isParameterizedType()) {
            for (ITypeBinding typeArgument : typeBinding.getTypeArguments()) {
                if (typeReferenceContainsTypeVariable(typeArgument))
                    return true;
            }
        }

        if (typeBinding.isArray())
            return typeReferenceContainsTypeVariable(typeBinding.getComponentType());

        return false;
    }

    /**
     * Determines the access modifier specified in list of modifiers. If no access modifier is specified, the default
     * access of Package is returned.
     *
     * @param extendedModifiers extended modifiers to check
     * @return AccessModifier specified or Package by default
     */
    public static AccessLevel getAccessModifier(List<?> extendedModifiers) {
        for (Object extendedModifierObject : extendedModifiers) {
            IExtendedModifier extendedModifier = (IExtendedModifier) extendedModifierObject;
            if (extendedModifier.isModifier()) {
                Modifier modifier = (Modifier) extendedModifier;
                if (modifier.isPublic())
                    return AccessLevel.PUBLIC;
                else if (modifier.isProtected())
                    return AccessLevel.PROTECTED;
                else if (modifier.isPrivate())
                    return AccessLevel.PRIVATE;
            }
        }

        return AccessLevel.PACKAGE;
    }

    public static boolean isFinal(BodyDeclaration bodyDeclaration) {
        return containsFinal(bodyDeclaration.modifiers());
    }

    public static boolean isAbstract(BodyDeclaration bodyDeclaration) {
        return containsAbstract(bodyDeclaration.modifiers());
    }

    public static boolean isStatic(BodyDeclaration bodyDeclaration) {
        return containsStatic(bodyDeclaration.modifiers());
    }

    public static boolean isPrivate(BodyDeclaration bodyDeclaration) {
        return getAccessModifier(bodyDeclaration.modifiers()) == AccessLevel.PRIVATE;
    }

    public static boolean isFinal(AbstractTypeDeclaration typeDeclaration) {
        // Enums are implicitly final
        return typeDeclaration instanceof EnumDeclaration || containsFinal(typeDeclaration.modifiers());
    }

    public static boolean isInterface(AbstractTypeDeclaration typeDeclaration) {
        return typeDeclaration instanceof TypeDeclaration && ((TypeDeclaration) typeDeclaration).isInterface();
    }

    public static boolean isAbstract(TypeDeclaration typeDeclaration) {
        return containsAbstract(typeDeclaration.modifiers());
    }

    public static boolean isConstructor(MethodDeclaration methodDeclaration) {
        return methodDeclaration.isConstructor();
    }

    public static boolean isStatic(IMethodBinding methodBinding) {
        return (methodBinding.getModifiers() & Modifier.STATIC) != 0;
    }

    public static boolean isType(Type type, String qualifiedTypeName) {
        return isType(type.resolveBinding(), qualifiedTypeName);
    }

    public static boolean isType(@Nullable ITypeBinding typeBinding, String qualifiedTypeName) {
        return typeBinding != null && typeBinding.getQualifiedName().equals(qualifiedTypeName);
    }

    public static boolean isThisMethod(MethodDeclaration methodDeclaration, String methodName, String... parameterTypes) {
        // If the method names don't match, it's not the specified method
        if (!methodDeclaration.getName().getIdentifier().equals(methodName))
            return false;

        // If the method parameter counts don't match, it's not the specified method
        if (methodDeclaration.parameters().size() != parameterTypes.length)
            return false;

        // Ensure each parameter has the expected type
        int index = 0;
        for (Object parameterObject : methodDeclaration.parameters()) {
            SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameterObject;

            if (!isType(parameter.getType(), parameterTypes[index]))
                return false;

            ++index;
        }

        return true;
    }

    public static boolean isThisName(Name name, String nameString) {
        // Check first, in the case of a qualified name, whether the name string has a suffix that's the simple name
        // portion of the qualified name.   This check is just an optimization, so in the typical case where there is
        // no match it won't allocate a new object & build the full name string to compare
        if (name instanceof QualifiedName) {
            if (!nameString.endsWith(((QualifiedName) name).getName().getIdentifier()))
                return false;
        }

        return name.getFullyQualifiedName().equals(nameString);
    }

    public static boolean isArrayLengthField(FieldAccess fieldAccess) {
        if (!fieldAccess.getName().getIdentifier().equals("length"))
            return false;
        @Nullable IVariableBinding variableBinding = fieldAccess.resolveFieldBinding();
        return variableBinding != null && variableBinding.getType().isArray();
    }

    public static boolean isArrayLengthField(QualifiedName qualifiedName) {
        if (!qualifiedName.getName().getIdentifier().equals("length"))
            return false;
        @Nullable ITypeBinding binding = qualifiedName.getQualifier().resolveTypeBinding();
        return binding != null;
    }

    /**
     * Returns the position following the last character in the node; just a shortcut for adding the length to the start
     * position. Note that the end position may be (one) past the end of the source.
     *
     * @param node ASTNode in question
     * @return last position in the node + 1
     */
    public static int getEndPosition(ASTNode node) {
        return node.getStartPosition() + node.getLength();
    }

    /**
     * Returns the position following the last character in the last node in the list. Note that the end position may be
     * (one) past the end of the source.
     *
     * @param nodes list of nodes; each item in the list should actually be of a type that's a subclass of ASTNode
     * @return last position of the last node in the list + 1
     */
    public static int getEndPosition(List<?> nodes) {
        Object lastNodeObject = null;
        for (Object node : nodes)
            lastNodeObject = node;

        return getEndPosition((ASTNode) lastNodeObject);
    }

    public static @Nullable String qualifierFromQualifiedName(String qualifiedName) {
        int lastPeriodIndex = qualifiedName.lastIndexOf('.');
        if (lastPeriodIndex == -1)
            return null;
        else return qualifiedName.substring(0, lastPeriodIndex);
    }

    public static String simpleNameFromQualifiedName(String qualifiedName) {
        int lastPeriodIndex = qualifiedName.lastIndexOf('.');
        if (lastPeriodIndex == -1)
            return qualifiedName;
        else return qualifiedName.substring(lastPeriodIndex + 1);
    }

    public static boolean directlyImplementsInterface(ITypeBinding typeBinding, String desiredInterfaceQualifiedName) {
        for (ITypeBinding interfaceTypeBinding : typeBinding.getInterfaces()) {
            if (interfaceTypeBinding.getQualifiedName().equals(desiredInterfaceQualifiedName))
                return true;
        }

        return false;
    }

    public static boolean implementsInterface(ITypeBinding typeBinding, String interfaceQualifiedName) {
        while (typeBinding != null) {
            if (directlyImplementsInterface(typeBinding, interfaceQualifiedName))
                return true;
            typeBinding = typeBinding.getSuperclass();
        }

        return false;
    }

    public static boolean isFunctionalInterface(TypeDeclaration typeDeclaration) {
        return anyMatch(typeDeclaration.modifiers(),
                (IExtendedModifier extendedModifier) -> isFunctionalInterface(extendedModifier));
    }

/*
    public static boolean isFunctionalInterface(ITypeBinding typeBinding) {
        typeBinding.


        boolean hasAnnotation = false;
        for (Object extendedModifierObject : typeDeclaration.modifiers()) {
            IExtendedModifier extendedModifier = (IExtendedModifier) extendedModifierObject;
            if (isFunctionalInterface(extendedModifier)) {
                hasAnnotation = true;
                break;
            }
        }

        return hasAnnotation;
    }
*/

    public static boolean isNumericPrimitiveType(Type type) {
        if (!(type instanceof PrimitiveType))
            return false;

        PrimitiveType.Code code = ((PrimitiveType) type).getPrimitiveTypeCode();
        return code == PrimitiveType.BYTE || code == PrimitiveType.SHORT || code == PrimitiveType.INT ||
               code == PrimitiveType.LONG || code == PrimitiveType.FLOAT || code == PrimitiveType.DOUBLE;
    }

    public static boolean isGenericImport(ImportDeclaration importDeclaration) {
        @Nullable IBinding binding = importDeclaration.resolveBinding();
        return binding != null && binding instanceof ITypeBinding && ((ITypeBinding) binding).isGenericType();
    }

    public static boolean isFunctionalInterface(ITypeBinding typeBinding) {
        if (!typeBinding.isInterface())
            return false;

        int methodCount = typeBinding.getDeclaredMethods().length;
        if (methodCount != 1)
            return false;

        // See if the type has the FunctionalInterface annotation
        return anyMatch(typeBinding.getAnnotations(),
                (IAnnotationBinding annotationBinding) -> annotationBinding.getAnnotationType().getQualifiedName().equals("java.lang.FunctionalInterface"));

        // TODO: Ensure no default implementation (I think) nor constants defined for the interface
    }

    public static boolean isFunctionalInterfaceImplementation(Translator translator, Type type) {
        return isFunctionalInterface(translator.resolveTypeBinding(type));
    }

    /**
     * Add all the wildcards referenced from a given type, searching recursively in the type definition to add all of
     * them.   That is, for a type like this:
     * <p/>
     * "Foo< Bar<? extends Fizz>, <Blip <? super Pop>>, ? >"
     * <p/>
     * 3 wildcard types would be added (<? extends Fizz>, <? super Pop>, and ?) since essentially the ? appears 3 times
     * the in the type definition above.
     *
     * @param type
     * @param wildcardTypes
     */
    public static void addWildcardTypes(Type type, ArrayList<WildcardType> wildcardTypes) {
        if (type.isParameterizedType()) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            forEach(parameterizedType.typeArguments(), (Type typeArgument) -> {
                addWildcardTypes(typeArgument, wildcardTypes);
            });
        } else if (type.isWildcardType()) {
            WildcardType wildcardType = (WildcardType) type;

            wildcardTypes.add(wildcardType);
            @Nullable Type bound = wildcardType.getBound();

            if (bound != null)
                addWildcardTypes(bound, wildcardTypes);
        }
    }

    /**
     * Checks if the cast expression is creating an array (which in Java can never be generic) and then turns around and
     * casts it to an array of a generic type.   In Java, which doesn't allow directly creating a generic array,
     * creating a non-generic array and casting is the the only way to create an array of generic type.   However, in
     * other languages, like C#, that don't do type erasure for generics you can create a generic array directly--and in
     * fact you have to if that's what you want to end up with.   This method helps us map from the Java way (cast) to
     * target language (create directly, with no cast).
     *
     * @param castExpression cast expression in question
     * @return true if the expression creates an array then casts it to a generic array
     */
    public static boolean isGenericArrayCreation(CastExpression castExpression) {
        if (!(castExpression.getExpression() instanceof ArrayCreation))
            return false;

        Type type = castExpression.getType();
        return type instanceof ArrayType && isGenericType(((ArrayType) type).getElementType());
    }

    /**
     * Returns true if the specified type is generic in any way (that is, it has generic parameters, like
     * ArrayList&lt;E&gt;, is a type variable itself, like E, or is an array of a generic type, like E[]).
     *
     * @param type type in question
     * @return true if it's a generic type
     */
    public static boolean isGenericType(Type type) {
        if (type instanceof ParameterizedType)
            return true;

        if (type instanceof ArrayType)
            return isGenericType(((ArrayType) type).getElementType());

        if (type instanceof SimpleType) {
            ITypeBinding typeBinding = type.resolveBinding();
            if (typeBinding != null && typeBinding.isTypeVariable())
                return true;
        }

        return false;
    }

    @FunctionalInterface
    public interface ConsumerWithFirst<T> {
        public void accept(T elmt, boolean first);
    }

    public static <T> boolean forEach(List list, Consumer<T> consumer) {
        boolean hasItem = false;
        for (Object elmtObject : list) {
            T elmt = (T) elmtObject;
            consumer.accept(elmt);
            hasItem = true;
        }
        return hasItem;
    }

    public static @Nullable <T> T getFirst(List list) {
        Iterator iterator = list.iterator();
        if (iterator.hasNext()) {
            Object elmtObject = iterator.next();
            return (T) elmtObject;
        } else return null;
    }

    public static <T> boolean forEach(List list, ConsumerWithFirst<T> consumerWithFirst) {
        boolean first = true;
        for (Object elmtObject : list) {
            T elmt = (T) elmtObject;
            consumerWithFirst.accept(elmt, first);
            first = false;
        }
        return !first;
    }

    public static <T> boolean anyMatch(List list, Predicate<? super T> predicate) {
        for (Object elmtObject : list) {
            T elmt = (T) elmtObject;
            if (predicate.test(elmt))
                return true;
        }
        return false;
    }

    public static <T> boolean anyMatch(T[] array, Predicate<? super T> predicate) {
        for (T elmt : array) {
            if (predicate.test(elmt))
                return true;
        }
        return false;
    }

    public static boolean anyTypeOrAncestorMatch(ITypeBinding typeBinding, Predicate<? super ITypeBinding> predicate) {
        if (predicate.test(typeBinding))
            return true;

        return anyAncestorMatch(typeBinding, predicate);
    }

    public static boolean anyAncestorMatch(ITypeBinding typeBinding, Predicate<? super ITypeBinding> predicate) {
        ITypeBinding superClassTypeBinding = typeBinding.getSuperclass();
        if (superClassTypeBinding != null && anyTypeOrAncestorMatch(superClassTypeBinding, predicate))
            return true;

        for (ITypeBinding interfaceTypeBinding : typeBinding.getInterfaces()) {
            if (anyTypeOrAncestorMatch(interfaceTypeBinding, predicate))
                return true;
        }

        return false;
    }

    /**
     * Check all the superclasses (all the way up the tree) for the given type, seeing if any of them match the
     * specified predicate.   Note that the type itself isn't checked, just its superclasses.   Interfaces aren't
     * checked either; use anyAncestorInterfaceMatch for that.
     *
     * @param typeBinding type (normally a class)
     * @param predicate   predicate to test against
     * @return true if any superclass matches the specified test
     */
    public static boolean anySuperclassMatch(ITypeBinding typeBinding, Predicate<? super ITypeBinding> predicate) {
        // See if this method doesn't have the @Override annotation but actually is an override, by iterating through
        // all ancestor classes/interfaces and checking methods on each to see if this method overrides
        ITypeBinding superclass = typeBinding.getSuperclass();

        while (superclass != null) {
            if (predicate.test(superclass))
                return true;
            superclass = superclass.getSuperclass();
        }

        return false;
    }

    /**
     * Check all the ancestor interfaces of the given type, to see if any of them match the specified predicate.
     * Ancestor interfaces include super interfaces for a type & their super interfaces, all the way up the tree. Super
     * interfaces implemented by are are also included.   Note that superclasses themselves aren't tested (use
     * anySuperclassMatch for that), but all interfaces implemented by superclasses are tested.
     *
     * @param typeBinding type (normally a class) in question
     * @param predicate   predicate to test against
     * @return true if any superinterface matches the specified test
     */
    public static boolean anyAncestorInterfaceMatch(ITypeBinding typeBinding, Predicate<? super ITypeBinding> predicate) {
        // See if this method doesn't have the @Override annotation but actually is an override, by iterating through
        // all ancestor classes/interfaces and checking methods on each to see if this method overrides
        for (ITypeBinding interfaceBinding : typeBinding.getInterfaces()) {
            if (predicate.test(interfaceBinding))
                return true;

            if (anyAncestorMatch(interfaceBinding, predicate))
                return true;
        }

        return anySuperclassMatch(typeBinding, superclass -> anyAncestorInterfaceMatch(superclass, predicate));
    }

    public static BigInteger getIntegerLiteralValue(NumberLiteral numberLiteral) {
        // Get the literal token string, normalizing by removing any _ delimiter characters and converting to upper case
        String token = numberLiteral.getToken().replace("_", "").toUpperCase();

        // Strip off any integer type suffix at the end
        if (token.endsWith("L"))
            token = token.substring(0, token.length() - 1);

        if (token.startsWith("0X"))
            return new BigInteger(token.substring(2), 16);
        else if (token.startsWith("0B"))
            return new BigInteger(token.substring(2), 2);
        else if (token.startsWith("0") && !token.equals("0"))
            return new BigInteger(token.substring(1), 8);
        else return new BigInteger(token, 10);
    }
}
