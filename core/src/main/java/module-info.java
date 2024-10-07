module matching.core.main {
    requires jakarta.annotation;
    requires jakarta.persistence;
    requires jakarta.validation;
    requires static lombok;
    requires org.springframework.modulith.api;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.data.jpa;
    requires spring.tx;
    requires spring.data.commons;
    requires edu.stanford.nlp.corenlp;
    requires org.hibernate.orm.core;
    requires org.slf4j;
    requires spring.beans;
    requires org.postgresql.jdbc;
    requires spring.jdbc;

    exports com.filmfit.core.common;
    exports com.filmfit.core.film;
}