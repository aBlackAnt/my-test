/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
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

package com.example.demo.dynamic.compile.impl;

import com.example.demo.dynamic.compile.JavaSourceCompiler;
import com.example.demo.dynamic.compile.exception.CompileExprException;
import com.example.demo.dynamic.compile.exception.JdkCompileException;
import com.example.demo.dynamic.compile.model.JavaSource;
import com.example.demo.dynamic.compile.model.JdkCompilerClassLoader;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.List;

public class JdkCompiler implements JavaSourceCompiler {

    private List<String> options;

    public JdkCompiler(){
        options = new ArrayList<String>();
        // options.add("-target");
        // options.add("1.6");
    }

    public Class compile(String sourceString) {
        JavaSource source = new JavaSource(sourceString);
        return compile(source);
    }

    public Class compile(JavaSource javaSource) {
        try {

            final DiagnosticCollector<JavaFileObject> errs = new DiagnosticCollector<JavaFileObject>();
            JdkCompileTask compileTask = new JdkCompileTask(new JdkCompilerClassLoader(this.getClass().getClassLoader()),
                options);
            String fullName = javaSource.getPackageName() + "." + javaSource.getClassName();
            Class newClass = compileTask.compile(fullName, javaSource.getSource(), errs);
            return newClass;
        } catch (JdkCompileException ex) {
            DiagnosticCollector<JavaFileObject> diagnostics = ex.getDiagnostics();
            throw new CompileExprException("compile error, source : \n" + javaSource + ", "
                                           + diagnostics.getDiagnostics(), ex);
        } catch (Exception ex) {
            throw new CompileExprException("compile error, source : \n" + javaSource, ex);
        }

    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

}
