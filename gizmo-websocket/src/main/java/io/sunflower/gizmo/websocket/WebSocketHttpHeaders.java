/*
 * Copyright (C) 2017. the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sunflower.gizmo.websocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * the HTTP headers defined by the WebSocket specification RFC 6455.
 *
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public class WebSocketHttpHeaders {

  public static final String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";

  public static final String SEC_WEBSOCKET_EXTENSIONS = "Sec-WebSocket-Extensions";

  public static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";

  public static final String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";

  public static final String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";

  private static final long serialVersionUID = -6644521016187828916L;


  private final ListMultimap<String, String> headers;

  /**
   * Create a new instance.
   */
  public WebSocketHttpHeaders() {
    headers = ArrayListMultimap.create();
  }

  /**
   * Create an instance that wraps the given pre-existing HttpHeaders and also propagate all changes
   * to it.
   *
   * @param headers the HTTP headers to wrap
   */
  public WebSocketHttpHeaders(ListMultimap<String, String> headers) {
    this.headers = headers;
  }

  /**
   * Sets the (new) value of the {@code Sec-WebSocket-Accept} header.
   *
   * @param secWebSocketAccept the value of the header
   */
  public void setSecWebSocketAccept(String secWebSocketAccept) {
    set(SEC_WEBSOCKET_ACCEPT, secWebSocketAccept);
  }

  /**
   * Returns the value of the {@code Sec-WebSocket-Accept} header.
   *
   * @return the value of the header
   */
  public String getSecWebSocketAccept() {
    return getFirst(SEC_WEBSOCKET_ACCEPT);
  }

  /**
   * Returns the value of the {@code Sec-WebSocket-Extensions} header.
   *
   * @return the value of the header
   */
  public List<WebSocketExtension> getSecWebSocketExtensions() {
    List<String> values = get(SEC_WEBSOCKET_EXTENSIONS);
    if (values == null || values.isEmpty()) {
      return Collections.emptyList();
    } else {
      List<WebSocketExtension> result = new ArrayList<>(values.size());
      for (String value : values) {
        result.addAll(WebSocketExtension.parseExtensions(value));
      }
      return result;
    }
  }

  /**
   * Sets the (new) value(s) of the {@code Sec-WebSocket-Extensions} header.
   *
   * @param extensions the values for the header
   */
  public void setSecWebSocketExtensions(List<WebSocketExtension> extensions) {
    List<String> result = new ArrayList<>(extensions.size());
    for (WebSocketExtension extension : extensions) {
      result.add(extension.toString());
    }
    set(SEC_WEBSOCKET_EXTENSIONS, toCommaDelimitedString(result));
  }

  /**
   * Sets the (new) value of the {@code Sec-WebSocket-Key} header.
   *
   * @param secWebSocketKey the value of the header
   */
  public void setSecWebSocketKey(String secWebSocketKey) {
    set(SEC_WEBSOCKET_KEY, secWebSocketKey);
  }

  /**
   * Returns the value of the {@code Sec-WebSocket-Key} header.
   *
   * @return the value of the header
   */
  public String getSecWebSocketKey() {
    return getFirst(SEC_WEBSOCKET_KEY);
  }

  /**
   * Sets the (new) value of the {@code Sec-WebSocket-Protocol} header.
   *
   * @param secWebSocketProtocol the value of the header
   */
  public void setSecWebSocketProtocol(String secWebSocketProtocol) {
    if (secWebSocketProtocol != null) {
      set(SEC_WEBSOCKET_PROTOCOL, secWebSocketProtocol);
    }
  }

  /**
   * Sets the (new) value of the {@code Sec-WebSocket-Protocol} header.
   *
   * @param secWebSocketProtocols the value of the header
   */
  public void setSecWebSocketProtocol(List<String> secWebSocketProtocols) {
    set(SEC_WEBSOCKET_PROTOCOL, toCommaDelimitedString(secWebSocketProtocols));
  }

  private String toCommaDelimitedString(List<String> secWebSocketProtocols) {
    return Joiner.on(",").join(secWebSocketProtocols);
  }

  /**
   * Returns the value of the {@code Sec-WebSocket-Key} header.
   *
   * @return the value of the header
   */
  public List<String> getSecWebSocketProtocol() {
    List<String> values = get(SEC_WEBSOCKET_PROTOCOL);
    if (values == null || values.isEmpty()) {
      return Collections.emptyList();
    } else if (values.size() == 1) {
      return getValuesAsList(SEC_WEBSOCKET_PROTOCOL);
    } else {
      return values;
    }
  }

  public List<String> getValuesAsList(String headerName) {
    List<String> values = get(headerName);
    if (values != null) {
      List<String> result = new ArrayList<>();
      for (String value : values) {
        if (value != null) {
          String[] tokens =
              Splitter.on(",")
                  .omitEmptyStrings()
                  .trimResults().splitToList(value).toArray(new String[]{});
          for (String token : tokens) {
            result.add(token);
          }
        }
      }
      return result;
    }
    return Collections.emptyList();
  }

  /**
   * Sets the (new) value of the {@code Sec-WebSocket-Version} header.
   *
   * @param secWebSocketVersion the value of the header
   */
  public void setSecWebSocketVersion(String secWebSocketVersion) {
    set(SEC_WEBSOCKET_VERSION, secWebSocketVersion);
  }

  /**
   * Returns the value of the {@code Sec-WebSocket-Version} header.
   *
   * @return the value of the header
   */
  public String getSecWebSocketVersion() {
    return getFirst(SEC_WEBSOCKET_VERSION);
  }

  // Single string methods

  /**
   * Return the first header value for the given header name, if any.
   *
   * @param headerName the header name
   * @return the first header value; or {@code null}
   */
  public String getFirst(String headerName) {
    List<String> results = this.headers.get(headerName);
    return results.isEmpty() ? null : results.get(0);
  }


  /**
   * Set the given, single header value under the given name.
   *
   * @param headerName the header name
   * @param headerValue the header value
   * @throws UnsupportedOperationException if adding headers is not supported
   */
  public void set(String headerName, String headerValue) {
    this.headers.put(headerName, headerValue);
  }

  public List<String> get(String key) {
    return this.headers.get(key);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof WebSocketHttpHeaders)) {
      return false;
    }
    WebSocketHttpHeaders otherHeaders = (WebSocketHttpHeaders) other;
    return this.headers.equals(otherHeaders.headers);
  }

  @Override
  public int hashCode() {
    return this.headers.hashCode();
  }

  @Override
  public String toString() {
    return this.headers.toString();
  }

}
