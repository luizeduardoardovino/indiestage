package br.edu.infnet.indiestage.espacos.exception;
import java.util.LinkedHashMap;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> tratarStatus(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(ProblemDetail.forStatusAndDetail(e.getStatusCode(),e.getReason()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> tratarValidacao(MethodArgumentNotValidException e) {
        ProblemDetail problema=ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Confira os campos informados");
        var campos=new LinkedHashMap<String,String>();
        e.getBindingResult().getFieldErrors().forEach(erro->campos.putIfAbsent(
                erro.getField(),erro.getDefaultMessage()));
        problema.setProperty("campos",campos);
        return ResponseEntity.badRequest().body(problema);
    }
}