/*
 * Copyright (c) 2013-2017 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.model.common.expression;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import com.evolveum.midpoint.repo.common.expression.ExpressionFactory;
import com.evolveum.midpoint.repo.common.expression.evaluator.AsIsExpressionEvaluatorFactory;
import com.evolveum.midpoint.repo.common.expression.evaluator.LiteralExpressionEvaluatorFactory;
import com.evolveum.midpoint.model.common.ConstantsManager;
import com.evolveum.midpoint.model.common.expression.evaluator.ConstExpressionEvaluatorFactory;
import com.evolveum.midpoint.model.common.expression.evaluator.GenerateExpressionEvaluatorFactory;
import com.evolveum.midpoint.model.common.expression.evaluator.PathExpressionEvaluatorFactory;
import com.evolveum.midpoint.model.common.expression.functions.FunctionLibrary;
import com.evolveum.midpoint.model.common.expression.functions.FunctionLibraryUtil;
import com.evolveum.midpoint.model.common.expression.script.ScriptExpressionEvaluatorFactory;
import com.evolveum.midpoint.model.common.expression.script.ScriptExpressionFactory;
import com.evolveum.midpoint.model.common.expression.script.jsr223.Jsr223ScriptEvaluator;
import com.evolveum.midpoint.model.common.expression.script.xpath.XPathScriptEvaluator;
import com.evolveum.midpoint.model.common.stringpolicy.ValuePolicyProcessor;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.crypto.ProtectorImpl;
import com.evolveum.midpoint.schema.util.ObjectResolver;
import com.evolveum.midpoint.security.api.SecurityEnforcer;
import com.evolveum.midpoint.test.util.MidPointTestConstants;

/**
 * @author Radovan Semancik
 *
 */
public class ExpressionTestUtil {
	
	public static ProtectorImpl createInitializedProtector(PrismContext prismContext) {
		ProtectorImpl protector = new ProtectorImpl();
        protector.setKeyStorePath(MidPointTestConstants.KEYSTORE_PATH);
        protector.setKeyStorePassword(MidPointTestConstants.KEYSTORE_PASSWORD);
        protector.init();
        return protector;
	}
	
	public static ExpressionFactory createInitializedExpressionFactory(ObjectResolver resolver, ProtectorImpl protector, 
			PrismContext prismContext, SecurityEnforcer securityEnforcer) {
    	ExpressionFactory expressionFactory = new ExpressionFactory(securityEnforcer, prismContext);
    	expressionFactory.setObjectResolver(resolver);
    	
    	// asIs
    	AsIsExpressionEvaluatorFactory asIsFactory = new AsIsExpressionEvaluatorFactory(prismContext, protector);
    	expressionFactory.addEvaluatorFactory(asIsFactory);
    	expressionFactory.setDefaultEvaluatorFactory(asIsFactory);

    	// value
    	LiteralExpressionEvaluatorFactory valueFactory = new LiteralExpressionEvaluatorFactory(prismContext);
    	expressionFactory.addEvaluatorFactory(valueFactory);
    	
		// const
    	ConstantsManager constManager = new ConstantsManager(createConfiguration());
    	ConstExpressionEvaluatorFactory constFactory = new ConstExpressionEvaluatorFactory(protector, constManager, prismContext);
    	expressionFactory.addEvaluatorFactory(constFactory);
    	
    	// path
    	PathExpressionEvaluatorFactory pathFactory = new PathExpressionEvaluatorFactory(prismContext, protector);
    	pathFactory.setObjectResolver(resolver);
    	expressionFactory.addEvaluatorFactory(pathFactory);
    	
    	// generate
    	ValuePolicyProcessor valuePolicyGenerator = new ValuePolicyProcessor();
    	valuePolicyGenerator.setExpressionFactory(expressionFactory);
    	GenerateExpressionEvaluatorFactory generateFactory = new GenerateExpressionEvaluatorFactory(protector, valuePolicyGenerator, prismContext);
    	generateFactory.setObjectResolver(resolver);
    	expressionFactory.addEvaluatorFactory(generateFactory);

    	// script
    	Collection<FunctionLibrary> functions = new ArrayList<FunctionLibrary>();
        functions.add(FunctionLibraryUtil.createBasicFunctionLibrary(prismContext, protector));
        functions.add(FunctionLibraryUtil.createLogFunctionLibrary(prismContext));
        ScriptExpressionFactory scriptExpressionFactory = new ScriptExpressionFactory(prismContext, protector);
        scriptExpressionFactory.setObjectResolver(resolver);
        scriptExpressionFactory.setFunctions(functions);
        XPathScriptEvaluator xpathEvaluator = new XPathScriptEvaluator(prismContext);
        scriptExpressionFactory.registerEvaluator(XPathScriptEvaluator.XPATH_LANGUAGE_URL, xpathEvaluator);
        Jsr223ScriptEvaluator groovyEvaluator = new Jsr223ScriptEvaluator("Groovy", prismContext, protector);
        scriptExpressionFactory.registerEvaluator(groovyEvaluator.getLanguageUrl(), groovyEvaluator);
        ScriptExpressionEvaluatorFactory scriptExpressionEvaluatorFactory = new ScriptExpressionEvaluatorFactory(scriptExpressionFactory, securityEnforcer);
        expressionFactory.addEvaluatorFactory(scriptExpressionEvaluatorFactory);
        
        return expressionFactory;
	}
	
	private static Configuration createConfiguration() {
    	BaseConfiguration config = new BaseConfiguration();
    	config.addProperty("foo", "foobar");
		return config;
	}

}
