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

package io.sunflower.ewf.jaxy;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.sunflower.ewf.ApplicationRoutes;
import io.sunflower.ewf.Router;
import io.sunflower.ewf.support.Mode;
import io.sunflower.ewf.support.Settings;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * Implementation of a JAX-RS style route builder.
 *
 * @author michael
 */
@Singleton
public class JaxyRoutes implements ApplicationRoutes {

    private final static Logger logger = LoggerFactory.getLogger(JaxyRoutes.class);

    private final Mode runtimeMode;
    private Reflections reflections;
    private Set<Method> methods;
    private Map<Class<?>, Set<String>> resources;
    private Router router;
    private final String scanPkgs;

    @Inject
    public JaxyRoutes(Settings settings) {
        if (settings.isDev()) {
            runtimeMode = Mode.dev;
        } else if (settings.isTest()) {
            runtimeMode = Mode.test;
        } else {
            runtimeMode = Mode.prod;
        }
        this.scanPkgs = settings.getResourcePkgs();
    }

    /**
     * Scans, identifies, and registers annotated controller methods for the current runtime
     * settings.
     */
    @Override
    public void init(Router router) {
        this.router = router;
        configureReflections();

        resources = Maps.newHashMap();
        methods = Sets.newHashSet();

        processFoundMethods();
        sortMethods();
        registerMethods();
    }

    /**
     * Takes all methods and registers them at the controller using the path: Class:@Path +
     * Method:@Path. If no @Path Annotation is present at the method just the Class:@Path is used.
     */
    private void registerMethods() {
        // register routes for all the methods
        for (Method method : methods) {

            final Class<?> resourceClass = method.getDeclaringClass();
            final Path methodPath = method.getAnnotation(Path.class);
            final Set<String> resourcePaths = resources.get(resourceClass);

            String[] paths = {"/"};
            if (methodPath != null) {
                paths = methodPath.value();
            }
            for (String controllerPath : resourcePaths) {

                for (String methodPathSpec : paths) {

                    final String httpMethod = getHttpMethod(method);
                    final String fullPath = controllerPath + methodPathSpec;
                    final String methodName = method.getName();

                    router.METHOD(httpMethod).route(fullPath).with(resourceClass, methodName);

                }

            }

        }
    }

    /**
     * Takes the found methods and checks if they have a valid format. If they do, the controller path
     * classes for these methods are generated
     */
    private void processFoundMethods() {
        for (Method method : findResourceMethods()) {
            if (allowMethod(method)) {

                // add the method to our todo list
                methods.add(method);

                // generate the paths for the controller class
                final Class<?> resourceClass = method.getDeclaringClass();

                if (!resources.containsKey(resourceClass)) {

                    Set<String> paths = collectPaths(resourceClass);
                    resources.put(resourceClass, paths);

                }
            }
        }
    }

    /**
     * Sorts the methods into registration order
     */
    private void sortMethods() {
        List<Method> methodList = new ArrayList<>(methods);

        methodList.sort((m1, m2) -> {
            int o1 = Integer.MAX_VALUE;
            if (m1.isAnnotationPresent(Order.class)) {
                Order order = m1.getAnnotation(Order.class);
                o1 = order.value();
            }

            int o2 = Integer.MAX_VALUE;
            if (m2.isAnnotationPresent(Order.class)) {
                Order order = m2.getAnnotation(Order.class);
                o2 = order.value();
            }

            if (o1 == o2) {
                // same or unsorted, compare controller+method
                String s1 = m1.getDeclaringClass().getName() + "."
                        + m1.getName();
                String s2 = m2.getDeclaringClass().getName() + "."
                        + m2.getName();
                return s1.compareTo(s2);
            }

            if (o1 < o2) {
                return -1;
            } else {
                return 1;
            }
        });
        methods = new LinkedHashSet<>(methodList);
    }

    /**
     * Searches for Methods that have either a Path Annotation or a HTTP-Method Annotation
     */
    @SuppressWarnings("unchecked")
    private Set<Method> findResourceMethods() {
        Set<Method> methods = Sets.newLinkedHashSet();

        methods.addAll(reflections.getMethodsAnnotatedWith(Path.class));
        Reflections annotationReflections = new Reflections("", new TypeAnnotationsScanner(),
                new SubTypesScanner());
        for (Class<?> httpMethod : annotationReflections.getTypesAnnotatedWith(HttpMethod.class)) {
            if (httpMethod.isAnnotation()) {
                methods
                        .addAll(reflections.getMethodsAnnotatedWith((Class<? extends Annotation>) httpMethod));
            }
        }

        return methods;
    }

    private void configureReflections() {
        ConfigurationBuilder builder = new ConfigurationBuilder();

        Set<URL> packagesToScan = getPackagesToScanForRoutes();
        builder.addUrls(packagesToScan);

        builder.addScanners(new MethodAnnotationsScanner());
        reflections = new Reflections(builder);
    }

    /**
     * Recursively builds the paths for the controller class.
     *
     * @return the paths for the controller
     */
    private Set<String> collectPaths(Class<?> resourceClass) {
        Set<String> parentPaths = Collections.emptySet();
        if (resourceClass.getSuperclass() != null) {
            parentPaths = collectPaths(resourceClass.getSuperclass());
        }

        Set<String> paths = Sets.newLinkedHashSet();
        Path controllerPath = resourceClass.getAnnotation(Path.class);

        if (controllerPath == null) {
            return parentPaths;
        }

        if (parentPaths.isEmpty()) {

            // add all controller paths
            paths.addAll(Arrays.asList(controllerPath.value()));

        } else {

            // create controller paths based on the parent paths
            for (String parentPath : parentPaths) {

                for (String path : controllerPath.value()) {
                    paths.add(parentPath + path);
                }

            }

        }

        return paths;
    }

    /**
     * Returns the set of packages to scan for annotated controller methods.
     *
     * @return the set of packages to scan
     */
    public Set<URL> getPackagesToScanForRoutes() {

        Set<URL> packagesToScanForRoutes = Sets.newHashSet();

        packagesToScanForRoutes.addAll(ClasspathHelper
                .forPackage(scanPkgs));

        return packagesToScanForRoutes;

    }

    /**
     * Determines if this method may be registered as a route. Ninja properties are considered as well
     * as runtime modes.
     *
     * @return true if the method can be registered as a route
     */
    private boolean allowMethod(Method method) {

        // NinjaProperties-based route exclusions/inclusions
        if (method.isAnnotationPresent(Requires.class)) {
            String key = method.getAnnotation(Requires.class).value();
            String value = System.getProperty(key);
            if (value == null) {
                return false;
            }
        }

        // NinjaMode-based route exclusions/inclusions
        Set<Mode> modes = Sets.newTreeSet();
        for (Annotation annotation : method.getAnnotations()) {

            Class<? extends Annotation> annotationClass = annotation
                    .annotationType();

            if (annotationClass.isAnnotationPresent(RuntimeMode.class)) {

                RuntimeMode mode = annotationClass.getAnnotation(RuntimeMode.class);
                modes.add(mode.value());

            }
        }

        return modes.isEmpty() || modes.contains(runtimeMode);
    }

    /**
     * Returns the HTTP method for the controller method. Defaults to GET if unspecified.
     *
     * @return the http method for this controller method
     */
    private String getHttpMethod(Method method) {

        for (Annotation annotation : method.getAnnotations()) {

            Class<? extends Annotation> annotationClass = annotation.annotationType();

            if (annotationClass.isAnnotationPresent(HttpMethod.class)) {
                HttpMethod httpMethod = annotationClass.getAnnotation(HttpMethod.class);
                return httpMethod.value();
            }

        }

        // default to GET
        logger.info(String.format("%s.%s does not specify an HTTP method annotation! Defaulting to GET.",
                method.getClass().getName(), method.getName()));

        return HttpMethod.GET;
    }

}
