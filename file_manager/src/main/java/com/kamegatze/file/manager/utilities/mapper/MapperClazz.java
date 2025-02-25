package com.kamegatze.file.manager.utilities.mapper;

import java.util.*;

public interface MapperClazz {
    <From, To> To mapperToClazz(From fromClazz, Class<To> toClass);
    <FromKey, FromValue, ToKey, ToValue> Map<ToKey, ToValue> mapperToMap(Map<FromKey, FromValue> from, Class<Map<ToKey, ToValue>> toClass);
    <From, To> List<To> mapperToList(Collection<From> fromCollection, Class<To> toClass);
    <From, To> Set<To> mapperToSet(Collection<From> fromCollection, Class<To> toClass);
    <From, To> Deque<To> mapperToDeque(Collection<From> fromCollection, Class<To> toClass);
}
