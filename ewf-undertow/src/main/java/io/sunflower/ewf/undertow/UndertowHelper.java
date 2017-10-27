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
package io.sunflower.ewf.undertow;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.sunflower.ewf.uploads.FileItem;
import io.sunflower.ewf.internal.ParameterFileItem;
import io.undertow.server.handlers.form.FormData.FormValue;
import io.undertow.util.HeaderValues;

public class UndertowHelper {

  static public void createOrMerge(Map<String, String[]> parameters, String name,
      Deque<FormValue> formValues) {
    String[] current = parameters.get(name);
    int index = 0;
    int size = formValues.size();

    // prepare for merge or allocate new
    if (current != null) {
      index = current.length;
      size += current.length;
      String[] future = new String[size];
      System.arraycopy(current, 0, future, 0, current.length);
      current = future;
    } else {
      current = new String[size];
    }

    // copy values!
    for (FormValue formValue : formValues) {

      // make sure to skip all file uploads.
      if (formValue.isFile()) {
        continue;
      }

      // standard form parameter
      current[index] = formValue.getValue();
      index++;

    }

    parameters.put(name, current);
  }

  /**
   * Gets a single (the first) {@link ParameterFileItem} from the given {@link FormValue}s.
   */
  public static ParameterFileItem getFileItem(String formName, Deque<FormValue> formValues) {
    for (FormValue formValue : formValues) {

      // make sure to skip all non-file upload fields
      if (!formValue.isFile()) {
        continue;
      }

      // we have a (first) file upload. create and return
      return getFileItemFromFormValue(formValue);
    }
    return null;
  }

  /**
   * Populate the given {@link List} of {@link FileItem}s with file uploads with the given parameter
   * name.
   */
  public static void populateFileItemList(List<FileItem> fileItemList, String name,
      Deque<FormValue> formValues) {
    for (FormValue formValue : formValues) {

      // make sure to skip all non-file upload fields
      if (!formValue.isFile()) {
        continue;
      }

      // we have a file upload
      fileItemList.add(getFileItemFromFormValue(formValue));
    }
  }

  /**
   * Populates the given {@link Map} of {@link FileItem}s with all file uploads from the given
   * form.
   */
  public static void populateFileItemMap(Map<String, List<FileItem>> fileItemMap, String name,
      Deque<FormValue> formValues) {
    List<FileItem> fileItemList = null;
    if (fileItemMap.containsKey(name)) {
      fileItemList = fileItemMap.get(name);
    } else {
      fileItemList = new ArrayList<>();
      fileItemMap.put(name, fileItemList);
    }

    for (FormValue formValue : formValues) {

      // make sure to skip all non-file upload fields
      if (!formValue.isFile()) {
        continue;
      }

      // we have a file upload
      FileItem fileItem = getFileItemFromFormValue(formValue);
      fileItemList.add(fileItem);
    }
  }

  /**
   * Creates a new {@link ParameterFileItem} from the given {@link FormValue} (which is assumed to
   * be representing a file upload).
   */
  private static ParameterFileItem getFileItemFromFormValue(FormValue formValue) {
    Map<String, String> fileItemHeaders = new HashMap<>();
    for (Iterator<HeaderValues> headerIterator = formValue.getHeaders().iterator();
        headerIterator.hasNext(); ) {
      HeaderValues hv = headerIterator.next();
      String headerName = hv.getHeaderName().toString();

      for (Iterator<String> headerValueIterator = hv.iterator(); headerValueIterator.hasNext(); ) {
        String headerValue = headerValueIterator.next();
        fileItemHeaders.put(headerName, headerValue);
      }
    }
    return new ParameterFileItem(formValue.getFileName(), formValue.getPath().toFile(),
        fileItemHeaders);
  }


}
