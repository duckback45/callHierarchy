package com.example.callhierarchy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.refactoring.MethodInvocationSearch;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class CallHierarchyApplicationTests {

    @Test
    public void testDocumentation() throws Exception {
        SpoonAPI spoon = new Launcher();
        spoon.getEnvironment().setNoClasspath(true);
        spoon.addInputResource("C:/Users/bsh/IdeaProjects/tenis/src");
        MethodInvocationSearch processor = new MethodInvocationSearch();

        spoon.buildModel();

        CtModel ctModel = spoon.getModel();
        Factory factory = spoon.getFactory();

        CtInterface<?> anInterface = factory.Interface().get("com.spring.tenis.service.MemberService");
        CtMethod<?> ctMethod = anInterface.getMethodsByName("memberList").get(0);

        List<String> callers = ctModel.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation element) {
                CtExecutableReference executable = element.getExecutable();
                if (executable.getSimpleName().equals(ctMethod.getSimpleName())
                        && executable.isOverriding(ctMethod.getReference())) {
                    return true;
                }
                return false;
            }
        }).stream().map(i -> {
            CtMethod parent = i.getParent(CtMethod.class);
            return parent.getAnnotations().stream().filter(s -> s.getAnnotationType().getSimpleName().equals("RequestMapping")).map(v -> v.getValue("value").toString()).findFirst().get();
        }).collect(Collectors.toList());

        System.out.println("callersssss : " + callers);
    }

}
