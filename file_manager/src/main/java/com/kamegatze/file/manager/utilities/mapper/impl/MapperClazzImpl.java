package com.kamegatze.file.manager.utilities.mapper.impl;

import com.kamegatze.file.manager.utilities.mapper.MapperClazz;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MapperClazzImpl implements MapperClazz {
    private final ModelMapper modelMapper;
    @Override
    public <From, To> To mapperToClazz(From fromClazz, Class<To> toClass) {
        return modelMapper.map(fromClazz, toClass);
    }
    @Override
    public <FromKey, FromValue, ToKey, ToValue> Map<ToKey, ToValue> mapperToMap(Map<FromKey, FromValue> from,
                                                                              Class<Map<ToKey, ToValue>> toClass) {
        return mapperToClazz(from, toClass);
    }
    @Override
    public <From, To> List<To> mapperToList(Collection<From> froms, Class<To> toClass) {
        return froms.parallelStream().map(from -> mapperToClazz(from, toClass)).toList();
    }
    @Override
    public <From, To> Set<To> mapperToSet(Collection<From> froms, Class<To> toClass) {
        return froms.parallelStream().map(from -> mapperToClazz(from, toClass)).collect(Collectors.toSet());
    }
    @Override
    public <From, To> Deque<To> mapperToDeque(Collection<From> froms, Class<To> toClass) {
        return froms.parallelStream().map(from -> mapperToClazz(from, toClass))
                .collect(Collectors.toCollection(ArrayDeque::new));
    }
}
