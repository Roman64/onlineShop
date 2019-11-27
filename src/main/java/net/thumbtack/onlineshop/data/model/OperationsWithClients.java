package net.thumbtack.onlineshop.data.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
public class OperationsWithClients implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false)
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="EEE, d MMM yyyy HH:mm:ss Z")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @NotNull
    private LocalDateTime date;

    @Column(nullable = false)
    @NotNull
    private String session;

    @Column(nullable = false)
    @NotNull
    private int clientId;

    @Column(nullable = false)
    @NotNull
    private String method;
}
