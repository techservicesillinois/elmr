package edu.illinois.techservices.elmr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

class MockHttpExchange extends HttpExchange {

  private Headers responseHeaders = new Headers();

  private int statusCode;

  private long responseLength;

  private final String method;

  private OutputStream os = new ByteArrayOutputStream();

  private boolean isClosed = false;

  MockHttpExchange(String method) {
    this.method = method;
  }

	@Override
	public Headers getRequestHeaders() {
		return null;
	}

	@Override
	public Headers getResponseHeaders() {
		return responseHeaders;
	}

	@Override
	public URI getRequestURI() {
		return null;
	}

	@Override
	public String getRequestMethod() {
		return method;
	}

	@Override
	public HttpContext getHttpContext() {
		return null;
	}

	@Override
	public void close() {
    isClosed = true;
	}

	@Override
	public InputStream getRequestBody() {
		return null;
	}

	@Override
	public OutputStream getResponseBody() {
		return os;
	}

	@Override
	public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
    statusCode = rCode;
    this.responseLength = responseLength;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return null;
	}

	@Override
	public int getResponseCode() {
		return 0;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return null;
	}

	@Override
	public String getProtocol() {
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		return null;
	}

	@Override
	public void setAttribute(String name, Object value) {

	}

	@Override
	public void setStreams(InputStream i, OutputStream o) {
    os = o;
	}

	@Override
	public HttpPrincipal getPrincipal() {
		return null;
  }

  int getStatusCode() {
    return statusCode;
  }

  long getResponseLength() {
    return responseLength;
  }

  boolean isClosed() {
    return isClosed;
  }

  OutputStream getResponseOS() {
    return os;
  }
}
