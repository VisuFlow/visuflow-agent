package de.unipaderborn.visuflow.agent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class Agent {

	public static void premain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
		inst.addTransformer(new SootMainTransformer(), true);
		inst.addTransformer(new UnitFqnTagger(), true);
		inst.addTransformer(new SootFlowAnalysisTransformer(), true);
	}
}
