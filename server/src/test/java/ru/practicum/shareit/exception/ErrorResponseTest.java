package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ErrorResponseTest {
    private ErrorResponse errorResponse;

    @Test
    void getErrorTest() {
        errorResponse = new ErrorResponse("error");

        Assertions.assertEquals(errorResponse.getError(), "error");
    }
}
