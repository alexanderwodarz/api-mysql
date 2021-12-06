package de.alexanderwodarz.code.database.annotation;

import de.alexanderwodarz.code.database.enums.ColumnDefault;
import de.alexanderwodarz.code.database.enums.ColumnType;

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

    ColumnType type() default ColumnType.empty;

    ColumnDefault defaultValue() default ColumnDefault.empty;

    int defaultInt() default 0;

    boolean autoIncrement() default false;

    boolean primaryKey() default false;

}
