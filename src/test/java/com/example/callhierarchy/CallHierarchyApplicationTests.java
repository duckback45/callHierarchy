package com.example.callhierarchy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
class CallHierarchyApplicationTests {

    @Test
    public void testDocumentation() throws Exception {
        SpoonAPI spoon = new Launcher();
        spoon.getEnvironment().setNoClasspath(true);
        spoon.addInputResource("C:/Aproject/backend/fnd-next-api/src");

        spoon.buildModel();

        CtModel ctModel = spoon.getModel();
        Factory factory = spoon.getFactory();

        HashSet<CtMethod> ctMethodHashSet = new HashSet<CtMethod>();
        LinkedHashMap<String, String> requestMappingValueMap = new LinkedHashMap<>();
        CtInterface<?> anInterface = factory.Interface().get("com.mohajiplatform.product.client.PlatformRestTemplateClient");
        ctMethodHashSet.add(anInterface.getMethodsByName("getSomeRentCarInsuranceNotationList").get(0));


        for (int i = 0; i < 10; i++) {
            List<CtMethod> ctMethodList = ctModel.getElements(new TypeFilter<CtInvocation>(CtInvocation.class) {
                @Override
                public boolean matches(CtInvocation element) {
                    CtExecutableReference executable = element.getExecutable();
                    if (ctMethodHashSet.stream().filter(Objects::nonNull).anyMatch(s -> s.getSimpleName().equals(executable.getSimpleName()))) {
                        return true;
                    }

                    return false;
                }
            }).stream().map(invocation -> {
                CtMethod parent = invocation.getParent(CtMethod.class);
                return parent;
            }).toList();

            ctMethodHashSet.addAll(ctMethodList);
        }

        List<CtMethod> callers = new ArrayList<>(ctMethodHashSet);

        for (int i = 0; i < 10; i++) {
            ctModel.getRootPackage().accept(new CtScanner() {
                @Override
                public <T> void visitCtInvocation(CtInvocation<T> invocation) {
                    CtExecutableReference<T> executable = invocation.getExecutable();
                    for (int i = 0; i < callers.size(); i++) {
                        CtMethod method = callers.get(i);
                        if (method == null)
                            continue;
                        if (method.getSignature().equals(executable.getSignature())) {
                            CtMethod ctMethodByAdd = executable.getParent(CtMethod.class);
                            if (!callers.contains(ctMethodByAdd))
                                callers.add(ctMethodByAdd);
                            break;
                        }
                    }

                    super.visitCtInvocation(invocation);
                }
            });
        }
//
        if (callers.size() > 0) {
            for (int i = 0; i < callers.size(); i++) {
                CtMethod method = callers.get(i);
                if (method == null)
                    break;

                CtElement parent = method.getParent();
                try {
                    while (true) {
                        if (parent == null)
                            break;
                        if (Optional.ofNullable(parent.getParent().getPosition().getFile()).isEmpty())
                            break;
                        if (parent.getAnnotations().size() > 0) {
                            Optional<Object> endPointAnnotationValue = parent.getAnnotations().stream().filter(s -> s.getAnnotationType().toString().contains("controller")).toList().stream().filter(s -> Objects.nonNull(s.getValue("value"))).map(s -> s.getValue("value")).findAny();

                            if (endPointAnnotationValue.isPresent() && !requestMappingValueMap.containsKey(endPointAnnotationValue.get().toString())) {
                                requestMappingValueMap.put(endPointAnnotationValue.toString(), requestMappingValueMap.toString());
                            }
                            break;
                        }
                        parent = method.getParent();
                    }
                } catch (Exception e) {

                }
            }
        }

        for (int i = 0; i < 10; i++) {
            ctModel.getRootPackage().accept(new CtScanner() {
                @Override
                public <T> void visitCtInvocation(CtInvocation<T> invocation) {
                    CtExecutableReference<T> executable = invocation.getExecutable();
                    for (int i = 0; i < callers.size(); i++) {
                        CtMethod method = callers.get(i);
                        if (method == null)
                            continue;
                        if (method.getSignature().equals(executable.getSignature())) {
                            CtElement parent = executable.getParent();
                            try {

                                while (true) {
                                    parent = parent.getParent();

                                    if (parent == null)
                                        break;
                                    if (parent.getAnnotations().size() > 0) {
                                        Optional<Object> endPointAnnotationValue = parent.getAnnotations().stream().filter(s -> s.getAnnotationType().toString().contains("controller")).toList().stream().filter(s -> Objects.nonNull(s.getValue("value"))).map(s -> s.getValue("value")).findAny();
//
                                        if (endPointAnnotationValue.isPresent() && !requestMappingValueMap.containsKey(endPointAnnotationValue.get().toString())) {
                                            requestMappingValueMap.put(endPointAnnotationValue.get().toString(), parent.getComments().get(0).getContent());
                                        }
                                        break;
                                    }
                                }
                            } catch (Exception e) {

                            }
                        }
                    }

                    super.visitCtInvocation(invocation);
                }
            });
        }

        AtomicInteger count = new AtomicInteger(1);

        requestMappingValueMap.forEach((keys, value) -> System.out.printf("end-point %d. %s \n", count.getAndIncrement(), keys));

        AtomicInteger commentCount = new AtomicInteger(1);

        requestMappingValueMap.forEach((keys, value) -> System.out.printf("comment %d. %s \n", commentCount.getAndIncrement(), value));
    }
}
