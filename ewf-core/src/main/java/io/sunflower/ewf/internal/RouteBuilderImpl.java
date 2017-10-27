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

package io.sunflower.ewf.internal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import io.sunflower.ewf.ApplicationFilters;
import io.sunflower.ewf.Filter;
import io.sunflower.ewf.FilterChain;
import io.sunflower.ewf.FilterWith;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.RouteBuilder;
import io.sunflower.ewf.support.ResourceMethods.ResourceMethod;
import io.sunflower.ewf.params.internal.ResourceMethodInvoker;
import io.sunflower.ewf.support.LambdaRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author michael
 */
public class RouteBuilderImpl implements RouteBuilder {

  private static final Logger log = LoggerFactory.getLogger(RouteBuilder.class);

  private String httpMethod;
  private String uri;
  private Method functionalMethod;
  /**
   * method to use for parameter/annotation extraction
   */
  private Optional<Method> implementationMethod;

  /**
   * instance to invoke
   */
  private Optional<Object> targetObject;
  private Optional<List<Class<? extends Filter>>> globalFiltersOptional;
  private final List<Class<? extends Filter>> localFilters;

  private ApplicationFilters globalFilters;

  @com.google.inject.Inject(optional = true)
  public void setGlobalFilters(ApplicationFilters globalFilters) {
    this.globalFilters = globalFilters;
  }

  @Inject
  public RouteBuilderImpl() {
    this.implementationMethod = Optional.empty();
    this.targetObject = Optional.empty();
    this.globalFiltersOptional = Optional.empty();
    this.localFilters = Lists.newArrayList();
  }

  public RouteBuilderImpl GET() {
    httpMethod = Route.HTTP_METHOD_GET;
    return this;
  }

  public RouteBuilderImpl POST() {
    httpMethod = Route.HTTP_METHOD_POST;
    return this;
  }

  public RouteBuilderImpl PUT() {
    httpMethod = Route.HTTP_METHOD_PUT;
    return this;
  }

  public RouteBuilderImpl DELETE() {
    httpMethod = Route.HTTP_METHOD_DELETE;
    return this;
  }

  public RouteBuilderImpl OPTIONS() {
    httpMethod = Route.HTTP_METHOD_OPTIONS;
    return this;
  }

  public RouteBuilderImpl HEAD() {
    httpMethod = Route.HTTP_METHOD_HEAD;
    return this;
  }

  public RouteBuilderImpl WS() {
    httpMethod = Route.HTTP_METHOD_GET;
    return this;
  }

  public RouteBuilderImpl METHOD(String method) {
    httpMethod = method;
    return this;
  }

  @Override
  public void with(Class resourceClass, String resourceMethod) {
    this.functionalMethod
        = verifyControllerMethod(resourceClass, resourceMethod);
  }

  @Override
  public Void with(ResourceMethod resourceMethod) {
    LambdaRoute lambdaRoute = LambdaRoute.resolve(resourceMethod);
    this.functionalMethod = lambdaRoute.getFunctionalMethod();
    this.implementationMethod = lambdaRoute.getImplementationMethod();
    this.targetObject = lambdaRoute.getTargetObject();
    return null;
  }

  @Override
  public RouteBuilder globalFilters(List<Class<? extends Filter>> filtersToAdd) {
    this.globalFiltersOptional = Optional.of(filtersToAdd);
    return this;
  }

  @SafeVarargs
  @Override
  public final RouteBuilder globalFilters(Class<? extends Filter>... filtersToAdd) {
    List<Class<? extends Filter>> globalFiltersTemp = Lists.newArrayList(filtersToAdd);
    globalFilters(globalFiltersTemp);
    return this;
  }

  @Override
  public RouteBuilder filters(List<Class<? extends Filter>> filtersToAdd) {
    this.localFilters.addAll(filtersToAdd);
    return this;
  }

  @SafeVarargs
  @Override
  public final RouteBuilder filters(Class<? extends Filter>... filtersToAdd) {
    List<Class<? extends Filter>> filtersTemp = Lists.newArrayList(filtersToAdd);
    filters(filtersTemp);
    return this;
  }

  @Override
  public RouteBuilder route(String uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Routes are usually defined in conf/Routes.java as router.GET().route("/teapot").with(FilterController.class,
   * "teapot");
   *
   * Unfortunately "teapot" is not checked by the compiler. We do that here at runtime.
   *
   * We are reloading when there are changes. So this is almost as good as compile time checking.
   *
   * @param resourceClass The controller class
   * @param resourceMethod The method
   * @return The actual method
   */
  private Method verifyControllerMethod(Class<?> resourceClass,
      String resourceMethod) {
    try {
      Method methodFromQueryingClass = null;

      // 1. Make sure method is in class
      // 2. Make sure only one method is there. Otherwise we cannot really
      // know what to do with the parameters.
      for (Method method : resourceClass.getMethods()) {
        if (method.getName().equals(resourceMethod)) {
          if (methodFromQueryingClass == null) {
            methodFromQueryingClass = method;
          } else {
            throw new NoSuchMethodException();
          }
        }
      }

      if (methodFromQueryingClass == null) {
        throw new NoSuchMethodException();
      }

      // make sure that the return type of that controller method
      // is of type Result.
      if (Result.class.isAssignableFrom(methodFromQueryingClass.getReturnType())) {
        return methodFromQueryingClass;
      } else {
        throw new NoSuchMethodException();
      }

    } catch (SecurityException e) {
      log.error("Error while checking for valid Resource / resourceMethod combination", e);
    } catch (NoSuchMethodException e) {
      log.error("Error in route configuration!!!");
      log.error("Can not find Resource " + resourceClass.getName() + " and method " + resourceMethod);
      log.error("Hint: make sure the resource method returns a Result!");
      log.error("Hint: RouteHandler does not allow more than one method with the same name!");
    }
    return null;
  }

  /**
   * Build the route.
   *
   * @param injector The getInjector to build the route with
   * @return The built route
   */
  public Route buildRoute(Injector injector) {
    if (functionalMethod == null) {
      log.error("Error in route configuration for {}", uri);
      throw new IllegalStateException("Route missing a resource method");
    }

    // Calculate filters
    LinkedList<Class<? extends Filter>> allFilters = new LinkedList<>();

    allFilters.addAll(calculateGlobalFilters(this.globalFiltersOptional));

    allFilters.addAll(this.localFilters);

    allFilters.addAll(calculateFiltersForClass(functionalMethod.getDeclaringClass()));
    FilterWith filterWith = functionalMethod.getAnnotation(FilterWith.class);

    if (filterWith != null) {
      allFilters.addAll(Arrays.asList(filterWith.value()));
    }

    FilterChain filterChain = buildFilterChain(injector, allFilters);

    return new Route(httpMethod, uri, functionalMethod, filterChain);
  }

  private List<Class<? extends Filter>> calculateGlobalFilters(Optional<List<Class<? extends Filter>>> globalFiltersList) {

    List<Class<? extends Filter>> allFilters = Lists.newArrayList();

    // Setting globalFilters in route will deactivate the filters defined by globalFilters
    if (globalFiltersList.isPresent()) {
      allFilters.addAll(globalFiltersList.get());
    } else {
      if (this.globalFilters != null) {
        this.globalFilters.addFilters(allFilters);
      }
    }

    return allFilters;

  }

  private FilterChain buildFilterChain(Injector injector,
      LinkedList<Class<? extends Filter>> filters) {

    if (filters.isEmpty()) {

      // either target object (functional method) or guice will create new instance
      Provider<?> targetProvider = (targetObject.isPresent() ?
          Providers.of(targetObject.get())
          : injector.getProvider(functionalMethod.getDeclaringClass()));

      // invoke functional method with optionally using impl for argument extraction
      ResourceMethodInvoker methodInvoker
          = ResourceMethodInvoker.build(
          functionalMethod, implementationMethod.orElse(functionalMethod), injector);

      return new FilterChainEnd(targetProvider, methodInvoker);

    } else {

      Class<? extends Filter> filter = filters.pop();

      Provider<? extends Filter> filterProvider = injector.getProvider(filter);

      return new FilterChainImpl(filterProvider, buildFilterChain(injector, filters));

    }
  }

  private Set<Class<? extends Filter>> calculateFiltersForClass(Class resourceClass) {

    LinkedHashSet<Class<? extends Filter>> filters = new LinkedHashSet<>();

    //
    // Step up the superclass tree, so that superclass filters come first
    //

    // Superclass
    if (resourceClass.getSuperclass() != null) {
      filters.addAll(calculateFiltersForClass(resourceClass.getSuperclass()));
    }

    // Interfaces
    if (resourceClass.getInterfaces() != null) {
      for (Class clazz : resourceClass.getInterfaces()) {
        filters.addAll(calculateFiltersForClass(clazz));
      }
    }

    // Now add from here
    FilterWith filterWith = (FilterWith) resourceClass.getAnnotation(FilterWith.class);
    if (filterWith != null) {
      filters.addAll(Arrays.asList(filterWith.value()));
    }

    // And return
    return filters;
  }
}
