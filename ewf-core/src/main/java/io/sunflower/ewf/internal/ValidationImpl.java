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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.sunflower.ewf.validation.ConstraintViolation;
import io.sunflower.ewf.validation.Validation;

import java.util.List;
import java.util.Map;

/**
 * Validation object
 *
 * @author michael
 */
public class ValidationImpl implements Validation {

    private final Map<String, List<ConstraintViolation>> violations = Maps.newHashMap();

    @Override
    public boolean hasViolations() {
        return !this.violations.isEmpty();
    }

    @Override
    public boolean hasViolation(String paramName) {
        return this.violations.containsKey(paramName);
    }

    @Override
    public List<ConstraintViolation> getViolations() {
        List<ConstraintViolation> sumViolations = Lists.newArrayList();
        for (List<ConstraintViolation> fieldViolation : this.violations.values()) {
            sumViolations.addAll(fieldViolation);
        }
        return sumViolations;
    }

    @Override
    public List<ConstraintViolation> getViolations(String paramName) {
        if (this.violations.containsKey(paramName)) {
            return this.violations.get(paramName);
        } else {
            return Lists.newArrayList();
        }
    }

    @Override
    public void addViolation(ConstraintViolation violation) {
        if (!this.violations.containsKey(violation.getFieldKey())) {
            this.violations.put(violation.getFieldKey(), Lists.newArrayList());
        }
        this.violations.get(violation.getFieldKey()).add(violation);
    }
}
