package com.shekhargulati.reactivex.rxokhttp;


import com.shekhargulati.reactivex.rxokhttp.functions.*;
import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.Buffer;
import okio.ByteString;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import rx.Observable;

import javax.net.SocketFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

class OkHttpUnixSocketRxHttpClient implements RxHttpClient {

    private final DefaultOkHttpBasedRxHttpClient client;

    public OkHttpUnixSocketRxHttpClient(final String unixSocketPath) {
        UnixSocketFactory socketFactory = new UnixSocketFactory();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .socketFactory(socketFactory)
                .dns(socketFactory)
                .build();
        client = new DefaultOkHttpBasedRxHttpClient(unixSocketPath, okHttpClient, (baseApiUrl, endpoint, params) -> socketFactory.urlForUnixSocketPath(baseApiUrl, endpoint));
    }

    private static class UnixSocketFactory extends SocketFactory implements Dns {

        public UnixSocketFactory() {
            if (!AFUNIXSocket.isSupported()) {
                throw new UnsupportedOperationException("AFUNIXSocket.isSupported() == false");
            }
        }

        public HttpUrl urlForUnixSocketPath(String unixSocketPath, String path) {
            return new HttpUrl.Builder()
                    .scheme("http")
                    .host(UnixSocket.encodeHostname(unixSocketPath))
                    .addPathSegment(path)
                    .build();
        }

        @Override
        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            return hostname.endsWith(".socket")
                    ? Collections.singletonList(InetAddress.getByAddress(hostname, new byte[]{0, 0, 0, 0}))
                    : Dns.SYSTEM.lookup(hostname);
        }

        @Override
        public Socket createSocket() throws IOException {
            return new UnixSocket();
        }

        @Override
        public Socket createSocket(String s, int i) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    private static class UnixSocket extends Socket {

        private AFUNIXSocket socket;

        @Override
        public void connect(SocketAddress endpoint, int timeout) throws IOException {
            InetAddress address = ((InetSocketAddress) endpoint).getAddress();
            String socketPath = decodeHostname(address);

            System.out.println("connect via '" + socketPath + "'...");
            File socketFile = new File(socketPath);

            socket = AFUNIXSocket.newInstance();
            socket.connect(new AFUNIXSocketAddress(socketFile), timeout);
            socket.setSoTimeout(timeout);
        }

        @Override
        public void bind(SocketAddress bindpoint) throws IOException {
            socket.bind(bindpoint);
        }

        @Override
        public boolean isConnected() {
            return socket.isConnected();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return socket.getOutputStream();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return socket.getInputStream();
        }

        private static String encodeHostname(String path) {
            return Encoder.encode(path) + ".socket";
        }

        private static String decodeHostname(InetAddress address) {
            String hostName = address.getHostName();
            return Encoder.decode(hostName.substring(0, hostName.indexOf(".socket")));
        }

        private static class Encoder {
            static String encode(String text) {
                return ByteString.encodeUtf8(text).hex();
            }

            static String decode(String hex) {
                return ByteString.decodeHex(hex).utf8();
            }
        }
    }

    @Override
    public Observable<String> get(String endpoint, QueryParameter... queryParameters) {
        return client.get(endpoint, queryParameters);
    }

    @Override
    public Observable<String> get(String endpoint, Map<String, String> headers, QueryParameter... queryParameters) {
        return client.get(endpoint, headers, queryParameters);
    }

    @Override
    public <R> Observable<R> get(String endpoint, StringResponseTransformer<R> transformer, QueryParameter... queryParameters) {
        return client.get(endpoint, transformer, queryParameters);
    }

    @Override
    public <R> Observable<R> get(String endpoint, Map<String, String> headers, StringResponseTransformer<R> transformer, QueryParameter... queryParameters) {
        return client.get(endpoint, headers, transformer, queryParameters);
    }

    @Override
    public <R> Observable<R> get(String endpoint, StringResponseToCollectionTransformer<R> transformer, QueryParameter... queryParameters) {
        return client.get(endpoint, transformer, queryParameters);
    }

    @Override
    public <R> Observable<R> get(String endpoint, Map<String, String> headers, StringResponseToCollectionTransformer<R> transformer, QueryParameter... queryParameters) {
        return client.get(endpoint, headers, transformer, queryParameters);
    }

    @Override
    public <R> Observable<R> get(String endpoint, ResponseTransformer<R> transformer, QueryParameter... queryParameters) {
        return client.get(endpoint, transformer, queryParameters);
    }

    @Override
    public Observable<String> getResponseStream(String endpoint, QueryParameter... queryParameters) {
        return client.getResponseStream(endpoint, queryParameters);
    }

    @Override
    public Observable<String> getResponseStream(String endpoint, Map<String, String> headers, QueryParameter... queryParameters) {
        return client.getResponseStream(endpoint, headers, queryParameters);
    }

    @Override
    public <T> Observable<T> getResponseStream(String endpoint, Map<String, String> headers, StringResponseTransformer<T> transformer, QueryParameter... queryParameters) {
        return client.getResponseStream(endpoint, headers, transformer, queryParameters);
    }

    @Override
    public Observable<Buffer> getResponseBufferStream(String endpoint, QueryParameter... queryParameters) {
        return client.getResponseBufferStream(endpoint, queryParameters);
    }

    @Override
    public <T> Observable<T> getResponseStream(String endpoint, StringResponseTransformer<T> transformer, QueryParameter... queryParameters) {
        return client.getResponseStream(endpoint, transformer, queryParameters);
    }

    @Override
    public Observable<HttpStatus> getResponseHttpStatus(String endpointPath, QueryParameter... queryParameters) {
        return client.getResponseHttpStatus(endpointPath, queryParameters);
    }

    @Override
    public Observable<HttpStatus> post(String endpoint, QueryParameter... queryParameters) {
        return client.post(endpoint, queryParameters);
    }

    @Override
    public Observable<HttpStatus> post(String endpoint, Map<String, String> headers, QueryParameter... queryParameters) {
        return client.post(endpoint, headers, queryParameters);
    }

    @Override
    public Observable<HttpStatus> post(String endpoint, String body, QueryParameter... queryParameters) {
        return client.post(endpoint, body, queryParameters);
    }

    @Override
    public Observable<HttpStatus> post(String endpoint, Map<String, String> headers, String body, QueryParameter... queryParameters) {
        return client.post(endpoint, headers, body, queryParameters);
    }

    @Override
    public <R> Observable<R> post(String endpoint, ResponseBodyTransformer<R> bodyTransformer, QueryParameter... queryParameters) {
        return client.post(endpoint, bodyTransformer, queryParameters);
    }

    @Override
    public <R> Observable<R> post(String endpoint, String postBody, ResponseBodyTransformer<R> bodyTransformer, QueryParameter... queryParameters) {
        return client.post(endpoint, postBody, bodyTransformer, queryParameters);
    }

    @Override
    public <R> Observable<R> post(String endpoint, Map<String, String> headers, ResponseBodyTransformer<R> bodyTransformer, QueryParameter... queryParameters) {
        return client.post(endpoint, headers, bodyTransformer, queryParameters);
    }

    @Override
    public <R> Observable<R> post(String endpoint, String postBody, ResponseTransformer<R> transformer, QueryParameter... queryParameters) {
        return client.post(endpoint, postBody, transformer, queryParameters);
    }

    @Override
    public <R> Observable<R> post(String endpoint, Map<String, String> headers, String postBody, ResponseTransformer<R> transformer, QueryParameter... queryParameters) {
        return client.post(endpoint, headers, postBody, transformer, queryParameters);
    }

    @Override
    public Observable<String> postAndReceiveResponse(String endpoint, QueryParameter... queryParameters) {
        return client.postAndReceiveResponse(endpoint, queryParameters);
    }

    @Override
    public Observable<String> postAndReceiveResponse(String endpoint, Map<String, String> headers, QueryParameter... queryParameters) {
        return client.postAndReceiveResponse(endpoint, headers, queryParameters);
    }

    @Override
    public Observable<String> postAndReceiveResponse(String endpoint, Map<String, String> headers, Predicate<String> errorChecker, QueryParameter... queryParameters) {
        return client.postAndReceiveResponse(endpoint, headers, errorChecker, queryParameters);
    }

    @Override
    public Observable<String> postAndReceiveResponse(String endpoint, Map<String, String> headers, String postBody, Predicate<String> errorChecker, QueryParameter... queryParameters) {
        return client.postAndReceiveResponse(endpoint, headers, postBody, errorChecker, queryParameters);
    }

    @Override
    public Observable<String> postAndReceiveStream(String endpoint, String postBody, QueryParameter... queryParameters) {
        return client.postAndReceiveStream(endpoint, postBody, queryParameters);
    }

    @Override
    public Observable<String> postAndReceiveStream(String endpoint, Map<String, String> headers, String postBody, QueryParameter... queryParameters) {
        return client.postAndReceiveStream(endpoint, headers, postBody, queryParameters);
    }

    @Override
    public <R> Observable<R> postTarStream(String endpoint, Path pathToTarArchive, BufferTransformer<R> transformer) {
        return client.postTarStream(endpoint, pathToTarArchive, transformer);
    }

    @Override
    public <R> Observable<R> postTarStream(String endpoint, Path pathToTarArchive, ResponseTransformer<R> transformer) {
        return client.postTarStream(endpoint, pathToTarArchive, transformer);
    }

    @Override
    public Observable<HttpStatus> postTarStream(String endpoint, Path pathToTarArchive) {
        return client.postTarStream(endpoint, pathToTarArchive);
    }

    @Override
    public Observable<HttpStatus> delete(String endpoint, QueryParameter... queryParameters) {
        return client.delete(endpoint, queryParameters);
    }

    @Override
    public Observable<HttpStatus> delete(String endpoint, Map<String, String> headers, QueryParameter... queryParameters) {
        return client.delete(endpoint, headers, queryParameters);
    }

    @Override
    public Observable<Response> head(String endpoint, QueryParameter... queryParameters) {
        return client.head(endpoint, queryParameters);
    }

    @Override
    public Observable<Response> head(String endpoint, Map<String, String> headers, QueryParameter... queryParameters) {
        return client.head(endpoint, headers, queryParameters);
    }
}
