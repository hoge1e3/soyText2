package jp.tonyu.soytext2.extjs;

import java.util.HashSet;

import jp.tonyu.soytext2.js.DocumentScriptable;

public class ClassDef {
	public final HashSet<String> fields=new HashSet<String>();
	public final HashSet<String> methods=new HashSet<String>();
	public final DocumentScriptable src;
	public final ClassDef superClass;
	public ClassDef(DocumentScriptable src, ClassDef superClass) {
		this.src=src;
		this.superClass=superClass;
	}
	public boolean isField(String name) {
		return fields.contains(name) || superClass!=null && superClass.isField(name);
	}
	public boolean isMethod(String name) {
		return methods.contains(name) || superClass!=null && superClass.isMethod(name);
	}
	@Override
	public String toString() {
		StringBuffer b=new StringBuffer("{");
		if (superClass!=null) {
			b.append("SuperClass =");
			b.append(superClass);
		}
		b.append(", Fields=");
		b.append(fields);
		b.append(", Methods=");
		b.append(methods);
		return b+"}";
	}
}