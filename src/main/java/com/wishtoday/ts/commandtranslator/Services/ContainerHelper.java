package com.wishtoday.ts.commandtranslator.Services;

import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.stream.Collectors;

public class ContainerHelper {
    private static List<Class<?>> getInterfaces(Class<?> c, List<Class<?>> list) {
        list.addAll(List.of(c.getInterfaces()));
        for (Class<?> i : c.getInterfaces()) {
            getInterfaces(i, list);
        }
        return list;
    }
    //Why not deal with superclasses? Because I never use stupid extends classes.
    static List<Class<?>> getInterfaces(Class<?> c) {
        return getInterfaces(c, Lists.newArrayList())
                .stream().distinct().collect(Collectors.toList());
    }
}
