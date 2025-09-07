package org.example.task_management.model.exception;

import lombok.Getter;
import org.example.task_management.model.Status;

@Getter
public class InvalidTaskTransitionException extends RuntimeException {

    private final Long taskId;
    private final Status fromStatus;
    private final Status toStatus;

    public InvalidTaskTransitionException(Long taskId, Status fromStatus, Status toStatus, String message) {
        super(message);
        this.taskId = taskId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

}
