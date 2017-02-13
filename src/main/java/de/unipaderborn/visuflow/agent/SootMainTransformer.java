package de.unipaderborn.visuflow.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class SootMainTransformer implements ClassFileTransformer {

	private ClassPool pool;

	public SootMainTransformer() {
		pool = ClassPool.getDefault();
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
			throws IllegalClassFormatException {

		if(className.equals("soot/Main")) {
			try {
				pool.insertClassPath(new ByteArrayClassPath(className, classfileBuffer));
				CtClass cclass = pool.get(className.replaceAll("/", "."));
				CtMethod main = cclass.getDeclaredMethod("main");
				main.insertBefore("de.unipaderborn.visuflow.agent.MonitorClient.getInstance().connect();");
				main.insertBefore("de.unipaderborn.visuflow.agent.MonitorClient.getInstance().start();");
				main.insertAfter("de.unipaderborn.visuflow.agent.MonitorClient.getInstance().close();");

				if (!cclass.isFrozen()) {
					return cclass.toBytecode();
				} else {
					throw new RuntimeException(className + " is frozen");
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		// no transformation
		return null;
	}
}
