package de.alexanderwodarz.code.database.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    String name() default "";

    int length() default 0;

    String foreignKey() default "";

    String foreignKeyUpdate() default "";

    String foreignKeyDelete() default "";

    String foreignKeyTable() default "";

    boolean autoIncrement() default false;

    boolean primaryKey() default false;

}
