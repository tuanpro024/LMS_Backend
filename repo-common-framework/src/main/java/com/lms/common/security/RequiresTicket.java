package com.lms.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Đặt trên method Controller/Service để yêu cầu user phải có ticket hợp lệ
 * (status OPEN hoặc DONE) cho module tương ứng trước khi thực hiện CUD.
 *
 * <p>ADMIN và TEACHER_MANAGER tự động được bypass.</p>
 *
 * <pre>
 * {@code
 * @PostMapping
 * @RequiresTicket(module = TicketModuleEnum.FLASHCARD)
 * public ResponseEntity<?> createPackage(...) { ... }
 * }
 * </pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresTicket {
    TicketModuleEnum module();
}
