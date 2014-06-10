/**
 * Copyright (c) 2014 Evolveum
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
package com.evolveum.midpoint.infra.wsutil;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;

/**
 * @author semancik
 *
 */
public abstract class AbstractWebServiceClient<P,S extends Service> {
	
	private Options options = new Options();
	private boolean verbose = false;
	
	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	protected abstract S createService() throws Exception;
	
	protected abstract Class<P> getPortClass();
	
	protected abstract String getDefaultUsername();
	
	protected String getPasswordType() {
		return WSConstants.PW_DIGEST;
	}
	
	protected abstract String getDefaultPassword();
	
	protected abstract String getDefaultEndpointUrl();
	
	protected abstract int invoke(P port);
	
	public void main(String[] args) {
		try {
			init();
			P port = createPort(args);
			int exitCode = invoke(port);
			System.exit(exitCode);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	protected void init() {
		options.addOption("u", "user", true, "Username");
		options.addOption("p", "password", true, "Password");
		options.addOption("e", "endpoint", true, "Endpoint URL");
		options.addOption("v", "verbose", false, "Verbose mode");
		options.addOption("h", "help", false, "Usage help");
	}

	protected P createPort(String[] args) throws Exception {
		
		String password = getDefaultPassword();
		String username = getDefaultUsername();
		String endpointUrl = getDefaultEndpointUrl();
		
		CommandLineParser cliParser = new GnuParser();
		CommandLine commandLine = cliParser.parse(options, args, true);
		if (commandLine.hasOption('h')) {
			printHelp();
			System.exit(0);
		}
		if (commandLine.hasOption('p')) {
			password = commandLine.getOptionValue('p');
		}
		if (commandLine.hasOption('u')) {
			username = commandLine.getOptionValue('u');
		}
		if (commandLine.hasOption('e')) {
			endpointUrl = commandLine.getOptionValue('e');
		}
		if (commandLine.hasOption('v')) {
			verbose = true;
		}
		
		if (verbose) {
			System.out.println("Username: "+username);
			System.out.println("Password: <not shown>");
			System.out.println("Endpoint URL: "+endpointUrl);
		}

        // uncomment this if you want to use Fiddler or any other proxy
        //ProxySelector.setDefault(new MyProxySelector("127.0.0.1", 8888));
		
		P modelPort = createService().getPort(getPortClass());
		BindingProvider bp = (BindingProvider)modelPort;
		Map<String, Object> requestContext = bp.getRequestContext();
		requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl);
		
		org.apache.cxf.endpoint.Client client = ClientProxy.getClient(modelPort);
		org.apache.cxf.endpoint.Endpoint cxfEndpoint = client.getEndpoint();
		
		Map<String,Object> wssProps = new HashMap<String,Object>();
		
		wssProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
		wssProps.put(WSHandlerConstants.USER, username);
		wssProps.put(WSHandlerConstants.PASSWORD_TYPE, getPasswordType());
		wssProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientPasswordHandler.class.getName());
		ClientPasswordHandler.setPassword(password);
		
		WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(wssProps);
		cxfEndpoint.getOutInterceptors().add(wssOut);
        // enable the following to get client-side logging of outgoing requests and incoming responses
        //cxfEndpoint.getOutInterceptors().add(new LoggingOutInterceptor());
        //cxfEndpoint.getInInterceptors().add(new LoggingInInterceptor());

		return modelPort;
	}
	
	protected void printHelp() {
		final String commandLineSyntax = System.getProperty("sun.java.command");
		final HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(commandLineSyntax, options);
	}
}
