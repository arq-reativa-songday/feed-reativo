package br.ufrn.imd.feed.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServicesCommunicationException extends RuntimeException {
    public ServicesCommunicationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ServicesCommunicationException(String msg) {
        super(msg);
    }
}
