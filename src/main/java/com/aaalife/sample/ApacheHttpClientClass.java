package com.aaalife.sample;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLSession;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;

public class ApacheHttpClientClass {
	public static final String PEER_CERTIFICATES = "PEER_CERTIFICATES";

	public static void main(String[] args) throws IOException {
		System.out.println("HTTP Get Method below  >>>>>>>>>>>>>>> \n");
		httpGetRequest();
		System.out.println("Http Post Method Follows >>>>>>>>>>>>> \n");
		httpPostRequest();
		System.out.println("Http Put Method Follows >>>>>>>>>>>>> \n");
		httpPutequest();
		System.out.println("Http Delete Method Follows >>>>>>>>>>>>> \n");
		httpDeleteRequest();
		System.out.println("Http Get Certs from Client Follows >>>>>>>>>>>>> \n");
		getClientCertificates();
		System.out.println("\n-------------------END--------------------");

	}

	public static void httpGetRequest() throws IOException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpGet httpGet = new HttpGet("http://httpbin.org/get");
			System.out.println("Executing Request " + httpGet.getRequestLine());

			ResponseHandler<String> responseHandler = getResponse();
			String responseBody = httpClient.execute(httpGet, responseHandler);
			System.out.println("---------------------------------------------");
			System.out.println(responseBody);
		}
	}

	public static void httpPostRequest() throws IOException {
		 try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
	            HttpPost httpPost = new HttpPost("http://httpbin.org/post");
	            httpPost.setEntity(new StringEntity("Hello, World"));

	            System.out.println("Executing request " + httpPost.getRequestLine());

	            ResponseHandler<String> responseHandler = getResponse();
	            String responseBody = httpclient.execute(httpPost, responseHandler);
	            System.out.println("----------------------------------------");
	            System.out.println(responseBody);
	        }
	    }
	

	public static void httpPutequest() throws IOException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpPut httpPut = new HttpPut("http://httpbin.org/put");
			httpPut.setEntity(new StringEntity("Hello, World"));

			System.out.println("Executing request " + httpPut.getRequestLine());

			ResponseHandler<String> responseHandler = getResponse();
			String responseBody = httpclient.execute(httpPut, responseHandler);
			System.out.println("----------------------------------------");
			System.out.println(responseBody);
		}
	}

	public static void httpDeleteRequest() throws IOException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpDelete httpDelete = new HttpDelete("http://httpbin.org/delete");

			System.out.println("Executing request " + httpDelete.getRequestLine());

			ResponseHandler<String> responseHandler = getResponse();
			String responseBody = httpclient.execute(httpDelete, responseHandler);
			System.out.println("----------------------------------------");
			System.out.println(responseBody);
		}

	}

	private static ResponseHandler<String> getResponse() {
		ResponseHandler<String> responseHandler = response -> {
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status < 300) {
				HttpEntity entity = response.getEntity();
				return entity != null ? EntityUtils.toString(entity) : null;
			} else {
				throw new ClientProtocolException("Unexpected response status: " + status);
			}
		};
		return responseHandler;
	}

	public static void getClientCertificates() throws IOException {
		// create http response certificate interceptor
		HttpResponseInterceptor certificateInterceptor = (httpResponse, context) -> {
			ManagedHttpClientConnection routedConnection = (ManagedHttpClientConnection) context
					.getAttribute(HttpCoreContext.HTTP_CONNECTION);
			SSLSession sslSession = routedConnection.getSSLSession();
			if (sslSession != null) {

				// get the server certificates from the {@Link SSLSession}
				Certificate[] certificates = sslSession.getPeerCertificates();

				// add the certificates to the context, where we can later grab it from
				context.setAttribute(PEER_CERTIFICATES, certificates);
			}
		};

		// create closable http client and assign the certificate interceptor
		

		try(CloseableHttpClient httpClient = HttpClients.custom().addInterceptorLast(certificateInterceptor).build()){

			// make HTTP GET request to resource server only on https
			HttpGet httpget = new HttpGet("https://google.com");
			System.out.println("Executing request " + httpget.getRequestLine());

			// create http context where the certificate will be added
			HttpContext context = new BasicHttpContext();
			httpClient.execute(httpget, context);

			// obtain the server certificates from the context
			Certificate[] peerCertificates = (Certificate[]) context.getAttribute(PEER_CERTIFICATES);

			// loop over certificates and print meta-data
			for (Certificate certificate : peerCertificates) {
				X509Certificate real = (X509Certificate) certificate;
				System.out.println("----------------------------------------");
				System.out.println("Cert_Type: " + real.getType());
				System.out.println("Signing Algorithm: " + real.getSigAlgName());
				System.out.println("IssuerDN Principal: " + real.getIssuerX500Principal());
				System.out.println("SubjectDN Principal: " + real.getSubjectX500Principal());
				System.out.println("Issued On: " + DateUtils.formatDate(real.getNotBefore(), "dd-MM-yyyy"));
				System.out.println("Expired On: " + DateUtils.formatDate(real.getNotAfter(), "dd-MM-yyyy"));
			}

		} 

	}
}
