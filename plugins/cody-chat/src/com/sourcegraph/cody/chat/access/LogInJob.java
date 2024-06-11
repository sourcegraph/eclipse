package com.sourcegraph.cody.chat.access;

import static java.lang.System.out;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;


public class LogInJob extends Job {
	
	private static final String LOG_IN_URL = "https://sourcegraph.com/user/settings/tokens/new/callback?requestFrom=JETBRAINS-";
	
	public LogInJob() {
		super("Loging in...");
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		var server = new Server();
		var connector = new ServerConnector(server);
		connector.setPort(42477); // REMOVE!!!
		server.addConnector(connector);
		
		var tokenFuture = new CompletableFuture<String>();

		server.setHandler(new Handler.Abstract() {
			@Override
			public boolean handle(Request request, Response response, Callback callback) throws Exception {
				var token = extractToken(request.getHttpURI());
				if (token.isPresent()) {
					tokenFuture.complete(token.get());
					callback.succeeded();
				} else {
					var reason = new RuntimeException("Request " + request + " does not contain a token");
					tokenFuture.completeExceptionally(reason); // propagate to kill this job and shut down the server
					callback.failed(reason); // propagate to server
				}
				return true;
			}
		});

		try {
			server.start();
			var port = connector.getLocalPort();
			
			// open login page
			
			var url = LOG_IN_URL + port;
			Display.getDefault().asyncExec(() -> {
					Program.launch(url);
				});
			
			// wait for response
			var response = tokenFuture.get();
			out.println("!!! " + response);		
			
			return Status.OK_STATUS;
		} catch (Exception e) {
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		} finally {
			try {
				server.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

	private Optional<String> extractToken(HttpURI httpURI) {
		var matcher = Pattern.compile("token=([^&]*)").matcher(httpURI.getQuery());
		if (matcher.find()) {
			return Optional.of(matcher.group(1));
		} else {
			return Optional.empty();
		}
	}

}
