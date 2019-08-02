package com.kongapi.routerprocessor;

import com.google.auto.service.AutoService;
import com.kongapi.routerannotation.PagePath;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class RouterProcessor extends AbstractProcessor {

    String packageName;
    // 元素操作的辅助类
    Messager messager;
    Elements elementUtils;
    Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        //messager.printMessage(Diagnostic.Kind.ERROR, "初始化");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        
        MethodSpec.Builder getFragmentBuilder = MethodSpec.methodBuilder("getFragment")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.OBJECT)
                //.returns(ClassName.get("com.kongapi.commonlibrary.base", "BaseFragment", new String[0]))
                .addParameter(String.class, "page_path")
                .addStatement("Object fragment = null")
                .addCode("switch (page_path){\n");

        // 获得被该注解声明的元素
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(PagePath.class);
        // 遍历
        for (Element ele : elements) {
            // 判断该元素是否为类
            if (ele.getKind() == ElementKind.CLASS) {
                TypeElement classElement = (TypeElement) ele;
                String className = classElement.getQualifiedName().toString();
                String classSimpleName = classElement.getSimpleName().toString();
                PagePath pagePath = classElement.getAnnotation(PagePath.class);
                getFragmentBuilder.addCode("case $S:\n", pagePath.pagePath())
                        .addStatement(String.format("fragment = new %s()", className))
                        .addStatement("break");
                if (packageName == null) {
                    String[] strings = className.split("\\.");
                    packageName = (strings[0] + "." + strings[1] + "." + strings[2]);
                }
            }
        }

        getFragmentBuilder.addCode("}\n")
                .addStatement("return fragment");
        MethodSpec methodSpec = getFragmentBuilder.build();

        TypeSpec RouterHelp = TypeSpec.classBuilder("RouterHelp")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodSpec)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, RouterHelp)
                .build();

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<>();
        annotataions.add(PagePath.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
