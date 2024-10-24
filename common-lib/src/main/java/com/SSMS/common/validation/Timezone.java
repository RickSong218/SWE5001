package com.ssms.common.validation;

import javax.validation.Constraint;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = com.ssms.common.validation.TimezoneValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Timezone {
    String message() default "Invalid timezone";
    Class[] groups() default {};
    Class[] payload() default {};
}
