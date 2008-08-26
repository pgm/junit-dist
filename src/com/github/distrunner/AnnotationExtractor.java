package com.github.distrunner;

import java.io.InputStream;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;


public class AnnotationExtractor extends ClassAdapter implements AnnotationVisitor {
	
	static class EmptyVisitor implements ClassVisitor, AnnotationVisitor, FieldVisitor, MethodVisitor {
		public void visit(int arg0, int arg1, String arg2, String arg3,
				String arg4, String[] arg5) {
		}

		public void visitAttribute(Attribute arg0) {
		}

		public void visitEnd() {
		}

		public FieldVisitor visitField(int arg0, String arg1, String arg2,
				String arg3, Object arg4) {
			return this;
		}

		public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
		}

		public MethodVisitor visitMethod(int arg0, String arg1, String arg2,
				String arg3, String[] arg4) {
			return this;
		}

		public void visitOuterClass(String arg0, String arg1, String arg2) {
		}

		public void visitSource(String arg0, String arg1) {
		}

		public void visit(String arg0, Object arg1) {
		}

		public AnnotationVisitor visitAnnotation(String arg0, String arg1) {
			return this;
		}

		public AnnotationVisitor visitArray(String arg0) {
			return this;
		}

		public void visitEnum(String arg0, String arg1, String arg2) {
		}

		public AnnotationVisitor visitAnnotationDefault() {
			// TODO Auto-generated method stub
			return this;
		}

		public void visitCode() {
			// TODO Auto-generated method stub
			
		}

		public void visitFieldInsn(int arg0, String arg1, String arg2, String arg3) {
			// TODO Auto-generated method stub
			
		}

		public void visitIincInsn(int arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		public void visitInsn(int arg0) {
			// TODO Auto-generated method stub
			
		}

		public void visitIntInsn(int arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		public void visitJumpInsn(int arg0, Label arg1) {
			// TODO Auto-generated method stub
			
		}

		public void visitLabel(Label arg0) {
			// TODO Auto-generated method stub
			
		}

		public void visitLdcInsn(Object arg0) {
			// TODO Auto-generated method stub
			
		}

		public void visitLineNumber(int arg0, Label arg1) {
			// TODO Auto-generated method stub
			
		}

		public void visitLocalVariable(String arg0, String arg1, String arg2,
				Label arg3, Label arg4, int arg5) {
			// TODO Auto-generated method stub
			
		}

		public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
			// TODO Auto-generated method stub
			
		}

		public void visitMaxs(int arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		public void visitMethodInsn(int arg0, String arg1, String arg2, String arg3) {
			// TODO Auto-generated method stub
			
		}

		public void visitMultiANewArrayInsn(String arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1,
				boolean arg2) {
			// TODO Auto-generated method stub
			return this;
		}

		public void visitTableSwitchInsn(int arg0, int arg1, Label arg2,
				Label[] arg3) {
			// TODO Auto-generated method stub
			
		}

		public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2,
				String arg3) {
			// TODO Auto-generated method stub
			
		}

		public void visitTypeInsn(int arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}

		public void visitVarInsn(int arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
			// TODO Auto-generated method stub
			return this;
		}
	}

	static final EmptyVisitor EMPTY_VISITOR = new EmptyVisitor();

	String group;
	
	public AnnotationExtractor() {
		super(EMPTY_VISITOR);
	}

	public AnnotationVisitor visitAnnotation(String annotationName, boolean accessible) {
		if(annotationName.equals("L"+TestGroup.class.getName().replace(".", "/")+";")) {
			return this;
		}
		return EMPTY_VISITOR;
	}

	public void visit(String name, Object value) {
		if(name.equals("value")) {
			this.group = value.toString();
		} else {
			throw new RuntimeException("unexpected parameter: "+name);
		}
	}

	public AnnotationVisitor visitAnnotation(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return EMPTY_VISITOR;
	}

	public AnnotationVisitor visitArray(String arg0) {
		// TODO Auto-generated method stub
		return EMPTY_VISITOR;
	}

	public void visitEnum(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	public String getGroup() {
		return group;
	}
	
	public static String getGroupForClass(String className) {
		try {
			InputStream is = AnnotationExtractor.class.getClassLoader().getResourceAsStream(className.replace(".", "/")+".class");
			ClassReader reader = new ClassReader(is);
			AnnotationExtractor extractor = new AnnotationExtractor();
			reader.accept(extractor, true);
			return extractor.getGroup();
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
