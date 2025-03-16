package com.kamegatze.authorization.remote.security.filter.model;

import java.util.Objects;

public class Authority {
    private String name;

    public Authority(String authority) {
        this.name = authority;
    }

    public Authority() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Authority authority1 = (Authority) o;
        return Objects.equals(name, authority1.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Authority{" +
                "authority='" + name + '\'' +
                '}';
    }
}
