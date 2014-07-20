package juniversal.cplusplus.astwriters;

import java.util.List;

import juniversal.cplusplus.Context;

import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TypeParameter;

public class ASTWriterUtil {

	public static String getNamespaceNameForPackageName(Name packageName) {
		if (packageName == null)
			return ASTWriterUtil.getNamespaceNameForPackageName((String) null);
		else return ASTWriterUtil.getNamespaceNameForPackageName(packageName.getFullyQualifiedName());
	}

	public static String getNamespaceNameForPackageName(String packageName) {
		if (packageName == null)
			return "unnamed";
		else return packageName.replace('.', '_');
	}

	public static void writeIncludeForTypeName(Name typeName, Context context) {
		String typeNameString = typeName.getFullyQualifiedName();
		String includePath = typeNameString.replace('.', '/');
	
		context.writeln("#include \"" + includePath + ".h\"");
	}

	/**
	 * Write out the type parameters in the specified list, surrounded by "<" and ">".  
	 * 
	 * @param typeParameters list of TypeParameter objects
	 * @param includeClassKeyword if true, each parameter is prefixed with "class "
	 * @param context context to output to
	 */
	public static void writeTypeParameters(List<TypeParameter> typeParameters, boolean includeClassKeyword, Context context) {
		// If we're writing the implementation of a generic method, include the "template<...>" prefix
	
		boolean first = true;
	
		context.write("<");
		for (TypeParameter typeParameter : typeParameters) {
			if (! first)
				context.write(", ");
	
			if (includeClassKeyword)
				context.write("class ");
			context.write(typeParameter.getName().getIdentifier());
	
			first = false;
		}
	
		context.write(">");
	}
}
